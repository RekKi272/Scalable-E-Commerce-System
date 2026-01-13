package com.hmkeyewear.product_service.messaging;

import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.product_service.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StockUpdateRequestConsumer {

    private final ProductService productService;


    // Handler stock update request from ORDER-SERVICE
    @RabbitListener(queues = "${app.rabbitmq.stock-update-request.queue}")
    public void receiveStockUpdateRequest(List<OrderDetailRequestDto> orderDetailRequestDtoList) {
        try {
            productService.updateStock(orderDetailRequestDtoList);
        } catch (Exception e) {
            System.err.println("Error updating stock: " + e.getMessage());
        }
    }
}
