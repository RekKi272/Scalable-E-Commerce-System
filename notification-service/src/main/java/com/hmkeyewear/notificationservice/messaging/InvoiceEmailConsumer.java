package com.hmkeyewear.notificationservice.messaging;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.notificationservice.service.EmailServiceSender;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceEmailConsumer {

    private final EmailServiceSender emailSender;

    @RabbitListener(queues = "${app.rabbitmq.order-mail.queue}")
    public void handle(InvoiceEmailEvent event) {
        String html = """
            <h3>Hóa đơn thanh toán</h3>
            <p>Mã đơn: %s</p>
            <p>Số tiền: %s</p>
            <a href="%s">Tải hóa đơn</a>
        """.formatted(
                event.getOrderId(),
                event.getTotalAmount(),
                event.getInvoiceUrl()
        );

        emailSender.sendHtml(
                event.getEmail(),
                "Hóa đơn thanh toán",
                html
        );
    }
}
