package com.hmkeyewear.blog_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class BlogEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlogEventConsumer.class);

    @RabbitListener(queues = {"${app.rabbitmq.blog.queue}"})
    public void consume(Object message){
        LOGGER.info("Received message -> {}",message.toString());
    }
}
