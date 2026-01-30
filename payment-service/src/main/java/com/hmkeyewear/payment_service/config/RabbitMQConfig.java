package com.hmkeyewear.payment_service.config;

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

    @Value("${app.rabbitmq.order-status.routing-key}")
    private String orderStatusRoutingKey;

    @Bean
    public Queue paymentRequestQueue() {
        return QueueBuilder.durable(paymentRequestQueue).build();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder
                .bind(paymentRequestQueue())
                .to(exchange())
                .with(paymentRequestRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(messageConverter());
        return rt;
    }
}
