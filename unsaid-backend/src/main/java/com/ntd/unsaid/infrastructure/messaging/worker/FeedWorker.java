package com.ntd.unsaid.infrastructure.messaging.worker;

import com.ntd.unsaid.application.service.FeedService;
import com.ntd.unsaid.domain.event.PostCreatedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedWorker {

    FeedService feedService;

    public void handle(PostCreatedEvent event) {
        feedService.fanOutToFollowers(
                event.postId(),
                event.authorId(),
                event.followerCount(),
                event.createdAt().toEpochMilli()
        );
    }
}

