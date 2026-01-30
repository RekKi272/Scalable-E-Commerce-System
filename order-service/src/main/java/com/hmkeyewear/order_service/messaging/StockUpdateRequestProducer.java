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

    @Value("${app.rabbitmq.stock-update-increase.routing-key}")
    private String increaseRoutingKey;

    @Value("${app.rabbitmq.stock-update-decrease.routing-key}")
    private String decreaseRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public StockUpdateRequestProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Khi tạo đơn / bán thành công
    public void sendIncreaseQuantitySell(List<OrderDetailRequestDto> items) {
        LOGGER.info("Send INCREASE quantitySell: {}", items);
        rabbitTemplate.convertAndSend(exchangeName, increaseRoutingKey, items);
    }

    // Khi huỷ đơn / payment fail / khách không nhận
    public void sendDecreaseQuantitySell(List<OrderDetailRequestDto> items) {
        LOGGER.info("Send DECREASE quantitySell: {}", items);
        rabbitTemplate.convertAndSend(exchangeName, decreaseRoutingKey, items);
    }
}
