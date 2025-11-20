package com.hmkeyewear.file_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class FileEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileEventConsumer.class);

    @RabbitListener(queues = {"${app.rabbitmq.file.queue}"})
    public void consume(Object message){
        LOGGER.info("File event received -> {}", message == null ? "null" : message.toString());
    }
}
