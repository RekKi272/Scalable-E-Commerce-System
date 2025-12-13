package com.hmkeyewear.cart_service.controller;

import com.hmkeyewear.cart_service.dto.DiscountRequestDto;
import com.hmkeyewear.cart_service.dto.DiscountResponseDto;
import com.hmkeyewear.cart_service.service.DiscountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("discount")
public class DiscountController {
    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    // CREATE Discount
    @PostMapping("/create")
    public ResponseEntity<?> createDiscount(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody DiscountRequestDto discountRequestDto) throws ExecutionException, InterruptedException {
        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền tạo mã giảm giá");
        }

        DiscountResponseDto response = discountService.createDiscount(discountRequestDto, userId);
        return ResponseEntity.ok(response);
    }

    // Get Discount By Id
    @GetMapping("/get")
    public ResponseEntity<?> getDiscountById(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String discountId) throws ExecutionException, InterruptedException {

        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        DiscountResponseDto response = discountService.getDiscountById(discountId);
        return ResponseEntity.ok(response);
    }

    // Get ALL Discount
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllDiscounts(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId) throws ExecutionException, InterruptedException {
        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }
        if ("ROLE_USER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền truy ");
        }

        List<DiscountResponseDto> response = discountService.getAllDiscounts();
        return ResponseEntity.ok(response);
    }

    // UPDATE Discount
    @PutMapping("/update")
    public ResponseEntity<?> updateDiscount(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody DiscountRequestDto discountRequestDto) throws ExecutionException, InterruptedException {
        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền chỉnh sửa mã giảm giá");
        }

        DiscountResponseDto response = discountService.updateDiscount(userId, discountRequestDto);
        return ResponseEntity.ok(response);
    }

    // DELETE Discount
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteDiscount(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "discountId") String discountId) throws ExecutionException, InterruptedException {

        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa mã giảm giá");
        }

        return ResponseEntity.ok(discountService.deleteDiscount(discountId));
    }
}
