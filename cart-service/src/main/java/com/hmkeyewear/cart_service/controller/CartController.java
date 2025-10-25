package com.hmkeyewear.cart_service.controller;

import com.hmkeyewear.cart_service.dto.CartRequestDto;
import com.hmkeyewear.cart_service.dto.CartResponseDto;
import com.hmkeyewear.cart_service.dto.AddToCartRequestDto;
import com.hmkeyewear.cart_service.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

//    @PostMapping("/create")
//    public ResponseEntity<CartResponseDto> createCart(@RequestBody CartRequestDto dto) throws ExecutionException, InterruptedException {
//        return ResponseEntity.ok(cartService.createCart(dto));
//    }

    /**
     *  Lấy giỏ hàng của người dùng đã đăng nhập
     */
    @GetMapping("/get")
    public ResponseEntity<?> getCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) throws ExecutionException, InterruptedException {

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
     *  Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody AddToCartRequestDto request
    ) throws ExecutionException, InterruptedException {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để thêm vào giỏ hàng");
        }


        CartResponseDto updatedCart = cartService.addToCart(request, userId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     *  Cập nhật toàn bộ giỏ hàng
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CartRequestDto dto
    ) throws ExecutionException, InterruptedException {

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để cập nhật giỏ hàng");
        }

        // Gán customerId theo userId
        dto.setCustomerId(userId);

        CartResponseDto updatedCart = cartService.updateCart(userId, dto);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     *  Xóa giỏ hàng (thường dùng khi thanh toán xong)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) throws ExecutionException, InterruptedException {

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
    public ResponseEntity<CartResponseDto> updateItemQuantity(
            @RequestHeader("X-User-Id") String customerId,
            @RequestParam String productId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer quantity)
            throws ExecutionException, InterruptedException {

        if (action == null && quantity == null) {
            return ResponseEntity.badRequest().build();
        }

        CartResponseDto updatedCart = cartService.updateItemQuantity(customerId, productId, action, quantity);
        return ResponseEntity.ok(updatedCart);
    }
}
