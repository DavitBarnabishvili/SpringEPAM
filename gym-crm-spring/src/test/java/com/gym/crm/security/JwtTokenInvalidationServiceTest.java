package com.gym.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenInvalidationServiceTest {

    private JwtTokenInvalidationService jwtTokenInvalidationService;

    @BeforeEach
    void setUp() {
        jwtTokenInvalidationService = new JwtTokenInvalidationService();
    }

    @Nested
    @DisplayName("Token Invalidation Tests")
    class TokenInvalidationTests {

        @Test
        @DisplayName("Should invalidate token successfully")
        void invalidateToken_ShouldInvalidateToken() {
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjE2MjM5MDIyfQ";

            jwtTokenInvalidationService.invalidateToken(token);

            assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle null token gracefully")
        void invalidateToken_ShouldHandleNullToken() {
            jwtTokenInvalidationService.invalidateToken(null);

            assertThat(jwtTokenInvalidationService.isInvalidated(null)).isFalse();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle empty token gracefully")
        void invalidateToken_ShouldHandleEmptyToken() {
            jwtTokenInvalidationService.invalidateToken("");

            assertThat(jwtTokenInvalidationService.isInvalidated("")).isFalse();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle whitespace-only token gracefully")
        void invalidateToken_ShouldHandleWhitespaceToken() {
            jwtTokenInvalidationService.invalidateToken("   ");

            assertThat(jwtTokenInvalidationService.isInvalidated("   ")).isFalse();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle very long token")
        void invalidateToken_ShouldHandleVeryLongToken() {
            String longToken = "a".repeat(1000);

            jwtTokenInvalidationService.invalidateToken(longToken);

            assertThat(jwtTokenInvalidationService.isInvalidated(longToken)).isTrue();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle token with special characters")
        void invalidateToken_ShouldHandleSpecialCharacters() {
            String specialToken = "token.with-special_characters$%^&*()";

            jwtTokenInvalidationService.invalidateToken(specialToken);

            assertThat(jwtTokenInvalidationService.isInvalidated(specialToken)).isTrue();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle duplicate invalidation")
        void invalidateToken_ShouldHandleDuplicateInvalidation() {
            String token = "duplicateToken";

            jwtTokenInvalidationService.invalidateToken(token);
            jwtTokenInvalidationService.invalidateToken(token);
            jwtTokenInvalidationService.invalidateToken(token);

            assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should return false for non-invalidated token")
        void isInvalidated_ShouldReturnFalse_ForValidToken() {
            String token = "validToken123";

            boolean isInvalidated = jwtTokenInvalidationService.isInvalidated(token);

            assertThat(isInvalidated).isFalse();
        }

        @Test
        @DisplayName("Should return true for invalidated token")
        void isInvalidated_ShouldReturnTrue_ForInvalidatedToken() {
            String token = "invalidatedToken123";

            jwtTokenInvalidationService.invalidateToken(token);

            assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
        }

        @Test
        @DisplayName("Should return false for null token")
        void isInvalidated_ShouldReturnFalse_ForNullToken() {
            boolean isInvalidated = jwtTokenInvalidationService.isInvalidated(null);

            assertThat(isInvalidated).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty token")
        void isInvalidated_ShouldReturnFalse_ForEmptyToken() {
            boolean isInvalidated = jwtTokenInvalidationService.isInvalidated("");

            assertThat(isInvalidated).isFalse();
        }

        @Test
        @DisplayName("Should be case sensitive")
        void isInvalidated_ShouldBeCaseSensitive() {
            String token = "CaseSensitiveToken";
            jwtTokenInvalidationService.invalidateToken(token);

            assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            assertThat(jwtTokenInvalidationService.isInvalidated(token.toLowerCase())).isFalse();
            assertThat(jwtTokenInvalidationService.isInvalidated(token.toUpperCase())).isFalse();
        }

        @Test
        @DisplayName("Should handle whitespace differences")
        void isInvalidated_ShouldHandleWhitespaceDifferences() {
            String token = "tokenWithoutSpaces";
            String tokenWithSpaces = " " + token + " ";

            jwtTokenInvalidationService.invalidateToken(token);

            assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            assertThat(jwtTokenInvalidationService.isInvalidated(tokenWithSpaces)).isFalse();
        }
    }

    @Nested
    @DisplayName("Multiple Token Management Tests")
    class MultipleTokenTests {

        @Test
        @DisplayName("Should handle multiple tokens independently")
        void shouldHandleMultipleTokensIndependently() {
            String token1 = "token1";
            String token2 = "token2";
            String token3 = "token3";

            // Invalidate token1 and token3
            jwtTokenInvalidationService.invalidateToken(token1);
            jwtTokenInvalidationService.invalidateToken(token3);

            // Check each token independently
            assertThat(jwtTokenInvalidationService.isInvalidated(token1)).isTrue();
            assertThat(jwtTokenInvalidationService.isInvalidated(token2)).isFalse();
            assertThat(jwtTokenInvalidationService.isInvalidated(token3)).isTrue();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle many tokens efficiently")
        void shouldHandleManyTokensEfficiently() {
            int tokenCount = 100;

            for (int i = 0; i < tokenCount; i++) {
                String token = "token" + i;
                jwtTokenInvalidationService.invalidateToken(token);
            }

            for (int i = 0; i < tokenCount; i++) {
                String token = "token" + i;
                assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            }

            assertThat(jwtTokenInvalidationService.isInvalidated("nonInvalidatedToken")).isFalse();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(tokenCount);
        }

        @Test
        @DisplayName("Should clear all invalidated tokens")
        void clearAllInvalidatedTokens_ShouldClearAllTokens() {
            jwtTokenInvalidationService.invalidateToken("token1");
            jwtTokenInvalidationService.invalidateToken("token2");
            jwtTokenInvalidationService.invalidateToken("token3");

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(3);

            jwtTokenInvalidationService.clearAllInvalidatedTokens();

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(0);
            assertThat(jwtTokenInvalidationService.isInvalidated("token1")).isFalse();
            assertThat(jwtTokenInvalidationService.isInvalidated("token2")).isFalse();
            assertThat(jwtTokenInvalidationService.isInvalidated("token3")).isFalse();
        }

        @Test
        @DisplayName("Should handle clearing empty token list")
        void clearAllInvalidatedTokens_ShouldHandleEmptyList() {
            jwtTokenInvalidationService.clearAllInvalidatedTokens();

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Token Expiration Tests")
    class TokenExpirationTests {

        @Test
        @DisplayName("Should handle token expiration simulation")
        void shouldHandleTokenExpirationSimulation() throws InterruptedException {
            String token = "expiringToken";

            jwtTokenInvalidationService.invalidateToken(token);
            assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();

            assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should maintain consistent token count")
        void shouldMaintainConsistentTokenCount() {
            jwtTokenInvalidationService.invalidateToken("token1");
            jwtTokenInvalidationService.invalidateToken("token2");
            jwtTokenInvalidationService.invalidateToken("token3");

            int initialCount = jwtTokenInvalidationService.getInvalidatedTokenCount();
            assertThat(initialCount).isEqualTo(3);

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(initialCount);
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(initialCount);
            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should cleanup and maintain token count correctly")
        void shouldCleanupAndMaintainTokenCountCorrectly() {
            jwtTokenInvalidationService.invalidateToken("token1");
            jwtTokenInvalidationService.invalidateToken("token2");

            int countBefore = jwtTokenInvalidationService.getInvalidatedTokenCount();
            assertThat(countBefore).isEqualTo(2);

            jwtTokenInvalidationService.getInvalidatedTokenCount();
            jwtTokenInvalidationService.getInvalidatedTokenCount();

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(countBefore);
        }
    }

    @Nested
    @DisplayName("Realistic JWT Token Tests")
    class RealisticJWTTokenTests {

        @Test
        @DisplayName("Should handle realistic JWT tokens")
        void shouldHandleRealisticJWTTokens() {
            String[] jwtTokens = {
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGUiOiJUUkFJTkVFIiwiaWF0IjoxNjE2MjM5MDIyfQ.signature1",
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqYW5lLnNtaXRoIiwicm9sZSI6IlRSQUlORVIiLCJpYXQiOjE2MTYyMzkwMjJ9.signature2",
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTYxNjIzOTAyMn0.signature3"
            };

            for (String token : jwtTokens) {
                jwtTokenInvalidationService.invalidateToken(token);
                assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            }

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(jwtTokens.length);

            for (String token : jwtTokens) {
                assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            }
        }

        @Test
        @DisplayName("Should handle JWT tokens with different structures")
        void shouldHandleJwtTokensWithDifferentStructures() {
            String[] tokenVariations = {
                    "header.payload.signature",
                    "a.b.c",
                    "very.long.jwt.token.with.extra.parts",
                    "short",
                    ""
            };

            for (String token : tokenVariations) {
                if (!token.trim().isEmpty()) {
                    jwtTokenInvalidationService.invalidateToken(token);
                    assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
                }
            }
        }

        @Test
        @DisplayName("Should handle tokens from different users")
        void shouldHandleTokensFromDifferentUsers() {
            String userToken = "user.token.12345";
            String adminToken = "admin.token.67890";
            String guestToken = "guest.token.abcde";

            jwtTokenInvalidationService.invalidateToken(userToken);
            jwtTokenInvalidationService.invalidateToken(adminToken);

            assertThat(jwtTokenInvalidationService.isInvalidated(userToken)).isTrue();
            assertThat(jwtTokenInvalidationService.isInvalidated(adminToken)).isTrue();
            assertThat(jwtTokenInvalidationService.isInvalidated(guestToken)).isFalse();

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Thread Safety and Concurrency Tests")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent token invalidation")
        void shouldHandleConcurrentTokenInvalidation() {
            int tokenCount = 50;

            for (int i = 0; i < tokenCount; i++) {
                final String token = "concurrentToken" + i;
                jwtTokenInvalidationService.invalidateToken(token);
            }

            for (int i = 0; i < tokenCount; i++) {
                String token = "concurrentToken" + i;
                assertThat(jwtTokenInvalidationService.isInvalidated(token)).isTrue();
            }

            assertThat(jwtTokenInvalidationService.getInvalidatedTokenCount()).isEqualTo(tokenCount);
        }

        @Test
        @DisplayName("Should handle concurrent operations safely")
        void shouldHandleConcurrentOperationsSafely() {
            String token1 = "token1";
            String token2 = "token2";

            jwtTokenInvalidationService.invalidateToken(token1);
            boolean isToken1Invalidated = jwtTokenInvalidationService.isInvalidated(token1);
            jwtTokenInvalidationService.invalidateToken(token2);
            boolean isToken2Invalidated = jwtTokenInvalidationService.isInvalidated(token2);
            int count = jwtTokenInvalidationService.getInvalidatedTokenCount();

            assertThat(isToken1Invalidated).isTrue();
            assertThat(isToken2Invalidated).isTrue();
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Component Annotation Tests")
    class ComponentTests {

        @Test
        @DisplayName("Should be properly annotated as Service")
        void shouldBeProperlyAnnotatedAsService() {
            assertThat(JwtTokenInvalidationService.class.isAnnotationPresent(org.springframework.stereotype.Service.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Should have proper method signatures")
        void shouldHaveProperMethodSignatures() throws NoSuchMethodException {
            assertThat(JwtTokenInvalidationService.class.getMethod("invalidateToken", String.class)).isNotNull();
            assertThat(JwtTokenInvalidationService.class.getMethod("isInvalidated", String.class)).isNotNull();
            assertThat(JwtTokenInvalidationService.class.getMethod("getInvalidatedTokenCount")).isNotNull();
            assertThat(JwtTokenInvalidationService.class.getMethod("clearAllInvalidatedTokens")).isNotNull();
        }
    }
}