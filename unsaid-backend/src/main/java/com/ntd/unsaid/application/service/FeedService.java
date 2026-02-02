package com.ntd.unsaid.application.service;

import com.ntd.unsaid.application.dto.FeedPostDTO;
import com.ntd.unsaid.domain.repository.FollowRepository;
import com.ntd.unsaid.infrastructure.caching.RedisRepository;
import com.ntd.unsaid.utils.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedService {

    FollowRepository followRepository;
    RedisRepository redisRepository;

    @Async("taskExecutor")
    public void fanOutToFollowers(String postId, String authorId, int followerCount, long createdAt) {
        boolean isCelebrity = followerCount >= Constant.CELEBRITY_FOLLOWER_THRESHOLD;
        redisRepository.pushToUserFeed(authorId, postId, createdAt);
        if (isCelebrity) {
            redisRepository.pushToPostTimeline(authorId, postId, createdAt);
            redisRepository.pushToCelebrityUsers(authorId); // Currently this point is not necessary
            return;
        }
        List<String> followerIds = followRepository.findFollowerIdsByFollowingId(authorId);
        if (followerIds == null || followerIds.isEmpty()) {
            return;
        }
        redisRepository.pushPostToFollowers(postId, followerIds, createdAt);
    }



    public List<?> getFeedV2(String userId, int page) {

        List<String> postIds = getFeedPostIds(userId, page);
        if (postIds.isEmpty()) return Collections.emptyList();


        Map<String, FeedPostDTO> cachedPostsMap = redisRepository.getPostsFromCacheMGET(postIds);

        return postIds.stream()
                .map(cachedPostsMap::get)
                .filter(Objects::nonNull)
                .toList();

    }



    public List<String> getFeedPostIds(String userId, int page) {
        String finalKey = RedisKeys.userFeedComputed(userId);

        if (redisRepository.existKey(finalKey)) {
            return redisRepository.getPostIdsFromFeedComputed(userId, page);
        }

        String userFeedKey = RedisKeys.userFeed(userId);

        Set<String> celebIds = redisRepository.getFollowingCelebs(userId);
        List<String> keysToMerge = new ArrayList<>();
        keysToMerge.add(userFeedKey);
        celebIds.forEach(id -> keysToMerge.add(RedisKeys.userPostTimeline(id)));

        redisRepository.unionMergeFeeds(userFeedKey, keysToMerge, finalKey);

        return redisRepository.getPostIdsFromFeedComputed(userId, page);
    }

    public Set<String> getFollowingIds(String userId, Set<String> targetIds) {
        // Parse List to Array to be compatible with ANY(?)
        String[] idsArray = targetIds.toArray(new String[0]);
        return followRepository.findFollowingIds(userId, idsArray);
    }
}
