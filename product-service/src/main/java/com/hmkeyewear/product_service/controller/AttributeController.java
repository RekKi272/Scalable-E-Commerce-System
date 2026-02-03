package com.hmkeyewear.product_service.controller;

import com.hmkeyewear.product_service.dto.AttributeCategoryLinkRequestDto;
import com.hmkeyewear.product_service.dto.AttributeRequestDto;
import com.hmkeyewear.product_service.dto.AttributeResponseDto;
import com.hmkeyewear.product_service.service.AttributeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("category/attribute")
public class AttributeController {

    private final AttributeService service;

    public AttributeController(AttributeService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @RequestBody AttributeRequestDto dto)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_ADMIN", "ROLE_EMPLOYER").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Không có quyền tạo attribute");
        }
        return ResponseEntity.ok(service.create(dto, username));
    }

    @PutMapping("/update/{attributeId}")
    public ResponseEntity<?> update(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String attributeId,
            @RequestBody AttributeRequestDto dto)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_ADMIN", "ROLE_EMPLOYER").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Không có quyền cập nhật attribute");
        }
        return ResponseEntity.ok(service.update(attributeId, dto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String attributeId)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Không có quyền xoá attribute");
        }
        return ResponseEntity.ok(service.delete(attributeId));
    }

    @GetMapping("/option")
    public ResponseEntity<List<AttributeResponseDto>> option()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.getOptions());
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> paging(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_ADMIN", "ROLE_EMPLOYER").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Không có quyền xem attribute");
        }
        return ResponseEntity.ok(service.getPaging(page, size));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<AttributeResponseDto>> getByCategory(
            @RequestParam("categoryId") String categoryId)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(service.getByCategoryId(categoryId));
    }

    @PostMapping("/update-link")
    public ResponseEntity<?> updateLink(
            @RequestHeader("X-User-Role") String role,
            @RequestBody AttributeCategoryLinkRequestDto dto)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_ADMIN", "ROLE_EMPLOYER")
                .contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Không có quyền thao tác");
        }

        return ResponseEntity.ok(
                service.updateCategoryLink(
                        dto.getAttributeId(),
                        dto.getCategoryId(),
                        dto.isAttach()));
    }
}
