package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;

import java.util.concurrent.ExecutionException;

@Service
public class OrderStatusUpdateConsumer {

    private final OrderService orderService;
    private final InvoiceEmailProducer invoiceEmailProducer;

    public OrderStatusUpdateConsumer(OrderService orderService, InvoiceEmailProducer invoiceEmailProducer) {
        this.orderService = orderService;
        this.invoiceEmailProducer = invoiceEmailProducer;
    }

    /**
     * Listening queue: order_status_update_queue
     * Message sent from payment-service AFTER Payment DONE (CAN BE FAILED, CANCELLED, SUCCEED, etc)
     */
    @RabbitListener(queues = "${app.rabbitmq.order-status.queue}")
    public void orderStatusUpdateReceive(OrderPaymentStatusUpdateDto orderPaymentStatusUpdateDto) throws ExecutionException, InterruptedException {
        if (orderPaymentStatusUpdateDto.getStatus().equals("DELIVERING")) {
            OrderResponseDto orderResponseDto = orderService.getOrder(orderPaymentStatusUpdateDto.getOrderId());
            InvoiceEmailEvent invoiceEmailEvent = new InvoiceEmailEvent(orderResponseDto.getOrderId(), orderResponseDto.getEmail(), orderResponseDto.getSummary(), null);
            invoiceEmailProducer.sendEmailRequest(invoiceEmailEvent);
        }

        orderService.updateOrderStatus(orderPaymentStatusUpdateDto);
    }
}
