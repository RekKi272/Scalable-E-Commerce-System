package com.hmkeyewear.api_gateway.filter;

import com.hmkeyewear.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private final JwtUtil jwtUtil;

    // Endpoint allows public no need token
    private static final String[] OPEN_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "/auth/token",
            "/auth/validate",
            "/banner/active",
            "/blog/active",
            "/blog/get",
            "/eureka"
    };

    // Gateway-level authorization
    private static final Map<String, String[]> ROLE_ACCESS = Map.of(
            "/product/admin", new String[] { "ROLE_ADMIN" },
            "/product/employer", new String[] { "ROLE_EMPLOYER", "ROLE_ADMIN" },
            "/product/user", new String[] { "ROLE_USER", "ROLE_EMPLOYER", "ROLE_ADMIN" });

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip endpoint public
        for (String open : OPEN_ENDPOINTS) {
            if (path.startsWith(open)) {
                return chain.filter(exchange);
            }
        }

        // Take Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String role = jwtUtil.extractRole(token);
            String username = jwtUtil.extractUsername(token);
            String customerId = jwtUtil.extractCustomerId(token);
            String storeId = jwtUtil.extractStoreId(token);

            // Debug
            System.out.println("[GATEWAY] User: " + username + " | Role: " + role + " | Path: " + path);

            // Check if the role is allowed to access the route
            for (Map.Entry<String, String[]> entry : ROLE_ACCESS.entrySet()) {
                if (path.startsWith(entry.getKey())) {
                    boolean allowed = false;
                    for (String allowedRole : entry.getValue()) {
                        if (allowedRole.equalsIgnoreCase(role)) {
                            allowed = true;
                            break;
                        }
                    }
                    if (!allowed) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }
            }
            // Pass role & username information down to microservice (via header)
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.add("X-User-Id", customerId);
                        headers.add("X-User-Name", username);
                        headers.add("X-User-Role", role);
                        headers.add("X-User-StoreId", storeId != null ? storeId : "UNKNOWN");
                    }))
                    .build();

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
