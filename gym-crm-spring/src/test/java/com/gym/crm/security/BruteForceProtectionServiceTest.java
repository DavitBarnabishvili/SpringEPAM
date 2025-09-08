package com.gym.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class BruteForceProtectionServiceTest {

    private BruteForceProtectionService bruteForceProtectionService;

    @BeforeEach
    void setUp() {
        bruteForceProtectionService = new BruteForceProtectionService();
        ReflectionTestUtils.setField(bruteForceProtectionService, "maxAttempts", 3);
        ReflectionTestUtils.setField(bruteForceProtectionService, "lockDurationMillis", 300000L); // 5 minutes
    }

    @Nested
    @DisplayName("Initial State Tests")
    class InitialStateTests {

        @Test
        @DisplayName("Should not block new user")
        void isBlocked_ShouldReturnFalse_ForNewUser() {
            boolean isBlocked = bruteForceProtectionService.isBlocked("newuser");

            assertThat(isBlocked).isFalse();
        }

        @Test
        @DisplayName("Should return zero remaining lock time for new user")
        void getRemainingLockTimeMinutes_ShouldReturnZero_ForNewUser() {
            long remainingTime = bruteForceProtectionService.getRemainingLockTimeMinutes("newuser");

            assertThat(remainingTime).isZero();
        }
    }

    @Nested
    @DisplayName("Failed Login Attempts Tests")
    class FailedLoginAttemptsTests {

        @Test
        @DisplayName("Should not block user after first failed attempt")
        void loginFailed_ShouldNotBlock_AfterFirstAttempt() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);

            assertThat(bruteForceProtectionService.isBlocked(username)).isFalse();
        }

        @Test
        @DisplayName("Should not block user after second failed attempt")
        void loginFailed_ShouldNotBlock_AfterSecondAttempt() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);

            assertThat(bruteForceProtectionService.isBlocked(username)).isFalse();
        }

        @Test
        @DisplayName("Should block user after third failed attempt")
        void loginFailed_ShouldBlock_AfterThirdAttempt() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);

            assertThat(bruteForceProtectionService.isBlocked(username)).isTrue();
        }

        @Test
        @DisplayName("Should keep user blocked after additional failed attempts")
        void loginFailed_ShouldKeepBlocked_AfterAdditionalAttempts() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);

            assertThat(bruteForceProtectionService.isBlocked(username)).isTrue();
        }

        @Test
        @DisplayName("Should handle empty username gracefully")
        void loginFailed_ShouldHandleEmptyUsername() {
            bruteForceProtectionService.loginFailed("");

            bruteForceProtectionService.loginFailed("testuser");
            assertThat(bruteForceProtectionService.isBlocked("testuser")).isFalse();
        }
    }

    @Nested
    @DisplayName("Successful Login Tests")
    class SuccessfulLoginTests {

        @Test
        @DisplayName("Should clear attempts after successful login")
        void loginSucceeded_ShouldClearAttempts() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);

            bruteForceProtectionService.loginSucceeded(username);

            bruteForceProtectionService.loginFailed(username);
            assertThat(bruteForceProtectionService.isBlocked(username)).isFalse();
        }

        @Test
        @DisplayName("Should unlock blocked user after successful login")
        void loginSucceeded_ShouldUnlockUser() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);
            assertThat(bruteForceProtectionService.isBlocked(username)).isTrue();

            bruteForceProtectionService.loginSucceeded(username);
            assertThat(bruteForceProtectionService.isBlocked(username)).isFalse();
        }

        @Test
        @DisplayName("Should handle successful login for non-existing user")
        void loginSucceeded_ShouldHandleNonExistingUser() {
            bruteForceProtectionService.loginSucceeded("nonexistentuser");

            assertThat(bruteForceProtectionService.isBlocked("nonexistentuser")).isFalse();
        }
    }

    @Nested
    @DisplayName("Lock Duration Tests")
    class LockDurationTests {

        @Test
        @DisplayName("Should return correct remaining lock time")
        void getRemainingLockTimeMinutes_ShouldReturnCorrectTime() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);

            long remainingTime = bruteForceProtectionService.getRemainingLockTimeMinutes(username);
            assertThat(remainingTime).isBetween(4L, 5L);
        }

        @Test
        @DisplayName("Should return zero remaining time for non-blocked user")
        void getRemainingLockTimeMinutes_ShouldReturnZero_ForNonBlockedUser() {
            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);

            long remainingTime = bruteForceProtectionService.getRemainingLockTimeMinutes(username);

            assertThat(remainingTime).isZero();
        }

        @Test
        @DisplayName("Should unblock user after lock duration expires")
        void isBlocked_ShouldReturnFalse_AfterLockDurationExpires() throws InterruptedException {
            ReflectionTestUtils.setField(bruteForceProtectionService, "lockDurationMillis", 100L); // 100ms

            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);
            bruteForceProtectionService.loginFailed(username);
            assertThat(bruteForceProtectionService.isBlocked(username)).isTrue();

            Thread.sleep(150);

            assertThat(bruteForceProtectionService.isBlocked(username)).isFalse();
        }
    }

    @Nested
    @DisplayName("Multiple Users Tests")
    class MultipleUsersTests {

        @Test
        @DisplayName("Should handle multiple users independently")
        void shouldHandleMultipleUsersIndependently() {
            String user1 = "user1";
            String user2 = "user2";

            bruteForceProtectionService.loginFailed(user1);
            bruteForceProtectionService.loginFailed(user1);
            bruteForceProtectionService.loginFailed(user1);

            assertThat(bruteForceProtectionService.isBlocked(user1)).isTrue();
            assertThat(bruteForceProtectionService.isBlocked(user2)).isFalse();

            bruteForceProtectionService.loginFailed(user2);
            assertThat(bruteForceProtectionService.isBlocked(user2)).isFalse();

            bruteForceProtectionService.loginSucceeded(user1);
            assertThat(bruteForceProtectionService.isBlocked(user1)).isFalse();

            bruteForceProtectionService.loginFailed(user2);
            bruteForceProtectionService.loginFailed(user2);
            assertThat(bruteForceProtectionService.isBlocked(user2)).isTrue();
        }

        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccess() {
            String username = "testuser";

            Runnable failedLoginTask = () -> bruteForceProtectionService.loginFailed(username);
            Runnable checkBlockedTask = () -> bruteForceProtectionService.isBlocked(username);

            Thread thread1 = new Thread(failedLoginTask);
            Thread thread2 = new Thread(checkBlockedTask);

            thread1.start();
            thread2.start();

            try {
                thread1.join();
                thread2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle configuration with zero max attempts")
        void shouldHandleZeroMaxAttempts() {
            ReflectionTestUtils.setField(bruteForceProtectionService, "maxAttempts", 0);

            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);

            assertThat(bruteForceProtectionService.isBlocked(username)).isTrue();
        }

        @Test
        @DisplayName("Should handle configuration with negative max attempts")
        void shouldHandleNegativeMaxAttempts() {
            ReflectionTestUtils.setField(bruteForceProtectionService, "maxAttempts", -1);

            String username = "testuser";

            bruteForceProtectionService.loginFailed(username);

            assertThat(bruteForceProtectionService.isBlocked(username)).isTrue();
        }

        @Test
        @DisplayName("Should handle very long usernames")
        void shouldHandleVeryLongUsernames() {
            String longUsername = "a".repeat(1000);

            bruteForceProtectionService.loginFailed(longUsername);
            bruteForceProtectionService.loginFailed(longUsername);
            bruteForceProtectionService.loginFailed(longUsername);

            assertThat(bruteForceProtectionService.isBlocked(longUsername)).isTrue();
        }

        @Test
        @DisplayName("Should handle usernames with special characters")
        void shouldHandleSpecialCharacters() {
            String specialUsername = "user@domain.com!#$%^&*()";

            bruteForceProtectionService.loginFailed(specialUsername);
            bruteForceProtectionService.loginFailed(specialUsername);
            bruteForceProtectionService.loginFailed(specialUsername);

            assertThat(bruteForceProtectionService.isBlocked(specialUsername)).isTrue();
        }
    }
}