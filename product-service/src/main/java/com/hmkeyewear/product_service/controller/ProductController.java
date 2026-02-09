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
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
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
    public ResponseEntity<?> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
            throws InterruptedException, ExecutionException {

        return ResponseEntity.ok(
                productService.getAllProducts(page, size));
    }

    // GET ACTIVE only
    @GetMapping("/getActive")
    public ResponseEntity<?> getActiveProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
            throws InterruptedException, ExecutionException {

        return ResponseEntity.ok(
                productService.getActiveProducts(page, size));
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
    public ResponseEntity<List<ProductInforResponseDto>> searchProductsByName(
            @RequestParam("keyword") String keyword)
            throws InterruptedException, ExecutionException {

        return ResponseEntity.ok(productService.searchProduct(keyword));
    }

    // FILTER Product
    @GetMapping("/filter")
    public ResponseEntity<List<ProductInforResponseDto>> filterProducts(
            @RequestParam(value = "brandId", required = false) String brandId,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(
                productService.filterProducts(brandId, categoryId, minPrice, maxPrice));
    }

    // DELETE Product
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("X-User-Role") String role,
            @RequestParam("productId") String productId)
            throws InterruptedException, ExecutionException {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa sản phẩm");
        }

        return ResponseEntity.ok(productService.deleteProduct(productId));
    }
}
