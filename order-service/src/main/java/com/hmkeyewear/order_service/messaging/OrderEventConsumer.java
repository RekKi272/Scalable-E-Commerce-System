package com.hmkeyewear.order_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventConsumer.class);

    @RabbitListener(queues = {"${app.rabbitmq.order.queue}"})
    public void consume(Object message){
        LOGGER.info("Received message -> {}",message.toString());
    }
}
