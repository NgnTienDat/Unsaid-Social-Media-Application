package com.ntd.unsaid.infrastructure.messaging.consumer;

import com.ntd.unsaid.config.RabbitMQConfig;
import com.ntd.unsaid.domain.event.PostCreatedEvent;
import com.ntd.unsaid.infrastructure.messaging.worker.FeedWorker;
import com.ntd.unsaid.infrastructure.messaging.worker.NotificationWorker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostCreatedConsumer {

    FeedWorker feedWorker;
    NotificationWorker notificationWorker;

    @RabbitListener(queues = RabbitMQConfig.POST_CREATED_QUEUE)
    public void handle(PostCreatedEvent event) {

        // 1. Fan-out feed
        feedWorker.handle(event);

        // 2. Notification
        notificationWorker.handle(event);
    }
}
