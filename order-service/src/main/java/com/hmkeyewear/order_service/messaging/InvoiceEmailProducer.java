package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
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

    public void sendEmailRequest(InvoiceEmailEvent event) {
        log.info(
                "📤 [ORDER-SERVICE] Sending InvoiceEmailEvent | orderId={} | email={}",
                event.getOrderId(),
                event.getEmail());

        rabbitTemplate.convertAndSend(exchangeName, orderMailRoutingKey, event);

        log.info(
                "✅ [ORDER-SERVICE] InvoiceEmailEvent SENT to exchange={} routingKey={}",
                exchangeName,
                orderMailRoutingKey);
    }
}
