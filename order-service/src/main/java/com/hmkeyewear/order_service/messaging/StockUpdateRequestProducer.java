package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockUpdateRequestProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockUpdateRequestProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.stock-update-request.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public StockUpdateRequestProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Producing queue: stock_update_queue
     * Message sent to product-service WHEN ORDER IS CREATED
     */
    public void sendMessage(List<OrderDetailRequestDto> items) {
        LOGGER.info("Send stock update request: {}", items);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, items);
    }
}
