package com.hmkeyewear.cart_service.messaging;

import com.hmkeyewear.common_dto.dto.PaymentRequestDto;
import com.hmkeyewear.common_dto.dto.VNPayResponseDto;
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

    /**
     * Producing queue: payment_request_queue
     * Message sent to payment-service WHEN USER click CHECKOUT for ONLINE PAYMENT (VNPAY)
     */
    public VNPayResponseDto sendPaymentRequest(PaymentRequestDto payload) {
        return (VNPayResponseDto) rabbitTemplate.convertSendAndReceive(exchangeName, routingKey, payload);
    }

}
