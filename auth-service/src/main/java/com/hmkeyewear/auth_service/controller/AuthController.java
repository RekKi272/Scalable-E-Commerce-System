package com.hmkeyewear.auth_service.controller;

import com.hmkeyewear.auth_service.dto.AuthResponseDto;
import com.hmkeyewear.auth_service.dto.LoginRequestDto;
import com.hmkeyewear.auth_service.dto.RegisterCustomerRequestDto;
import com.hmkeyewear.auth_service.dto.RegisterStaffRequestDto;
import com.hmkeyewear.auth_service.model.RefreshToken;
import com.hmkeyewear.auth_service.model.User;
import com.hmkeyewear.auth_service.service.AuthService;
import com.hmkeyewear.auth_service.service.JwtService;
import com.hmkeyewear.auth_service.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

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
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response)
            throws ExecutionException, InterruptedException {

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        if (!authenticate.isAuthenticated()) {
            throw new RuntimeException("Invalid email or password");
        }

        // Auth Service create accessToken and refreshToken
        AuthResponseDto authResponseDto = authService.login(loginRequestDto);

        // Set refreshToken into HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refresh_token", authResponseDto.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        // Secure refresh token
        authResponseDto.setRefreshToken(null);

        return ResponseEntity.ok(authResponseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false)
            String refreshToken)
        throws ExecutionException, InterruptedException {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verify + rotate refresh token
        RefreshToken token = refreshTokenService.verify(refreshToken);

        User user = authService.findById(token.getUserId());

        String newAccessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                user.getStoreId()
        );

        return ResponseEntity.ok(newAccessToken);
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam String token) {
        authService.validateToken(token);
        return "Token is valid";
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false)
            String refreshToken,
            HttpServletResponse response
    ) throws ExecutionException, InterruptedException {

        if (refreshToken != null) {
            refreshTokenService.logout(refreshToken);
        }

        ResponseCookie clear = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clear.toString());
        return ResponseEntity.ok("Logged out");
    }
}
