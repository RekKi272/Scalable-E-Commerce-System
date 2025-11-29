package com.hmkeyewear.order_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.mapper.OrderMapper;

import com.hmkeyewear.order_service.messaging.OrderEventProducer;
import com.hmkeyewear.order_service.messaging.StockUpdateRequestProducer;
import com.hmkeyewear.order_service.model.Order;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import java.util.concurrent.ExecutionException;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;
    private final StockUpdateRequestProducer stockUpdateRequestProducer;

    private static final String COLLECTION_NAME = "orders";

    // Constructor
    public OrderService(OrderMapper orderMapper, OrderEventProducer orderEventProducer, StockUpdateRequestProducer stockUpdateRequestProducer) {
        this.orderMapper = orderMapper;
        this.orderEventProducer = orderEventProducer;
        this.stockUpdateRequestProducer = stockUpdateRequestProducer;
    }

    // CREATE Order
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Create document with auto create ID
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Order order = new Order();
        order.setOrderId(docRef.getId());
        order.setUserId(orderRequestDto.getUserId());
        order.setSummary(orderRequestDto.getSummary());
        order.setStatus(orderRequestDto.getStatus());
        order.setShipFee(orderRequestDto.getShipFee());
        order.setDiscountId(orderRequestDto.getDiscountId());
        order.setCreatedAt(Timestamp.now());
        order.setUpdatedAt(null);
        order.setDetails(orderRequestDto.getDetails());

        ApiFuture<WriteResult> result = docRef.set(order);
        result.get();

        // --- Send message to RabbitMQ ---
        orderEventProducer.sendMessage(order);

        return orderMapper.toOrderResponseDto(order);
    }

    public void saveOrder(OrderRequestDto orderRequestDto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Create document with auto create ID
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Order order = new Order();
        order.setOrderId(docRef.getId());
        order.setUserId(orderRequestDto.getUserId());
        order.setSummary(orderRequestDto.getSummary());
        order.setStatus("PENDING");
        order.setShipFee(orderRequestDto.getShipFee());
        order.setDiscountId(orderRequestDto.getDiscountId());
        order.setCreatedAt(Timestamp.now());
        order.setUpdatedAt(null);
        order.setDetails(orderRequestDto.getDetails());

        ApiFuture<WriteResult> result = docRef.set(order);
        result.get();

        // --- Send message to RabbitMQ ---
        orderEventProducer.sendMessage(order);
        // --- Send message to update product stock---
        stockUpdateRequestProducer.sendMessage(order.getDetails());

        orderMapper.toOrderResponseDto(order);
    }

    // READ Order
    public OrderResponseDto getOrder(String orderId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(orderId);
        ApiFuture<DocumentSnapshot> result = docRef.get();
        DocumentSnapshot document = result.get();
        if (document.exists()) {
            return orderMapper.toOrderResponseDto(document.toObject(Order.class));
        }
        return null;
    }

    // UPDATE order
    public OrderResponseDto updateOrder(String orderId, OrderRequestDto orderRequestDto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(orderId);

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Order with ID " + orderId + " not found");
        }

        Order order = snapshot.toObject(Order.class);

        assert order != null;
        order.setUserId(orderRequestDto.getUserId());
        order.setSummary(orderRequestDto.getSummary());
        order.setStatus(orderRequestDto.getStatus());
        order.setShipFee(orderRequestDto.getShipFee());
        order.setDiscountId(orderRequestDto.getDiscountId());
        order.setDetails(orderRequestDto.getDetails());
        order.setUpdatedAt(Timestamp.now());

        ApiFuture<WriteResult> result = docRef.set(order);
        result.get();

        // --- Send message to RabbitMQ ---
        orderEventProducer.sendMessage(order);

        return orderMapper.toOrderResponseDto(order);
    }

    // DELETE
    public String deleteOrder(String orderId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(orderId).delete();

        // --- Send message to RabbitMQ ---
        orderEventProducer.sendMessage(orderId);

        return "Order " + orderId + " deleted at " + result.get().getUpdateTime();
    }
}
