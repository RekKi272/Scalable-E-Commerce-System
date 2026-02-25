package com.hmkeyewear.cart_service.controller;

import com.hmkeyewear.cart_service.dto.DiscountRequestDto;
import com.hmkeyewear.cart_service.dto.DiscountResponseDto;
import com.hmkeyewear.cart_service.service.DiscountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/discount")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDiscount(
            @RequestHeader(name = "X-User-Role") String role,
            @RequestHeader(name = "X-User-Id") String userId,
            @RequestBody DiscountRequestDto discountRequestDto)
            throws ExecutionException, InterruptedException {

        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền tạo mã giảm giá");
        }

        DiscountResponseDto response = discountService.createDiscount(discountRequestDto, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getDiscountById(
            @RequestParam(name = "discountId") String discountId)
            throws ExecutionException, InterruptedException {

        DiscountResponseDto response = discountService.getDiscountById(discountId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllDiscounts(
            @RequestHeader(name = "X-User-Role") String role,
            @RequestHeader(name = "X-User-Id") String userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
            throws ExecutionException, InterruptedException {

        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        if ("ROLE_USER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền truy cập");
        }

        return ResponseEntity.ok(
                discountService.getDiscountsPaging(page, size));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateDiscount(
            @RequestHeader(name = "X-User-Role") String role,
            @RequestHeader(name = "X-User-Id") String userId,
            @RequestBody DiscountRequestDto discountRequestDto)
            throws ExecutionException, InterruptedException {

        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền chỉnh sửa mã giảm giá");
        }

        DiscountResponseDto response = discountService.updateDiscount(userId, discountRequestDto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteDiscount(
            @RequestHeader(name = "X-User-Role") String role,
            @RequestHeader(name = "X-User-Id") String userId,
            @RequestParam(name = "discountId") String discountId)
            throws ExecutionException, InterruptedException {

        if (userId == null) {
            return ResponseEntity.status(403).body("Vui lòng đăng nhập");
        }

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa mã giảm giá");
        }

        return ResponseEntity.ok(
                discountService.deleteDiscount(discountId));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateDiscount(
            @RequestParam(name = "discountId") String discountId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(
                discountService.validateDiscountUsable(discountId));
    }
}
