package com.hmkeyewear.blog_service.controller;

import com.hmkeyewear.blog_service.dto.BlogRequestDto;
import com.hmkeyewear.blog_service.dto.BlogResponseDto;
import com.hmkeyewear.blog_service.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @PostMapping("/create")
    public ResponseEntity<BlogResponseDto> createBlog(
            @RequestParam String createdBy,
            @RequestBody BlogRequestDto blogRequestDto
    ) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.createBlog(createdBy, blogRequestDto));
    }

    @GetMapping("/get/{blogId}")
    public ResponseEntity<BlogResponseDto> getBlog(@PathVariable String blogId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.getBlogById(blogId));
    }

    @PutMapping("/update/{blogId}")
    public ResponseEntity<BlogResponseDto> updateBlog(
            @PathVariable String blogId,
            @RequestBody BlogRequestDto dto
    ) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.updateBlog(blogId, dto));
    }

    @DeleteMapping("/delete/{blogId}")
    public ResponseEntity<String> deleteBlog(@PathVariable String blogId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.deleteBlog(blogId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<BlogResponseDto>> getAllActiveBlogs()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.getAllActiveBlogs());
    }
}
