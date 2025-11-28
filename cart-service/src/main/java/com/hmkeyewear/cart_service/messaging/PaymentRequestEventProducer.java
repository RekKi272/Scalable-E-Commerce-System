package com.hmkeyewear.cart_service.messaging;

import com.hmkeyewear.cart_service.dto.PaymentRequestDto;
import com.hmkeyewear.cart_service.dto.VNPayResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentRequestEventProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestEventProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.payment-request.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public PaymentRequestEventProducer(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public VNPayResponseDto sendPaymentRequest(PaymentRequestDto payload) {
        Object response = rabbitTemplate.convertSendAndReceive(exchangeName, routingKey, payload);
        return (VNPayResponseDto) response;
    }
}
