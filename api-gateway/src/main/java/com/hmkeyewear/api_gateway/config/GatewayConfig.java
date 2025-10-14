package com.hmkeyewear.api_gateway.config;

// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class GatewayConfig {
// @Bean
// public RouteLocator routes(RouteLocatorBuilder builder) {
// return builder.routes()
// .route("auth-service", r -> r.path("/auth/**").uri("lb://auth-service"))
// .route("cart-service", r -> r.path("/cart/**").uri("lb://cart-service"))
// .route("order-service", r -> r.path("/order/**").uri("lb://order-service"))
// .route("blog-service", r -> r.path("/blog/**").uri("lb://blog-service"))
// .route("product-service", r ->
// r.path("/product/**").uri("lb://product-service"))
// .route("user-service", r -> r.path("/user/**").uri("lb://user-service"))
// .build();
// }
// }
