package com.ntd.unsaid.application.service;

import com.ntd.unsaid.config.RabbitMQConfig;
import com.ntd.unsaid.domain.event.PostCreatedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostEventPublisher {

    RabbitTemplate rabbitTemplate;

    public void publishPostCreated(PostCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POST_EXCHANGE,
                RabbitMQConfig.POST_CREATED_ROUTING_KEY,
                event
        );
    }
}

