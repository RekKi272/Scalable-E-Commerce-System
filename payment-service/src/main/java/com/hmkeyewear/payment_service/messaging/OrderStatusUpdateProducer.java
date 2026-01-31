package com.hmkeyewear.payment_service.messaging;

import com.hmkeyewear.common_dto.dto.OrderStatusEventDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStatusUpdateProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusUpdateProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.order-status.routing-key}")
    private String routingKey;

    public void sendUpdateStatusRequest(OrderStatusEventDto event) {
        LOGGER.info("Send ORDER STATUS update: {}", event);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }
}
