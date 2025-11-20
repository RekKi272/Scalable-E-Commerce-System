package com.hmkeyewear.file_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
public class FileEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileEventProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.file.routing-key}")
    private String fileRoutingKey;

    private final RabbitTemplate rabbitTemplate;
    public FileEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Object message){
        LOGGER.info("File message sent -> {}", message == null ? "null" : message.toString());
        rabbitTemplate.convertAndSend(exchangeName, fileRoutingKey, message);
    }
}
