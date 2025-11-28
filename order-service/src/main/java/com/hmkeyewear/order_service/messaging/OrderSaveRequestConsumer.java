package com.hmkeyewear.order_service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderSaveRequestDto;
import com.hmkeyewear.order_service.mapper.OrderMapper;
import com.hmkeyewear.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSaveRequestConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaveRequestConsumer.class);

    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderSaveRequestConsumer(ObjectMapper objectMapper, OrderService orderService, OrderMapper orderMapper) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    /**
     * Lắng nghe queue: order-save-request
     * Message được gửi từ payment-service sau khi thanh toán thành công
     */
    @RabbitListener(queues = "${app.rabbitmq.order-save-listener.queue}")
    public void consumeOrderSaveRequest(String message) {
        try {
            LOGGER.info("Received Order Save Request Message: {}", message);

            // Convert JSON → DTO
            OrderSaveRequestDto saveDto = objectMapper.readValue(message, OrderSaveRequestDto.class);

            OrderRequestDto orderRequestDto = orderMapper.toOrderRequestDto(saveDto);

            // Gọi service để lưu đơn hàng
            orderService.saveOrder(orderRequestDto);

            LOGGER.info("Order saved successfully for user: {}", orderRequestDto.getUserId());

        } catch (Exception e) {
            LOGGER.error("Failed to process Order Save Request message", e);
        }
    }
}
