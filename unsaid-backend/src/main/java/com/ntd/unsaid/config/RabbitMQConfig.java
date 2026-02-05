package com.ntd.unsaid.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String POST_EXCHANGE = "post.exchange";
    public static final String POST_CREATED_QUEUE = "post.created.queue";
    public static final String POST_CREATED_ROUTING_KEY = "post.created";

    public static final String ACTION_EXCHANGE = "action.exchange";
    public static final String ACTION_QUEUE = "action.created.queue";
    public static final String ACTION_ROUTING_KEY = "action.created";


    @Bean
    public TopicExchange postExchange() {
        return new TopicExchange(POST_EXCHANGE, true, false);
    }

    @Bean
    public Queue postCreatedQueue() {
        return QueueBuilder.durable(POST_CREATED_QUEUE).build();
    }

    @Bean
    public Binding postCreatedBinding() {
        return BindingBuilder
                .bind(postCreatedQueue())
                .to(postExchange())
                .with(POST_CREATED_ROUTING_KEY);
    }

    @Bean
    public TopicExchange actionExchange() {
        return new TopicExchange(ACTION_EXCHANGE, true, false);
    }

    @Bean
    public Queue actionCreatedQueue() {
        return QueueBuilder.durable(ACTION_QUEUE).build();
    }

    @Bean
    public Binding actionCreatedBinding() {
        return BindingBuilder
                .bind(actionCreatedQueue())
                .to(actionExchange())
                .with(ACTION_ROUTING_KEY);
    }


    @Bean
    public JacksonJsonMessageConverter jacksonConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter jacksonMessageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }


}
