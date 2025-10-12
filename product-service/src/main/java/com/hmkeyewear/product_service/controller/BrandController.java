package com.hmkeyewear.product_service.controller;

import com.hmkeyewear.product_service.dto.BrandRequestDto;
import com.hmkeyewear.product_service.dto.BrandResponseDto;
import com.hmkeyewear.product_service.service.BrandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("brand")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBrand(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @RequestBody BrandRequestDto dto)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_EMPLOYER", "ROLE_ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền tạo brand");
        }
        BrandResponseDto response = brandService.createBrand(dto, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<BrandResponseDto> getBrand(@RequestParam String brandId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(brandService.getBrandById(brandId));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<BrandResponseDto>> getAllBrands() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @PutMapping("/update/{brandId}")
    public ResponseEntity<?> updateBrand(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @PathVariable String brandId, @RequestBody BrandRequestDto dto)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_EMPLOYER", "ROLE_ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền chỉnh sửa brand");
        }

        BrandResponseDto response = brandService.updateBrand(brandId, dto, username); // tạm thời admin
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteBrand(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String brandId)
            throws ExecutionException, InterruptedException {

        if(!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa brand");
        }

        return ResponseEntity.ok(brandService.deleteBrand(brandId));
    }
}
