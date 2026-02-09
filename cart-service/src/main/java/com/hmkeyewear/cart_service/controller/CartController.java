package com.hmkeyewear.cart_service.controller;

import com.hmkeyewear.cart_service.dto.*;
import com.hmkeyewear.cart_service.service.CartService;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import com.hmkeyewear.common_dto.dto.PaymentRequestDto;
import com.hmkeyewear.common_dto.dto.VNPayResponseDto;
import com.hmkeyewear.cart_service.messaging.OrderCheckoutRequestEventProducer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // @PostMapping("/create")
    // public ResponseEntity<CartResponseDto> createCart(@RequestBody CartRequestDto
    // dto) throws ExecutionException, InterruptedException {
    // return ResponseEntity.ok(cartService.createCart(dto));
    // }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    /**
     * Lấy giỏ hàng của người dùng đã đăng nhập
     */
    @GetMapping("/get")
    public ResponseEntity<?> getCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role)
            throws ExecutionException, InterruptedException {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để xem giỏ hàng");
        }

        CartResponseDto cart = cartService.getCart(userId);
        if (cart == null) {
            return ResponseEntity.ok("Giỏ hàng hiện đang trống");
        }
        return ResponseEntity.ok(cart);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody AddToCartRequestDto request) throws ExecutionException, InterruptedException {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để thêm vào giỏ hàng");
        }

        CartResponseDto updatedCart = cartService.addToCart(request, userId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Cập nhật toàn bộ giỏ hàng
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CartRequestDto dto) throws ExecutionException, InterruptedException {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để cập nhật giỏ hàng");
        }

        // Gán customerId theo userId
        dto.setUserId(userId);

        CartResponseDto updatedCart = cartService.updateCart(userId, dto);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Xóa 1 sản phẩm trong giỏ hàng
     */
    @DeleteMapping("/removeItem")
    public ResponseEntity<?> removeItem(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("productId") String productId,
            @RequestParam(value = "variantId", required = false) String variantId)
            throws ExecutionException, InterruptedException {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để xóa sản phẩm");
        }

        try {
            CartResponseDto updatedCart = cartService.removeItem(userId, productId, variantId);
            return ResponseEntity.ok(updatedCart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Xóa giỏ hàng (thường dùng khi thanh toán xong)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId)
            throws ExecutionException, InterruptedException {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để xóa giỏ hàng");
        }

        String result = cartService.deleteCart(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Cập nhật số lượng sản phẩm (2 cách: +1/-1 hoặc nhập số cụ thể)
     * Ví dụ:
     * PATCH /carts/updateQuantity?productId=prd_001&action=increment
     * PATCH /carts/updateQuantity?productId=prd_001&quantity=5
     */
    @PatchMapping("/updateQuantity")
    public ResponseEntity<?> updateItemQuantity(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "productId") String productId,
            @RequestParam(name = "variantId", required = false) String variantId,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "quantity", required = false) Integer quantity)
            throws ExecutionException, InterruptedException {

        if (action == null && quantity == null) {
            return ResponseEntity.badRequest().build();
        }

        CartResponseDto updatedCart = cartService.updateItemQuantity(userId, productId, variantId, action, quantity);

        return ResponseEntity.ok(updatedCart);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String email,
            @RequestBody CheckoutRequestDto checkoutRequest,
            HttpServletRequest request)
            throws ExecutionException, InterruptedException {

        OrderResponseDto order = cartService.checkout(userId, email, checkoutRequest, request);

        if ("BANK_TRANSFER".equalsIgnoreCase(order.getPaymentMethod())) {
            String ipAddress = request.getRemoteAddr();
            VNPayResponseDto payment = cartService.createPayment(order, ipAddress);

            System.out.println(payment.getPaymentUrl());

            return ResponseEntity.ok(payment);
        }

        cartService.clearCart(userId);
        return ResponseEntity.ok(order);
    }

}
