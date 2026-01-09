package com.hmkeyewear.auth_service.messaging;

import com.hmkeyewear.common_dto.dto.VerifyOtpRequestDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class OtpEventProducer {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.forgot-password.routing_key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public OtpEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOtp(String email, String otp) {
        VerifyOtpRequestDto verifyOtpRequestDto = new VerifyOtpRequestDto(email, otp);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, verifyOtpRequestDto);
    }


}
