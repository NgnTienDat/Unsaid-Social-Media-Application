package com.ntd.unsaid.infrastructure.caching;

import com.ntd.unsaid.application.dto.FeedPostDTO;
import com.ntd.unsaid.utils.Constant;
import com.ntd.unsaid.utils.RedisKeys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;

import static com.ntd.unsaid.utils.Constant.PAGE_SIZE;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisRepository {

    StringRedisTemplate redisTemplate;
    RedisTemplate<String, FeedPostDTO> feedPostRedisTemplate;
    // ObjectMapper objectMapper;

    public void savePostToCache(String postId, FeedPostDTO dto) {
        String key = RedisKeys.postData(postId);
        feedPostRedisTemplate.opsForValue().set(key, dto, Duration.ofHours(1));
    }

    /*==============================FEED=============================*/
    public void pushPostToFollowers(String postId, List<String> userIds, long score) {
        // 1. Lấy serializer chuẩn của StringRedisTemplate để chuyển String -> byte[]
        RedisSerializer<String> serializer = redisTemplate.getStringSerializer();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // KHÔNG cast sang StringRedisConnection
            // Sử dụng trực tiếp RedisConnection (làm việc với byte[])

            // Serialize value (postId) một lần để dùng lại (tối ưu hiệu năng)
            byte[] valueBytes = serializer.serialize(postId);

            for (String userId : userIds) {
                String key = RedisKeys.userFeed(userId);

                // Serialize Key
                byte[] keyBytes = serializer.serialize(key);

                // Gọi lệnh cấp thấp (Low-level API) với byte[]
                connection.zAdd(keyBytes, score, valueBytes);
                connection.zRemRange(keyBytes, 0, -Constant.MAX_FEED_SIZE - 1);
            }
            return null;
        });
    }

    public void pushToUserFeed(String userId, String postId, long score) {
        String key = RedisKeys.userFeed(userId);
        redisTemplate.opsForZSet().add(key, postId, score);
        redisTemplate.opsForZSet().removeRange(key, 0, -Constant.MAX_FEED_SIZE - 1);
    }

    public Set<ZSetOperations.TypedTuple<String>> getPushedFeed(
            String userId, long start, long end
    ) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(RedisKeys.userFeed(userId), start, end);
    }


    /*===========================CELEBRITY USERS========================*/
    public void pushToCelebrityUsers(String userId) {
        String key = "celebrity_users";
        redisTemplate.opsForSet().add(key, userId);
    }

    public Set<String> getCelebrityUserIds() {
        String key = "celebrity_users";
        return redisTemplate.opsForSet().members(key);
    }


    /*===========================FOLLOWING CELEBS========================*/
    public void pushToFollowingCelebs(String userId, String celebId) {
        String key = "user:following_celebs:" + userId;
        redisTemplate.opsForSet().add(key, celebId);
    }



    public Set<String> getFollowingCelebs(String userId) {
        return redisTemplate.opsForSet()
                .members("user:following_celebs:" + userId);
    }

    /*===========================POST TIMELINE========================*/


    public List<Object> getCelebrityPosts(Set<String> celebIds, int limit) {
        // SessionCallback cung cấp 'operations' đã được cấu hình serializer
        return redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                // Ép kiểu operations về đúng loại bạn đang dùng (thường là String, String hoặc String, Object)
                // Giả sử redisTemplate của bạn là <String, String> hoặc <String, Object>
                RedisOperations<String, Object> ops = (RedisOperations<String, Object>) operations;

                for (String celebId : celebIds) {
                    String key = RedisKeys.userPostTimeline(celebId);
                    ops.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
                }
                return null;
            }
        });
    }


    public void unionMergeFeeds(String userFeedKey, List<String> keysToMerge, String finalKey) {
        // Thực hiện Union và lưu kết quả vào finalKey
        redisTemplate.opsForZSet().unionAndStore(userFeedKey, keysToMerge, finalKey);
        // Đặt TTL cho kết quả (ví dụ 10 giây để user F5 không spam lệnh Union)
        redisTemplate.expire(finalKey, Duration.ofSeconds(10));
    }


    public Map<String, FeedPostDTO> getPostsFromCacheMGET(List<String> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();

        // 1. Create keys
        List<String> keys = postIds.stream()
                .map(RedisKeys::postData)
                .toList();

        // 2. Get multi values from Redis
        List<FeedPostDTO> results = feedPostRedisTemplate.opsForValue().multiGet(keys);

        // 3. Map results to Map<String, FeedPostDTO>, ignoring nulls (cache misses)
        Map<String, FeedPostDTO> postMap = new HashMap<>();
        for (int i = 0; i < postIds.size(); i++) {
            FeedPostDTO post = results.get(i);
            if (post != null) {
                postMap.put(postIds.get(i), post);
            }
        }
        return postMap;
    }

    public boolean existKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public List<String> getPostIdsFromFeedComputed(String userId, int page) {
        int start = (page - 1) * PAGE_SIZE;
        return redisTemplate.opsForZSet()
                .reverseRange(RedisKeys.userFeedComputed(userId), start, start + PAGE_SIZE - 1)
                .stream().toList();
    }

    public void pushToPostTimeline(String userId, String postId, long score) {
        String key = RedisKeys.userPostTimeline(userId);
        redisTemplate.opsForZSet().add(key, postId, score);
        redisTemplate.opsForZSet().removeRange(key, 0, -Constant.MAX_TIMELINE_SIZE - 1);
    }
}