package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.OrderStatusEventDto;
import com.hmkeyewear.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderUpdateStatusListener {

    private final OrderService orderService;

    @RabbitListener(queues = "${app.rabbitmq.order-status.queue}")
    public void handleOrderStatusUpdate(OrderStatusEventDto event)
            throws Exception {

        orderService.updateOrderStatus(
                event.getOrderId(),
                event.getStatus(),
                "SYSTEM_PAYMENT");
    }
}
