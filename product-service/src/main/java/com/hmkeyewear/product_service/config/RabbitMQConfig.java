package com.hmkeyewear.product_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
    @Value("${app.rabbitmq.stock-update-increase.queue}")
    private String stockUpdateIncreaseQueueName;

    @Value("${app.rabbitmq.stock-update-decrease.queue}")
    private String stockUpdateDecreaseQueueName;

    @Value("${app.rabbitmq.stock-update-increase.routing-key}")
    private String stockUpdateIncreaseRoutingKey;

    @Value("${app.rabbitmq.stock-update-decrease.routing-key}")
    private String stockUpdateDecreaseRoutingKey;

    // ---- Queues ----
    @Bean
    public Queue productQueue() {
        return QueueBuilder
                .durable(productQueueName).build();
    }

    @Bean
    public Queue brandQueue() {
        return QueueBuilder
                .durable(brandQueueName).build();
    }

    @Bean
    public Queue categoryQueue() {
        return QueueBuilder
                .durable(categoryQueueName).build();
    }

    @Bean
    public Queue stockUpdateIncreaseQueue() {
        return QueueBuilder.durable(stockUpdateIncreaseQueueName).build();
    }

    @Bean
    public Queue stockUpdateDecreaseQueue() {
        return QueueBuilder.durable(stockUpdateDecreaseQueueName).build();
    }

    // ---- Exchange ----
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName, true, false);
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
    public Binding stockUpdateIncreaseBinding() {
        return BindingBuilder
                .bind(stockUpdateIncreaseQueue())
                .to(exchange())
                .with(stockUpdateIncreaseRoutingKey);
    }

    @Bean
    public Binding stockUpdateDecreaseBinding() {
        return BindingBuilder
                .bind(stockUpdateDecreaseQueue())
                .to(exchange())
                .with(stockUpdateDecreaseRoutingKey);
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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
