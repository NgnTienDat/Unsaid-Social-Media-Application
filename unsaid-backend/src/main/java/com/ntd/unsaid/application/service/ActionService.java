package com.ntd.unsaid.application.service;

import com.ntd.unsaid.application.dto.request.ActionRequest;
import com.ntd.unsaid.domain.enums.*;
import com.ntd.unsaid.domain.event.ActionMessage;
import com.ntd.unsaid.exception.AppException;
import com.ntd.unsaid.infrastructure.messaging.producer.RabbitMQPublisher;
import com.ntd.unsaid.utils.RedisKeys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    RedisScript<Long> likeUnlikeScript;




    public void likeOrUnlikePost(String userId, ActionRequest actionRequest) {

        String postId = actionRequest.getPostId();

        String isLikedKey = RedisKeys.userLiked(postId); // key: posts:liked_users:{postId}
        String likeCountKey = RedisKeys.postLikeCount(postId);
        String dirtySetKey = RedisKeys.dirtyPosts();

        Long result = redisTemplate.execute(
                likeUnlikeScript,
                List.of(isLikedKey, likeCountKey, dirtySetKey),
                userId,
                postId
        );

        if (result == null) {
            throw new AppException(ErrorCode.REDIS_OPERATION_FAILED);
        }

        ActionType actionType = (result == 1L) ? ActionType.LIKE : ActionType.UNLIKE;
//        ActionType actionType = ActionType.LIKE;

        ActionMessage actionMessage = ActionMessage.builder()
                .postId(postId)
                .actionType(actionType.getValue())
                .userId(userId)
                .createdAt(Instant.now())
                .build();

        rabbitMQPublisher.publishActionMessage(actionMessage);

    }


}
