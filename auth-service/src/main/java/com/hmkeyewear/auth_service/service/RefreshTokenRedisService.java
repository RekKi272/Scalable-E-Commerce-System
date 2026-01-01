package com.hmkeyewear.auth_service.service;

import com.hmkeyewear.auth_service.model.RefreshToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenRedisService {
    private static final int EXPIRE_DAYS = 7;
    private final StringRedisTemplate redisTemplate;

    public RefreshTokenRedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* ========== HASH TOKEN ========== */
    private String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }

    /* ========== LOGIN: revoke toàn bộ token cũ ========= */
    public void revokeAllByUser(String userId) {

        String userKey = "user_refresh:" + userId;

        Set<String> tokens = redisTemplate.opsForSet().members(userKey);
        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(t ->
                    redisTemplate.delete("refresh:" + t)
            );
        }

        redisTemplate.delete(userKey);
    }

    /* ========== CREATE REFRESH TOKEN ========= */
    public String create(String userId) {

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);

        redisTemplate.opsForValue().set(
                "refresh:" + tokenHash,
                userId,
                EXPIRE_DAYS,
                TimeUnit.DAYS
        );

        redisTemplate.opsForSet().add(
                "user_refresh:" + userId,
                tokenHash
        );

        return rawToken;
    }

    /* ========== VALIDATE REFRESH TOKEN ========= */
    public RefreshToken verify(String rawToken) {

        String tokenHash = hash(rawToken);

        String userId = redisTemplate.opsForValue()
                .get("refresh:" + tokenHash);

        if (userId == null) {
            throw new RuntimeException("Refresh token invalid or expired");
        }

        return RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .build();
    }

    /* ========== LOGOUT ========= */
    public void logout(String rawToken) {

        String tokenHash = hash(rawToken);
        String refreshKey = "refresh:" + tokenHash;

        String userId = redisTemplate.opsForValue().get(refreshKey);
        if (userId != null) {
            redisTemplate.delete(refreshKey);
            redisTemplate.opsForSet()
                    .remove("user_refresh:" + userId, tokenHash);
        }
    }
}
