package com.hmkeyewear.product_service.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.product_service.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockUpdateRequestConsumer {

    private final ProductService productService;

    private final ObjectMapper objectMapper;

    public StockUpdateRequestConsumer(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    // Handler stock update request
    @RabbitListener(queues = "${app.rabbitmq.stock-update-request.queue}")
    public void receiveStockUpdateRequest(String message) {
        try {
            List<OrderDetailRequestDto> items = objectMapper.readValue(message, new TypeReference<List<OrderDetailRequestDto>>() {});
            productService.updateStock(items);
        } catch (Exception e) {
            System.err.println("Error updating stock: " + e.getMessage());
        }
    }
}
