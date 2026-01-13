package com.hmkeyewear.notificationservice.messaging;

import com.hmkeyewear.common_dto.dto.VerifyOtpRequestDto;
import com.hmkeyewear.notificationservice.service.EmailServiceSender;
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
    public void process(VerifyOtpRequestDto verifyOtpRequestDto) {
        String html = """
            <h3>Mã OTP Quên mật khẩu</h3>
            <p>Mã Otp: %s</p>
        """.formatted(
                verifyOtpRequestDto.getOtp()
        );

        emailSender.sendHtml(verifyOtpRequestDto.getEmail(), "Forgot Password OTP", html);
    }
}
