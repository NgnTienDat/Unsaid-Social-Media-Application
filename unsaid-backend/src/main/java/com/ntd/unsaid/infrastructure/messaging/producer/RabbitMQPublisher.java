package com.ntd.unsaid.infrastructure.messaging.producer;

import com.ntd.unsaid.config.RabbitMQConfig;
import com.ntd.unsaid.domain.event.ActionMessage;
import com.ntd.unsaid.domain.event.PostCreatedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RabbitMQPublisher {

    RabbitTemplate rabbitTemplate;

    public void publishPostCreated(PostCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POST_EXCHANGE,
                RabbitMQConfig.POST_CREATED_ROUTING_KEY,
                event
        );
    }

    public void publishActionMessage(ActionMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACTION_EXCHANGE,
                RabbitMQConfig.ACTION_ROUTING_KEY,
                message
        );
    }
}

