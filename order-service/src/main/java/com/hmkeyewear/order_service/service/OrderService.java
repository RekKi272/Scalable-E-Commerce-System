package com.hmkeyewear.order_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.PageResponseDto;
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
import java.time.format.DateTimeFormatter;
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
        private static final String COUNTER_COLLECTION = "counters";
        private static final String ORDER_COUNTER_DOC = "orderCounter";

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

        public List<String> getStatusOrderOption() {
                return Arrays.stream(OrderStatus.values())
                                .map(Enum::name)
                                .toList();
        }

        // ===== CREATE ORDER ID =====
        private String generateOrderCode(Firestore db)
                        throws ExecutionException, InterruptedException {

                // ===== FORMAT DATE: yymmdd =====
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                String dateKey = today.format(DateTimeFormatter.ofPattern("yyMMdd"));

                DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(ORDER_COUNTER_DOC);

                ApiFuture<String> future = db.runTransaction(transaction -> {

                        DocumentSnapshot snapshot = transaction.get(counterRef).get();

                        Long lastSeqObj = (snapshot.exists() && snapshot.contains(dateKey))
                                        ? snapshot.getLong(dateKey)
                                        : null;

                        long lastSeq = (lastSeqObj != null) ? lastSeqObj : -1;
                        long newSeq = lastSeq + 1;

                        // bảo vệ tràn
                        if (newSeq > 999) {
                                throw new RuntimeException("Lỗi tràn thứ tự đơn hàng theo ngày " + dateKey);
                        }

                        // lưu lại counter theo ngày
                        transaction.set(
                                        counterRef,
                                        Map.of(dateKey, newSeq),
                                        SetOptions.merge());

                        String formattedSeq = String.format("%03d", newSeq);
                        return dateKey + formattedSeq; // yymmddXXX
                });

                return future.get();
        }

        // ===== CREATE ORDER =====
        public OrderResponseDto createOrder(OrderRequestDto dto, String userId)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();
                String orderCode = generateOrderCode(db);
                Order order = new Order();

                // ===== BASIC =====
                order.setOrderId(orderCode);
                order.setUserId(dto.getUserId());
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
                db.collection(COLLECTION_NAME).document(orderCode).set(order).get();

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

        public PageResponseDto<OrderResponseDto> getOrdersByEmail(
                        String email,
                        int page,
                        int size)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();

                Query baseQuery = db.collection(COLLECTION_NAME)
                                .whereEqualTo("email", email)
                                .orderBy("createdAt", Query.Direction.DESCENDING);

                Query pagedQuery = baseQuery
                                .offset(page * size)
                                .limit(size);

                List<OrderResponseDto> items = pagedQuery.get().get().getDocuments()
                                .stream()
                                .map(doc -> orderMapper.toOrderResponseDto(doc.toObject(Order.class)))
                                .toList();

                long totalElements = baseQuery.get().get().size();
                int totalPages = (int) Math.ceil((double) totalElements / size);

                return new PageResponseDto<>(
                                items,
                                page,
                                size,
                                totalElements,
                                totalPages);
        }

        public PageResponseDto<OrderResponseDto> getOrdersByPhone(
                        String phone,
                        int page,
                        int size)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();

                Query baseQuery = db.collection(COLLECTION_NAME)
                                .whereEqualTo("phone", phone)
                                .orderBy("createdAt", Query.Direction.DESCENDING);

                Query pagedQuery = baseQuery
                                .offset(page * size)
                                .limit(size);

                List<OrderResponseDto> items = pagedQuery.get().get().getDocuments()
                                .stream()
                                .map(doc -> orderMapper.toOrderResponseDto(doc.toObject(Order.class)))
                                .toList();

                long totalElements = baseQuery.get().get().size();
                int totalPages = (int) Math.ceil((double) totalElements / size);

                return new PageResponseDto<>(
                                items,
                                page,
                                size,
                                totalElements,
                                totalPages);
        }

        public PageResponseDto<OrderResponseDto> getOrdersByDiscountId(
                        String discountId,
                        int page,
                        int size)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();

                Query baseQuery = db.collection(COLLECTION_NAME)
                                .whereEqualTo("discount.discountId", discountId)
                                .orderBy("createdAt", Query.Direction.DESCENDING);

                Query pagedQuery = baseQuery
                                .offset(page * size)
                                .limit(size);

                List<OrderResponseDto> items = pagedQuery
                                .get()
                                .get()
                                .getDocuments()
                                .stream()
                                .map(doc -> orderMapper.toOrderResponseDto(doc.toObject(Order.class)))
                                .toList();

                long totalElements = baseQuery.get().get().size();
                int totalPages = (int) Math.ceil((double) totalElements / size);

                return new PageResponseDto<>(
                                items,
                                page,
                                size,
                                totalElements,
                                totalPages);
        }

        public PageResponseDto<OrderResponseDto> getOrdersByDateRange(
                        LocalDate fromDate,
                        LocalDate toDate,
                        int page,
                        int size)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();

                Timestamp from = Timestamp.of(
                                Date.from(fromDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()));
                Timestamp to = Timestamp.of(
                                Date.from(toDate.plusDays(1)
                                                .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant()));

                Query baseQuery = db.collection(COLLECTION_NAME)
                                .whereGreaterThanOrEqualTo("createdAt", from)
                                .whereLessThan("createdAt", to)
                                .orderBy("createdAt", Query.Direction.DESCENDING);

                Query pagedQuery = baseQuery
                                .offset(page * size)
                                .limit(size);

                List<OrderResponseDto> items = pagedQuery.get().get().getDocuments()
                                .stream()
                                .map(doc -> orderMapper.toOrderResponseDto(doc.toObject(Order.class)))
                                .toList();

                long totalElements = baseQuery.get().get().size();
                int totalPages = (int) Math.ceil((double) totalElements / size);

                return new PageResponseDto<>(
                                items,
                                page,
                                size,
                                totalElements,
                                totalPages);
        }

        // DELETE
        public String deleteOrder(String orderId)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();
                ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME).document(orderId).delete();

                // --- Send message to RabbitMQ ---
                orderEventProducer.sendMessage(orderId);

                return "Đơn hàng " + orderId + " Xóa tại " + result.get().getUpdateTime();
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
                        throw new RuntimeException("Đơn hàng không tìm thấy");
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
                        case DELIVERING:
                                order.setStatus(nextStatus.name());
                                break;
                        case PAID:
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

        public PageResponseDto<OrderResponseDto> filterStatusOrders(
                        List<String> statuses,
                        int page,
                        int size)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();

                Query query = db.collection(COLLECTION_NAME);

                // ===== STATUS FILTER =====
                if (statuses != null && !statuses.isEmpty()) {
                        query = query.whereIn("status", statuses);
                }

                // ===== SORT =====
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);

                // ===== PAGINATION =====
                Query pagedQuery = query
                                .offset(page * size)
                                .limit(size);

                List<OrderResponseDto> items = pagedQuery
                                .get()
                                .get()
                                .getDocuments()
                                .stream()
                                .map(doc -> orderMapper.toOrderResponseDto(doc.toObject(Order.class)))
                                .toList();

                long totalElements = query.get().get().size();
                int totalPages = (int) Math.ceil((double) totalElements / size);

                return new PageResponseDto<>(
                                items,
                                page,
                                size,
                                totalElements,
                                totalPages);
        }
}
