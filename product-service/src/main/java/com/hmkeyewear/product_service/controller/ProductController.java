package com.hmkeyewear.product_service.controller;

import com.hmkeyewear.product_service.dto.ProductInforResponseDto;
import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.dto.BatchRequestDto;
import com.hmkeyewear.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // CREATE Product
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProductRequestDto productRequestDto)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền tạo sản phẩm");
        }

        ProductResponseDto response = productService.createProduct(productRequestDto, userId);

        return ResponseEntity.ok(response);
    }

    // READ ONE PRODUCT
    @GetMapping("/get")
    public ResponseEntity<ProductResponseDto> getProductById(@RequestParam("productId") String productId)
            throws InterruptedException, ExecutionException {
        ProductResponseDto response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    // READ ALL
    @GetMapping("/getAll")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() throws InterruptedException, ExecutionException {
        List<ProductResponseDto> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    // GET ACTIVE only
    @GetMapping("/getActive")
    public ResponseEntity<List<ProductInforResponseDto>> GetActiveProducts()
            throws InterruptedException, ExecutionException {
        List<ProductInforResponseDto> response = productService.getActiveProducts();
        return ResponseEntity.ok(response);
    }

    // UPDATE PRODUCT
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("productId") String productId,
            @Valid @RequestBody ProductRequestDto productRequestDto)
            throws InterruptedException, ExecutionException {

        if (!List.of("ROLE_ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền sửa sản phẩm");
        }

        ProductResponseDto response = productService.updateProduct(productId, productRequestDto, userId);

        return ResponseEntity.ok(response);
    }

    // SEARCH Product by Name
    @GetMapping("/search")
    public ResponseEntity<List<ProductInforResponseDto>> searchProductsByName(@RequestParam String keyword)
            throws InterruptedException, ExecutionException {
        return ResponseEntity.ok(productService.searchProductByName(keyword));
    }

    // FILTER Product
    @GetMapping("/filter")
    public ResponseEntity<List<ProductInforResponseDto>> filterProducts(
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(productService.filterProducts(brandId, categoryId, minPrice, maxPrice));
    }

    // DELETE Product
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String productId)
            throws InterruptedException, ExecutionException {
        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa sản phẩm");
        }

        return ResponseEntity.ok(productService.deleteProduct(productId));
    }

    // POST nhập hàng batch
    @PostMapping("/import")
    public ResponseEntity<?> importInventoryBatch(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @Valid @RequestBody BatchRequestDto batchDto) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thao tác kho");
        }

        try {
            var responses = productService.updateInventoryBatchWithType(batchDto.getItems(), username, "IMPORT");
            return ResponseEntity.ok(responses);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật kho: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    // POST bán hàng batch
    @PostMapping("/sell")
    public ResponseEntity<?> sellInventoryBatch(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @Valid @RequestBody BatchRequestDto batchDto) {

        if (!List.of("ROLE_ADMIN", "ROLE_EMPLOYER").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền sửa sản phẩm");
        }

        try {
            var responses = productService.updateInventoryBatchWithType(batchDto.getItems(), username, "SELL");
            return ResponseEntity.ok(responses);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật kho: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    // POST bán hàng batch (online không cần xác thực)
    @PostMapping("/sell-online")
    public ResponseEntity<?> sellInventoryBatch(
            @Valid @RequestBody BatchRequestDto batchDto) {

        try {
            var responses = productService.updateInventoryBatchWithType(batchDto.getItems(), "ONLINE", "SELL");
            return ResponseEntity.ok(responses);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật kho: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
