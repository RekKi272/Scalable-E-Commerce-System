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
@CrossOrigin(origins = "*")
@RequestMapping("product")
public class ProductController {

    public ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // CREATE Product
    @PostMapping("/create")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto productRequestDto)
            throws ExecutionException, InterruptedException {

        ProductResponseDto response = productService.createProduct(productRequestDto, "admin");
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
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody ProductRequestDto productRequestDto)
            throws InterruptedException, ExecutionException {

        String updatedBy = "admin"; // temp

        ProductResponseDto response = productService.updateProduct(productId, productRequestDto, updatedBy);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public String deleteProduct(@RequestParam String productId) throws InterruptedException, ExecutionException {
        return productService.deleteProduct(productId);
    }

}
