package com.hmkeyewear.user_service.controller;

import com.hmkeyewear.user_service.dto.UserRequestDto;
import com.hmkeyewear.user_service.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** ==================== COMMON UTILS ==================== */

    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equalsIgnoreCase(role);
    }

    /** ==================== USER SELF ==================== */

    @GetMapping("/get/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("X-User-Id") String userId)
            throws ExecutionException, InterruptedException {
        try {
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/update/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserRequestDto dto) throws ExecutionException, InterruptedException {
        try {
            return ResponseEntity.ok(userService.updateProfile(userId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /** ==================== ADMIN ==================== */

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllUsers(@RequestHeader("X-User-Role") String role)
            throws ExecutionException, InterruptedException {

        if (!isAdmin(role))
            return ResponseEntity.status(403).body("Access denied: Admin only");

        try {
            return ResponseEntity.ok(userService.getAllUser());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/getByRole")
    public ResponseEntity<?> getUsersByRole(
            @RequestHeader("X-User-Role") String roleHeader,
            @RequestParam String role) throws ExecutionException, InterruptedException {

        if (!isAdmin(roleHeader))
            return ResponseEntity.status(403).body("Access denied: Admin only");

        try {
            return ResponseEntity.ok(userService.getUserByRole(role));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/getByPhone")
    public ResponseEntity<?> getUserByPhone(
            @RequestHeader("X-User-Role") String roleHeader,
            @RequestParam String phone) throws ExecutionException, InterruptedException {

        if (!isAdmin(roleHeader))
            return ResponseEntity.status(403).body("Access denied: Admin only");

        try {
            return ResponseEntity.ok(userService.getUserByPhone(phone));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getUser(
            @RequestHeader("X-User-Role") String roleHeader,
            @RequestParam String userId) throws ExecutionException, InterruptedException {

        if (!isAdmin(roleHeader) && !"EMPLOYER".equalsIgnoreCase(roleHeader)) {
            return ResponseEntity.status(403).body("Access denied: Admin or Employer only");
        }

        try {
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestHeader("X-User-Role") String roleHeader,
            @RequestParam String userId,
            @Valid @RequestBody UserRequestDto dto,
            @RequestHeader("X-User-Id") String updatedBy) throws ExecutionException, InterruptedException {

        if (!isAdmin(roleHeader))
            return ResponseEntity.status(403).body("Access denied: Admin only");

        try {
            return ResponseEntity.ok(userService.updateUser(userId, dto, updatedBy));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
