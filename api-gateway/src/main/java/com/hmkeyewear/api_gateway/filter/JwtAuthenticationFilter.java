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
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    // Dùng constructor injection với @RequiredArgsConstructor, không @Autowired
    private final JwtUtil jwtUtil;

    // Các endpoint public không cần token
    private static final String[] OPEN_ENDPOINTS = {
            "/auth/login",
            "/auth/register-customer",
            "/auth/token",
            "/auth/validate",
            "/auth/refresh",
            "/banner/active",
            "/blog/active",
            "/blog/get",
            "/eureka"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        // Skip public endpoints
        for (String open : OPEN_ENDPOINTS) {
            if (path.startsWith(open)) {
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
