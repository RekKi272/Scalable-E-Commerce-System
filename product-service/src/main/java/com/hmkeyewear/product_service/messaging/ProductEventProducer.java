package com.hmkeyewear.product_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProductEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.product.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public ProductEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Object message) {
        LOGGER.info("Message sent -> {}", message.toString());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}
