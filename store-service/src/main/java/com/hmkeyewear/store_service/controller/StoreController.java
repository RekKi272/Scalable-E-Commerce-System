package com.hmkeyewear.store_service.controller;

import com.hmkeyewear.store_service.dto.StoreRequestDto;
import com.hmkeyewear.store_service.dto.StoreResponseDto;
import com.hmkeyewear.store_service.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // CREATE store
    @PostMapping("/create")
    public ResponseEntity<?> createStore(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @Valid @RequestBody StoreRequestDto requestDto) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền tạo chi nhánh");
        }

        try {
            StoreResponseDto response = storeService.createStore(requestDto, username);
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi tạo chi nhánh: " + e.getMessage());
        }
    }

    // GET ALL
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllStores() {
        try {
            List<StoreResponseDto> stores = storeService.getAllStores();
            return ResponseEntity.ok(stores);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi lấy danh sách chi nhánh: " + e.getMessage());
        }
    }

    // GET ONE
    @GetMapping("/get")
    public ResponseEntity<?> getStoreById(@RequestParam String storeId) {
        try {
            StoreResponseDto response = storeService.getStoreById(storeId);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi lấy chi nhánh: " + e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteStore(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String storeId) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa chi nhánh");
        }

        try {
            String message = storeService.deleteStore(storeId);
            return ResponseEntity.ok(message);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi xóa chi nhánh: " + e.getMessage());
        }
    }
}
