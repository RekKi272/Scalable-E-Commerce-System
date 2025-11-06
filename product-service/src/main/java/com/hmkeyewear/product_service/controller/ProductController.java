package com.hmkeyewear.product_service.controller;

import com.hmkeyewear.product_service.dto.ProductInforResponseDto;
import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("product")
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
    public ResponseEntity<ProductResponseDto> getProductById(@RequestParam String productId)
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
    public ResponseEntity<List<ProductInforResponseDto>> GetActiveProducts() throws InterruptedException, ExecutionException {
        List<ProductInforResponseDto> response = productService.getActiveProducts();
        return ResponseEntity.ok(response);
    }

    // UPDATE PRODUCT
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId,
            @Valid @RequestBody ProductRequestDto productRequestDto)
            throws InterruptedException, ExecutionException {

        if (!List.of("ROLE_EMPLOYER", "ROLE_ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền sửa sản phẩm");
        }

        ProductResponseDto response = productService.updateProduct(productId, productRequestDto, userId);
        return ResponseEntity.ok(response);
    }

    // SEARCH Product by Name
    @GetMapping("/search")
    public ResponseEntity<List<ProductInforResponseDto>> searchProductsByName(@RequestParam String keyword) throws InterruptedException, ExecutionException {
        return ResponseEntity.ok(productService.searchProductByName(keyword));
    }

    // DELETE Product
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String productId)
            throws InterruptedException, ExecutionException {
        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa sản phẩm");
        }
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }
}
