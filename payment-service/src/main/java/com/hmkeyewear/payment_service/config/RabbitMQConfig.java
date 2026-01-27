package com.hmkeyewear.payment_service.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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

    @Value("${app.rabbitmq.payment-request.queue}")
    private String paymentRequestQueue;

    @Value("${app.rabbitmq.payment-request.routing-key}")
    private String paymentRequestRoutingKey;

    @Value("${app.rabbitmq.order.queue}")
    private String orderQueue;

    @Value("${app.rabbitmq.order.routing-key}")
    private String orderRoutingKey;

    // Order status update
    @Value("${app.rabbitmq.order-status.queue}")
    private String orderStatusQueue;
    @Value("${app.rabbitmq.order-status.routing-key}")
    private String orderStatusRoutingKey;

    // ---- Queues ----
    @Bean
    public Queue paymentRequestQueue() {
        return QueueBuilder
                .durable(paymentRequestQueue).build();
    }

//    @Bean
//    public Queue orderQueue() {
//        return QueueBuilder
//                .durable(orderQueue).build();
//    }

//    @Bean
//    public Queue orderStatusQueue() {
//        return QueueBuilder
//                .durable(orderStatusQueue).build();
//    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // ---- Bindings ----
    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder
                .bind(paymentRequestQueue())
                .to(exchange())
                .with(paymentRequestRoutingKey);
    }

//    @Bean
//    public Binding orderBinding() {
//        return BindingBuilder
//                .bind(orderQueue())
//                .to(exchange())
//                .with(orderRoutingKey);
//    }

//    @Bean
//    public Binding orderStatusBinding() {
//        return BindingBuilder
//                .bind(orderStatusQueue())
//                .to(exchange())
//                .with(orderStatusRoutingKey);
//    }

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
