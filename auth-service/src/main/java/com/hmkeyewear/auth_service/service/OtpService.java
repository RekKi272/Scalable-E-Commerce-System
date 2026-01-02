package com.hmkeyewear.auth_service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class OtpService {

    private static final int OTP_TTL_SECONDS = 60; // 1 minutes
    private static final int VERIFIED_TTL_SECONDS = 600; // 10 minutes

    private static final String OTP_PREFIX = "otp:forgot:";
    private static final String VERIFIED_PREFIX = "otp:verified:";

    private final StringRedisTemplate redisTemplate;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* ========== Generate OTP ========== */
    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otp,
                Duration.ofSeconds(OTP_TTL_SECONDS)
        );

        return otp;
    }

    /* ========== Verify OTP ========== */
    public void verifyOtp(String email, String otp) {
        String otpKey = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        if (!storedOtp.equals(otp)) {
            throw new RuntimeException("OTP invalid");
        }

        // Mark OTP as verified
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + email,
                "true",
                Duration.ofSeconds(VERIFIED_TTL_SECONDS)
        );

        // Remove OTP (one-time use)
        redisTemplate.delete(otpKey);
    }

    /* ========== Check OTP Verified ========== */
    public boolean isOtpVerified(String email) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(VERIFIED_PREFIX + email)
        );
    }

    /* ========== Clear OTP Verified (After Reset Password) ========== */
    public void clearOtpVerified(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }
}
