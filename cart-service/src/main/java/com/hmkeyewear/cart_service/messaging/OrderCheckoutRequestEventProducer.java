package com.hmkeyewear.cart_service.messaging;

import com.hmkeyewear.common_dto.dto.PaymentRequestDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderCheckoutRequestEventProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCheckoutRequestEventProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.order-checkout.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public OrderCheckoutRequestEventProducer(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderCheckoutRequest(PaymentRequestDto payload) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, payload);
        LOGGER.info("Order Checkout request sent");
    }
}
