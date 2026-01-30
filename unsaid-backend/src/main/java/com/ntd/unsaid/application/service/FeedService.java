package com.ntd.unsaid.application.service;

import com.ntd.unsaid.application.dto.FeedPostDTO;
import com.ntd.unsaid.application.dto.response.PostResponse;
import com.ntd.unsaid.application.mapper.PostMapper;
import com.ntd.unsaid.domain.entity.Post;
import com.ntd.unsaid.domain.repository.FollowRepository;
import com.ntd.unsaid.domain.repository.PostRepository;
import com.ntd.unsaid.infrastructure.caching.RedisRepository;
import com.ntd.unsaid.utils.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedService {

    PostRepository postRepository;
    FollowRepository followRepository;
    RedisRepository redisRepository;
    PostMapper postMapper;

    private static final int PAGE_SIZE = 20;

    private record PostScore(String postId, Double score) {
    }

    public void fanOutToFollowers(String postId, String authorId, int followerCount, long createdAt) {
        boolean isCelebrity = followerCount >= Constant.CELEBRITY_FOLLOWER_THRESHOLD;
        redisRepository.pushToUserFeed(authorId, postId, createdAt);
        if (isCelebrity) {
            redisRepository.pushToPostTimeline(authorId, postId, createdAt);
            redisRepository.pushToCelebrityUsers(authorId);
            return;
        }
        List<String> followerIds = followRepository.findFollowerIdsByFollowingId(authorId);
        if (followerIds == null || followerIds.isEmpty()) {
            return;
        }
        redisRepository.pushPostToFollowers(postId, followerIds, createdAt);
    }


    public List<?> getFeed(String userId, int page) {

        int start = (page - 1) * PAGE_SIZE;
        int end = start + PAGE_SIZE - 1;

        // 1. Get pushed posts from Redis, key: feed:user:{userId}
        Set<ZSetOperations.TypedTuple<String>> pushed = redisRepository.getPushedFeed(userId, start, end);
        List<PostScore> merged = new ArrayList<>();
        if (pushed != null) {
            for (var t : pushed) merged.add(new PostScore(t.getValue(), t.getScore()));
        }

        // 2. Get followed celeb IDs, key: user:following_celebs:{userId}
        Set<String> celebIds = redisRepository.getFollowingCelebs(userId);
        if (celebIds == null || celebIds.isEmpty()) {
            Set<String> allCelebrityIds = redisRepository.getCelebrityUserIds();
            celebIds = getFollowingIds(userId, allCelebrityIds);
        }

        // 3. Fetch celeb postIds from celeb's post timeline
        List<Object> rawResults = redisRepository.getCelebrityPosts(celebIds, 2);

        for (Object obj : rawResults) {
            @SuppressWarnings("unchecked")
            Set<ZSetOperations.TypedTuple<String>> posts =
                    (Set<ZSetOperations.TypedTuple<String>>) obj;
            if (posts == null) continue;
            for (var t : posts) {
                merged.add(new PostScore(t.getValue(), t.getScore()));
            }
        }

        // 4. Sắp xếp và lấy Unique IDs (Giữ nguyên logic)
        merged.sort((a, b) -> Double.compare(b.score(), a.score()));
        List<String> orderedPostIds = new ArrayList<>(PAGE_SIZE);
        // HashSet nên có capacity lớn hơn size thực tế để tránh collision
        Set<String> seen = new HashSet<>(PAGE_SIZE * 2);

        for (PostScore p : merged) {
            if (seen.add(p.postId())) {
                orderedPostIds.add(p.postId());
            }
            if (orderedPostIds.size() == PAGE_SIZE) break;
        }
        if (orderedPostIds.isEmpty()) return Collections.emptyList();


        // 5. Thử lấy nội dung bài viết từ Redis Cache (MGET)
        // Giả sử hàm này trả về Map<String, FeedPostDTO>

        Map<String, FeedPostDTO> cachedPostsMap = redisRepository.getPostsFromCacheMGET(orderedPostIds);

        List<String> missingIds = orderedPostIds.stream()
                .filter(id -> !cachedPostsMap.containsKey(id))
                .toList();

        if (!missingIds.isEmpty()) {
            // Use optimized query with JOIN FETCH to avoid N+1 problem
            List<Post> dbPosts = postRepository.findAllByIdInWithDetails(new HashSet<>(missingIds));
            for (Post post : dbPosts) {
                FeedPostDTO dto = postMapper.toFeedPostDTO(post);
                redisRepository.savePostToCache(post.getId(), dto);
                cachedPostsMap.put(post.getId(), dto);
            }
        }

        return orderedPostIds.stream()
                .map(cachedPostsMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<?> getFeedV2(String userId, int page) {

        int start = (page - 1) * PAGE_SIZE;
        int end = start + PAGE_SIZE - 1;

        List<String> postIds = getFeedPostIds(userId, page);
        if (postIds.isEmpty()) return Collections.emptyList();


        // 5. Thử lấy nội dung bài viết từ Redis Cache (MGET)
        // Giả sử hàm này trả về Map<String, FeedPostDTO>

        Map<String, FeedPostDTO> cachedPostsMap = redisRepository.getPostsFromCacheMGET(postIds);

        return postIds.stream()
                .map(cachedPostsMap::get)
                .filter(Objects::nonNull)
                .toList();

//        return orderedPostIds.stream()
//                .map(cachedPostsMap::get)
//                .filter(Objects::nonNull)
//                .toList();
    }

    public List<String> getFeedPostIds(String userId, int page) {
        String finalKey = RedisKeys.userFeedComputed(userId);

        // 1. Kiểm tra xem key computed đã có sẵn chưa (Hit Cache)
        if (redisRepository.existKey(finalKey)) {
            return redisRepository.getPostIdsFromFeedComputed(userId, page);
        }

        // 2. Cache Miss: Tính toán bằng ZUNIONSTORE

        // Lấy key Feed bạn bè
        String userFeedKey = RedisKeys.userFeed(userId);

        // Lấy list key Celeb feeds
        Set<String> celebIds = redisRepository.getFollowingCelebs(userId);
        List<String> keysToMerge = new ArrayList<>();
        keysToMerge.add(userFeedKey);
        celebIds.forEach(id -> keysToMerge.add(RedisKeys.userPostTimeline(id)));

        // GỌI REDIS MERGE (Thao tác thay thế Java Merge)
        // ZUNIONSTORE finalKey numKeys key1 key2 ... AGGREGATE MAX
        redisRepository.unionMergeFeeds(userFeedKey, keysToMerge, finalKey);

        // 3. Lấy kết quả
        return redisRepository.getPostIdsFromFeedComputed(userId, page);
    }

    public Set<String> getFollowingIds(String userId, Set<String> targetIds) {
        // Parse List to Array to be compatible with ANY(?)
        String[] idsArray = targetIds.toArray(new String[0]);
        return followRepository.findFollowingIds(userId, idsArray);
    }
}
