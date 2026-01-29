// package com.hmkeyewear.order_service.messaging;

// import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
// import com.hmkeyewear.order_service.dto.OrderResponseDto;
// import com.hmkeyewear.order_service.service.OrderService;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.stereotype.Service;
// import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;

// import java.util.List;
// import java.util.concurrent.ExecutionException;

// @Service
// public class OrderStatusUpdateConsumer {

// private final OrderService orderService;
// private final InvoiceEmailProducer invoiceEmailProducer;

// public OrderStatusUpdateConsumer(OrderService orderService,
// InvoiceEmailProducer invoiceEmailProducer) {
// this.orderService = orderService;
// this.invoiceEmailProducer = invoiceEmailProducer;
// }

// /**
// * Listening queue: order_status_update_queue
// * Message sent from payment-service AFTER Payment DONE (CAN BE FAILED,
// * CANCELLED, SUCCEED, etc)
// */
// @RabbitListener(queues = "${app.rabbitmq.order-status.queue}")
// public void orderStatusUpdateReceive(OrderPaymentStatusUpdateDto dto)
// throws ExecutionException, InterruptedException {

// // 1. UPDATE ORDER STATUS TRƯỚC
// orderService.updateOrderStatus(dto);

// // 2. CHỈ XỬ LÝ GỬI MAIL KHI PAID
// if (!"PAID".equals(dto.getStatus())) {
// return;
// }

// // 3. LẤY ORDER SAU KHI ĐÃ UPDATE
// OrderResponseDto orderResponseDto = orderService.getOrder(dto.getOrderId());
// if (orderResponseDto == null || orderResponseDto.getEmail() == null) {
// return;
// }

// // 4. MAP ORDER DETAIL
// List<InvoiceEmailEvent.OrderDetail> details = orderResponseDto.getDetails()
// == null
// ? List.of()
// : orderResponseDto.getDetails().stream()
// .map(d -> new InvoiceEmailEvent.OrderDetail(
// d.getProductId(),
// d.getVariantId(),
// d.getProductName(),
// d.getQuantity(),
// d.getUnitPrice(),
// d.getTotalPrice()))
// .toList();

// // 5. MAP SHIP
// InvoiceEmailEvent.ShipInfo ship = null;
// if (orderResponseDto.getShip() != null) {
// ship = new InvoiceEmailEvent.ShipInfo(
// orderResponseDto.getShip().getAddressProvince(),
// orderResponseDto.getShip().getAddressWard(),
// orderResponseDto.getShip().getAddressDetail(),
// orderResponseDto.getShip().getShippingFee());
// }

// // 6. MAP DISCOUNT
// InvoiceEmailEvent.DiscountDetail discount = null;
// if (orderResponseDto.getDiscount() != null) {
// discount = new InvoiceEmailEvent.DiscountDetail(
// orderResponseDto.getDiscount().getDiscountId(),
// orderResponseDto.getDiscount().getValueType(),
// orderResponseDto.getDiscount().getValueDiscount());
// }

// // 7. SEND EMAIL EVENT
// InvoiceEmailEvent event = new InvoiceEmailEvent(
// orderResponseDto.getOrderId(),
// orderResponseDto.getUserId(),
// orderResponseDto.getEmail(),
// orderResponseDto.getFullname(),
// orderResponseDto.getPhone(),
// orderResponseDto.getPaymentMethod(),
// orderResponseDto.getStatus(),
// orderResponseDto.getPriceTemp(),
// orderResponseDto.getPriceDecreased(),
// orderResponseDto.getSummary(),
// details,
// ship,
// discount,
// orderResponseDto.getNote());

// invoiceEmailProducer.sendEmailRequest(event);
// }
// }
