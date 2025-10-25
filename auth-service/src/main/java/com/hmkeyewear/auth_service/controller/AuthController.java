package com.hmkeyewear.auth_service.controller;

import com.hmkeyewear.auth_service.dto.AuthResponseDto;
import com.hmkeyewear.auth_service.dto.LoginRequestDto;
import com.hmkeyewear.auth_service.dto.RegisterRequestDto;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto registerRequestDto)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(authService.register(registerRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/token")
    public String getToken(@RequestBody LoginRequestDto loginRequestDto)
            throws ExecutionException, InterruptedException {

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        if (authenticate.isAuthenticated()) {
            // Lấy thông tin customer từ Firestore
            var customer = authService.findByEmail(loginRequestDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Gọi generateToken 3 tham số: email, role, storeId
            return authService.generateToken(
                    customer.getCustomerId(),
                    loginRequestDto.getEmail(),
                    authService.getRoleFromEmail(loginRequestDto.getEmail()),
                    customer.getStoreId());
        } else {
            throw new RuntimeException("invalid access");
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam String token) {
        authService.validateToken(token);
        return "Token is valid";
    }
}
