package com.hmkeyewear.order_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;

import com.hmkeyewear.order_service.constant.PaymentMethod;
import com.hmkeyewear.order_service.mapper.InvoiceEmailMapper;
import com.hmkeyewear.order_service.mapper.OrderMapper;
import com.hmkeyewear.order_service.constant.OrderStatus;
import com.hmkeyewear.order_service.messaging.OrderEventProducer;
import com.hmkeyewear.order_service.messaging.StockUpdateRequestProducer;
import com.hmkeyewear.order_service.messaging.InvoiceEmailProducer;
import com.hmkeyewear.order_service.model.Order;
import com.hmkeyewear.order_service.util.OrderAuditUtil;
import com.hmkeyewear.order_service.util.OrderStatusValidator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;
    private final StockUpdateRequestProducer stockUpdateRequestProducer;
    private final InvoiceEmailProducer invoiceEmailProducer;
    private final InvoiceEmailMapper invoiceEmailMapper;

    private final OrderPriceCalculator priceCalculator;
    private final OrderStatusResolver statusResolver;

    private static final String COLLECTION_NAME = "orders";

    public OrderService(
            OrderMapper orderMapper,
            OrderEventProducer orderEventProducer,
            StockUpdateRequestProducer stockUpdateRequestProducer,
            InvoiceEmailProducer invoiceEmailProducer,
            InvoiceEmailMapper invoiceEmailMapper,
            OrderPriceCalculator priceCalculator,
            OrderStatusResolver statusResolver) {

        this.orderMapper = orderMapper;
        this.orderEventProducer = orderEventProducer;
        this.stockUpdateRequestProducer = stockUpdateRequestProducer;
        this.priceCalculator = priceCalculator;
        this.statusResolver = statusResolver;
        this.invoiceEmailProducer = invoiceEmailProducer;
        this.invoiceEmailMapper = invoiceEmailMapper;
    }

    // ===== CREATE ORDER =====
    public OrderResponseDto createOrder(OrderRequestDto dto, String userId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Order order = new Order();

        // ===== BASIC =====
        order.setOrderId(docRef.getId());
        order.setUserId(userId);
        order.setEmail(dto.getEmail());
        order.setFullName(dto.getFullName());
        order.setPhone(dto.getPhone());

        order.setPaymentMethod(dto.getPaymentMethod());
        order.setNote(dto.getNote());

        // ===== DATA =====
        order.setDetails(dto.getDetails());
        order.setDiscount(dto.getDiscount());
        order.setShip(dto.getShip());

        // ===== PRICE =====
        double priceTemp = priceCalculator.calculatePriceTemp(dto.getDetails());

        double priceDecreased = dto.getDiscount() != null
                ? priceCalculator.calculatePriceDecreased(priceTemp, dto.getDiscount())
                : 0;

        double shippingFee = dto.getShip() != null
                ? priceCalculator.calculateShippingFee(dto.getShip())
                : 0;

        double summary = priceCalculator.calculateSummary(
                priceTemp,
                priceDecreased,
                shippingFee);

        order.setPriceTemp(priceTemp);
        order.setPriceDecreased(priceDecreased);
        order.setSummary(summary);

        // ===== STATUS =====
        PaymentMethod method = PaymentMethod.valueOf(dto.getPaymentMethod());
        order.setStatus(statusResolver.resolveInitStatus(method));

        // ===== AUDIT =====
        OrderAuditUtil.setCreateAudit(order, userId);

        // ===== SAVE =====
        docRef.set(order).get();

        // ===== TRỪ KHO =====
        stockUpdateRequestProducer.sendIncreaseQuantitySell(order.getDetails());

        // ===== EVENT =====
        orderEventProducer.sendMessage(order);

        // ===== RETURN RESPONSE =====
        return orderMapper.toOrderResponseDto(order);
    }

    // READ Order
    public OrderResponseDto getOrder(String orderId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(orderId);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            return orderMapper.toOrderResponseDto(document.toObject(Order.class));
        }
        return null;
    }

    public List<OrderResponseDto> getOrdersByEmail(String email)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        List<OrderResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            result.add(orderMapper.toOrderResponseDto(doc.toObject(Order.class)));
        }
        return result;
    }

    public List<OrderResponseDto> getOrdersByPhone(String phone)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("phone", phone)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        List<OrderResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            result.add(orderMapper.toOrderResponseDto(doc.toObject(Order.class)));
        }
        return result;
    }

    public List<OrderResponseDto> getOrdersByDateRange(
            LocalDate fromDate,
            LocalDate toDate)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Timestamp from = Timestamp.of(
                Date.from(fromDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()));
        Timestamp to = Timestamp.of(
                Date.from(toDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()));

        ApiFuture<QuerySnapshot> future = db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", from)
                .whereLessThan("createdAt", to)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        List<OrderResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            result.add(orderMapper.toOrderResponseDto(doc.toObject(Order.class)));
        }
        return result;
    }

    // DELETE
    public String deleteOrder(String orderId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(orderId).delete();

        // --- Send message to RabbitMQ ---
        orderEventProducer.sendMessage(orderId);

        return "Order " + orderId + " deleted at " + result.get().getUpdateTime();
    }

    // UPDATE ORDER STATUS AFTER PAYMENT
    public OrderResponseDto updateOrderStatus(
            String orderId,
            String newStatus,
            String userId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(orderId);

        Order order = docRef.get().get().toObject(Order.class);

        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        OrderStatus currentStatus;
        OrderStatus nextStatus;

        try {
            currentStatus = OrderStatus.valueOf(order.getStatus());
            nextStatus = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }

        // ===== VALIDATE FLOW =====
        OrderStatusValidator.validateStatusTransition(currentStatus, nextStatus);

        // ===== SIDE EFFECT THEO TRẠNG THÁI =====
        switch (nextStatus) {

            case PAID:
            case DELIVERING:
                order.setStatus(nextStatus.name());
                break;

            case COMPLETED:
                order.setStatus(nextStatus.name());

                InvoiceEmailEvent event = invoiceEmailMapper.toEvent(order);
                invoiceEmailProducer.sendEmailRequest(event);

                break;

            case CANCEL:
                stockUpdateRequestProducer
                        .sendDecreaseQuantitySell(order.getDetails());

                order.setStatus(nextStatus.name());
                break;

            default:
                // FAILED đã bị chặn trong validator
                throw new RuntimeException("Không hỗ trợ trạng thái này");
        }

        // ===== AUDIT =====
        OrderAuditUtil.setUpdateAudit(order, userId);

        // ===== SAVE =====
        docRef.set(order).get();

        return orderMapper.toOrderResponseDto(order);
    }

}
