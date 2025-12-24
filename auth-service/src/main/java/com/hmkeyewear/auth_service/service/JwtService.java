package com.hmkeyewear.auth_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_MINUTES = 15;

    private Claims extractAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }



    public void validateToken(final String token) {
        Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
    }

    /* ================= ACCESS TOKEN ================= */

    public String generateAccessToken(String userId, String userName, String role, String storeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("storeId", storeId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + 1000 * 60 * ACCESS_TOKEN_MINUTES)) // Access Token only last 15 minutes
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* ================= REFRESH TOKEN ================= */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /* ================= PARSE ================= */
    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String extractUsername(String token) {
        return extractAllClaimsFromToken(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaimsFromToken(token).get("role", String.class);
    }

    public String extractUserId(String token) {
        return extractAllClaimsFromToken(token).get("userId", String.class);
    }
}
