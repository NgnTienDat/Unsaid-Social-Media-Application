package com.ntd.unsaid.infrastructure.messaging.consumer;

import com.ntd.unsaid.config.RabbitMQConfig;
import com.ntd.unsaid.domain.event.ActionMessage;
import com.ntd.unsaid.infrastructure.messaging.worker.ActionWorker;
import com.ntd.unsaid.infrastructure.messaging.worker.NotificationWorker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.worker.enabled", havingValue = "true")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActionConsumer {

    ActionWorker actionWorker;

    @RabbitListener(queues = RabbitMQConfig.ACTION_QUEUE)
    public void handle(ActionMessage message) {
        try {
            actionWorker.handle(message);
        } catch (Exception e) {
            log.error("Failed to process message {}", message, e);
            throw e; // để Rabbit retry
        }
    }
}
