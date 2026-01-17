package com.hmkeyewear.order_service.controller;

import com.hmkeyewear.order_service.dto.OrderRequestDto;
import com.hmkeyewear.order_service.dto.OrderResponseDto;
import com.hmkeyewear.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody OrderRequestDto orderRequestDto)
            throws ExecutionException, InterruptedException {

        if (role == null || userId == null) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập để tạo đơn hàng");
        }

        orderRequestDto.setCreatedBy(userId);

        OrderResponseDto orderResponseDto = orderService.createOrder(orderRequestDto, role);

        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getOrder(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("orderId") String orderId)
            throws ExecutionException, InterruptedException {

        if (role == null || userId == null) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập để xem đơn ");
        }

        OrderResponseDto orderResponseDto = orderService.getOrder(orderId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/get-by-email")
    public ResponseEntity<?> getOrderByEmail(
            @RequestHeader("X-User-Role") String role,
            @RequestParam("userEmail") String userEmail)
            throws ExecutionException, InterruptedException {

        if (role == null) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập");
        }

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện");
        }

        List<OrderResponseDto> orders = orderService.getOrdersByEmail(userEmail);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/get-by-phone")
    public ResponseEntity<?> getOrdersByPhone(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("phone") String phone)
            throws ExecutionException, InterruptedException {

        if (role == null || userId == null) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập để xem đơn hàng");
        }

        return ResponseEntity.ok(orderService.getOrdersByPhone(phone));
    }

    @GetMapping("/my-order")
    public ResponseEntity<?> getMyOrder(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String userEmail)
            throws ExecutionException, InterruptedException {

        if (role == null || userEmail == null) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập");
        }

        if (!"ROLE_CUSTOMER".equals(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xem đơn hàng");
        }

        List<OrderResponseDto> orders = orderService.getOrdersByEmail(userEmail);

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<?> updateOrder(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("orderId") String orderId,
            @RequestBody OrderRequestDto orderRequestDto)
            throws ExecutionException, InterruptedException {

        if (role == null || userId == null) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập để sửa đơn ");
        }

        OrderResponseDto orderResponseDto = orderService.updateOrder(orderId, orderRequestDto, userId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteOrder(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("orderId") String orderId)
            throws ExecutionException, InterruptedException {

        if (role == null || userId == null) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập để xóa đơn hàng");
        }

        return ResponseEntity.ok(orderService.deleteOrder(orderId));
    }

    // ----- STATISTIC -----

    @GetMapping("/statistic/month")
    public ResponseEntity<?> statisticByMonth(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
        }

        return ResponseEntity.ok(orderService.statisticByMonth(year, month));
    }

    @GetMapping("/statistic/week")
    public ResponseEntity<?> statisticByWeek(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("localDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate localDate)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
        }

        return ResponseEntity.ok(orderService.statisticByWeek(localDate));
    }

    @GetMapping("/statistic/year")
    public ResponseEntity<?> statisticByYear(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("year") int year)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
        }

        return ResponseEntity.ok(orderService.statisticByYear(year));
    }

}
