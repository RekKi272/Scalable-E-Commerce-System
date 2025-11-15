package com.hmkeyewear.cart_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CartEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartEventProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.cart.routing-key}")
    private String orderRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public CartEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Object message){
        LOGGER.info("Message sent -> {}", message.toString());
        rabbitTemplate.convertAndSend(exchangeName, orderRoutingKey, message);
    }
}
