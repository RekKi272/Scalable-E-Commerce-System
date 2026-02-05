package com.hmkeyewear.product_service.controller;

import com.hmkeyewear.product_service.dto.VariantImportRequestDto;
import com.hmkeyewear.product_service.dto.VariantListResponseDto;
import com.hmkeyewear.product_service.model.Variant;
import com.hmkeyewear.product_service.service.VariantService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/product/variant")
public class VariantController {

    private final VariantService variantService;

    public VariantController(VariantService variantService) {
        this.variantService = variantService;
    }

    @PostMapping("/{productId}")
    public Variant createVariant(
            @PathVariable("productId") String productId,
            @RequestBody Variant variant) throws Exception {
        return variantService.addVariant(productId, variant);
    }

    @GetMapping("/{productId}")
    public List<Variant> getAllVariants(
            @PathVariable("productId") String productId) throws Exception {
        return variantService.getAllVariantsOfProduct(productId);
    }

    @PutMapping("/{productId}")
    public Variant updateVariant(
            @PathVariable("productId") String productId,
            @RequestBody Variant variant) throws Exception {
        return variantService.updateVariant(productId, variant);
    }

    @DeleteMapping("/{productId}/{variantId}")
    public void deleteVariant(
            @PathVariable("productId") String productId,
            @PathVariable("variantId") String variantId) throws Exception {
        variantService.deleteVariant(productId, variantId);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<VariantListResponseDto>> getAllVariants()
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(variantService.getAllVariants());
    }

    @PostMapping("/import")
    public ResponseEntity<?> importVariants(
            @RequestHeader("X-User-Role") String role,
            @RequestBody List<VariantImportRequestDto> items)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_ADMIN", "ROLE_EMPLOYER").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền nhập kho");
        }

        variantService.importVariants(items);
        return ResponseEntity.ok("Nhập kho thành công");
    }

}
