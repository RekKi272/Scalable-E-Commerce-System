package com.hmkeyewear.order_service.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import com.hmkeyewear.order_service.dto.*;
import com.hmkeyewear.order_service.mapper.OrderMapper;
import com.hmkeyewear.order_service.model.Order;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class OrderStatisticService {

        private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

        private final OrderMapper orderMapper;

        public OrderStatisticService(OrderMapper orderMapper) {
                this.orderMapper = orderMapper;
        }

        public OrderStatisticResponseDto statistic(
                        LocalDate fromDate,
                        LocalDate toDate)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();

                Timestamp from = Timestamp.of(
                                Date.from(fromDate.atStartOfDay(VN_ZONE).toInstant()));
                Timestamp to = Timestamp.of(
                                Date.from(toDate.plusDays(1).atStartOfDay(VN_ZONE).toInstant()));

                List<Order> orders = db.collection("orders")
                                .whereGreaterThanOrEqualTo("createdAt", from)
                                .whereLessThan("createdAt", to)
                                .get()
                                .get()
                                .toObjects(Order.class);

                List<Order> completedOrders = orders.stream()
                                .filter(o -> "COMPLETED".equals(o.getStatus()))
                                .toList();

                double expectedRevenue = orders.stream()
                                .mapToDouble(Order::getSummary)
                                .sum();

                double actualRevenue = completedOrders.stream()
                                .mapToDouble(Order::getSummary)
                                .sum();

                double maxRevenue = completedOrders.stream()
                                .mapToDouble(Order::getSummary)
                                .max()
                                .orElse(0);

                double minRevenue = completedOrders.stream()
                                .mapToDouble(Order::getSummary)
                                .min()
                                .orElse(0);

                RevenueDto revenueDto = new RevenueDto(
                                expectedRevenue,
                                actualRevenue,
                                maxRevenue,
                                minRevenue);

                Map<String, Long> statusCount = orders.stream()
                                .collect(Collectors.groupingBy(
                                                Order::getStatus,
                                                Collectors.counting()));

                List<OrderResponseDto> responseOrders = orders.stream()
                                .map(orderMapper::toOrderResponseDto)
                                .toList();

                Map<String, Double> chartMap = new LinkedHashMap<>();
                LocalDate cursor = fromDate;
                while (!cursor.isAfter(toDate)) {
                        chartMap.put(cursor.toString(), 0.0);
                        cursor = cursor.plusDays(1);
                }

                for (Order o : completedOrders) {
                        String date = o.getCreatedAt()
                                        .toDate()
                                        .toInstant()
                                        .atZone(VN_ZONE)
                                        .toLocalDate()
                                        .toString();

                        chartMap.put(date, chartMap.get(date) + o.getSummary());
                }

                List<NameValueDto> revenueChart = chartMap.entrySet().stream()
                                .map(e -> new NameValueDto(e.getKey(), e.getValue()))
                                .toList();

                List<OrderResponseDto> top5Orders = completedOrders.stream()
                                .sorted(Comparator.comparingDouble(Order::getSummary).reversed())
                                .limit(5)
                                .map(orderMapper::toOrderResponseDto)
                                .toList();

                Map<String, TopCustomerDto> customerMap = new HashMap<>();
                Map<String, TopProductDto> productMap = new HashMap<>();

                for (Order o : completedOrders) {

                        customerMap.compute(o.getEmail(), (k, v) -> {
                                if (v == null) {
                                        return new TopCustomerDto(
                                                        o.getUserId(),
                                                        o.getFullName(),
                                                        o.getEmail(),
                                                        o.getSummary());
                                }
                                v.setTotalAmount(v.getTotalAmount() + o.getSummary());
                                return v;
                        });

                        for (OrderDetailRequestDto d : o.getDetails()) {
                                productMap.compute(d.getProductId(), (k, v) -> {
                                        if (v == null) {
                                                return new TopProductDto(
                                                                d.getProductId(),
                                                                d.getProductName(),
                                                                d.getQuantity());
                                        }
                                        v.setTotalQuantity(v.getTotalQuantity() + d.getQuantity());
                                        return v;
                                });
                        }
                }

                return new OrderStatisticResponseDto(
                                revenueDto,
                                orders.size(),
                                statusCount,
                                responseOrders,
                                revenueChart,
                                top5Orders,
                                customerMap.values().stream()
                                                .sorted(Comparator.comparingDouble(TopCustomerDto::getTotalAmount)
                                                                .reversed())
                                                .limit(5)
                                                .toList(),
                                productMap.values().stream()
                                                .sorted(Comparator.comparingInt(TopProductDto::getTotalQuantity)
                                                                .reversed())
                                                .limit(5)
                                                .toList());
        }

        public OrderStatisticResponseDto statisticYear(int year)
                        throws ExecutionException, InterruptedException {

                Firestore db = FirestoreClient.getFirestore();

                LocalDate fromDate = LocalDate.of(year, 1, 1);
                LocalDate toDate = LocalDate.of(year, 12, 31);

                Timestamp from = Timestamp.of(
                                Date.from(fromDate.atStartOfDay(VN_ZONE).toInstant()));
                Timestamp to = Timestamp.of(
                                Date.from(toDate.plusDays(1).atStartOfDay(VN_ZONE).toInstant()));

                List<Order> orders = db.collection("orders")
                                .whereGreaterThanOrEqualTo("createdAt", from)
                                .whereLessThan("createdAt", to)
                                .get()
                                .get()
                                .toObjects(Order.class);

                List<Order> completedOrders = orders.stream()
                                .filter(o -> "COMPLETED".equals(o.getStatus()))
                                .toList();

                /* ===== REVENUE ===== */
                double expectedRevenue = orders.stream()
                                .mapToDouble(Order::getSummary)
                                .sum();

                double actualRevenue = completedOrders.stream()
                                .mapToDouble(Order::getSummary)
                                .sum();

                double maxRevenue = completedOrders.stream()
                                .mapToDouble(Order::getSummary)
                                .max()
                                .orElse(0);

                double minRevenue = completedOrders.stream()
                                .mapToDouble(Order::getSummary)
                                .min()
                                .orElse(0);

                RevenueDto revenueDto = new RevenueDto(
                                expectedRevenue,
                                actualRevenue,
                                maxRevenue,
                                minRevenue);

                /* ===== STATUS COUNT ===== */
                Map<String, Long> statusCount = orders.stream()
                                .collect(Collectors.groupingBy(
                                                Order::getStatus,
                                                Collectors.counting()));

                /* ===== CHART: GROUP BY MONTH ===== */
                Map<String, Double> chartMap = new LinkedHashMap<>();

                for (int m = 1; m <= 12; m++) {
                        String key = String.format("%d-%02d", year, m);
                        chartMap.put(key, 0.0);
                }

                for (Order o : completedOrders) {
                        LocalDate date = o.getCreatedAt()
                                        .toDate()
                                        .toInstant()
                                        .atZone(VN_ZONE)
                                        .toLocalDate();

                        String key = String.format("%d-%02d", date.getYear(), date.getMonthValue());
                        chartMap.put(key, chartMap.get(key) + o.getSummary());
                }

                List<NameValueDto> revenueChart = chartMap.entrySet().stream()
                                .map(e -> new NameValueDto(e.getKey(), e.getValue()))
                                .toList();

                /* ===== TOP ORDERS ===== */
                List<OrderResponseDto> top5Orders = completedOrders.stream()
                                .sorted(Comparator.comparingDouble(Order::getSummary).reversed())
                                .limit(5)
                                .map(orderMapper::toOrderResponseDto)
                                .toList();

                /* ===== TOP CUSTOMER & PRODUCT ===== */
                Map<String, TopCustomerDto> customerMap = new HashMap<>();
                Map<String, TopProductDto> productMap = new HashMap<>();

                for (Order o : completedOrders) {

                        customerMap.compute(o.getEmail(), (k, v) -> {
                                if (v == null) {
                                        return new TopCustomerDto(
                                                        o.getUserId(),
                                                        o.getFullName(),
                                                        o.getEmail(),
                                                        o.getSummary());
                                }
                                v.setTotalAmount(v.getTotalAmount() + o.getSummary());
                                return v;
                        });

                        for (OrderDetailRequestDto d : o.getDetails()) {
                                productMap.compute(d.getProductId(), (k, v) -> {
                                        if (v == null) {
                                                return new TopProductDto(
                                                                d.getProductId(),
                                                                d.getProductName(),
                                                                d.getQuantity());
                                        }
                                        v.setTotalQuantity(v.getTotalQuantity() + d.getQuantity());
                                        return v;
                                });
                        }
                }

                return new OrderStatisticResponseDto(
                                revenueDto,
                                orders.size(),
                                statusCount,
                                orders.stream().map(orderMapper::toOrderResponseDto).toList(),
                                revenueChart,
                                top5Orders,
                                customerMap.values().stream()
                                                .sorted(Comparator.comparingDouble(TopCustomerDto::getTotalAmount)
                                                                .reversed())
                                                .limit(5)
                                                .toList(),
                                productMap.values().stream()
                                                .sorted(Comparator.comparingInt(TopProductDto::getTotalQuantity)
                                                                .reversed())
                                                .limit(5)
                                                .toList());
        }

}
