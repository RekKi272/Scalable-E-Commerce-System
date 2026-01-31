package com.hmkeyewear.notificationservice.messaging;

import com.hmkeyewear.common_dto.dto.VerifyOtpRequestDto;
import com.hmkeyewear.notificationservice.service.EmailServiceSender;
import com.hmkeyewear.notificationservice.util.RenderBodyOTP;
import com.hmkeyewear.notificationservice.util.RenderForm;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OtpEventConsumer {

    private final EmailServiceSender emailSender;

    /**
     * Listening queue: forgot_pass_queue
     * Message sent from auth-service WHEN USER FORGOT PASSWORD
     */
    @RabbitListener(queues = "${app.rabbitmq.forgot-password.queue}")
    public void process(VerifyOtpRequestDto dto) {

        String body = RenderBodyOTP.render(dto.getOtp());
        String html = RenderForm.wrapBody(body);

        emailSender.sendHtml(
                dto.getEmail(),
                "Xác minh OTP - HMK Eyewear",
                html);
    }
}
