package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockRefundRequestProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockRefundRequestProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.stock-refund-request.routing-key}")
    private String stockRefundRequestRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public StockRefundRequestProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Producing queue: stock_refund_queue
     * Message sent to product-service WHEN ORDER WAS CANCELED OR REFUND
     */
    public void sendStockRefundRequest(List<OrderDetailRequestDto> orderDetailRequestDtoList) {
        LOGGER.info("Sending stock refund request to product-service: -> {}", orderDetailRequestDtoList.toString());
        rabbitTemplate.convertAndSend(exchangeName, stockRefundRequestRoutingKey, orderDetailRequestDtoList);
    }
}
