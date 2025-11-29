package com.hmkeyewear.payment_service.messaging;

import com.hmkeyewear.payment_service.dto.OrderSaveRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderSaveRequestProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaveRequestProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.order.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public OrderSaveRequestProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendSaveRequest(OrderSaveRequestDto orderSaveRequestDto) {
        LOGGER.info("Sending order save request to {}", orderSaveRequestDto);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, orderSaveRequestDto);
    }
}
