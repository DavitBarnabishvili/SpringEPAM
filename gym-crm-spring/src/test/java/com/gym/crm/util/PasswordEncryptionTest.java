package com.gym.crm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordEncryptionTest {

    private PasswordEncryption passwordEncryption;

    @BeforeEach
    void setUp() {
        passwordEncryption = new PasswordEncryption();
    }

    @Test
    void encode_ShouldReturnEncodedPassword() {
        String rawPassword = "myPassword123";

        String encoded = passwordEncryption.encode(rawPassword);

        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEmpty();
        assertThat(encoded).contains(":");
        assertThat(encoded).isNotEqualTo(rawPassword);
    }

    @Test
    void encode_ShouldGenerateDifferentSalts() {
        String rawPassword = "myPassword123";

        String encoded1 = passwordEncryption.encode(rawPassword);
        String encoded2 = passwordEncryption.encode(rawPassword);

        assertThat(encoded1).isNotEqualTo(encoded2);

        String salt1 = encoded1.split(":")[0];
        String salt2 = encoded2.split(":")[0];
        assertThat(salt1).isNotEqualTo(salt2);
    }

    @Test
    void encode_ShouldThrowException_WhenPasswordNull() {
        assertThatThrownBy(() -> passwordEncryption.encode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }

    @Test
    void encode_ShouldThrowException_WhenPasswordEmpty() {
        assertThatThrownBy(() -> passwordEncryption.encode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }

    @Test
    void matches_ShouldReturnTrue_WhenPasswordCorrect() {
        String rawPassword = "myPassword123";
        String encoded = passwordEncryption.encode(rawPassword);

        boolean matches = passwordEncryption.matches(rawPassword, encoded);

        assertThat(matches).isTrue();
    }

    @Test
    void matches_ShouldReturnFalse_WhenPasswordIncorrect() {
        String rawPassword = "myPassword123";
        String encoded = passwordEncryption.encode(rawPassword);

        boolean matches = passwordEncryption.matches("wrongPassword", encoded);

        assertThat(matches).isFalse();
    }

    @Test
    void matches_ShouldReturnFalse_WhenRawPasswordNull() {
        String encoded = passwordEncryption.encode("password");

        boolean matches = passwordEncryption.matches(null, encoded);

        assertThat(matches).isFalse();
    }

    @Test
    void matches_ShouldReturnFalse_WhenEncodedPasswordNull() {
        boolean matches = passwordEncryption.matches("password", null);

        assertThat(matches).isFalse();
    }

    @Test
    void matches_ShouldReturnFalse_WhenBothNull() {
        boolean matches = passwordEncryption.matches(null, null);

        assertThat(matches).isFalse();
    }

    @Test
    void matches_ShouldReturnFalse_WhenEncodedPasswordMalformed() {
        boolean matches = passwordEncryption.matches("password", "malformed-no-separator");

        assertThat(matches).isFalse();
    }

    @Test
    void matches_ShouldReturnFalse_WhenEncodedPasswordInvalidBase64() {
        boolean matches = passwordEncryption.matches("password", "invalid:notbase64!");

        assertThat(matches).isFalse();
    }

    @Test
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
    void migratePassword_ShouldReturnEncodedPassword() {
        String plainPassword = "plainTextPassword";

        String migrated = passwordEncryption.migratePassword(plainPassword);

        assertThat(migrated).isNotNull();
        assertThat(migrated).contains(":");
        assertThat(passwordEncryption.matches(plainPassword, migrated)).isTrue();
    }

    @Test
    void isEncoded_ShouldReturnTrue_WhenPasswordEncoded() {
        String encoded = passwordEncryption.encode("password");

        boolean isEncoded = passwordEncryption.isEncoded(encoded);

        assertThat(isEncoded).isTrue();
    }

    @Test
    void isEncoded_ShouldReturnFalse_WhenPasswordPlain() {
        boolean isEncoded = passwordEncryption.isEncoded("plainPassword");

        assertThat(isEncoded).isFalse();
    }

    @Test
    void isEncoded_ShouldReturnFalse_WhenPasswordNull() {
        boolean isEncoded = passwordEncryption.isEncoded(null);

        assertThat(isEncoded).isFalse();
    }

    @Test
    void encode_ShouldHandleSpecialCharacters() {
        String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()";

        String encoded = passwordEncryption.encode(passwordWithSpecialChars);

        assertThat(passwordEncryption.matches(passwordWithSpecialChars, encoded)).isTrue();
    }

    @Test
    void encode_ShouldHandleUnicodeCharacters() {
        String passwordWithUnicode = "пароль密码パスワード";

        String encoded = passwordEncryption.encode(passwordWithUnicode);

        assertThat(passwordEncryption.matches(passwordWithUnicode, encoded)).isTrue();
    }

    @Test
    void encode_ShouldHandleLongPasswords() {
        String longPassword = "a".repeat(100);

        String encoded = passwordEncryption.encode(longPassword);

        assertThat(passwordEncryption.matches(longPassword, encoded)).isTrue();
    }

    @Test
    void encode_ShouldHandleSingleCharacterPassword() {
        String singleChar = "a";

        String encoded = passwordEncryption.encode(singleChar);

        assertThat(passwordEncryption.matches(singleChar, encoded)).isTrue();
    }

    @Test
    void matches_ShouldHandleWhitespaceInPassword() {
        String passwordWithSpaces = "password with spaces";
        String encoded = passwordEncryption.encode(passwordWithSpaces);

        boolean matches = passwordEncryption.matches(passwordWithSpaces, encoded);

        assertThat(matches).isTrue();
    }

    @Test
    void matches_ShouldNotTrimPassword() {
        String passwordWithTrailingSpace = "password ";
        String passwordWithoutSpace = "password";
        String encoded = passwordEncryption.encode(passwordWithTrailingSpace);

        boolean matchesWithSpace = passwordEncryption.matches(passwordWithTrailingSpace, encoded);
        boolean matchesWithoutSpace = passwordEncryption.matches(passwordWithoutSpace, encoded);

        assertThat(matchesWithSpace).isTrue();
        assertThat(matchesWithoutSpace).isFalse();
    }

    @Test
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
}