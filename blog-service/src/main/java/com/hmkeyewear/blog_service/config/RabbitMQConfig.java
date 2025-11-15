package com.hmkeyewear.blog_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
        return new Queue(blogQueueName);
    }

    @Bean
    public Queue bannerQueue() {
        return new Queue(bannerQueueName);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
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
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
