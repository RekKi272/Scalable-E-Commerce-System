package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.order_service.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;

import java.util.concurrent.ExecutionException;

@Service
public class OrderStatusUpdateConsumer {

    private final OrderService orderService;

    public OrderStatusUpdateConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.order-status.queue}")
    public void orderStatusUpdateReceive(OrderPaymentStatusUpdateDto orderPaymentStatusUpdateDto) throws ExecutionException, InterruptedException {
        orderService.updateOrderStatus(orderPaymentStatusUpdateDto);
    }
}
