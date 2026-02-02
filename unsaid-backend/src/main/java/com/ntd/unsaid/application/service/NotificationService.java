package com.ntd.unsaid.application.service;


import com.ntd.unsaid.application.dto.NotificationDTO;
import com.ntd.unsaid.domain.repository.FollowRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    SimpMessagingTemplate messagingTemplate;
    FollowRepository followRepository;

    @Async("taskExecutor")
    public void notifyPostCreated(
            String authorId,
            String postId,
            long createdAt
    ) {
        List<String> followerIds =
                followRepository.findFollowerEmailsByFollowingId(authorId);

        if (followerIds == null || followerIds.isEmpty()) return;

        for (String followerId : followerIds) {

            NotificationDTO notification = NotificationDTO.builder()
                    .type("POST_CREATED")
                    .postId(postId)
                    .fromUserId(authorId)
                    .message("Someone you follow just posted")
                    .createdAt(createdAt)
                    .build();

            messagingTemplate.convertAndSendToUser(
                    followerId, // MUST match Principal name. Currently, using email as Principal name
                    "/queue/post/notifications",
                    notification
            );
        }
    }
}

