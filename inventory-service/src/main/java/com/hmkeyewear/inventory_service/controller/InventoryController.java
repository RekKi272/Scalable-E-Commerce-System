package com.hmkeyewear.inventory_service.controller;

import com.hmkeyewear.inventory_service.dto.InventoryRequestDto;
import com.hmkeyewear.inventory_service.dto.InventoryResponseDto;
import com.hmkeyewear.inventory_service.dto.InventoryBatchRequestDto;
import com.hmkeyewear.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // POST nhập hàng
    @PostMapping("/import")
    public ResponseEntity<?> importInventory(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @RequestHeader("X-User-StoreId") String storeId,
            @Valid @RequestBody InventoryRequestDto dto) {

        if (!"ROLE_EMPLOYER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thao tác kho");
        }

        try {
            InventoryResponseDto response = inventoryService.updateInventoryWithType(dto, storeId, username, "IMPORT");
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật kho: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST bán hàng
    @PostMapping("/sell")
    public ResponseEntity<?> sellInventory(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @RequestHeader("X-User-StoreId") String storeId,
            @Valid @RequestBody InventoryRequestDto dto) {

        if (!"ROLE_EMPLOYER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thao tác kho");
        }

        try {
            InventoryResponseDto response = inventoryService.updateInventoryWithType(dto, storeId, username, "SELL");
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật kho: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST nhập hàng batch
    @PostMapping("/import-batch")
    public ResponseEntity<?> importInventoryBatch(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @RequestHeader("X-User-StoreId") String storeId,
            @Valid @RequestBody InventoryBatchRequestDto batchDto) {

        if (!"ROLE_EMPLOYER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thao tác kho");
        }

        try {
            var responses = inventoryService.updateInventoryBatchWithType(batchDto.getItems(), storeId, username, "IMPORT");
            return ResponseEntity.ok(responses);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật kho: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST bán hàng batch
    @PostMapping("/sell-batch")
    public ResponseEntity<?> sellInventoryBatch(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @RequestHeader("X-User-StoreId") String storeId,
            @Valid @RequestBody InventoryBatchRequestDto batchDto) {

        if (!"ROLE_EMPLOYER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thao tác kho");
        }

        try {
            var responses = inventoryService.updateInventoryBatchWithType(batchDto.getItems(), storeId, username, "SELL");
            return ResponseEntity.ok(responses);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật kho: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET tất cả inventory của một cửa hàng (employee)
    @GetMapping("/store")
    public ResponseEntity<?> getInventoryByStore(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-StoreId") String storeId) {

        if (!"ROLE_EMPLOYER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xem kho");
        }

        try {
            return ResponseEntity.ok(inventoryService.getByStore(storeId));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi lấy dữ liệu kho: " + e.getMessage());
        }
    }

    // GET tất cả inventory của tất cả cửa hàng (admin)
    @GetMapping("/all")
    public ResponseEntity<?> getAllInventory(
            @RequestHeader("X-User-Role") String role) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xem toàn bộ kho");
        }

        try {
            return ResponseEntity.ok(inventoryService.getAll());
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi lấy dữ liệu kho: " + e.getMessage());
        }
    }
}
