package com.hmkeyewear.notificationservice.messaging;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.notificationservice.service.EmailServiceSender;
import com.hmkeyewear.notificationservice.util.RenderBodyInvoice;
import com.hmkeyewear.notificationservice.util.RenderForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceEmailConsumer {

    private final EmailServiceSender emailSender;

    @RabbitListener(queues = "${app.rabbitmq.order-mail.queue}")
    public void handle(InvoiceEmailEvent event) {

        log.info(
                "📥 [NOTIFICATION-SERVICE] Received InvoiceEmailEvent | orderId={} | email={}",
                event.getOrderId(),
                event.getEmail());

        String body = RenderBodyInvoice.render(event);
        String html = RenderForm.wrapBody(body);

        emailSender.sendHtml(
                event.getEmail(),
                "Xác nhận đơn hàng - HMK Eyewear",
                html);

        log.info(
                "📧 [NOTIFICATION-SERVICE] Invoice email SENT successfully | orderId={}",
                event.getOrderId());
    }
}
