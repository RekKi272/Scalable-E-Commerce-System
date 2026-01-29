package com.hmkeyewear.cart_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.common_dto.dto.OrderSaveRequestDto;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderSaveRequestProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaveRequestProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.order-save.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    public OrderSaveRequestProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Producing queue: order_save_request_queue
     * Message sent to order-service WHEN USER click CHECKOUT for ONLINE PAYMENT to save temp order
     */
    public String sendSaveRequest(OrderSaveRequestDto orderSaveRequestDto) {
        try {
            // Chuyển DTO -> JSON String
            String json = objectMapper.writeValueAsString(orderSaveRequestDto);
            LOGGER.info("Sending order save request JSON: {}", json);

            // Gửi JSON string
            Object response = rabbitTemplate.convertSendAndReceive(exchangeName, routingKey, orderSaveRequestDto);
            return response != null ? response.toString() : null;

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to convert OrderSaveRequestDto to JSON", e);
            throw new RuntimeException(e);
        }
    }
}
