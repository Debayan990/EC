package com.cts.filter;

import com.cts.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthServiceClient authServiceClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip authentication for auth service endpoints
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        // Check if this is a core service request
        if (path.startsWith("/api/items") || path.startsWith("/api/inventory") || path.startsWith("/api/cart") || path.startsWith("/api/notifications") || path.startsWith("/api/logs")) {
            String token = getTokenFromRequest(request);

            if (!StringUtils.hasText(token)) {
                return onError(exchange, "No token provided", HttpStatus.UNAUTHORIZED);
            }

            // Validate token via REST API call to auth service
            return authServiceClient.validateTokenAndGetUserInfo(token)
                    .flatMap(userInfo -> {
                        if (userInfo == null || !userInfo.isValid()) {
                            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                        }

                        String method = request.getMethod().name();

                        boolean isAdmin = userInfo.getRoles().contains("ADMIN") ||
                                userInfo.getRoles().contains("ROLE_ADMIN");

                        // Audit Logs: STRICTLY ADMIN ONLY for ALL operations
                        if (path.startsWith("/api/logs")) {
                            if (!isAdmin) {
                                return onError(exchange, "Admin privileges required for Logs", HttpStatus.FORBIDDEN);
                            }
                        }

                        // Inventory, Items, Notifications: ADMIN required for State Changes (POST, PUT, DELETE)
                        else if ((path.startsWith("/api/items") || path.startsWith("/api/inventory") || path.startsWith("/api/notification")) &&
                                (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"))) {
                            if (!isAdmin) {
                                return onError(exchange, "Admin privileges required for Catalog management", HttpStatus.FORBIDDEN);
                            }
                        }

                        // Cart: No specific Admin check needed here.


                        // Add username and roles to request headers to pass
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-User-Name", userInfo.getUsername())
                                .header("X-User-Roles", String.join(",", userInfo.getRoles()))
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    })
                    .onErrorResume(throwable -> onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED));
        }

        return chain.filter(exchange);
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\":\"" + error + "\",\"status\":" + httpStatus.value() + "}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // High priority
    }
}
