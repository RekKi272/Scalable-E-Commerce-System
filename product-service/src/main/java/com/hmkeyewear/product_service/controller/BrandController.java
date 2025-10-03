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
@CrossOrigin(origins = "*")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/create")
    public ResponseEntity<BrandResponseDto> createBrand(@RequestBody BrandRequestDto dto)
            throws ExecutionException, InterruptedException {
        BrandResponseDto response = brandService.createBrand(dto, "admin"); // tạm thời dùng admin
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
    public ResponseEntity<BrandResponseDto> updateBrand(@PathVariable String brandId, @RequestBody BrandRequestDto dto)
            throws ExecutionException, InterruptedException {
        BrandResponseDto response = brandService.updateBrand(brandId, dto, "admin"); // tạm thời admin
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteBrand(@RequestParam String brandId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(brandService.deleteBrand(brandId));
    }
}
