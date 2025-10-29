package com.gym.crm.util.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "2a94f384162d40fc7abee842ebd0ff94aa7895aa549605bd0b8f29ca456e5329";
    private static final long TEST_EXPIRATION = 300000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_EXPIRATION);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should create valid token with all parameters")
        void generateToken_ShouldCreateValidToken() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should create valid token with null userId")
        void generateToken_ShouldWorkWithNullUserId() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", null);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Should create valid token with overloaded method")
        void generateToken_Overloaded_ShouldCreateValidToken() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE");

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Should generate different tokens each time")
        void generatedTokens_ShouldBeDifferent() throws InterruptedException {
            String token1 = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);
            Thread.sleep(10); // Ensure different timestamp
            String token2 = jwtUtil.generateToken("john.doe", "TRAINEE", 124L);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should handle special characters in username")
        void generateToken_ShouldHandleSpecialCharacters() {
            String specialUsername = "user@domain.com";
            String token = jwtUtil.generateToken(specialUsername, "TRAINEE", 123L);

            assertThat(token).isNotNull();
            assertThat(jwtUtil.extractUsername(token)).isEqualTo(specialUsername);
        }

        @Test
        @DisplayName("Should handle very long usernames")
        void generateToken_ShouldHandleLongUsernames() {
            String longUsername = "user.with.very.long.username.that.might.cause.issues";
            String token = jwtUtil.generateToken(longUsername, "TRAINEE", 123L);

            assertThat(token).isNotNull();
            assertThat(jwtUtil.extractUsername(token)).isEqualTo(longUsername);
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {

        @Test
        @DisplayName("Should extract correct username")
        void extractUsername_ShouldReturnCorrectUsername() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

            String username = jwtUtil.extractUsername(token);

            assertThat(username).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("Should extract correct role")
        void extractRole_ShouldReturnCorrectRole() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

            String role = jwtUtil.extractRole(token);

            assertThat(role).isEqualTo("TRAINEE");
        }

        @Test
        @DisplayName("Should extract correct userId")
        void extractUserId_ShouldReturnCorrectUserId() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

            Long userId = jwtUtil.extractUserId(token);

            assertThat(userId).isEqualTo(123L);
        }

        @Test
        @DisplayName("Should return null when userId not set")
        void extractUserId_ShouldReturnNull_WhenUserIdNotSet() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE");

            Long userId = jwtUtil.extractUserId(token);

            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("Should handle large user IDs")
        void extractUserId_ShouldHandleLargeIds() {
            Long largeId = 999999999L;
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", largeId);

            Long extractedId = jwtUtil.extractUserId(token);

            assertThat(extractedId).isEqualTo(largeId);
        }

        @Test
        @DisplayName("Should work with different usernames")
        void extractUsername_ShouldWorkWithDifferentUsernames() {
            String token1 = jwtUtil.generateToken("user.one", "TRAINEE", 1L);
            String token2 = jwtUtil.generateToken("user.two", "TRAINER", 2L);

            String username1 = jwtUtil.extractUsername(token1);
            String username2 = jwtUtil.extractUsername(token2);

            assertThat(username1).isEqualTo("user.one");
            assertThat(username2).isEqualTo("user.two");
        }

        @Test
        @DisplayName("Should work with different roles")
        void extractRole_ShouldWorkWithDifferentRoles() {
            String token1 = jwtUtil.generateToken("john.doe", "TRAINEE", 1L);
            String token2 = jwtUtil.generateToken("jane.smith", "TRAINER", 2L);

            String role1 = jwtUtil.extractRole(token1);
            String role2 = jwtUtil.extractRole(token2);

            assertThat(role1).isEqualTo("TRAINEE");
            assertThat(role2).isEqualTo("TRAINER");
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should return true for valid token")
        void validateToken_ShouldReturnTrue_WhenTokenValid() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

            boolean valid = jwtUtil.validateToken(token);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid token")
        void validateToken_ShouldReturnFalse_WhenTokenInvalid() {
            String invalidToken = "invalid.token.here";

            boolean valid = jwtUtil.validateToken(invalidToken);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false for malformed token")
        void validateToken_ShouldReturnFalse_WhenTokenMalformed() {
            String malformedToken = "not-a-jwt-token";

            boolean valid = jwtUtil.validateToken(malformedToken);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false for null token")
        void validateToken_ShouldReturnFalse_WhenTokenNull() {
            boolean valid = jwtUtil.validateToken(null);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty token")
        void validateToken_ShouldReturnFalse_WhenTokenEmpty() {
            boolean valid = jwtUtil.validateToken("");

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false for tampered signature")
        void validateToken_ShouldReturnFalse_WhenSignatureInvalid() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);
            String tamperedToken = token.substring(0, token.lastIndexOf('.')) + ".invalidsignature";

            boolean valid = jwtUtil.validateToken(tamperedToken);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false for token with missing parts")
        void validateToken_ShouldReturnFalse_WhenTokenIncomplete() {
            String incompleteToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIn0";

            boolean valid = jwtUtil.validateToken(incompleteToken);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false for token with extra parts")
        void validateToken_ShouldReturnFalse_WhenTokenHasExtraParts() {
            String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);
            String tokenWithExtraParts = token + ".extrapart";

            boolean valid = jwtUtil.validateToken(tokenWithExtraParts);

            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle whitespace in extraction methods")
        void extractMethods_ShouldHandleWhitespaceGracefully() {
            String username = jwtUtil.extractUsername("   ");
            String role = jwtUtil.extractRole("   ");
            Long userId = jwtUtil.extractUserId("   ");

            assertThat(username).isNull();
            assertThat(role).isNull();
            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("Should handle null parameters in extraction methods")
        void extractMethods_ShouldHandleNullParameters() {
            String username = jwtUtil.extractUsername(null);
            String role = jwtUtil.extractRole(null);
            Long userId = jwtUtil.extractUserId(null);

            assertThat(username).isNull();
            assertThat(role).isNull();
            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("Should maintain consistency across multiple operations")
        void shouldMaintainConsistencyAcrossOperations() {
            String originalUsername = "test.user";
            String originalRole = "TRAINEE";
            Long originalUserId = 456L;

            String token = jwtUtil.generateToken(originalUsername, originalRole, originalUserId);

            assertThat(jwtUtil.extractUsername(token)).isEqualTo(originalUsername);
            assertThat(jwtUtil.extractUsername(token)).isEqualTo(originalUsername);

            assertThat(jwtUtil.extractRole(token)).isEqualTo(originalRole);
            assertThat(jwtUtil.extractRole(token)).isEqualTo(originalRole);

            assertThat(jwtUtil.extractUserId(token)).isEqualTo(originalUserId);
            assertThat(jwtUtil.extractUserId(token)).isEqualTo(originalUserId);

            assertThat(jwtUtil.validateToken(token)).isTrue();
            assertThat(jwtUtil.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("Should handle minimum and maximum user IDs")
        void shouldHandleExtremeUserIds() {
            Long minId = 1L;
            Long maxId = Long.MAX_VALUE;

            String tokenMin = jwtUtil.generateToken("user", "TRAINEE", minId);
            String tokenMax = jwtUtil.generateToken("user", "TRAINEE", maxId);

            assertThat(jwtUtil.extractUserId(tokenMin)).isEqualTo(minId);
            assertThat(jwtUtil.extractUserId(tokenMax)).isEqualTo(maxId);
            assertThat(jwtUtil.validateToken(tokenMin)).isTrue();
            assertThat(jwtUtil.validateToken(tokenMax)).isTrue();
        }

        @Test
        @DisplayName("Should handle zero user ID")
        void shouldHandleZeroUserId() {
            Long zeroId = 0L;
            String token = jwtUtil.generateToken("user", "TRAINEE", zeroId);

            assertThat(jwtUtil.extractUserId(token)).isEqualTo(zeroId);
            assertThat(jwtUtil.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("Should handle all supported roles")
        void shouldHandleAllSupportedRoles() {
            String[] roles = {"TRAINEE", "TRAINER", "ADMIN", "USER"};

            for (String role : roles) {
                String token = jwtUtil.generateToken("user", role, 1L);
                assertThat(jwtUtil.extractRole(token)).isEqualTo(role);
                assertThat(jwtUtil.validateToken(token)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should use consistent secret for signature")
        void shouldUseConsistentSecret() {
            String token1 = jwtUtil.generateToken("user", "TRAINEE", 1L);
            String token2 = jwtUtil.generateToken("user", "TRAINEE", 1L);

            // Different tokens due to timestamp, but both should be valid with same secret
            assertThat(jwtUtil.validateToken(token1)).isTrue();
            assertThat(jwtUtil.validateToken(token2)).isTrue();
        }

        @Test
        @DisplayName("Should not validate tokens with different secrets")
        void shouldNotValidateTokensWithDifferentSecrets() {
            String token = jwtUtil.generateToken("user", "TRAINEE", 1L);

            JwtUtil differentSecretJwtUtil = new JwtUtil("different_secret_abee842ebd0ff94aa7895aa549605bd0b8f29ca456e5329", TEST_EXPIRATION);

            assertThat(differentSecretJwtUtil.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("Should include proper claims in token")
        void shouldIncludeProperClaims() {
            String username = "test.user";
            String role = "TRAINEE";
            Long userId = 123L;

            String token = jwtUtil.generateToken(username, role, userId);

            assertThat(jwtUtil.extractUsername(token)).isEqualTo(username);
            assertThat(jwtUtil.extractRole(token)).isEqualTo(role);
            assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
        }
    }
}