package com.hmkeyewear.product_service.config;

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

    @Value("${app.rabbitmq.product.queue}")
    private String productQueueName;

    @Value("${app.rabbitmq.product.routing-key}")
    private String productRoutingKey;

    @Value("${app.rabbitmq.brand.queue}")
    private String brandQueueName;

    @Value("${app.rabbitmq.brand.routing-key}")
    private String brandRoutingKey;

    @Value("${app.rabbitmq.category.queue}")
    private String categoryQueueName;

    @Value("${app.rabbitmq.category.routing-key}")
    private String categoryRoutingKey;

    // Stock update request queue
    @Value("${app.rabbitmq.stock-update-request.queue}")
    private String stockUpdateRequestQueueName;

    @Value("${app.rabbitmq.stock-update-request.routing-key}")
    private String stockUpdateRequestRoutingKey;

    // ---- Queues ----
    @Bean
    public Queue productQueue() {
        return new Queue(productQueueName, true);
    }

    @Bean
    public Queue brandQueue() {
        return new Queue(brandQueueName, true);
    }

    @Bean
    public Queue categoryQueue() {
        return new Queue(categoryQueueName, true);
    }

    @Bean
    public Queue stockUpdateRequestQueue() {
        return new Queue(stockUpdateRequestQueueName, true);
    }

    // ---- Exchange ----
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    // ---- Bindings ----
    @Bean
    public Binding productBinding() {
        return BindingBuilder
                .bind(productQueue())
                .to(exchange())
                .with(productRoutingKey);
    }

    @Bean
    public Binding brandBinding() {
        return BindingBuilder
                .bind(brandQueue())
                .to(exchange())
                .with(brandRoutingKey);
    }

    @Bean
    public Binding categoryBinding() {
        return BindingBuilder
                .bind(categoryQueue())
                .to(exchange())
                .with(categoryRoutingKey);
    }

    @Bean
    public Binding stockUpdateRequestBinding() {
        return BindingBuilder
                .bind(stockUpdateRequestQueue())
                .to(exchange())
                .with(stockUpdateRequestRoutingKey);
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

