package com.hmkeyewear.auth_service.controller;

import com.hmkeyewear.auth_service.dto.*;
import com.hmkeyewear.auth_service.model.RefreshToken;
import com.hmkeyewear.auth_service.model.User;
import com.hmkeyewear.auth_service.service.*;
import com.hmkeyewear.auth_service.dto.ChangePasswordRequestDto;
import com.hmkeyewear.common_dto.dto.ForgotPasswordRequestDto;
import com.hmkeyewear.common_dto.dto.ResetPasswordRequestDto;
import com.hmkeyewear.common_dto.dto.VerifyOtpRequestDto;
import com.hmkeyewear.common_dto.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final JwtService jwtService;
    private final OtpService otpService;

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

        // Get user to revoke old token
        Optional<User> optionalUser = authService.findByEmail(loginRequestDto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();
        refreshTokenRedisService.revokeAllByUser(user.getUserId());

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
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response)
            throws ExecutionException, InterruptedException {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verify current refresh token
        RefreshToken token = refreshTokenRedisService.verify(refreshToken);

        User user = authService.findById(token.getUserId());

        // Rotate refresh token
        refreshTokenRedisService.logout(refreshToken);
        String newRefreshToken = refreshTokenRedisService.create(user.getUserId());

        String newAccessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                user.getStoreId());

        ResponseCookie cookie = ResponseCookie.from(
                "refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(newAccessToken);
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam String token) {
        authService.validateToken(token);
        return "Token is valid";
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) throws ExecutionException, InterruptedException {

        if (refreshToken != null) {
            refreshTokenRedisService.logout(refreshToken);
        }

        ResponseCookie clear = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clear.toString());
        return ResponseEntity.ok(
                new ApiResponse(true, "Logged out"));

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto)
            throws ExecutionException, InterruptedException {

        authService.forgotPasswordRequest(forgotPasswordRequestDto.getEmail());
        return ResponseEntity.ok(
                new ApiResponse(true, "Email has been sent"));

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(
            @RequestBody VerifyOtpRequestDto dto) {
        try {
            otpService.verifyOtp(dto.getEmail(), dto.getOtp());
            return ResponseEntity.ok(
                    new ApiResponse(true, "OTP verified"));
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestBody ResetPasswordRequestDto dto) {
        // Validate email
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Email is required"));
        }

        // Validate password
        if (dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password is required"));
        }

        // Password mismatch
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password confirmation does not match"));
        }

        // Password policy
        if (dto.getNewPassword().length() < 8) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password must be at least 8 characters"));
        }

        try {
            // 5. Reset password
            authService.resetPassword(
                    dto.getEmail(),
                    dto.getNewPassword());

            return ResponseEntity.ok(
                    new ApiResponse(true, "Password reset successfully"));

        } catch (RuntimeException ex) {
            // User not found, OTP chưa verify, v.v.
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage()));

        } catch (Exception ex) {
            // Unexpected error
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Internal server error"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestHeader("X-User-Name") String email,
            @RequestBody ChangePasswordRequestDto dto) {
        // Validate input
        if (dto.getOldPassword() == null || dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password is required"));
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password confirmation does not match"));
        }

        if (dto.getNewPassword().length() < 8) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Password must be at least 8 characters"));
        }

        try {
            authService.changePassword(
                    email,
                    dto.getOldPassword(),
                    dto.getNewPassword());

            return ResponseEntity.ok(
                    new ApiResponse(true, "Password changed successfully"));

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage()));

        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Internal server error"));
        }
    }
}
