package com.hmkeyewear.order_service.controller;

import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import com.hmkeyewear.common_dto.dto.OrderStatusUpdateRequestDto;
import com.hmkeyewear.order_service.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        if ((!"ROLE_EMPLOYER".equals(role) && !"ROLE_ADMIN".equals(role))) {
            return ResponseEntity.status(403).body("Bạn cần đăng nhập để tạo đơn hàng");
        }

        OrderResponseDto orderResponseDto = orderService.createOrder(orderRequestDto, userId);

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

    @PutMapping("/update-status/{orderId}")
    public ResponseEntity<?> updateOrder(
            @RequestHeader(name = "X-User-Role") String role,
            @RequestHeader(name = "X-User-Id") String userId,
            @PathVariable(name = "orderId") String orderId,
            @RequestBody OrderStatusUpdateRequestDto request)
            throws ExecutionException, InterruptedException {

        if (role == null || userId == null) {
            return ResponseEntity.status(403)
                    .body("Bạn cần đăng nhập để cập nhật trạng thái đơn");
        }

        try {
            OrderResponseDto response = orderService.updateOrderStatus(orderId, request.getStatus(), userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
}
