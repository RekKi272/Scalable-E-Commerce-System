package com.hmkeyewear.payment_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusUpdateProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusUpdateProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.order-status.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public OrderStatusUpdateProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * PRODUCER queue: order_status_update_queue
     * Message sent TO notification-service WHEN PAYMENT DONE (FAILED, PAID, etc..)
     */
    public void sendUpdateStatusRequest(OrderPaymentStatusUpdateDto orderStatus) {
        LOGGER.info("Sending update status {}at Order {}", orderStatus.getOrderId(), orderStatus.getStatus());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, orderStatus);
    }

}
