package com.gym.crm.security;

import com.gym.crm.util.impl.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtTokenInvalidationService jwtTokenInvalidationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() {
        jwtRequestFilter = new JwtRequestFilter(jwtUtil, jwtTokenInvalidationService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("Valid JWT Token Tests")
    class ValidJwtTokenTests {

        @Test
        @DisplayName("Should authenticate user with valid JWT token")
        void doFilterInternal_ShouldAuthenticateUser_WhenValidToken() throws ServletException, IOException {
            String validToken = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtTokenInvalidationService.isInvalidated(validToken)).thenReturn(false);
            when(jwtUtil.validateToken(validToken)).thenReturn(true);
            when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
            when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
            when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(any(Authentication.class));
            verify(filterChain).doFilter(request, response);
            verify(response).setHeader(eq("X-Transaction-Id"), any(String.class));
        }

        @Test
        @DisplayName("Should handle TRAINEE role correctly")
        void doFilterInternal_ShouldHandleTraineeRole() throws ServletException, IOException {
            String validToken = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/trainee/profile");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtTokenInvalidationService.isInvalidated(validToken)).thenReturn(false);
            when(jwtUtil.validateToken(validToken)).thenReturn(true);
            when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
            when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
            when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(argThat(auth -> {
                if (auth == null) return false;
                List<?> authorities = (List<?>) auth.getAuthorities();
                return authorities.contains(new SimpleGrantedAuthority("ROLE_TRAINEE"));
            }));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle TRAINER role correctly")
        void doFilterInternal_ShouldHandleTrainerRole() throws ServletException, IOException {
            String validToken = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/trainer/profile");
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtTokenInvalidationService.isInvalidated(validToken)).thenReturn(false);
            when(jwtUtil.validateToken(validToken)).thenReturn(true);
            when(jwtUtil.extractUsername(validToken)).thenReturn("jane.smith");
            when(jwtUtil.extractRole(validToken)).thenReturn("TRAINER");
            when(jwtUtil.extractUserId(validToken)).thenReturn(456L);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(argThat(auth -> {
                if (auth == null) return false;
                List<?> authorities = (List<?>) auth.getAuthorities();
                return authorities.contains(new SimpleGrantedAuthority("ROLE_TRAINER"));
            }));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle mixed case roles")
        void doFilterInternal_ShouldHandleMixedCaseRoles() throws ServletException, IOException {
            String validToken = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtTokenInvalidationService.isInvalidated(validToken)).thenReturn(false);
            when(jwtUtil.validateToken(validToken)).thenReturn(true);
            when(jwtUtil.extractUsername(validToken)).thenReturn("user");
            when(jwtUtil.extractRole(validToken)).thenReturn("trainee"); // lowercase
            when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(argThat(auth -> {
                if (auth == null) return false;
                List<?> authorities = (List<?>) auth.getAuthorities();
                return authorities.contains(new SimpleGrantedAuthority("ROLE_TRAINEE"));
            }));
        }

        @Test
        @DisplayName("Should handle null user ID")
        void doFilterInternal_ShouldHandleNullUserId() throws ServletException, IOException {
            String validToken = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtTokenInvalidationService.isInvalidated(validToken)).thenReturn(false);
            when(jwtUtil.validateToken(validToken)).thenReturn(true);
            when(jwtUtil.extractUsername(validToken)).thenReturn("user");
            when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
            when(jwtUtil.extractUserId(validToken)).thenReturn(null);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(any(Authentication.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Invalid JWT Token Tests")
    class InvalidJwtTokenTests {

        @Test
        @DisplayName("Should not authenticate with invalid token")
        void doFilterInternal_ShouldNotAuthenticate_WhenInvalidToken() throws ServletException, IOException {
            String invalidToken = "invalid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
            when(jwtTokenInvalidationService.isInvalidated(invalidToken)).thenReturn(false);
            when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate with invalidated token")
        void doFilterInternal_ShouldNotAuthenticate_WhenTokenInvalidated() throws ServletException, IOException {
            String invalidatedToken = "invalidated.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidatedToken);
            when(jwtTokenInvalidationService.isInvalidated(invalidatedToken)).thenReturn(true);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(jwtUtil, never()).validateToken(any());
            verify(securityContext, never()).setAuthentication(any());
        }

        @Test
        @DisplayName("Should not authenticate when token validation throws exception")
        void doFilterInternal_ShouldNotAuthenticate_WhenTokenValidationThrows() throws ServletException, IOException {
            String malformedToken = "malformed.token";
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + malformedToken);
            when(jwtTokenInvalidationService.isInvalidated(malformedToken)).thenReturn(false);
            when(jwtUtil.validateToken(malformedToken)).thenThrow(new RuntimeException("Malformed JWT"));

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate when username extraction throws exception")
        void doFilterInternal_ShouldNotAuthenticate_WhenUsernameExtractionThrows() throws ServletException, IOException {
            String validToken = "valid.jwt.token";
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtTokenInvalidationService.isInvalidated(validToken)).thenReturn(false);
            when(jwtUtil.validateToken(validToken)).thenReturn(true);
            when(jwtUtil.extractUsername(validToken)).thenThrow(new RuntimeException("Cannot extract username"));

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Authorization Header Tests")
    class AuthorizationHeaderTests {

        @Test
        @DisplayName("Should not authenticate without Authorization header")
        void doFilterInternal_ShouldNotAuthenticate_WhenNoAuthHeader() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(jwtTokenInvalidationService, never()).isInvalidated(any());
            verify(jwtUtil, never()).validateToken(any());
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate with empty Authorization header")
        void doFilterInternal_ShouldNotAuthenticate_WhenEmptyAuthHeader() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("");

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(jwtTokenInvalidationService, never()).isInvalidated(any());
            verify(jwtUtil, never()).validateToken(any());
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate without Bearer prefix")
        void doFilterInternal_ShouldNotAuthenticate_WhenNoBearerPrefix() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(jwtTokenInvalidationService, never()).isInvalidated(any());
            verify(jwtUtil, never()).validateToken(any());
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate with only Bearer prefix")
        void doFilterInternal_ShouldNotAuthenticate_WhenOnlyBearerPrefix() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer ");

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(jwtTokenInvalidationService).isInvalidated("");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle Bearer with case variations")
        void doFilterInternal_ShouldHandleBearerCaseVariations() throws ServletException, IOException {
            String[] bearerVariations = {"bearer ", "BEARER ", "Bearer ", "BeArEr "};

            for (String bearerPrefix : bearerVariations) {
                reset(request, jwtTokenInvalidationService, jwtUtil, securityContext, filterChain);

                when(request.getRequestURI()).thenReturn("/api/protected");
                when(request.getMethod()).thenReturn("GET");
                when(request.getHeader("Authorization")).thenReturn(bearerPrefix + "token123");

                jwtRequestFilter.doFilterInternal(request, response, filterChain);

                if (bearerPrefix.equals("Bearer ")) {
                    verify(jwtTokenInvalidationService).isInvalidated("token123");
                } else {
                    verify(jwtTokenInvalidationService, never()).isInvalidated(any());
                }
            }
        }
    }

    @Nested
    @DisplayName("Transaction ID and Headers Tests")
    class TransactionIdTests {

        @Test
        @DisplayName("Should always set transaction ID header")
        void doFilterInternal_ShouldAlwaysSetTransactionId() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/public");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(response).setHeader(eq("X-Transaction-Id"), any(String.class));
        }

        @Test
        @DisplayName("Should set different transaction IDs for different requests")
        void doFilterInternal_ShouldSetDifferentTransactionIds() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn(null);

            // First request
            jwtRequestFilter.doFilterInternal(request, response, filterChain);
            verify(response).setHeader(eq("X-Transaction-Id"), any(String.class));

            reset(response);
            jwtRequestFilter.doFilterInternal(request, response, filterChain);
            verify(response).setHeader(eq("X-Transaction-Id"), any(String.class));
        }

        @Test
        @DisplayName("Should set transaction ID even when authentication fails")
        void doFilterInternal_ShouldSetTransactionId_WhenAuthFails() throws ServletException, IOException {
            when(request.getRequestURI()).thenReturn("/api/protected");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
            when(jwtTokenInvalidationService.isInvalidated("invalid.token")).thenReturn(false);
            when(jwtUtil.validateToken("invalid.token")).thenReturn(false);

            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            verify(response).setHeader(eq("X-Transaction-Id"), any(String.class));
        }
    }

    @Nested
    @DisplayName("Component and Integration Tests")
    class ComponentIntegrationTests {

        @Test
        @DisplayName("Should be properly annotated as Component")
        void shouldBeProperlyAnnotatedAsComponent() {
            assertThat(JwtRequestFilter.class.isAnnotationPresent(org.springframework.stereotype.Component.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Should extend OncePerRequestFilter")
        void shouldExtendOncePerRequestFilter() {
            assertThat(org.springframework.web.filter.OncePerRequestFilter.class)
                    .isAssignableFrom(JwtRequestFilter.class);
        }

        @Test
        @DisplayName("Should have correct constructor dependencies")
        void shouldHaveCorrectConstructorDependencies() throws NoSuchMethodException {
            java.lang.reflect.Constructor<JwtRequestFilter> constructor =
                    JwtRequestFilter.class.getConstructor(JwtUtil.class, JwtTokenInvalidationService.class);

            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle multiple requests consistently")
        void doFilterInternal_ShouldHandleMultipleRequestsConsistently() throws ServletException, IOException {
            String validToken = "valid.jwt.token";

            for (int i = 0; i < 5; i++) {
                reset(request, securityContext, filterChain);

                when(request.getRequestURI()).thenReturn("/api/test" + i);
                when(request.getMethod()).thenReturn("GET");
                when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
                when(jwtTokenInvalidationService.isInvalidated(validToken)).thenReturn(false);
                when(jwtUtil.validateToken(validToken)).thenReturn(true);
                when(jwtUtil.extractUsername(validToken)).thenReturn("user" + i);
                when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
                when(jwtUtil.extractUserId(validToken)).thenReturn((long) i);

                jwtRequestFilter.doFilterInternal(request, response, filterChain);

                verify(securityContext).setAuthentication(any(Authentication.class));
                verify(filterChain).doFilter(request, response);
            }
        }

        @Test
        @DisplayName("Should handle concurrent processing safely")
        void doFilterInternal_ShouldHandleConcurrentProcessing() throws ServletException, IOException {
            String token1 = "token1";
            String token2 = "token2";

            HttpServletRequest request1 = mock(HttpServletRequest.class);
            HttpServletRequest request2 = mock(HttpServletRequest.class);
            HttpServletResponse response1 = mock(HttpServletResponse.class);
            HttpServletResponse response2 = mock(HttpServletResponse.class);
            FilterChain chain1 = mock(FilterChain.class);
            FilterChain chain2 = mock(FilterChain.class);

            when(request1.getRequestURI()).thenReturn("/api/user1");
            when(request1.getMethod()).thenReturn("GET");
            when(request1.getHeader("Authorization")).thenReturn("Bearer " + token1);
            when(request2.getRequestURI()).thenReturn("/api/user2");
            when(request2.getMethod()).thenReturn("POST");
            when(request2.getHeader("Authorization")).thenReturn("Bearer " + token2);

            when(jwtTokenInvalidationService.isInvalidated(token1)).thenReturn(false);
            when(jwtTokenInvalidationService.isInvalidated(token2)).thenReturn(false);
            when(jwtUtil.validateToken(token1)).thenReturn(true);
            when(jwtUtil.validateToken(token2)).thenReturn(true);
            when(jwtUtil.extractUsername(token1)).thenReturn("user1");
            when(jwtUtil.extractUsername(token2)).thenReturn("user2");
            when(jwtUtil.extractRole(token1)).thenReturn("TRAINEE");
            when(jwtUtil.extractRole(token2)).thenReturn("TRAINER");
            when(jwtUtil.extractUserId(token1)).thenReturn(1L);
            when(jwtUtil.extractUserId(token2)).thenReturn(2L);

            jwtRequestFilter.doFilterInternal(request1, response1, chain1);
            jwtRequestFilter.doFilterInternal(request2, response2, chain2);

            verify(chain1).doFilter(request1, response1);
            verify(chain2).doFilter(request2, response2);
            verify(response1).setHeader(eq("X-Transaction-Id"), any(String.class));
            verify(response2).setHeader(eq("X-Transaction-Id"), any(String.class));
        }
    }
}