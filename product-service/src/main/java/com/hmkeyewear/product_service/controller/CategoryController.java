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
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CategoryRequestDto dto)
            throws ExecutionException, InterruptedException {
        CategoryResponseDto response = categoryService.createCategory(dto, "admin");
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
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable String categoryId,
            @RequestBody CategoryRequestDto dto) throws ExecutionException, InterruptedException {
        CategoryResponseDto response = categoryService.updateCategory(categoryId, dto, "admin");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCategory(@RequestParam String categoryId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
    }
}
