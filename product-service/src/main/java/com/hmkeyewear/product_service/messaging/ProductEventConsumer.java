package com.hmkeyewear.product_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventConsumer.class);

    @RabbitListener(queues = {"${app.rabbitmq.product.queue}"})
    public void consume(Object message) {
        LOGGER.info("Received message -> {}",message.toString());
    }
}
