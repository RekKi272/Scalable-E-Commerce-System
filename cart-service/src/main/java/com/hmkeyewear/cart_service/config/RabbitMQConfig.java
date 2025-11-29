package com.hmkeyewear.cart_service.config;

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

    @Value("${app.rabbitmq.cart.queue}")
    private String cartQueueName;

    @Value("${app.rabbitmq.cart.routing-key}")
    private String cartRoutingKey;

    // For payment workflow
    @Value("${app.rabbitmq.payment-request.queue}")
    private String paymentRequestQueueName;
    @Value("${app.rabbitmq.payment-request.routing-key}")
    private String paymentRequestRoutingKey;

    // Order checkout request
    @Value("${app.rabbitmq.order-checkout.queue}")
    private String orderCheckoutQueueName;
    @Value("${app.rabbitmq.order-checkout.routing-key}")
    private String orderCheckoutRoutingKey;

    // Discount
    @Value("${app.rabbitmq.discount.queue}")
    private String discountQueueName;
    @Value("${app.rabbitmq.discount.routing-key}")
    private String discountRoutingKey;

    @Bean
    public Queue cartQueue() {
        return new Queue(cartQueueName, true, false, false);
    }

    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(paymentRequestQueueName, true, false, false);
    }

    @Bean
    public Queue orderCheckoutQueue() {
        return new Queue(orderCheckoutQueueName, true, false, false);
    }

    @Bean
    public Queue discountQueue() {
        return new Queue(discountQueueName, true, false, false);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding cartBinding() {
        return BindingBuilder
                .bind(cartQueue())
                .to(exchange())
                .with(cartRoutingKey);
    }

    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder
                .bind(paymentRequestQueue())
                .to(exchange())
                .with(paymentRequestRoutingKey);
    }

    @Bean
    public Binding orderCheckoutBinding() {
        return BindingBuilder
                .bind(orderCheckoutQueue())
                .to(exchange())
                .with(orderCheckoutRoutingKey);
    }

    @Bean
    public Binding discountBinding() {
        return BindingBuilder
                .bind(discountQueue())
                .to(exchange())
                .with(discountRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        // We use convertSendAndReceive so configure reply timeout
        rabbitTemplate.setReplyTimeout(10_000); // 10s
        return rabbitTemplate;
    }
}
