package com.hmkeyewear.order_service.messaging;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.model.OrderDetail;
import com.hmkeyewear.order_service.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class OrderStatusUpdateConsumer {

    private final OrderService orderService;
    private final InvoiceEmailProducer invoiceEmailProducer;
    private final StockRefundRequestProducer stockRefundRequestProducer;

    public OrderStatusUpdateConsumer(OrderService orderService, InvoiceEmailProducer invoiceEmailProducer, StockRefundRequestProducer stockRefundRequestProducer) {
        this.orderService = orderService;
        this.invoiceEmailProducer = invoiceEmailProducer;
        this.stockRefundRequestProducer = stockRefundRequestProducer;
    }

    /**
     * Listening queue: order_status_update_queue
     * Message sent from payment-service AFTER Payment DONE (CAN BE FAILED, CANCELLED, SUCCEED, etc)
     */
    @RabbitListener(queues = "${app.rabbitmq.order-status.queue}")
    public void orderStatusUpdateReceive(OrderPaymentStatusUpdateDto orderPaymentStatusUpdateDto) throws ExecutionException, InterruptedException {
        OrderResponseDto orderResponseDto = orderService.getOrder(orderPaymentStatusUpdateDto.getOrderId());
        if (orderPaymentStatusUpdateDto.getStatus().equals("PAID")) {
            if(orderResponseDto.getEmail() != null) {
                InvoiceEmailEvent invoiceEmailEvent = new InvoiceEmailEvent(orderResponseDto.getOrderId(), orderResponseDto.getEmail(), orderResponseDto.getSummary(), null);
                invoiceEmailProducer.sendEmailRequest(invoiceEmailEvent);
            }

        }
        // IF ORDER WAS CANCELED OR REFUND
        else {
            List<OrderDetailRequestDto> orderDetailRequestDtos = new ArrayList<>();

            for (OrderDetail orderDetail : orderResponseDto.getDetails()) {
                OrderDetailRequestDto orderDetailRequestDto = new OrderDetailRequestDto();
                orderDetailRequestDto.setProductId(orderDetail.getProductId());
                orderDetailRequestDto.setVariantId(orderDetail.getVariantId());
                orderDetailRequestDto.setProductName(orderDetail.getProductName());
                orderDetailRequestDto.setUnitPrice(orderDetail.getUnitPrice());
                orderDetailRequestDto.setQuantity(orderDetail.getQuantity());
                // Add to list
                orderDetailRequestDtos.add(orderDetailRequestDto);
            }

            stockRefundRequestProducer.sendStockRefundRequest(orderDetailRequestDtos);
        }
        orderService.updateOrderStatus(orderPaymentStatusUpdateDto);
    }
}
