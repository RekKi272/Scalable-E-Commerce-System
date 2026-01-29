package com.hmkeyewear.order_service.config;

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

    @Value("${app.rabbitmq.order.queue}")
    private String orderQueueName;

    @Value("${app.rabbitmq.order.routing-key}")
    private String orderRoutingKey;

    // Order checkout request
    @Value("${app.rabbitmq.order-checkout.queue}")
    private String orderCheckoutQueueName;
    @Value("${app.rabbitmq.order-checkout.routing-key}")
    private String orderCheckoutRoutingKey;

    // Order save request handler
    @Value("${app.rabbitmq.order-save-listener.queue}")
    private String orderSaveListenerQueueName;
    @Value("${app.rabbitmq.order-save-listener.routing-key}")
    private String orderSaveListenerRoutingKey;

    // Update stock in product
    @Value("${app.rabbitmq.stock-update-request.queue}")
    private String stockUpdateRequestQueueName;
    @Value("${app.rabbitmq.stock-update-request.routing-key}")
    private String stockUpdateRequestRoutingKey;

    // Update order status after payment
    @Value("${app.rabbitmq.order-status.queue}")
    private String orderStatusQueueName;
    @Value("${app.rabbitmq.order-status.routing-key}")
    private String orderStatusRoutingKey;

    // Order mail request
    @Value("${app.rabbitmq.order-mail.queue}")
    private String orderMailQueue;
    @Value("${app.rabbitmq.order-mail.routing-key}")
    private String orderMailRoutingKey;

    // ---- Queues ----
    @Bean
    public Queue orderQueue() {
        return QueueBuilder
                .durable(orderQueueName).build();
    }

    @Bean
    public Queue orderCheckoutQueue() {
        return QueueBuilder
                .durable(orderCheckoutQueueName).build();
    }

    @Bean
    public Queue orderSaveListenerQueue() {
        return QueueBuilder
                .durable(orderSaveListenerQueueName).build();
    }

    @Bean
    public Queue stockUpdateRequestQueue() {
        return QueueBuilder
                .durable(stockUpdateRequestQueueName).build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder
                .durable(orderStatusQueueName).build();
    }

    // @Bean
    // public Queue orderMailQueue() {
    // return QueueBuilder
    // .durable(orderMailQueue).build();
    // }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // ---- Bindings ----
    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(exchange())
                .with(orderRoutingKey);
    }

    @Bean
    public Binding orderCheckoutBinding() {
        return BindingBuilder
                .bind(orderCheckoutQueue())
                .to(exchange())
                .with(orderCheckoutRoutingKey);
    }

    @Bean
    public Binding orderSaveListenerBinding() {
        return BindingBuilder
                .bind(orderSaveListenerQueue())
                .to(exchange())
                .with(orderSaveListenerRoutingKey);
    }

    @Bean
    public Binding stockUpdateRequestBinding() {
        return BindingBuilder
                .bind(stockUpdateRequestQueue())
                .to(exchange())
                .with(stockUpdateRequestRoutingKey);
    }

    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder
                .bind(orderStatusQueue())
                .to(exchange())
                .with(orderStatusRoutingKey);
    }

    // @Bean
    // public Binding orderMailBinding() {
    // return BindingBuilder
    // .bind(orderMailQueue())
    // .to(exchange())
    // .with(orderMailRoutingKey);
    // }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setReplyTimeout(10_000); // 10s
        return rabbitTemplate;
    }
}
