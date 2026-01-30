package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEmailProducer {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.order-mail.routing-key}")
    private String orderMailRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public InvoiceEmailProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Producer queue: order_mail_queue
     * Message sent TO notification-service AFTER Payment DONE (SUCCESS)
     */
    public void sendEmailRequest(InvoiceEmailEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, orderMailRoutingKey, event);
    }
}
