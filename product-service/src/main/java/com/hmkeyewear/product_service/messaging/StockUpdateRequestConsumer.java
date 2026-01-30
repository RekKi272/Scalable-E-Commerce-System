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

    // Khi tạo đơn / bán thành công -> CỘNG quantitySell
    @RabbitListener(queues = "${app.rabbitmq.stock-update-increase.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void receiveIncreaseQuantitySellRequest(
            List<OrderDetailRequestDto> orderDetailRequestDtoList) {
        productService.increaseQuantitySell(orderDetailRequestDtoList);
    }

    // Khi huỷ đơn / thanh toán thất bại / khách không nhận -> TRỪ quantitySell
    @RabbitListener(queues = "${app.rabbitmq.stock-update-decrease.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void receiveDecreaseQuantitySellRequest(
            List<OrderDetailRequestDto> orderDetailRequestDtoList) {
        productService.decreaseQuantitySell(orderDetailRequestDtoList);
    }
}
