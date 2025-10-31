package com.hmkeyewear.auth_service.controller;

import com.hmkeyewear.auth_service.dto.AuthResponseDto;
import com.hmkeyewear.auth_service.dto.LoginRequestDto;
import com.hmkeyewear.auth_service.dto.RegisterCustomerRequestDto;
import com.hmkeyewear.auth_service.dto.RegisterStaffRequestDto;
import com.hmkeyewear.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register-staff")
    public ResponseEntity<AuthResponseDto> registerStaff(
            @RequestBody RegisterStaffRequestDto dto,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Name") String createdBy)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            throw new RuntimeException("Only admin can register staff");
        }

        return ResponseEntity.ok(authService.registerStaff(dto, createdBy));
    }

    @PostMapping("/register-customer")
    public ResponseEntity<AuthResponseDto> registerCustomer(@RequestBody RegisterCustomerRequestDto dto)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(authService.registerCustomer(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto)
            throws ExecutionException, InterruptedException {

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        if (!authenticate.isAuthenticated()) {
            throw new RuntimeException("Invalid email or password");
        }

        // reuse login() to get token
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam String token) {
        authService.validateToken(token);
        return "Token is valid";
    }
}
