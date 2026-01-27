package com.hmkeyewear.blog_service.config;

import org.springframework.amqp.core.*;
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

    @Value("${app.rabbitmq.blog.queue}")
    private String blogQueueName;

    @Value("${app.rabbitmq.blog.routing-key}")
    private String blogRoutingKey;

    @Value("${app.rabbitmq.banner.queue}")
    private String bannerQueueName;

    @Value("${app.rabbitmq.banner.routing-key}")
    private String bannerRoutingKey;

    @Bean
    public Queue blogQueue() {
        return QueueBuilder
                .durable(blogQueueName).build();
    }

    @Bean
    public Queue bannerQueue() {
        return QueueBuilder
                .durable(bannerQueueName).build();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Binding blogBinding() {
        return BindingBuilder
                .bind(blogQueue())
                .to(exchange())
                .with(blogRoutingKey);
    }

    @Bean
    public Binding bannerBinding() {
        return BindingBuilder
                .bind(bannerQueue())
                .to(exchange())
                .with(bannerRoutingKey);
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
