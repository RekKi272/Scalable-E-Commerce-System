package com.hmkeyewear.blog_service.controller;

import com.hmkeyewear.blog_service.dto.BannerRequestDto;
import com.hmkeyewear.blog_service.dto.BannerResponseDto;
import com.hmkeyewear.blog_service.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @PostMapping("/create")
    public ResponseEntity<BannerResponseDto> createBanner(
            @RequestHeader("X-User-Name") String createdBy,
            @RequestHeader("X-User-Role") String role,
            @RequestBody BannerRequestDto dto) throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(bannerService.createBanner(createdBy, dto));
    }

    @PutMapping("/update/{bannerId}")
    public ResponseEntity<BannerResponseDto> updateBanner(
            @PathVariable String bannerId,
            @RequestHeader("X-User-Name") String updatedBy,
            @RequestHeader("X-User-Role") String role,
            @RequestBody BannerRequestDto dto) throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(bannerService.updateBanner(bannerId, dto, updatedBy));
    }

    @DeleteMapping("/delete/{bannerId}")
    public ResponseEntity<String> deleteBanner(
            @PathVariable String bannerId,
            @RequestHeader("X-User-Role") String role) throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(bannerService.deleteBanner(bannerId));
    }

    @GetMapping("/get/{bannerId}")
    public ResponseEntity<BannerResponseDto> getBannerById(
            @PathVariable String bannerId,
            @RequestHeader("X-User-Role") String role) throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(bannerService.getBannerById(bannerId));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<BannerResponseDto>> getAllBanners() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @GetMapping("/active")
    public ResponseEntity<List<BannerResponseDto>> getAllActiveBanners()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(bannerService.getAllActiveBanners());
    }
}
