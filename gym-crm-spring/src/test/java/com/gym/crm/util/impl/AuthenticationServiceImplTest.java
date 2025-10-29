package com.gym.crm.util.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.exception.InactiveAccountException;
import com.gym.crm.exception.InvalidCredentialsException;
import com.gym.crm.exception.UnauthorizedAccessException;
import com.gym.crm.util.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private PasswordEncryption passwordEncryption;

    private AuthenticationService authenticationService;
    private Trainee testTrainee;
    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(traineeDao, trainerDao, passwordEncryption);

        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Test St");
        testTrainee.setId(1L);
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("$2a$12$encodedBCryptPassword");
        testTrainee.setIsActive(true);

        TrainingType specialization = new TrainingType("Cardio");
        testTrainer = new Trainer("Jane", "Smith", specialization);
        testTrainer.setId(2L);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("$2a$12$encodedBCryptPassword");
        testTrainer.setIsActive(true);
    }

    @Nested
    @DisplayName("Trainee Authentication Tests")
    class TraineeAuthenticationTests {

        @Test
        @DisplayName("Should return trainee when credentials are valid")
        void authenticateTrainee_ShouldReturnTrainee_WhenCredentialsValid() {
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            Trainee authenticated = authenticationService.authenticateTrainee("john.doe", "password");

            assertThat(authenticated).isEqualTo(testTrainee);
            assertThat(authenticated.getUsername()).isEqualTo("john.doe");
            verify(passwordEncryption).matches("password", "$2a$12$encodedBCryptPassword");
        }

        @Test
        @DisplayName("Should throw exception when username is null")
        void authenticateTrainee_ShouldThrowException_WhenUsernameNull() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainee(null, "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Username is required");
        }

        @Test
        @DisplayName("Should throw exception when username is empty")
        void authenticateTrainee_ShouldThrowException_WhenUsernameEmpty() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainee("", "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Username is required");
        }

        @Test
        @DisplayName("Should throw exception when password is null")
        void authenticateTrainee_ShouldThrowException_WhenPasswordNull() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", null))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Password is required");
        }

        @Test
        @DisplayName("Should throw exception when password is empty")
        void authenticateTrainee_ShouldThrowException_WhenPasswordEmpty() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", ""))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Password is required");
        }

        @Test
        @DisplayName("Should throw exception when username not found")
        void authenticateTrainee_ShouldThrowException_WhenUsernameNotFound() {
            when(traineeDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.authenticateTrainee("nonexistent", "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void authenticateTrainee_ShouldThrowException_WhenPasswordIncorrect() {
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
            when(passwordEncryption.matches("wrongPassword", "$2a$12$encodedBCryptPassword")).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", "wrongPassword"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should throw exception when account is inactive")
        void authenticateTrainee_ShouldThrowException_WhenAccountInactive() {
            testTrainee.setIsActive(false);
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", "password"))
                    .isInstanceOf(InactiveAccountException.class)
                    .hasMessage("This account is inactive");
        }

        @Test
        @DisplayName("Should return true for valid trainee credentials")
        void isValidTraineeCredentials_ShouldReturnTrue_WhenCredentialsValid() {
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            boolean valid = authenticationService.isValidTraineeCredentials("john.doe", "password");

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid trainee credentials")
        void isValidTraineeCredentials_ShouldReturnFalse_WhenCredentialsInvalid() {
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.empty());

            boolean valid = authenticationService.isValidTraineeCredentials("john.doe", "password");

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false when trainee password doesn't match")
        void isValidTraineeCredentials_ShouldReturnFalse_WhenPasswordIncorrect() {
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
            when(passwordEncryption.matches("wrongPassword", "$2a$12$encodedBCryptPassword")).thenReturn(false);

            boolean valid = authenticationService.isValidTraineeCredentials("john.doe", "wrongPassword");

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false when trainee account is inactive")
        void isValidTraineeCredentials_ShouldReturnFalse_WhenAccountInactive() {
            testTrainee.setIsActive(false);
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            boolean valid = authenticationService.isValidTraineeCredentials("john.doe", "password");

            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("Trainer Authentication Tests")
    class TrainerAuthenticationTests {

        @Test
        @DisplayName("Should return trainer when credentials are valid")
        void authenticateTrainer_ShouldReturnTrainer_WhenCredentialsValid() {
            when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            Trainer authenticated = authenticationService.authenticateTrainer("jane.smith", "password");

            assertThat(authenticated).isEqualTo(testTrainer);
            assertThat(authenticated.getUsername()).isEqualTo("jane.smith");
            verify(passwordEncryption).matches("password", "$2a$12$encodedBCryptPassword");
        }

        @Test
        @DisplayName("Should throw exception when username is null")
        void authenticateTrainer_ShouldThrowException_WhenUsernameNull() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainer(null, "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Username is required");
        }

        @Test
        @DisplayName("Should throw exception when password is null")
        void authenticateTrainer_ShouldThrowException_WhenPasswordNull() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainer("jane.smith", null))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Password is required");
        }

        @Test
        @DisplayName("Should throw exception when username not found")
        void authenticateTrainer_ShouldThrowException_WhenUsernameNotFound() {
            when(trainerDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.authenticateTrainer("nonexistent", "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void authenticateTrainer_ShouldThrowException_WhenPasswordIncorrect() {
            when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
            when(passwordEncryption.matches("wrongPassword", "$2a$12$encodedBCryptPassword")).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.authenticateTrainer("jane.smith", "wrongPassword"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should throw exception when account is inactive")
        void authenticateTrainer_ShouldThrowException_WhenAccountInactive() {
            testTrainer.setIsActive(false);
            when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            assertThatThrownBy(() -> authenticationService.authenticateTrainer("jane.smith", "password"))
                    .isInstanceOf(InactiveAccountException.class)
                    .hasMessage("This account is inactive");
        }

        @Test
        @DisplayName("Should return true for valid trainer credentials")
        void isValidTrainerCredentials_ShouldReturnTrue_WhenCredentialsValid() {
            when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            boolean valid = authenticationService.isValidTrainerCredentials("jane.smith", "password");

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid trainer credentials")
        void isValidTrainerCredentials_ShouldReturnFalse_WhenCredentialsInvalid() {
            when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
            when(passwordEncryption.matches("wrongPassword", "$2a$12$encodedBCryptPassword")).thenReturn(false);

            boolean valid = authenticationService.isValidTrainerCredentials("jane.smith", "wrongPassword");

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false when trainer not found")
        void isValidTrainerCredentials_ShouldReturnFalse_WhenTrainerNotFound() {
            when(trainerDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

            boolean valid = authenticationService.isValidTrainerCredentials("nonexistent", "password");

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Should return false when trainer account is inactive")
        void isValidTrainerCredentials_ShouldReturnFalse_WhenAccountInactive() {
            testTrainer.setIsActive(false);
            when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
            when(passwordEncryption.matches("password", "$2a$12$encodedBCryptPassword")).thenReturn(true);

            boolean valid = authenticationService.isValidTrainerCredentials("jane.smith", "password");

            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("Access Validation Tests")
    class AccessValidationTests {

        @Test
        @DisplayName("Should pass validation when trainee username matches")
        void validateTraineeAccess_ShouldPass_WhenUsernameMatches() {
            when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));

            authenticationService.validateTraineeAccess("john.doe", 1L);

            verify(traineeDao).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when trainee username doesn't match")
        void validateTraineeAccess_ShouldThrowException_WhenUsernameDoesNotMatch() {
            when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));

            assertThatThrownBy(() -> authenticationService.validateTraineeAccess("other.user", 1L))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Users can only modify their own profile");
        }

        @Test
        @DisplayName("Should throw exception when trainee not found")
        void validateTraineeAccess_ShouldThrowException_WhenTraineeNotFound() {
            when(traineeDao.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.validateTraineeAccess("john.doe", 999L))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Target trainee not found");
        }

        @Test
        @DisplayName("Should throw exception when trainee validation parameters are null")
        void validateTraineeAccess_ShouldThrowException_WhenParametersNull() {
            assertThatThrownBy(() -> authenticationService.validateTraineeAccess(null, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Invalid access validation parameters");

            assertThatThrownBy(() -> authenticationService.validateTraineeAccess("john.doe", null))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Invalid access validation parameters");
        }

        @Test
        @DisplayName("Should pass validation when trainer username matches")
        void validateTrainerAccess_ShouldPass_WhenUsernameMatches() {
            when(trainerDao.findById(2L)).thenReturn(Optional.of(testTrainer));

            authenticationService.validateTrainerAccess("jane.smith", 2L);

            verify(trainerDao).findById(2L);
        }

        @Test
        @DisplayName("Should throw exception when trainer username doesn't match")
        void validateTrainerAccess_ShouldThrowException_WhenUsernameDoesNotMatch() {
            when(trainerDao.findById(2L)).thenReturn(Optional.of(testTrainer));

            assertThatThrownBy(() -> authenticationService.validateTrainerAccess("other.user", 2L))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Users can only modify their own profile");
        }

        @Test
        @DisplayName("Should throw exception when trainer not found")
        void validateTrainerAccess_ShouldThrowException_WhenTrainerNotFound() {
            when(trainerDao.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.validateTrainerAccess("jane.smith", 999L))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Target trainer not found");
        }

        @Test
        @DisplayName("Should throw exception when trainer validation parameters are null")
        void validateTrainerAccess_ShouldThrowException_WhenParametersNull() {
            assertThatThrownBy(() -> authenticationService.validateTrainerAccess(null, 2L))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Invalid access validation parameters");

            assertThatThrownBy(() -> authenticationService.validateTrainerAccess("jane.smith", null))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Invalid access validation parameters");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle whitespace in usernames")
        void shouldHandleWhitespaceInUsernames() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainee(" ", "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Username is required");

            assertThatThrownBy(() -> authenticationService.authenticateTrainer("  ", "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Username is required");
        }

        @Test
        @DisplayName("Should handle whitespace in passwords")
        void shouldHandleWhitespaceInPasswords() {
            assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", " "))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Password is required");

            assertThatThrownBy(() -> authenticationService.authenticateTrainer("jane.smith", "  "))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Password is required");
        }

        @Test
        @DisplayName("Should handle case sensitivity in usernames")
        void shouldHandleCaseSensitivityInUsernames() {
            when(traineeDao.findByUsername("JOHN.DOE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.authenticateTrainee("JOHN.DOE", "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should handle very long usernames")
        void shouldHandleVeryLongUsernames() {
            String longUsername = "a".repeat(1000);
            when(traineeDao.findByUsername(longUsername)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.authenticateTrainee(longUsername, "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should handle very long passwords")
        void shouldHandleVeryLongPasswords() {
            String longPassword = "a".repeat(1000);
            when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
            when(passwordEncryption.matches(longPassword, "$2a$12$encodedBCryptPassword")).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", longPassword))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should handle special characters in usernames and passwords")
        void shouldHandleSpecialCharacters() {
            String specialUsername = "user@domain.com";
            String specialPassword = "P@ssw0rd!#$%";

            Trainee specialTrainee = new Trainee("Special", "User", LocalDate.of(1990, 1, 1), "123 Test St");
            specialTrainee.setUsername(specialUsername);
            specialTrainee.setPassword("$2a$12$encodedSpecialPassword");
            specialTrainee.setIsActive(true);

            when(traineeDao.findByUsername(specialUsername)).thenReturn(Optional.of(specialTrainee));
            when(passwordEncryption.matches(specialPassword, "$2a$12$encodedSpecialPassword")).thenReturn(true);

            Trainee authenticated = authenticationService.authenticateTrainee(specialUsername, specialPassword);

            assertThat(authenticated).isEqualTo(specialTrainee);
        }
    }
}