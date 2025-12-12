package com.hmkeyewear.order_service.config;

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


    // ---- Queues ----
    @Bean
    public Queue orderQueue() {
        return new Queue(orderQueueName, true, false, false);
    }


    @Bean
    public Queue orderCheckoutQueue() {
        return new Queue(orderCheckoutQueueName, true, false, false);
    }

    @Bean
    public Queue orderSaveListenerQueue() {
        return new Queue(orderSaveListenerQueueName, true, false, false);
    }

    @Bean
    public Queue stockUpdateRequestQueue() {
        return new Queue(stockUpdateRequestQueueName, true, false, false);
    }

    @Bean
    public Queue orderStatusQueue() {
        return new Queue(orderStatusQueueName, true, false, false);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
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
