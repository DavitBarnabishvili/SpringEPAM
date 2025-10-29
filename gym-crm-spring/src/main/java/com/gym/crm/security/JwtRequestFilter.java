package com.gym.crm.security;

import com.gym.crm.util.impl.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUtil jwtUtil;
    private final JwtTokenInvalidationService jwtTokenInvalidationService;

    public JwtRequestFilter(JwtUtil jwtUtil, JwtTokenInvalidationService jwtTokenInvalidationService) {
        this.jwtUtil = jwtUtil;
        this.jwtTokenInvalidationService = jwtTokenInvalidationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        response.setHeader("X-Transaction-Id", transactionId);

        String requestPath = request.getRequestURI();
        logger.debug("Processing request: {} {}", request.getMethod(), requestPath);

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    if (jwtTokenInvalidationService.isInvalidated(token)) {
                        logger.warn("Attempt to use invalidated token for path: {}", requestPath);
                        return;
                    }

                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.extractUsername(token);
                        String role = jwtUtil.extractRole(token);
                        Long userId = jwtUtil.extractUserId(token);

                        logger.debug("Valid JWT token for user: {} with role: {}", username, role);

                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                        );

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        request.setAttribute("authenticatedUsername", username);
                        request.setAttribute("authenticatedUserId", userId);
                        request.setAttribute("authenticatedUserRole", role);

                    } else {
                        logger.warn("Invalid JWT token for path: {}", requestPath);
                    }

                } catch (Exception e) {
                    logger.error("Error processing JWT token for path: {}", requestPath, e);
                }
            }

        } catch (Exception e) {
            logger.error("Error in JWT filter for path: {}", requestPath, e);
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/trainees/register") ||
                path.startsWith("/api/trainers/register") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/training-types") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                path.startsWith("/management") ||
                path.startsWith("/metrics-dashboard") ||
                path.startsWith("/h2-console");
    }
}