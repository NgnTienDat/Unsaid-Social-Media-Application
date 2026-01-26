package com.ntd.unsaid.application.listener;

import com.ntd.unsaid.application.service.FeedService;
import com.ntd.unsaid.domain.event.PostCreatedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostEventListener {
    FeedService feedService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        // 1 push to author's feed and author's post timeline (optional, depending on design)
        // 2 fan out to followers
        feedService.fanOutToFollowers(event.postId(), event.authorId(), event.createdAt().toEpochMilli());
    }
}
