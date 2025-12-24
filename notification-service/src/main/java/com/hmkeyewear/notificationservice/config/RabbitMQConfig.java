package com.hmkeyewear.notificationservice.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    // For order mail sender workflow
    @Value("${app.rabbitmq.order-mail.queue}")
    private String orderMailQueue;
    @Value("${app.rabbitmq.order-mail.routing-key}")
    private String orderMailRoutingKey;

    // For forgot password sender workflow
    @Value("${app.rabbitmq.forgot-password.queue}")
    private String forgotPasswordQueue;
    @Value("${app.rabbitmq.forgot-password.routing_key}")
    private String forgotPasswordRoutingKey;

    // ---- Queues ----
    @Bean
    public Queue orderMailQueue() {
        return new Queue(orderMailQueue);
    }

    @Bean
    public Queue forgotPasswordQueue() {
        return new Queue(forgotPasswordQueue);
    }

    // ---- Exchange ----
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    // ---- Bindings ----
    @Bean
    public Binding orderMailBinding() {
        return BindingBuilder
                .bind(orderMailQueue())
                .to(exchange())
                .with(orderMailRoutingKey);
    }

    @Bean
    public Binding forgotPasswordBinding() {
        return BindingBuilder
                .bind(forgotPasswordQueue())
                .to(exchange())
                .with(forgotPasswordRoutingKey);
    }

    // ---- Message Converter & RabbitTemplate ----
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}
