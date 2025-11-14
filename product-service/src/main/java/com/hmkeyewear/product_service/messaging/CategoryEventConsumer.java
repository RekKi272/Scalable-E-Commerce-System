package com.hmkeyewear.product_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class CategoryEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryEventConsumer.class);

    @RabbitListener(queues = {"${app.rabbitmq.category.queue}"})
    public void consume(Object message) {
        LOGGER.info("Received message -> {}",message.toString());
    }
}
