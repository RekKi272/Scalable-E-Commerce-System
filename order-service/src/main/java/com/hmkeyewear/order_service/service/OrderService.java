package com.hmkeyewear.order_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.dto.RevenueChartResponseDto;
import com.hmkeyewear.order_service.dto.RevenueYearChartResponseDto;
import com.hmkeyewear.order_service.mapper.OrderMapper;

import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;
import com.hmkeyewear.order_service.messaging.OrderEventProducer;
import com.hmkeyewear.order_service.messaging.StockUpdateRequestProducer;
import com.hmkeyewear.order_service.model.Order;
import com.hmkeyewear.order_service.model.OrderDetail;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
        order.setEmail(orderRequestDto.getEmail());
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

    public String saveOrder(OrderRequestDto orderRequestDto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Create document with auto create ID
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Order order = new Order();
        order.setOrderId(docRef.getId());
        order.setUserId(orderRequestDto.getUserId());
        order.setEmail(orderRequestDto.getEmail());
        order.setSummary(orderRequestDto.getSummary());
        order.setStatus("PENDING");
        if(!Double.isNaN(orderRequestDto.getShipFee())) {
            order.setShipFee(orderRequestDto.getShipFee());
        }
        else {
            order.setShipFee(15000);
        }
        order.setDiscountId(orderRequestDto.getDiscountId());
        order.setCreatedAt(Timestamp.now());
        order.setUpdatedAt(null);
        order.setDetails(orderRequestDto.getDetails());

        ApiFuture<WriteResult> result = docRef.set(order);
        result.get();

        // --- Send message to RabbitMQ ---
        orderEventProducer.sendMessage(order);

        List<OrderDetailRequestDto> items = new ArrayList<>();

        for (OrderDetail detail : order.getDetails()) {
            OrderDetailRequestDto dto = new OrderDetailRequestDto();

            dto.setProductId(detail.getProductId());
            dto.setVariantId(detail.getVariantId());
            dto.setProductName(detail.getProductName());
            dto.setQuantity(detail.getQuantity());
            dto.setUnitPrice(detail.getUnitPrice());

            items.add(dto);
        }

        // --- Send message to product-service for updating product stock---
        stockUpdateRequestProducer.sendMessage(items);

        orderMapper.toOrderResponseDto(order);

        return order.getOrderId();
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
        order.setEmail(orderRequestDto.getEmail());
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

    // UPDATE ORDER STATUS AFTER PAYMENT
    public void updateOrderStatus(OrderPaymentStatusUpdateDto dto)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(dto.getOrderId());

        // Lấy snapshot
        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Order with ID " + dto.getOrderId() + " not found");
        }

        // Convert Firestore document → Order object
        Order order = snapshot.toObject(Order.class);

        assert order != null;
        order.setStatus(dto.getStatus());
        order.setUpdatedAt(Timestamp.now());

        // Cập nhật Firestore
        ApiFuture<WriteResult> result = docRef.set(order);
        result.get();

        // Gửi sự kiện RabbitMQ thông báo order được update
        orderEventProducer.sendMessage(order);
    }

    // Statistic Revenue for Chart
    public RevenueChartResponseDto statisticRevenueChart(
            LocalDate fromDate,
            LocalDate toDate
    ) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        Timestamp from = Timestamp.of(
                Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        Timestamp to = Timestamp.of(
                Date.from(toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        ApiFuture<QuerySnapshot> future = db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", from)
                .whereLessThan("createdAt", to)
                .whereIn("status", List.of("PAID", "SUCCESS", "COMPLETED","DELIVERING"))
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // Init map với 0 cho toàn bộ ngày
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            revenueMap.put(current.toString(), 0.0);
            current = current.plusDays(1);
        }

        // Group + sum
        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);

            LocalDate orderDate = order.getCreatedAt()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String key = orderDate.toString();
            revenueMap.put(
                    key,
                    revenueMap.get(key) + order.getSummary()
            );
        }

        return new RevenueChartResponseDto(revenueMap);
    }

    // Statistic by WEEK
    public RevenueChartResponseDto statisticByWeek(LocalDate anyDayInWeek)
            throws ExecutionException, InterruptedException {

        LocalDate startOfWeek = anyDayInWeek.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return statisticRevenueChart(startOfWeek, endOfWeek);
    }


    // Statistic by MONTH
    public RevenueChartResponseDto statisticByMonth(int year, int month)
            throws ExecutionException, InterruptedException {

        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

        return statisticRevenueChart(fromDate, toDate);
    }

    // Statistic by YEAR
    public RevenueYearChartResponseDto statisticRevenueByYear(int year)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        LocalDate fromDate = LocalDate.of(year, 1, 1);
        LocalDate toDate = LocalDate.of(year, 12, 31);

        Timestamp from = Timestamp.of(
                Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        Timestamp to = Timestamp.of(
                Date.from(toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // Query orders trong năm
        ApiFuture<QuerySnapshot> future = db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", from)
                .whereLessThan("createdAt", to)
                .whereIn("status", List.of("PAID", "SUCCESS", "COMPLETED", "DELIVERING"))
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // Init 12 tháng = 0
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            revenueByMonth.put(String.format("%02d", i), 0.0);
        }

        // Group theo tháng
        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);

            int month = order.getCreatedAt()
                    .toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .getMonthValue();

            String key = String.format("%02d", month);

            revenueByMonth.put(
                    key,
                    revenueByMonth.get(key) + order.getSummary()
            );
        }

        return new RevenueYearChartResponseDto(revenueByMonth);
    }

}
