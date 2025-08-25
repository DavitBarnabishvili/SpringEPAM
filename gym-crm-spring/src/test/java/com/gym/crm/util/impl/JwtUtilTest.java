package com.gym.crm.util.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_ShouldWorkWithNullUserId() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", null);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_Overloaded_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("john.doe");
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

        String role = jwtUtil.extractRole(token);

        assertThat(role).isEqualTo("TRAINEE");
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

        Long userId = jwtUtil.extractUserId(token);

        assertThat(userId).isEqualTo(123L);
    }

    @Test
    void extractUserId_ShouldReturnNull_WhenUserIdNotSet() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE");

        Long userId = jwtUtil.extractUserId(token);

        assertThat(userId).isNull();
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenValid() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);

        boolean valid = jwtUtil.validateToken(token);

        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenInvalid() {
        String invalidToken = "invalid.token.here";

        boolean valid = jwtUtil.validateToken(invalidToken);

        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenMalformed() {
        String malformedToken = "not-a-jwt-token";

        boolean valid = jwtUtil.validateToken(malformedToken);

        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenNull() {
        boolean valid = jwtUtil.validateToken(null);

        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenEmpty() {
        boolean valid = jwtUtil.validateToken("");

        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenSignatureInvalid() {
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);
        String tamperedToken = token.substring(0, token.lastIndexOf('.')) + ".invalidsignature";

        boolean valid = jwtUtil.validateToken(tamperedToken);

        assertThat(valid).isFalse();
    }

    @Test
    void extractUsername_ShouldWorkWithDifferentUsernames() {
        String token1 = jwtUtil.generateToken("user.one", "TRAINEE", 1L);
        String token2 = jwtUtil.generateToken("user.two", "TRAINER", 2L);

        String username1 = jwtUtil.extractUsername(token1);
        String username2 = jwtUtil.extractUsername(token2);

        assertThat(username1).isEqualTo("user.one");
        assertThat(username2).isEqualTo("user.two");
    }

    @Test
    void extractRole_ShouldWorkWithDifferentRoles() {
        String token1 = jwtUtil.generateToken("john.doe", "TRAINEE", 1L);
        String token2 = jwtUtil.generateToken("jane.smith", "TRAINER", 2L);

        String role1 = jwtUtil.extractRole(token1);
        String role2 = jwtUtil.extractRole(token2);

        assertThat(role1).isEqualTo("TRAINEE");
        assertThat(role2).isEqualTo("TRAINER");
    }

    @Test
    void generatedTokens_ShouldBeDifferent() throws InterruptedException {
        String token1 = jwtUtil.generateToken("john.doe", "TRAINEE", 123L);
        Thread.sleep(10);
        String token2 = jwtUtil.generateToken("john.doe", "TRAINEE", 124L);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractUserId_ShouldHandleLargeIds() {
        Long largeId = 999999999L;
        String token = jwtUtil.generateToken("john.doe", "TRAINEE", largeId);

        Long extractedId = jwtUtil.extractUserId(token);

        assertThat(extractedId).isEqualTo(largeId);
    }
}