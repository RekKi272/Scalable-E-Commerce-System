package com.hmkeyewear.cart_service.messaging;

import com.hmkeyewear.cart_service.model.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class CartEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CartEventConsumer.class);

    @RabbitListener(queues = {"${app.rabbitmq.cart.queue}"})
    public void consume(String message) {
        LOGGER.info("Received message: {}", message);
    }
}
