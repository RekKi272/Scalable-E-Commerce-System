package com.hmkeyewear.auth_service.config;

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

    @Value("${app.rabbitmq.user.queue}")
    private String userQueueName;

    @Value("${app.rabbitmq.user.routing_key}")
    private String userRoutingKey;

    // Forgot Password Sender
    @Value("${app.rabbitmq.forgot-password.queue}")
    private String forgotPasswordQueueName;
    @Value("${app.rabbitmq.forgot-password.routing_key}")
    private String forgotPasswordRoutingKey;

    @Bean
    public Queue userQueue() {
        return new Queue(userQueueName, true, false, true); // Auto delete message
    }

    @Bean
    public Queue forgotPasswordQueue() {
        return new Queue(forgotPasswordQueueName, true, false, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding userBinding() {
        return BindingBuilder
                .bind(userQueue())
                .to(exchange())
                .with(userRoutingKey);
    }

    @Bean
    public Binding forgotPasswordBinding() {
        return BindingBuilder
                .bind(forgotPasswordQueue())
                .to(exchange())
                .with(forgotPasswordRoutingKey);
    }

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
