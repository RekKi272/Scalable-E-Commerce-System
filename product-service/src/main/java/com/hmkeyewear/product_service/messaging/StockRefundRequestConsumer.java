package com.hmkeyewear.product_service.messaging;

import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.product_service.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockRefundRequestConsumer {
    private final ProductService productService;

    public StockRefundRequestConsumer(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Listening queue: stock_refund_queue
     * Message sent from order-service AFTER order was CANCELED OR REFUND
     */

    @RabbitListener(queues = "${app.rabbitmq.stock-refund-request.queue}")
    public void receiveStockRefundRequest(List<OrderDetailRequestDto> orderDetailRequestDtoList) {
        productService.refundStock(orderDetailRequestDtoList);
    }
}
