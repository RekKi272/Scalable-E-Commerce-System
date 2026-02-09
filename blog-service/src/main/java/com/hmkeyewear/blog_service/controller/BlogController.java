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

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/create")
    public ResponseEntity<BlogResponseDto> createBlog(
            @RequestHeader("X-User-Name") String createdBy,
            @RequestBody BlogRequestDto blogRequestDto) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.createBlog(createdBy, blogRequestDto));
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllBlogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(
                blogService.getAllBlogs(page, size));
    }

    @GetMapping("/get/{blogId}")
    public ResponseEntity<BlogResponseDto> getBlog(@PathVariable String blogId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.getBlogById(blogId));
    }

    @PutMapping("/update/{blogId}")
    public ResponseEntity<BlogResponseDto> updateBlog(
            @PathVariable String blogId,
            @RequestHeader("X-User-Name") String updatedBy,
            @RequestBody BlogRequestDto dto) throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(blogService.updateBlog(blogId, dto, updatedBy));
    }

    @DeleteMapping("/delete/{blogId}")
    public ResponseEntity<String> deleteBlog(@PathVariable String blogId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(blogService.deleteBlog(blogId));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveBlogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
            throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(
                blogService.getAllActiveBlogs(page, size));
    }

}
