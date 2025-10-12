package com.hmkeyewear.product_service.controller;

import com.hmkeyewear.product_service.dto.CategoryRequestDto;
import com.hmkeyewear.product_service.dto.CategoryResponseDto;
import com.hmkeyewear.product_service.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCategory(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @RequestBody CategoryRequestDto dto)
            throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_EMPLOYER", "ROLE_ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền sửa sản phẩm");
        }
        CategoryResponseDto response = categoryService.createCategory(dto, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<CategoryResponseDto> getCategory(@RequestParam String categoryId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PutMapping("/update/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String username,
            @PathVariable String categoryId,
            @RequestBody CategoryRequestDto dto) throws ExecutionException, InterruptedException {

        if (!List.of("ROLE_EMPLOYER", "ROLE_ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body("Bạn không có quyền  sửa danh mục sản phẩm");
        }

        CategoryResponseDto response = categoryService.updateCategory(categoryId, dto, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCategory(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String categoryId)
            throws ExecutionException, InterruptedException {
        if(!"ROLE_ADMIN".equalsIgnoreCase(role)){
            return ResponseEntity.status(403).body("Bạn không có quyền xóa danh mục sản phẩm");
        }
        return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
    }
}
