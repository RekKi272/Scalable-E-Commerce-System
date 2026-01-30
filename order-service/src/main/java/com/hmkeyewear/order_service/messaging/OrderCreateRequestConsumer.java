package com.hmkeyewear.order_service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import com.hmkeyewear.order_service.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderCreateRequestConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderCreateRequestConsumer(
            OrderService orderService,
            ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    /**
     * RPC listener – nhận request tạo đơn từ cart-service
     */
    @RabbitListener(queues = "${app.rabbitmq.order-checkout.queue}")
    public String consumeCreateOrder(String message) throws Exception {

        // ===== PARSE JSON =====
        Map<String, Object> payload = objectMapper.readValue(message, Map.class);

        String userId = (String) payload.get("userId");
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("Missing userId in payload");
        }

        OrderRequestDto orderRequest = objectMapper.convertValue(
                payload.get("order"),
                OrderRequestDto.class);

        // ===== CREATE ORDER =====
        OrderResponseDto order = orderService.createOrder(orderRequest, userId);

        // ===== RETURN JSON STRING (RPC) =====
        return objectMapper.writeValueAsString(order);
    }
}
