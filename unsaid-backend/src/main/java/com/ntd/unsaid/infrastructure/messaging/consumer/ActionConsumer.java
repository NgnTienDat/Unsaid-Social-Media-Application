package com.ntd.unsaid.infrastructure.messaging.consumer;

import com.ntd.unsaid.config.RabbitMQConfig;
import com.ntd.unsaid.domain.event.ActionMessage;
import com.ntd.unsaid.infrastructure.messaging.worker.ActionWorker;
import com.ntd.unsaid.infrastructure.messaging.worker.NotificationWorker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActionConsumer {

    ActionWorker actionWorker;

    @RabbitListener(queues = RabbitMQConfig.ACTION_QUEUE)
    public void handle(ActionMessage message) {
        actionWorker.handle(message);
    }
}
