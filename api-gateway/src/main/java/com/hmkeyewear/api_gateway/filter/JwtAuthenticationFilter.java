package com.hmkeyewear.api_gateway.filter;

import com.hmkeyewear.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    // Dùng constructor injection với @RequiredArgsConstructor, không @Autowired
    private final JwtUtil jwtUtil;
    private final AntPathMatcher matcher = new AntPathMatcher();

    // Các endpoint public không cần token
    private static final String[] OPEN_ENDPOINTS = {
            "/eureka",

            "/auth/login",
            "/auth/refresh",
            "/auth/register-customer",
            "/auth/forgot-password",
            "/auth/verify-otp",
            "/auth/reset-password",

            "/payment/vn-pay-ipn",
            "/payment/vn-pay-callback",

            "/product/get",
            "/product/getActive",
            "/product/search",
            "/product/filter",

            "/category/getAll",
            "/brand/getAll",

            "/banner/active",
            "/blog/active",
            "/blog/get/**",

            "/discount/get",
            "/discount/get/*",
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        // Skip public endpoints
        for (String open : OPEN_ENDPOINTS) {
            if (matcher.match(open, path)) {
                System.out.println("[GATEWAY] Public endpoint, skipping JWT filter: " + path);
                return chain.filter(exchange);
            }
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[GATEWAY] Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                System.out.println("[GATEWAY] Invalid JWT token");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extract info
            String role = jwtUtil.extractRole(token);
            String username = jwtUtil.extractUsername(token);
            String userId = jwtUtil.extractUserId(token);
            String storeId = jwtUtil.extractStoreId(token);

            System.out.println("[GATEWAY] User: " + username + " | Role: " + role + " | Path: " + path);

            // Pass headers to downstream service
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.set("X-User-Id", userId);
                        headers.set("X-User-Name", username);
                        headers.set("X-User-Role", role);
                        headers.set("X-User-StoreId", storeId != null ? storeId : "UNKNOWN");
                    }))
                    .build();

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            System.out.println("[GATEWAY] Exception in JWT filter: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Chạy trước các filter khác
    }
}
