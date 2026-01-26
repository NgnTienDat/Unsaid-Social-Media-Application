package com.ntd.unsaid.infrastructure.caching;

import com.ntd.unsaid.utils.Constant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedRedisRepository {

    StringRedisTemplate redisTemplate;

    public void pushPostToFollowers(String postId, List<String> userIds, long score) {
        // 1. Lấy serializer chuẩn của StringRedisTemplate để chuyển String -> byte[]
        RedisSerializer<String> serializer = redisTemplate.getStringSerializer();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // KHÔNG cast sang StringRedisConnection
            // Sử dụng trực tiếp RedisConnection (làm việc với byte[])

            // Serialize value (postId) một lần để dùng lại (tối ưu hiệu năng)
            byte[] valueBytes = serializer.serialize(postId);

            for (String userId : userIds) {
                String key = "feed:user:" + userId;

                // Serialize Key
                byte[] keyBytes = serializer.serialize(key);

                // Gọi lệnh cấp thấp (Low-level API) với byte[]
                connection.zAdd(keyBytes, score, valueBytes);
                connection.zRemRange(keyBytes, 0, -Constant.MAX_FEED_SIZE - 1);
            }
            return null;
        });
    }
}