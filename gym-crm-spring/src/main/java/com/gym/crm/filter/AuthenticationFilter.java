package com.gym.crm.filter;

import com.gym.crm.util.impl.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Order(1)
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    // Endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/trainees/register",
            "/api/trainers/register",
            "/api/auth/login",
            "/api/auth/change-password", // This uses its own auth via request body
            "/api/training-types",
            "/swagger-ui",
            "/api-docs",
            "/v3/api-docs",
            "/h2-console"
    );

    public AuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        httpResponse.setHeader("X-Transaction-Id", transactionId);

        logger.debug("Request: {} {}", httpRequest.getMethod(), path);

        try {
            if (requiresAuthentication(path)) {
                String authHeader = httpRequest.getHeader("Authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    logger.warn("Missing or invalid Authorization header for: {}", path);
                    sendUnauthorizedResponse(httpResponse, "Authentication required");
                    return;
                }

                String token = authHeader.substring(7);

                if (!jwtUtil.validateToken(token)) {
                    logger.warn("Invalid JWT token for path: {}", path);
                    sendUnauthorizedResponse(httpResponse, "Invalid or expired token");
                    return;
                }

                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);

                // Set attributes that controllers can access
                httpRequest.setAttribute("authenticatedUsername", username);
                httpRequest.setAttribute("authenticatedRole", role);
                httpRequest.setAttribute("authenticatedUserId", userId);

                logger.debug("User authenticated via JWT: {} ({}) with ID: {}", username, role, userId);
            }

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }

    private boolean requiresAuthentication(String path) {
        return PUBLIC_ENDPOINTS.stream().noneMatch(path::startsWith);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                java.time.Instant.now(), message
        ));
    }
}