package com.hmkeyewear.cart_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.Timestamp;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OrderCheckoutRequestEventProducer {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.order-checkout.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OrderCheckoutRequestEventProducer(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public OrderResponseDto convertSendAndReceive(
            OrderRequestDto orderRequest,
            String userId) {

        try {
            // ===== GỘP DATA =====
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("order", orderRequest);

            // ===== TO JSON =====
            String json = objectMapper.writeValueAsString(payload);
            log.info("Send CREATE_ORDER payload: {}", json);

            // ===== SEND & RECEIVE =====
            Object response = rabbitTemplate.convertSendAndReceive(
                    exchangeName,
                    routingKey,
                    json);

            if (response == null) {
                throw new RuntimeException("Order-service did not return response");
            }

            // ===== MAP RESPONSE =====
            return objectMapper.readValue(
                    response.toString(),
                    OrderResponseDto.class);

        } catch (JsonProcessingException e) {
            log.error("Failed to process create order message", e);
            throw new RuntimeException(e);
        }
    }
}
