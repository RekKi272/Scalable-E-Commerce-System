// package com.hmkeyewear.order_service.service;

// import com.google.api.core.ApiFuture;
// import com.google.cloud.firestore.*;
// import com.google.firebase.cloud.FirestoreClient;
// import com.hmkeyewear.common_dto.dto.OrderResponseDto;
// import com.hmkeyewear.order_service.dto.RevenueChartWithOrdersResponseDto;
// import
// com.hmkeyewear.order_service.dto.RevenueYearChartWithOrdersResponseDto;
// import com.hmkeyewear.order_service.mapper.OrderMapper;
// import com.hmkeyewear.order_service.model.Order;

// import org.springframework.stereotype.Service;

// import com.google.cloud.Timestamp;

// import java.time.DayOfWeek;
// import java.time.LocalDate;
// import java.time.ZoneId;
// import java.util.*;
// import java.util.concurrent.ExecutionException;

// @Service
// public class OrderStatisticService {

// private final OrderMapper orderMapper;

// private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

// public OrderStatisticService(OrderMapper orderMapper) {
// this.orderMapper = orderMapper;
// }

// private RevenueChartWithOrdersResponseDto statisticByDateRange(
// LocalDate fromDate,
// LocalDate toDate)
// throws ExecutionException, InterruptedException {

// Firestore db = FirestoreClient.getFirestore();

// Timestamp from = Timestamp.of(
// Date.from(fromDate.atStartOfDay(VN_ZONE).toInstant()));
// Timestamp to = Timestamp.of(
// Date.from(toDate.plusDays(1).atStartOfDay(VN_ZONE).toInstant()));

// ApiFuture<QuerySnapshot> future = db.collection("orders")
// .whereGreaterThanOrEqualTo("createdAt", from)
// .whereLessThan("createdAt", to)
// .whereIn("status", List.of("PAID", "SUCCESS", "COMPLETED", "DELIVERING"))
// .get();

// Map<String, Double> revenueMap = new LinkedHashMap<>();
// LocalDate current = fromDate;
// while (!current.isAfter(toDate)) {
// revenueMap.put(current.toString(), 0.0);
// current = current.plusDays(1);
// }

// List<OrderResponseDto> orders = new ArrayList<>();

// for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
// Order order = doc.toObject(Order.class);
// if (order == null || order.getCreatedAt() == null)
// continue;

// LocalDate orderDate = order.getCreatedAt()
// .toDate()
// .toInstant()
// .atZone(VN_ZONE)
// .toLocalDate();

// String key = orderDate.toString();
// revenueMap.put(key, revenueMap.get(key) + order.getSummary());

// orders.add(orderMapper.toOrderResponseDto(order));
// }

// return new RevenueChartWithOrdersResponseDto(revenueMap, orders);
// }

// public RevenueChartWithOrdersResponseDto statisticByWeek(LocalDate anyDay)
// throws ExecutionException, InterruptedException {

// LocalDate start = anyDay.with(DayOfWeek.MONDAY);
// LocalDate end = start.plusDays(6);
// return statisticByDateRange(start, end);
// }

// public RevenueChartWithOrdersResponseDto statisticByMonth(int year, int
// month)
// throws ExecutionException, InterruptedException {

// LocalDate from = LocalDate.of(year, month, 1);
// LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
// return statisticByDateRange(from, to);
// }

// public RevenueYearChartWithOrdersResponseDto statisticByYear(int year)
// throws ExecutionException, InterruptedException {

// Firestore db = FirestoreClient.getFirestore();

// LocalDate fromDate = LocalDate.of(year, 1, 1);
// LocalDate toDate = LocalDate.of(year, 12, 31);

// Timestamp from = Timestamp.of(
// Date.from(fromDate.atStartOfDay(VN_ZONE).toInstant()));
// Timestamp to = Timestamp.of(
// Date.from(toDate.plusDays(1).atStartOfDay(VN_ZONE).toInstant()));

// ApiFuture<QuerySnapshot> future = db.collection("orders")
// .whereGreaterThanOrEqualTo("createdAt", from)
// .whereLessThan("createdAt", to)
// .whereIn("status", List.of("PAID", "SUCCESS", "COMPLETED", "DELIVERING"))
// .get();

// Map<String, Double> revenueByMonth = new LinkedHashMap<>();
// for (int i = 1; i <= 12; i++) {
// revenueByMonth.put(String.format("%02d", i), 0.0);
// }

// List<OrderResponseDto> orders = new ArrayList<>();

// for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
// Order order = doc.toObject(Order.class);
// if (order == null || order.getCreatedAt() == null)
// continue;

// int month = order.getCreatedAt()
// .toDate()
// .toInstant()
// .atZone(VN_ZONE)
// .getMonthValue();

// String key = String.format("%02d", month);
// revenueByMonth.put(key, revenueByMonth.get(key) + order.getSummary());

// orders.add(orderMapper.toOrderResponseDto(order));
// }

// return new RevenueYearChartWithOrdersResponseDto(revenueByMonth, orders);
// }
// }
