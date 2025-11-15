package com.hmkeyewear.auth_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.user.routing_key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public UserEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Object message){
        LOGGER.info("Message sent -> {}", message.toString());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}
