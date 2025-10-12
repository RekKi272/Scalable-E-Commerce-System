package com.hmkeyewear.product_service.controller;

import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.model.Product;
import com.hmkeyewear.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("product")
public class ProductController {

    public ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // CREATE Product
    @PostMapping("/admin/create")
    public ResponseEntity<?> createProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @Valid @RequestBody ProductRequestDto productRequestDto)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền tạo sản phẩm");
        }

        ProductResponseDto response = productService.createProduct(productRequestDto, username);
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

    @GetMapping("/getUser")
    public String getUserName(@RequestParam String customerId) throws InterruptedException, ExecutionException {
        return productService.getCustomer(customerId);
    }

    // UPDATE PRODUCT
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @PathVariable String productId,
            @Valid @RequestBody ProductRequestDto productRequestDto)
            throws InterruptedException, ExecutionException {

        if (!List.of("ROLE_EMPLOYER", "ROLE_ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền sửa sản phẩm");
        }

        ProductResponseDto response = productService.updateProduct(productId, productRequestDto, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String productId)
            throws InterruptedException, ExecutionException {
        if(!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa sản phẩm");
        }
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }

}
