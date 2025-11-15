package com.hmkeyewear.user_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventConsumer.class);

    @RabbitListener(queues = {"${app.rabbitmq.user.queue}"})
    public void consume(Object message){
        LOGGER.info("Received message -> {}",message.toString());
    }
}
