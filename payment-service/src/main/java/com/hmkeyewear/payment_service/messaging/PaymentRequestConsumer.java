package com.hmkeyewear.payment_service.messaging;

import com.hmkeyewear.common_dto.dto.PaymentRequestDto;
import com.hmkeyewear.common_dto.dto.VNPayResponseDto;
import com.hmkeyewear.payment_service.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentRequestConsumer {

    private final PaymentService paymentService;

    public PaymentRequestConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Listening queue: payment_request_queue
     * Message sent from cart-service WHEN USER checkout their CART for ONLINE PAYMENT (VNPAY)
     */
    @RabbitListener(queues = "${app.rabbitmq.payment-request.queue}")
    public VNPayResponseDto receivePaymentRequest(PaymentRequestDto paymentRequestDto) {
        return paymentService.createPaymentForCartService(paymentRequestDto);
    }
}
