package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.dto.request.ChangeLoginRequest;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.User;
import com.gym.crm.exception.InvalidCredentialsException;
import com.gym.crm.exception.UserNotFoundException;
import com.gym.crm.service.TraineeService;
import com.gym.crm.util.AuthenticationService;
import com.gym.crm.util.CredentialsGeneratorService;
import com.gym.crm.util.PasswordEncryption;
import com.gym.crm.util.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CredentialsGeneratorService credentialsGenerator;

    @Mock
    private ValidationService validationService;

    @Mock
    private PasswordEncryption passwordEncryption;

    private TraineeService traineeService;
    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl(
                traineeDao,
                trainingDao,
                authenticationService,
                credentialsGenerator,
                validationService,
                passwordEncryption
        );

        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Test St");
        testTrainee.setId(1L);
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("encodedPassword");
        testTrainee.setIsActive(true);
    }

    @Test
    void createTrainee_ShouldGenerateCredentialsAndPersist() {
        Trainee inputTrainee = new Trainee("John", "Doe");

        when(credentialsGenerator.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(credentialsGenerator.generatePassword()).thenReturn("rawPassword123");
        when(passwordEncryption.encode("rawPassword123")).thenReturn("encodedPassword");
        when(traineeDao.create(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainee created = traineeService.createTrainee(inputTrainee);

        assertThat(created).isNotNull();
        assertThat(created.getUsername()).isEqualTo("john.doe");
        assertThat(created.getPassword()).isEqualTo("rawPassword123");
        assertThat(created.getIsActive()).isTrue();

        verify(validationService).validateTrainee(any(Trainee.class));
        verify(traineeDao).create(any(Trainee.class));
    }

    @Test
    void createTrainee_ShouldThrowException_WhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeService.createTrainee(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee cannot be null");
    }

    @Test
    void createTrainee_ShouldOverridePresetCredentials() {
        Trainee inputTrainee = new Trainee("John", "Doe");
        inputTrainee.setUsername("preset.username");
        inputTrainee.setPassword("preset.password");

        when(credentialsGenerator.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(credentialsGenerator.generatePassword()).thenReturn("generatedPassword");
        when(passwordEncryption.encode("generatedPassword")).thenReturn("encodedPassword");
        when(traineeDao.create(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainee created = traineeService.createTrainee(inputTrainee);

        assertThat(created.getUsername()).isEqualTo("john.doe");
        assertThat(created.getPassword()).isEqualTo("generatedPassword");
    }

    @Test
    void updateTrainee_ShouldUpdateExistingTrainee() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(testTrainee);

        Trainee updatedTrainee = traineeService.updateTrainee("JWT_AUTH", "JWT_AUTH", testTrainee);

        assertThat(updatedTrainee).isNotNull();
        assertThat(updatedTrainee.getUsername()).isEqualTo("john.doe");

        verify(validationService).validateTrainee(testTrainee);
        verify(traineeDao).update(testTrainee);
    }

    @Test
    void updateTrainee_ShouldAuthenticateWhenNotJWT() {
        when(authenticationService.authenticateTrainee("john.doe", "password")).thenReturn(testTrainee);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(testTrainee);

        traineeService.updateTrainee("john.doe", "password", testTrainee);

        verify(authenticationService).authenticateTrainee("john.doe", "password");
        verify(authenticationService).validateTraineeAccess("john.doe", 1L);
    }

    @Test
    void updateTrainee_ShouldThrowException_WhenTraineeNotFound() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainee("JWT_AUTH", "JWT_AUTH", testTrainee))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Trainee not found with id: 1");
    }

    @Test
    void updateTrainee_ShouldThrowException_WhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeService.updateTrainee("user", "pass", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee cannot be null");
    }

    @Test
    void updateTrainee_ShouldThrowException_WhenIdIsNull() {
        Trainee traineeWithoutId = new Trainee("John", "Doe");

        assertThatThrownBy(() -> traineeService.updateTrainee("user", "pass", traineeWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee id is required for update");
    }

    @Test
    void deleteTrainee_ShouldDeleteTraineeAndTrainings() {
        when(trainingDao.deleteByTraineeId(1L)).thenReturn(5);
        when(traineeDao.delete(1L)).thenReturn(true);

        boolean result = traineeService.deleteTrainee("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isTrue();
        verify(trainingDao).deleteByTraineeId(1L);
        verify(traineeDao).delete(1L);
    }

    @Test
    void deleteTrainee_ShouldAuthenticateWhenNotJWT() {
        when(authenticationService.authenticateTrainee("john.doe", "password")).thenReturn(testTrainee);
        when(traineeDao.delete(1L)).thenReturn(true);

        traineeService.deleteTrainee("john.doe", "password", 1L);

        verify(authenticationService).authenticateTrainee("john.doe", "password");
        verify(authenticationService).validateTraineeAccess("john.doe", 1L);
    }

    @Test
    void deleteTrainee_ShouldReturnFalse_WhenIdIsNull() {
        boolean result = traineeService.deleteTrainee("user", "pass", null);

        assertThat(result).isFalse();
        verify(traineeDao, never()).delete(any());
    }

    @Test
    void deleteTrainee_ShouldReturnFalse_WhenTraineeNotFound() {
        when(traineeDao.delete(999L)).thenReturn(false);

        boolean result = traineeService.deleteTrainee("JWT_AUTH", "JWT_AUTH", 999L);

        assertThat(result).isFalse();
    }

    @Test
    void findTraineeById_ShouldReturnTrainee_WhenExists() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> found = traineeService.findTraineeById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
    }

    @Test
    void findTraineeById_ShouldReturnEmpty_WhenNotExists() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Trainee> found = traineeService.findTraineeById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findTraineeById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<Trainee> found = traineeService.findTraineeById(null);

        assertThat(found).isEmpty();
        verify(traineeDao, never()).findById(any());
    }

    @Test
    void findTraineeByUsername_ShouldReturnTrainee_WhenExists() {
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> found = traineeService.findTraineeByUsername("john.doe");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
    }

    @Test
    void findTraineeByUsername_ShouldReturnEmpty_WhenNotExists() {
        when(traineeDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<Trainee> found = traineeService.findTraineeByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void findTraineeByUsername_ShouldReturnEmpty_WhenUsernameIsNullOrEmpty() {
        Optional<Trainee> found1 = traineeService.findTraineeByUsername(null);
        Optional<Trainee> found2 = traineeService.findTraineeByUsername("");
        Optional<Trainee> found3 = traineeService.findTraineeByUsername("  ");

        assertThat(found1).isEmpty();
        assertThat(found2).isEmpty();
        assertThat(found3).isEmpty();
        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    void findAllTrainees_ShouldReturnAllTrainees() {
        List<Trainee> trainees = Arrays.asList(testTrainee, new Trainee("Jane", "Smith"));
        when(traineeDao.findAll()).thenReturn(trainees);

        List<Trainee> found = traineeService.findAllTrainees();

        assertThat(found).hasSize(2);
        assertThat(found).isEqualTo(trainees);
    }

    @Test
    void findAllActiveTrainees_ShouldReturnActiveTrainees() {
        List<Trainee> activeTrainees = Collections.singletonList(testTrainee);
        when(traineeDao.findAllActive()).thenReturn(activeTrainees);

        List<Trainee> found = traineeService.findAllActiveTrainees();

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getIsActive()).isTrue();
    }

    @Test
    void findTraineesByAgeRange_ShouldReturnTraineesInRange() {
        LocalDate birthDate25YearsAgo = LocalDate.now().minusYears(25);
        LocalDate birthDate30YearsAgo = LocalDate.now().minusYears(30);

        Trainee trainee25 = new Trainee("Young", "Person");
        trainee25.setDateOfBirth(birthDate25YearsAgo);

        Trainee trainee30 = new Trainee("Older", "Person");
        trainee30.setDateOfBirth(birthDate30YearsAgo);

        Trainee trainee40 = new Trainee("Oldest", "Person");
        trainee40.setDateOfBirth(LocalDate.now().minusYears(40));

        when(traineeDao.findAll()).thenReturn(Arrays.asList(trainee25, trainee30, trainee40));

        List<Trainee> found = traineeService.findTraineesByAgeRange(24, 31);

        assertThat(found).hasSize(2);
        assertThat(found).contains(trainee25, trainee30);
    }

    @Test
    void findTraineesByAgeRange_ShouldReturnEmpty_WhenInvalidRange() {
        List<Trainee> found1 = traineeService.findTraineesByAgeRange(-1, 30);
        List<Trainee> found2 = traineeService.findTraineesByAgeRange(30, 20);
        List<Trainee> found3 = traineeService.findTraineesByAgeRange(20, -5);

        assertThat(found1).isEmpty();
        assertThat(found2).isEmpty();
        assertThat(found3).isEmpty();
    }

    @Test
    void activateTrainee_ShouldSetActiveTrue() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(testTrainee);

        boolean result = traineeService.activateTrainee("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isTrue();
        verify(traineeDao).update(argThat(User::getIsActive));
    }

    @Test
    void activateTrainee_ShouldAuthenticateWhenNotJWT() {
        when(authenticationService.authenticateTrainee("john.doe", "password")).thenReturn(testTrainee);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(testTrainee);

        traineeService.activateTrainee("john.doe", "password", 1L);

        verify(authenticationService).authenticateTrainee("john.doe", "password");
        verify(authenticationService).validateTraineeAccess("john.doe", 1L);
    }

    @Test
    void deactivateTrainee_ShouldSetActiveFalse() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(testTrainee);

        boolean result = traineeService.deactivateTrainee("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isTrue();
        verify(traineeDao).update(argThat(t -> !t.getIsActive()));
    }

    @Test
    void activateTrainee_ShouldReturnFalse_WhenTraineeNotFound() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        boolean result = traineeService.activateTrainee("JWT_AUTH", "JWT_AUTH", 999L);

        assertThat(result).isFalse();
    }

    @Test
    void activateTrainee_ShouldReturnFalse_WhenIdIsNull() {
        boolean result = traineeService.activateTrainee("JWT_AUTH", "JWT_AUTH", null);

        assertThat(result).isFalse();
        verify(traineeDao, never()).findById(any());
    }

    @Test
    void traineeExists_ShouldReturnTrue_WhenExists() {
        when(traineeDao.existsById(1L)).thenReturn(true);

        boolean exists = traineeService.traineeExists(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void traineeExists_ShouldReturnFalse_WhenNotExists() {
        when(traineeDao.existsById(999L)).thenReturn(false);

        boolean exists = traineeService.traineeExists(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void traineeExists_ShouldReturnFalse_WhenIdIsNull() {
        boolean exists = traineeService.traineeExists(null);

        assertThat(exists).isFalse();
    }

    @Test
    void changePassword_ShouldUpdatePassword() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("john.doe");
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(passwordEncryption.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncryption.encode("newPassword")).thenReturn("newEncodedPassword");
        when(traineeDao.update(any(Trainee.class))).thenReturn(testTrainee);

        traineeService.changePassword(testTrainee, request);

        verify(passwordEncryption).matches("oldPassword", "encodedPassword");
        verify(passwordEncryption).encode("newPassword");
        verify(traineeDao).update(argThat(t -> t.getPassword().equals("newEncodedPassword")));
    }

    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordInvalid() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(passwordEncryption.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> traineeService.changePassword(testTrainee, request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid old password");
    }

    @Test
    void activateTrainee_ShouldHandleUpdateException() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(traineeDao.update(any(Trainee.class))).thenThrow(new RuntimeException("DB error"));

        boolean result = traineeService.activateTrainee("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isFalse();
    }

    @Test
    void deactivateTrainee_ShouldHandleUpdateException() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(traineeDao.update(any(Trainee.class))).thenThrow(new RuntimeException("DB error"));

        boolean result = traineeService.deactivateTrainee("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isFalse();
    }
}