package com.ntd.unsaid.infrastructure.messaging;

import com.ntd.unsaid.application.service.FeedService;
import com.ntd.unsaid.domain.event.PostCreatedEvent;
import com.ntd.unsaid.domain.repository.FollowRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationWorker {

    FollowRepository followRepository;
    SimpMessagingTemplate messagingTemplate;

    public void handle(PostCreatedEvent event) {

        List<String> emails =
                followRepository.findFollowerEmailsByFollowingId(event.authorId());

        for (String userEmail : emails) {
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    "/queue/post/notifications",
                    "New post from user you follow"
            );
        }
    }
}

