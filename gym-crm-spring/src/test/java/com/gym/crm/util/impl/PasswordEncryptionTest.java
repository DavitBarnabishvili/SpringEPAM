package com.gym.crm.util.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordEncryptionTest {

    private PasswordEncryption passwordEncryption;

    @BeforeEach
    void setUp() {
        passwordEncryption = new PasswordEncryption();
    }

    @Nested
    @DisplayName("Password Encoding Tests")
    class PasswordEncodingTests {

        @Test
        @DisplayName("Should return encoded password with BCrypt format")
        void encode_ShouldReturnEncodedPassword() {
            String rawPassword = "myPassword123";

            String encoded = passwordEncryption.encode(rawPassword);

            assertThat(encoded).isNotNull();
            assertThat(encoded).isNotEmpty();
            assertThat(encoded).startsWith("$2a$12$");
            assertThat(encoded).isNotEqualTo(rawPassword);
            assertThat(encoded.length()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should generate different salts for same password")
        void encode_ShouldGenerateDifferentSalts() {
            String rawPassword = "myPassword123";

            String encoded1 = passwordEncryption.encode(rawPassword);
            String encoded2 = passwordEncryption.encode(rawPassword);

            assertThat(encoded1).isNotEqualTo(encoded2);
            assertThat(encoded1).startsWith("$2a$12$");
            assertThat(encoded2).startsWith("$2a$12$");
        }

        @Test
        @DisplayName("Should throw exception when password is null")
        void encode_ShouldThrowException_WhenPasswordNull() {
            assertThatThrownBy(() -> passwordEncryption.encode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception when password is empty")
        void encode_ShouldThrowException_WhenPasswordEmpty() {
            assertThatThrownBy(() -> passwordEncryption.encode(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be null or empty");
        }

        @Test
        @DisplayName("Should handle special characters in password")
        void encode_ShouldHandleSpecialCharacters() {
            String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()";

            String encoded = passwordEncryption.encode(passwordWithSpecialChars);

            assertThat(encoded).isNotNull();
            assertThat(encoded).startsWith("$2a$12$");
            assertThat(passwordEncryption.matches(passwordWithSpecialChars, encoded)).isTrue();
        }

        @Test
        @DisplayName("Should handle unicode characters in password")
        void encode_ShouldHandleUnicodeCharacters() {
            String passwordWithUnicode = "пароль密码パスワード";

            String encoded = passwordEncryption.encode(passwordWithUnicode);

            assertThat(encoded).isNotNull();
            assertThat(passwordEncryption.matches(passwordWithUnicode, encoded)).isTrue();
        }

        @Test
        @DisplayName("Should handle long passwords")
        void encode_ShouldHandleLongPasswords() {
            String longPassword = "a".repeat(72);

            String encoded = passwordEncryption.encode(longPassword);

            assertThat(encoded).isNotNull();
            assertThat(passwordEncryption.matches(longPassword, encoded)).isTrue();
        }

        @Test
        @DisplayName("Should handle single character password")
        void encode_ShouldHandleSingleCharacterPassword() {
            String singleChar = "a";

            String encoded = passwordEncryption.encode(singleChar);

            assertThat(encoded).isNotNull();
            assertThat(passwordEncryption.matches(singleChar, encoded)).isTrue();
        }
    }

    @Nested
    @DisplayName("Password Matching Tests")
    class PasswordMatchingTests {

        @Test
        @DisplayName("Should return true when password matches")
        void matches_ShouldReturnTrue_WhenPasswordCorrect() {
            String rawPassword = "myPassword123";
            String encoded = passwordEncryption.encode(rawPassword);

            boolean matches = passwordEncryption.matches(rawPassword, encoded);

            assertThat(matches).isTrue();
        }

        @Test
        @DisplayName("Should return false when password doesn't match")
        void matches_ShouldReturnFalse_WhenPasswordIncorrect() {
            String rawPassword = "myPassword123";
            String encoded = passwordEncryption.encode(rawPassword);

            boolean matches = passwordEncryption.matches("wrongPassword", encoded);

            assertThat(matches).isFalse();
        }

        @Test
        @DisplayName("Should return false when raw password is null")
        void matches_ShouldReturnFalse_WhenRawPasswordNull() {
            String encoded = passwordEncryption.encode("password");

            boolean matches = passwordEncryption.matches(null, encoded);

            assertThat(matches).isFalse();
        }

        @Test
        @DisplayName("Should return false when encoded password is null")
        void matches_ShouldReturnFalse_WhenEncodedPasswordNull() {
            boolean matches = passwordEncryption.matches("password", null);

            assertThat(matches).isFalse();
        }

        @Test
        @DisplayName("Should return false when both passwords are null")
        void matches_ShouldReturnFalse_WhenBothNull() {
            boolean matches = passwordEncryption.matches(null, null);

            assertThat(matches).isFalse();
        }

        @Test
        @DisplayName("Should return false for malformed encoded password")
        void matches_ShouldReturnFalse_WhenEncodedPasswordMalformed() {
            boolean matches = passwordEncryption.matches("password", "malformed-hash");

            assertThat(matches).isFalse();
        }

        @Test
        @DisplayName("Should handle case sensitivity correctly")
        void matches_ShouldHandleCaseSensitivity() {
            String encoded = passwordEncryption.encode("Password123");

            boolean matchesLowercase = passwordEncryption.matches("password123", encoded);
            boolean matchesUppercase = passwordEncryption.matches("PASSWORD123", encoded);
            boolean matchesCorrect = passwordEncryption.matches("Password123", encoded);

            assertThat(matchesLowercase).isFalse();
            assertThat(matchesUppercase).isFalse();
            assertThat(matchesCorrect).isTrue();
        }

        @Test
        @DisplayName("Should handle whitespace in password correctly")
        void matches_ShouldHandleWhitespaceInPassword() {
            String passwordWithSpaces = "password with spaces";
            String encoded = passwordEncryption.encode(passwordWithSpaces);

            boolean matches = passwordEncryption.matches(passwordWithSpaces, encoded);

            assertThat(matches).isTrue();
        }

        @Test
        @DisplayName("Should not trim password during matching")
        void matches_ShouldNotTrimPassword() {
            String passwordWithTrailingSpace = "password ";
            String passwordWithoutSpace = "password";
            String encoded = passwordEncryption.encode(passwordWithTrailingSpace);

            boolean matchesWithSpace = passwordEncryption.matches(passwordWithTrailingSpace, encoded);
            boolean matchesWithoutSpace = passwordEncryption.matches(passwordWithoutSpace, encoded);

            assertThat(matchesWithSpace).isTrue();
            assertThat(matchesWithoutSpace).isFalse();
        }
    }

    @Nested
    @DisplayName("BCrypt Specific Tests")
    class BCryptSpecificTests {

        @Test
        @DisplayName("Should produce unique hashes for same password multiple times")
        void encode_ShouldProduceDifferentHashesForSamePassword() {
            String password = "testPassword";

            String encoded1 = passwordEncryption.encode(password);
            String encoded2 = passwordEncryption.encode(password);
            String encoded3 = passwordEncryption.encode(password);

            assertThat(encoded1).isNotEqualTo(encoded2);
            assertThat(encoded2).isNotEqualTo(encoded3);
            assertThat(encoded1).isNotEqualTo(encoded3);

            assertThat(passwordEncryption.matches(password, encoded1)).isTrue();
            assertThat(passwordEncryption.matches(password, encoded2)).isTrue();
            assertThat(passwordEncryption.matches(password, encoded3)).isTrue();
        }

        @Test
        @DisplayName("Should use correct BCrypt cost factor")
        void encode_ShouldUseCostFactor12() {
            String password = "testPassword";

            String encoded = passwordEncryption.encode(password);

            assertThat(encoded).startsWith("$2a$12$");
        }

        @Test
        @DisplayName("Should handle empty string password when allowed")
        void encode_ShouldThrowForEmptyString() {
            assertThatThrownBy(() -> passwordEncryption.encode(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be null or empty");
        }

        @Test
        @DisplayName("Should handle very short passwords")
        void encode_ShouldHandleVeryShortPasswords() {
            String shortPassword = "1";

            String encoded = passwordEncryption.encode(shortPassword);

            assertThat(encoded).isNotNull();
            assertThat(passwordEncryption.matches(shortPassword, encoded)).isTrue();
        }

        @Test
        @DisplayName("Should be deterministic for same password and salt")
        void matches_ShouldBeConsistent() {
            String password = "testPassword";
            String encoded = passwordEncryption.encode(password);

            assertThat(passwordEncryption.matches(password, encoded)).isTrue();
            assertThat(passwordEncryption.matches(password, encoded)).isTrue();
            assertThat(passwordEncryption.matches(password, encoded)).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid BCrypt format")
        void matches_ShouldReturnFalseForInvalidBCryptFormat() {
            String password = "testPassword";

            boolean matches1 = passwordEncryption.matches(password, "not-a-bcrypt-hash");
            boolean matches2 = passwordEncryption.matches(password, "$2a$10$");
            boolean matches3 = passwordEncryption.matches(password, "plain-text-password");

            assertThat(matches1).isFalse();
            assertThat(matches2).isFalse();
            assertThat(matches3).isFalse();
        }
    }

    @Nested
    @DisplayName("Performance and Security Tests")
    class PerformanceAndSecurityTests {

        @Test
        @DisplayName("Should complete encoding within reasonable time")
        void encode_ShouldCompleteWithinReasonableTime() {
            String password = "testPassword";

            long startTime = System.currentTimeMillis();
            String encoded = passwordEncryption.encode(password);
            long endTime = System.currentTimeMillis();

            assertThat(encoded).isNotNull();
            assertThat(endTime - startTime).isLessThan(5000);
        }

        @Test
        @DisplayName("Should be resistant to timing attacks")
        void matches_ShouldTakeConsistentTime() {
            String password = "testPassword";
            String encoded = passwordEncryption.encode(password);
            String wrongPassword = "wrongPassword";

            long startTime1 = System.nanoTime();
            passwordEncryption.matches(password, encoded);
            long endTime1 = System.nanoTime();

            long startTime2 = System.nanoTime();
            passwordEncryption.matches(wrongPassword, encoded);
            long endTime2 = System.nanoTime();

            long time1 = endTime1 - startTime1;
            long time2 = endTime2 - startTime2;

            assertThat(Math.abs(time1 - time2)).isLessThan(Math.max(time1, time2));
        }
    }
}