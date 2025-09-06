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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        testTrainee.setPassword("encodedPassword");
        testTrainee.setIsActive(true);

        TrainingType specialization = new TrainingType("Cardio");
        testTrainer = new Trainer("Jane", "Smith", specialization);
        testTrainer.setId(2L);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("encodedPassword");
        testTrainer.setIsActive(true);
    }

    @Test
    void authenticateTrainee_ShouldReturnTrainee_WhenCredentialsValid() {
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("password", "encodedPassword")).thenReturn(true);

        Trainee authenticated = authenticationService.authenticateTrainee("john.doe", "password");

        assertThat(authenticated).isEqualTo(testTrainee);
        assertThat(authenticated.getUsername()).isEqualTo("john.doe");
    }

    @Test
    void authenticateTrainee_ShouldMigratePassword_WhenNotEncoded() {
        testTrainee.setPassword("plainPassword");
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(passwordEncryption.isEncoded("plainPassword")).thenReturn(false);
        when(passwordEncryption.encode("plainPassword")).thenReturn("newEncodedPassword");
        when(traineeDao.update(any(Trainee.class))).thenReturn(testTrainee);

        Trainee authenticated = authenticationService.authenticateTrainee("john.doe", "plainPassword");

        assertThat(authenticated).isEqualTo(testTrainee);
        verify(passwordEncryption).encode("plainPassword");
        verify(traineeDao).update(argThat(t -> t.getPassword().equals("newEncodedPassword")));
    }

    @Test
    void authenticateTrainee_ShouldThrowException_WhenUsernameNull() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainee(null, "password"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Username is required");
    }

    @Test
    void authenticateTrainee_ShouldThrowException_WhenUsernameEmpty() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainee("", "password"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Username is required");
    }

    @Test
    void authenticateTrainee_ShouldThrowException_WhenPasswordNull() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", null))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Password is required");
    }

    @Test
    void authenticateTrainee_ShouldThrowException_WhenPasswordEmpty() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", ""))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Password is required");
    }

    @Test
    void authenticateTrainee_ShouldThrowException_WhenUsernameNotFound() {
        when(traineeDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticateTrainee("nonexistent", "password"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticateTrainee_ShouldThrowException_WhenPasswordIncorrect() {
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", "wrongPassword"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticateTrainee_ShouldThrowException_WhenAccountInactive() {
        testTrainee.setIsActive(false);
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("password", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.authenticateTrainee("john.doe", "password"))
                .isInstanceOf(InactiveAccountException.class)
                .hasMessage("This account is inactive");
    }

    @Test
    void authenticateTrainer_ShouldReturnTrainer_WhenCredentialsValid() {
        when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("password", "encodedPassword")).thenReturn(true);

        Trainer authenticated = authenticationService.authenticateTrainer("jane.smith", "password");

        assertThat(authenticated).isEqualTo(testTrainer);
        assertThat(authenticated.getUsername()).isEqualTo("jane.smith");
    }

    @Test
    void authenticateTrainer_ShouldMigratePassword_WhenNotEncoded() {
        testTrainer.setPassword("plainPassword");
        when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
        when(passwordEncryption.isEncoded("plainPassword")).thenReturn(false);
        when(passwordEncryption.encode("plainPassword")).thenReturn("newEncodedPassword");
        when(trainerDao.update(any(Trainer.class))).thenReturn(testTrainer);

        Trainer authenticated = authenticationService.authenticateTrainer("jane.smith", "plainPassword");

        assertThat(authenticated).isEqualTo(testTrainer);
        verify(passwordEncryption).encode("plainPassword");
        verify(trainerDao).update(argThat(t -> t.getPassword().equals("newEncodedPassword")));
    }

    @Test
    void authenticateTrainer_ShouldThrowException_WhenUsernameNull() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainer(null, "password"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Username is required");
    }

    @Test
    void authenticateTrainer_ShouldThrowException_WhenPasswordNull() {
        assertThatThrownBy(() -> authenticationService.authenticateTrainer("jane.smith", null))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Password is required");
    }

    @Test
    void authenticateTrainer_ShouldThrowException_WhenUsernameNotFound() {
        when(trainerDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticateTrainer("nonexistent", "password"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticateTrainer_ShouldThrowException_WhenPasswordIncorrect() {
        when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.authenticateTrainer("jane.smith", "wrongPassword"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticateTrainer_ShouldThrowException_WhenAccountInactive() {
        testTrainer.setIsActive(false);
        when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("password", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.authenticateTrainer("jane.smith", "password"))
                .isInstanceOf(InactiveAccountException.class)
                .hasMessage("This account is inactive");
    }

    @Test
    void validateTraineeAccess_ShouldPass_WhenUsernameMatches() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));

        authenticationService.validateTraineeAccess("john.doe", 1L);

        verify(traineeDao).findById(1L);
    }

    @Test
    void validateTraineeAccess_ShouldThrowException_WhenUsernameDoesNotMatch() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));

        assertThatThrownBy(() -> authenticationService.validateTraineeAccess("other.user", 1L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Users can only modify their own profile");
    }

    @Test
    void validateTraineeAccess_ShouldThrowException_WhenTraineeNotFound() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.validateTraineeAccess("john.doe", 999L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Target trainee not found");
    }

    @Test
    void validateTraineeAccess_ShouldThrowException_WhenParametersNull() {
        assertThatThrownBy(() -> authenticationService.validateTraineeAccess(null, 1L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Invalid access validation parameters");

        assertThatThrownBy(() -> authenticationService.validateTraineeAccess("john.doe", null))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Invalid access validation parameters");
    }

    @Test
    void validateTrainerAccess_ShouldPass_WhenUsernameMatches() {
        when(trainerDao.findById(2L)).thenReturn(Optional.of(testTrainer));

        authenticationService.validateTrainerAccess("jane.smith", 2L);

        verify(trainerDao).findById(2L);
    }

    @Test
    void validateTrainerAccess_ShouldThrowException_WhenUsernameDoesNotMatch() {
        when(trainerDao.findById(2L)).thenReturn(Optional.of(testTrainer));

        assertThatThrownBy(() -> authenticationService.validateTrainerAccess("other.user", 2L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Users can only modify their own profile");
    }

    @Test
    void validateTrainerAccess_ShouldThrowException_WhenTrainerNotFound() {
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.validateTrainerAccess("jane.smith", 999L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Target trainer not found");
    }

    @Test
    void validateTrainerAccess_ShouldThrowException_WhenParametersNull() {
        assertThatThrownBy(() -> authenticationService.validateTrainerAccess(null, 2L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Invalid access validation parameters");

        assertThatThrownBy(() -> authenticationService.validateTrainerAccess("jane.smith", null))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Invalid access validation parameters");
    }

    @Test
    void isValidTraineeCredentials_ShouldReturnTrue_WhenCredentialsValid() {
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("password", "encodedPassword")).thenReturn(true);

        boolean valid = authenticationService.isValidTraineeCredentials("john.doe", "password");

        assertThat(valid).isTrue();
    }

    @Test
    void isValidTraineeCredentials_ShouldReturnFalse_WhenCredentialsInvalid() {
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.empty());

        boolean valid = authenticationService.isValidTraineeCredentials("john.doe", "password");

        assertThat(valid).isFalse();
    }

    @Test
    void isValidTrainerCredentials_ShouldReturnTrue_WhenCredentialsValid() {
        when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("password", "encodedPassword")).thenReturn(true);

        boolean valid = authenticationService.isValidTrainerCredentials("jane.smith", "password");

        assertThat(valid).isTrue();
    }

    @Test
    void isValidTrainerCredentials_ShouldReturnFalse_WhenCredentialsInvalid() {
        when(trainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));
        when(passwordEncryption.isEncoded("encodedPassword")).thenReturn(true);
        when(passwordEncryption.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        boolean valid = authenticationService.isValidTrainerCredentials("jane.smith", "wrongPassword");

        assertThat(valid).isFalse();
    }
}