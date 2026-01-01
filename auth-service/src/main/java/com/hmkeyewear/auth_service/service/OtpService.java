package com.hmkeyewear.auth_service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class OtpService {

    private static final int OTP_TTL_SECONDS = 60; // 5 minutes
    private static final String PREFIX = "otp:forgot:";

    private final StringRedisTemplate redisTemplate;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* ========== Generate OTP ========== */
    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        redisTemplate.opsForValue().set(
                PREFIX + email,
                otp,
                Duration.ofSeconds(OTP_TTL_SECONDS)
        );

        return otp;
    }

    /* ========== Verify OTP ========== */
    public void verifyOtp(String email, String otp) {
        String key = PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        if (!storedOtp.equals(otp)) {
            throw new RuntimeException("OTP invalid");
        }

        // One-time OTP - delete after verify
        redisTemplate.delete(key);
    }
}
