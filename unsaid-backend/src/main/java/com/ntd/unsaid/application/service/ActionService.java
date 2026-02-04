package com.ntd.unsaid.application.service;

import com.ntd.unsaid.application.dto.request.ActionRequest;
import com.ntd.unsaid.domain.enums.*;
import com.ntd.unsaid.domain.event.ActionMessage;
import com.ntd.unsaid.exception.AppException;
import com.ntd.unsaid.infrastructure.messaging.producer.RabbitMQPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActionService {

    StringRedisTemplate redisTemplate;
    RabbitMQPublisher rabbitMQPublisher;

    private static final RedisScript<Long> LIKE_UNLIKE_SCRIPT =
            new DefaultRedisScript<>(
                    """
                            if redis.call("SISMEMBER", KEYS[1], ARGV[1]) == 0 then
                                redis.call("SADD", KEYS[1], ARGV[1])
                                redis.call("INCR", KEYS[2])
                                redis.call("SADD", KEYS[3], ARGV[2])
                                return 1
                            else
                                redis.call("SREM", KEYS[1], ARGV[1])
                                redis.call("DECR", KEYS[2])
                                redis.call("SADD", KEYS[3], ARGV[2])
                                return 0
                            end
                            """,
                    Long.class
            );

    public String likeOrUnlikePostV2(String userId, ActionRequest actionRequest) {

        String postId = actionRequest.getPostId();

        String isLikedKey = "post:liked_users:" + postId;
        String likeCountKey = "post:like_count:" + postId;
        String dirtySetKey = "sys:dirty_posts";

        Long result = redisTemplate.execute(
                LIKE_UNLIKE_SCRIPT,
                List.of(isLikedKey, likeCountKey, dirtySetKey),
                userId,
                postId
        );

        if (result == null) {
            throw new AppException(ErrorCode.REDIS_OPERATION_FAILED);
        }

        ActionType actionType =
                (result == 1L) ? ActionType.LIKE : ActionType.UNLIKE;

        ActionMessage actionMessage = ActionMessage.builder()
                .postId(postId)
                .actionType(actionType.getValue())
                .userId(userId)
                .createdAt(Instant.now())
                .build();

        rabbitMQPublisher.publishActionMessage(actionMessage);

        return actionType.getValue();
    }


}
