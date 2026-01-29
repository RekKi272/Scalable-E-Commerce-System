package com.hmkeyewear.order_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;

import com.hmkeyewear.order_service.constant.PaymentMethod;
import com.hmkeyewear.order_service.mapper.OrderMapper;

import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;
import com.hmkeyewear.order_service.messaging.OrderEventProducer;
import com.hmkeyewear.order_service.messaging.StockUpdateRequestProducer;
import com.hmkeyewear.order_service.model.Order;
import com.hmkeyewear.order_service.util.OrderAuditUtil;

import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;
    private final StockUpdateRequestProducer stockUpdateRequestProducer;

    private final OrderPriceCalculator priceCalculator;
    private final OrderStatusResolver statusResolver;

    private static final String COLLECTION_NAME = "orders";

    public OrderService(
            OrderMapper orderMapper,
            OrderEventProducer orderEventProducer,
            StockUpdateRequestProducer stockUpdateRequestProducer,
            OrderPriceCalculator priceCalculator,
            OrderStatusResolver statusResolver) {

        this.orderMapper = orderMapper;
        this.orderEventProducer = orderEventProducer;
        this.stockUpdateRequestProducer = stockUpdateRequestProducer;
        this.priceCalculator = priceCalculator;
        this.statusResolver = statusResolver;
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
        order.setFullname(dto.getFullname());
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
        stockUpdateRequestProducer.sendMessage(order.getDetails());

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
    public void updateOrderStatus(OrderPaymentStatusUpdateDto dto)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(dto.getOrderId());

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Order with ID " + dto.getOrderId() + " not found");
        }

        Order order = snapshot.toObject(Order.class);
        if (order == null) {
            throw new RuntimeException("Cannot parse order data");
        }

        order.setStatus(dto.getStatus());
        order.setUpdatedAt(Timestamp.now());

        docRef.set(order).get();

        orderEventProducer.sendMessage(order);
    }

    // public String saveOrder(OrderRequestDto dto)
    // throws ExecutionException, InterruptedException {

    // Firestore db = FirestoreClient.getFirestore();
    // DocumentReference docRef = db.collection(COLLECTION_NAME).document();

    // Order order = new Order();
    // order.setOrderId(docRef.getId());

    // order.setUserId(dto.getUserId());
    // order.setEmail(dto.getEmail());
    // order.setFullname(dto.getFullname());
    // order.setPhone(dto.getPhone());
    // order.setPaymentMethod(dto.getPaymentMethod());
    // order.setNote(dto.getNote());
    // order.setDiscount(dto.getDiscount()); // object
    // order.setDetails(dto.getDetails());
    // order.setShip(dto.getShip()); // object

    // // ---- CALCULATE PRICE ----
    // double priceTemp = priceCalculator.calculatePriceTemp(dto.getDetails());

    // double priceDecreased = 0;
    // if (dto.getDiscount() != null) {
    // priceDecreased = priceCalculator.calculatePriceDecreased(priceTemp,
    // dto.getDiscount());
    // }

    // double shippingFee = 0;
    // if (dto.getShip() != null) {
    // shippingFee = priceCalculator.calculateShippingFee(dto.getShip());
    // }

    // double summary = priceCalculator.calculateSummary(priceTemp, priceDecreased,
    // shippingFee);

    // order.setPriceTemp(priceTemp);
    // order.setPriceDecreased(priceDecreased);
    // order.setSummary(summary);

    // // ---- SET STATUS ----
    // PaymentMethod method = PaymentMethod.valueOf(dto.getPaymentMethod());
    // order.setStatus(statusResolver.resolveInitStatus(method));

    // // ---- AUDIT ----
    // OrderAuditUtil.setCreateAudit(order, dto.getCreatedBy());

    // // ---- SAVE ----
    // docRef.set(order).get();

    // // ---- SEND EVENT ----
    // orderEventProducer.sendMessage(order);

    // // ---- PREPARE STOCK MESSAGE ----
    // List<OrderDetailRequestDto> items = new ArrayList<>();
    // for (OrderDetail detail : order.getDetails()) {
    // OrderDetailRequestDto item = new OrderDetailRequestDto();
    // item.setProductId(detail.getProductId());
    // item.setVariantId(detail.getVariantId());
    // item.setProductName(detail.getProductName());
    // item.setQuantity(detail.getQuantity());
    // item.setUnitPrice(detail.getUnitPrice());
    // items.add(item);
    // }

    // stockUpdateRequestProducer.sendMessage(items);

    // return order.getOrderId();
    // }
}
