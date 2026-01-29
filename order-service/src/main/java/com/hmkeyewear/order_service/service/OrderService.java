package com.hmkeyewear.order_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.order_service.constant.PaymentMethod;
import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.dto.RevenueChartWithOrdersResponseDto;
import com.hmkeyewear.order_service.dto.RevenueYearChartWithOrdersResponseDto;
import com.hmkeyewear.order_service.mapper.OrderMapper;

import com.hmkeyewear.common_dto.dto.OrderPaymentStatusUpdateDto;
import com.hmkeyewear.order_service.messaging.OrderEventProducer;
import com.hmkeyewear.order_service.messaging.StockUpdateRequestProducer;
import com.hmkeyewear.order_service.model.Order;
import com.hmkeyewear.order_service.model.OrderDetail;
import com.hmkeyewear.order_service.util.OrderAuditUtil;

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

    private final OrderPriceCalculator priceCalculator;
    private final OrderStatusResolver statusResolver;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String COLLECTION_NAME = "orders";

    // Constructor
    public OrderService(OrderMapper orderMapper,
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

    // CREATE Order
    public OrderResponseDto createOrder(OrderRequestDto dto, String role)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Order order = new Order();
        order.setOrderId(docRef.getId());

        // BASIC INFO
        order.setUserId(dto.getUserId());
        order.setEmail(dto.getEmail());
        order.setFullname(dto.getFullname());
        order.setPhone(dto.getPhone());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setDiscount(dto.getDiscount()); // object
        order.setDetails(dto.getDetails());
        order.setShip(dto.getShip()); // object
        order.setNote(dto.getNote());

        // ---- CALCULATE PRICE ----
        double priceTemp = priceCalculator.calculatePriceTemp(dto.getDetails());

        double priceDecreased = 0;
        if (dto.getDiscount() != null) {
            priceDecreased = priceCalculator.calculatePriceDecreased(priceTemp, dto.getDiscount());
        }

        double shippingFee = 0;
        if (dto.getShip() != null) {
            shippingFee = priceCalculator.calculateShippingFee(dto.getShip());
        }

        double summary = priceCalculator.calculateSummary(priceTemp, priceDecreased, shippingFee);

        order.setPriceTemp(priceTemp);
        order.setPriceDecreased(priceDecreased);
        order.setSummary(summary);

        // ---- SET STATUS ----
        PaymentMethod method = PaymentMethod.valueOf(dto.getPaymentMethod());
        order.setStatus(statusResolver.resolveInitStatus(method));

        // ---- AUDIT ----
        OrderAuditUtil.setCreateAudit(order, dto.getCreatedBy());

        // ---- SAVE ----
        docRef.set(order).get();

        // ---- SEND EVENT ----
        orderEventProducer.sendMessage(order);

        return orderMapper.toOrderResponseDto(order);
    }

    public String saveOrder(OrderRequestDto dto)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Order order = new Order();
        order.setOrderId(docRef.getId());

        order.setUserId(dto.getUserId());
        order.setEmail(dto.getEmail());
        order.setFullname(dto.getFullname());
        order.setPhone(dto.getPhone());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setNote(dto.getNote());
        order.setDiscount(dto.getDiscount()); // object
        order.setDetails(dto.getDetails());
        order.setShip(dto.getShip()); // object

        // ---- CALCULATE PRICE ----
        double priceTemp = priceCalculator.calculatePriceTemp(dto.getDetails());

        double priceDecreased = 0;
        if (dto.getDiscount() != null) {
            priceDecreased = priceCalculator.calculatePriceDecreased(priceTemp, dto.getDiscount());
        }

        double shippingFee = 0;
        if (dto.getShip() != null) {
            shippingFee = priceCalculator.calculateShippingFee(dto.getShip());
        }

        double summary = priceCalculator.calculateSummary(priceTemp, priceDecreased, shippingFee);

        order.setPriceTemp(priceTemp);
        order.setPriceDecreased(priceDecreased);
        order.setSummary(summary);

        // ---- SET STATUS ----
        PaymentMethod method = PaymentMethod.valueOf(dto.getPaymentMethod());
        order.setStatus(statusResolver.resolveInitStatus(method));

        // ---- AUDIT ----
        OrderAuditUtil.setCreateAudit(order, dto.getCreatedBy());

        // ---- SAVE ----
        docRef.set(order).get();

        // ---- SEND EVENT ----
        orderEventProducer.sendMessage(order);

        // ---- PREPARE STOCK MESSAGE ----
        List<OrderDetailRequestDto> items = new ArrayList<>();
        for (OrderDetail detail : order.getDetails()) {
            OrderDetailRequestDto item = new OrderDetailRequestDto();
            item.setProductId(detail.getProductId());
            item.setVariantId(detail.getVariantId());
            item.setProductName(detail.getProductName());
            item.setQuantity(detail.getQuantity());
            item.setUnitPrice(detail.getUnitPrice());
            items.add(item);
        }

        stockUpdateRequestProducer.sendMessage(items);

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

    public List<OrderResponseDto> getOrdersByEmail(String email)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<OrderResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);
            result.add(orderMapper.toOrderResponseDto(order));
        }

        return result;
    }

    public List<OrderResponseDto> getOrdersByPhone(String phone)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("phone", phone)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<OrderResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);
            result.add(orderMapper.toOrderResponseDto(order));
        }

        return result;
    }

    // UPDATE order
    public OrderResponseDto updateOrder(
            String orderId,
            OrderRequestDto dto,
            String updatedBy)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(orderId);

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Order with ID " + orderId + " not found");
        }

        Order order = snapshot.toObject(Order.class);
        if (order == null) {
            throw new RuntimeException("Cannot parse order data");
        }

        // ===== LƯU STATUS CŨ =====
        String oldStatus = order.getStatus();

        // ===== UPDATE DATA =====
        order.setEmail(dto.getEmail());
        order.setFullname(dto.getFullname());
        order.setPhone(dto.getPhone());
        order.setNote(dto.getNote());

        if (dto.getPaymentMethod() != null) {
            order.setPaymentMethod(dto.getPaymentMethod());
        }

        if (dto.getDetails() != null) {
            order.setDetails(dto.getDetails());
        }

        order.setDiscount(dto.getDiscount());
        order.setShip(dto.getShip());

        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
        }

        // ===== AUDIT =====
        OrderAuditUtil.setUpdateAudit(order, updatedBy);

        // ===== SAVE =====
        docRef.set(order).get();

        // ===== EVENT UPDATE ORDER =====
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
        if (order == null) {
            throw new RuntimeException("Cannot parse order data");
        }

        order.setStatus(dto.getStatus());
        order.setUpdatedAt(Timestamp.now());

        // Cập nhật Firestore
        ApiFuture<WriteResult> result = docRef.set(order);
        result.get();

        // Gửi sự kiện RabbitMQ thông báo order được update
        orderEventProducer.sendMessage(order);
    }

    private RevenueChartWithOrdersResponseDto statisticByDateRange(
            LocalDate fromDate,
            LocalDate toDate) throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Timestamp from = Timestamp.of(
                Date.from(fromDate.atStartOfDay(VN_ZONE).toInstant()));
        Timestamp to = Timestamp.of(
                Date.from(toDate.plusDays(1).atStartOfDay(VN_ZONE).toInstant()));

        ApiFuture<QuerySnapshot> future = db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", from)
                .whereLessThan("createdAt", to)
                .whereIn("status", List.of("PAID", "SUCCESS", "COMPLETED", "DELIVERING"))
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        Map<String, Double> revenueMap = new LinkedHashMap<>();
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            revenueMap.put(current.toString(), 0.0);
            current = current.plusDays(1);
        }

        List<OrderResponseDto> orders = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);
            if (order == null || order.getCreatedAt() == null)
                continue;

            LocalDate orderDate = order.getCreatedAt()
                    .toDate()
                    .toInstant()
                    .atZone(VN_ZONE)
                    .toLocalDate();

            String key = orderDate.toString();
            revenueMap.put(key, revenueMap.get(key) + order.getSummary());

            orders.add(orderMapper.toOrderResponseDto(order));
        }

        return new RevenueChartWithOrdersResponseDto(revenueMap, orders);
    }

    public RevenueChartWithOrdersResponseDto statisticByWeek(LocalDate anyDay)
            throws ExecutionException, InterruptedException {

        LocalDate start = anyDay.with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);

        return statisticByDateRange(start, end);
    }

    public RevenueChartWithOrdersResponseDto statisticByMonth(int year, int month)
            throws ExecutionException, InterruptedException {

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        return statisticByDateRange(from, to);
    }

    public RevenueYearChartWithOrdersResponseDto statisticByYear(int year)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        LocalDate fromDate = LocalDate.of(year, 1, 1);
        LocalDate toDate = LocalDate.of(year, 12, 31);

        Timestamp from = Timestamp.of(
                Date.from(fromDate.atStartOfDay(VN_ZONE).toInstant()));
        Timestamp to = Timestamp.of(
                Date.from(toDate.plusDays(1).atStartOfDay(VN_ZONE).toInstant()));

        ApiFuture<QuerySnapshot> future = db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", from)
                .whereLessThan("createdAt", to)
                .whereIn("status", List.of("PAID", "SUCCESS", "COMPLETED", "DELIVERING"))
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            revenueByMonth.put(String.format("%02d", i), 0.0);
        }

        List<OrderResponseDto> orders = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            Order order = doc.toObject(Order.class);
            if (order == null || order.getCreatedAt() == null)
                continue;

            int month = order.getCreatedAt()
                    .toDate()
                    .toInstant()
                    .atZone(VN_ZONE)
                    .getMonthValue();

            String key = String.format("%02d", month);
            revenueByMonth.put(key, revenueByMonth.get(key) + order.getSummary());

            orders.add(orderMapper.toOrderResponseDto(order));
        }

        return new RevenueYearChartWithOrdersResponseDto(revenueByMonth, orders);
    }

}
