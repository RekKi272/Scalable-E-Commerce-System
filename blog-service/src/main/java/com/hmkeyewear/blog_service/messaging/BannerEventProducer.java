package com.hmkeyewear.blog_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BannerEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BannerEventProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.banner.routing-key}")
    private String bannerRoutingKey;

    private final RabbitTemplate rabbitTemplate;
    public BannerEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Object message){
        LOGGER.info("Message sent -> {}", message.toString());
        rabbitTemplate.convertAndSend(exchangeName, bannerRoutingKey, message);
    }
}
