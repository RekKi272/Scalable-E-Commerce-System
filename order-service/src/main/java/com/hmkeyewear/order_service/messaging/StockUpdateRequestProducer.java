package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.order_service.mapper.OrderMapper;
import com.hmkeyewear.order_service.model.OrderDetail;
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
    private String orderRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    private final OrderMapper orderMapper;

    public StockUpdateRequestProducer(RabbitTemplate rabbitTemplate, OrderMapper orderMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderMapper = orderMapper;
    }

    public void sendMessage(List<OrderDetailRequestDto> orderDetailList) {
        LOGGER.info("Message sent -> {}", orderDetailList.toString());
        rabbitTemplate.convertAndSend(exchangeName, orderRoutingKey, orderDetailList);
    }
}
