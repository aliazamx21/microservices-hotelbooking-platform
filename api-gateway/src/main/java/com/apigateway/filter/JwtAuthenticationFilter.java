package com.api_gateway.filter;

import com.api_gateway.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    // Endpoints that bypass JWT verification
    private final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        boolean isSecured = openApiEndpoints.stream()
                .noneMatch(uri -> path.contains(uri));

        if (isSecured) {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            try {
                DecodedJWT decodedJWT = jwtUtil.validateToken(token);

                String username = decodedJWT.getSubject();
                String role = decodedJWT.getClaim("role").asString();

                ServerHttpRequest mutatedRequest = exchange.getRequest()
                        .mutate()
                        .header("X-Logged-In-User", username)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                logger.error("Invalid JWT token detected: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}