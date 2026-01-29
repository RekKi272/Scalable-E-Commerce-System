package com.hmkeyewear.notificationservice.messaging;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.notificationservice.service.EmailServiceSender;
import com.hmkeyewear.notificationservice.util.RenderBodyInvoice;
import com.hmkeyewear.notificationservice.util.RenderForm;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceEmailConsumer {

    private final EmailServiceSender emailSender;

    @RabbitListener(queues = "${app.rabbitmq.order-mail.queue}")
    public void handle(InvoiceEmailEvent event) {

        String body = RenderBodyInvoice.render(event);
        String html = RenderForm.wrapBody(body);

        emailSender.sendHtml(
                event.getEmail(),
                "Xác nhận đơn hàng - HMK Eyewear",
                html);
    }
}
