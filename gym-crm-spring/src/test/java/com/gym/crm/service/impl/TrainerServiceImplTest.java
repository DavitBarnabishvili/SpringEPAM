package com.gym.crm.service.impl;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dto.request.ChangeLoginRequest;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.entity.User;
import com.gym.crm.exception.InvalidCredentialsException;
import com.gym.crm.service.TrainerService;
import com.gym.crm.util.AuthenticationService;
import com.gym.crm.util.CredentialsGeneratorService;
import com.gym.crm.util.PasswordEncryption;
import com.gym.crm.util.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CredentialsGeneratorService credentialsGenerator;

    @Mock
    private ValidationService validationService;

    @Mock
    private PasswordEncryption passwordEncryption;

    private TrainerService trainerService;
    private Trainer testTrainer;
    private TrainingType testSpecialization;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl(
                trainerDao,
                authenticationService,
                credentialsGenerator,
                validationService,
                passwordEncryption
        );

        testSpecialization = new TrainingType("Cardio");
        testSpecialization.setId(1L);

        testTrainer = new Trainer("John", "Trainer", testSpecialization);
        testTrainer.setId(1L);
        testTrainer.setUsername("john.trainer");
        testTrainer.setPassword("encodedPassword");
        testTrainer.setIsActive(true);
    }

    @Test
    void createTrainer_ShouldGenerateCredentialsAndPersist() {
        Trainer inputTrainer = new Trainer("John", "Trainer", testSpecialization);

        when(credentialsGenerator.generateUsername("John", "Trainer")).thenReturn("john.trainer");
        when(credentialsGenerator.generatePassword()).thenReturn("rawPassword123");
        when(passwordEncryption.encode("rawPassword123")).thenReturn("encodedPassword");
        when(trainerDao.create(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainer created = trainerService.createTrainer(inputTrainer);

        assertThat(created).isNotNull();
        assertThat(created.getUsername()).isEqualTo("john.trainer");
        assertThat(created.getPassword()).isEqualTo("rawPassword123");
        assertThat(created.getIsActive()).isTrue();
        assertThat(created.getSpecialization()).isEqualTo(testSpecialization);

        verify(validationService).validateTrainer(any(Trainer.class));
        verify(trainerDao).create(any(Trainer.class));
    }

    @Test
    void createTrainer_ShouldThrowException_WhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerService.createTrainer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer cannot be null");
    }

    @Test
    void createTrainer_ShouldOverridePresetCredentials() {
        Trainer inputTrainer = new Trainer("John", "Trainer", testSpecialization);
        inputTrainer.setUsername("preset.username");
        inputTrainer.setPassword("preset.password");

        when(credentialsGenerator.generateUsername("John", "Trainer")).thenReturn("john.trainer");
        when(credentialsGenerator.generatePassword()).thenReturn("generatedPassword");
        when(passwordEncryption.encode("generatedPassword")).thenReturn("encodedPassword");
        when(trainerDao.create(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainer created = trainerService.createTrainer(inputTrainer);

        assertThat(created.getUsername()).isEqualTo("john.trainer");
        assertThat(created.getPassword()).isEqualTo("generatedPassword");
    }

    @Test
    void updateTrainer_ShouldUpdateExistingTrainer() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(testTrainer);

        Trainer updatedTrainer = trainerService.updateTrainer("JWT_AUTH", "JWT_AUTH", testTrainer);

        assertThat(updatedTrainer).isNotNull();
        assertThat(updatedTrainer.getUsername()).isEqualTo("john.trainer");
        assertThat(updatedTrainer.getSpecialization()).isEqualTo(testSpecialization);

        verify(validationService).validateTrainer(testTrainer);
        verify(trainerDao).update(testTrainer);
    }

    @Test
    void updateTrainer_ShouldPreserveSpecialization() {
        TrainingType newSpecialization = new TrainingType("Strength");
        newSpecialization.setId(2L);

        Trainer updateRequest = new Trainer("John", "Updated", newSpecialization);
        updateRequest.setId(1L);

        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer updated = trainerService.updateTrainer("JWT_AUTH", "JWT_AUTH", updateRequest);

        assertThat(updated.getSpecialization()).isEqualTo(testSpecialization);
        assertThat(updated.getSpecialization()).isNotEqualTo(newSpecialization);
    }

    @Test
    void updateTrainer_ShouldAuthenticateWhenNotJWT() {
        when(authenticationService.authenticateTrainer("john.trainer", "password")).thenReturn(testTrainer);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(testTrainer);

        trainerService.updateTrainer("john.trainer", "password", testTrainer);

        verify(authenticationService).authenticateTrainer("john.trainer", "password");
        verify(authenticationService).validateTrainerAccess("john.trainer", 1L);
    }

    @Test
    void updateTrainer_ShouldThrowException_WhenTrainerNotFound() {
        when(trainerDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateTrainer("JWT_AUTH", "JWT_AUTH", testTrainer))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trainer not found with id: 1");
    }

    @Test
    void updateTrainer_ShouldThrowException_WhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerService.updateTrainer("user", "pass", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer cannot be null");
    }

    @Test
    void updateTrainer_ShouldThrowException_WhenIdIsNull() {
        Trainer trainerWithoutId = new Trainer("John", "Trainer", testSpecialization);

        assertThatThrownBy(() -> trainerService.updateTrainer("user", "pass", trainerWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer id is required for update");
    }

    @Test
    void activateTrainer_ShouldSetActiveTrue() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(testTrainer);

        boolean result = trainerService.activateTrainer("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isTrue();
        verify(trainerDao).update(argThat(User::getIsActive));
    }

    @Test
    void activateTrainer_ShouldAuthenticateWhenNotJWT() {
        when(authenticationService.authenticateTrainer("john.trainer", "password")).thenReturn(testTrainer);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(testTrainer);

        trainerService.activateTrainer("john.trainer", "password", 1L);

        verify(authenticationService).authenticateTrainer("john.trainer", "password");
        verify(authenticationService).validateTrainerAccess("john.trainer", 1L);
    }

    @Test
    void deactivateTrainer_ShouldSetActiveFalse() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(testTrainer);

        boolean result = trainerService.deactivateTrainer("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isTrue();
        verify(trainerDao).update(argThat(t -> !t.getIsActive()));
    }

    @Test
    void activateTrainer_ShouldReturnFalse_WhenTrainerNotFound() {
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());

        boolean result = trainerService.activateTrainer("JWT_AUTH", "JWT_AUTH", 999L);

        assertThat(result).isFalse();
    }

    @Test
    void activateTrainer_ShouldReturnFalse_WhenIdIsNull() {
        boolean result = trainerService.activateTrainer("JWT_AUTH", "JWT_AUTH", null);

        assertThat(result).isFalse();
        verify(trainerDao, never()).findById(any());
    }

    @Test
    void deactivateTrainer_ShouldReturnFalse_WhenTrainerNotFound() {
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());

        boolean result = trainerService.deactivateTrainer("JWT_AUTH", "JWT_AUTH", 999L);

        assertThat(result).isFalse();
    }

    @Test
    void deactivateTrainer_ShouldReturnFalse_WhenIdIsNull() {
        boolean result = trainerService.deactivateTrainer("JWT_AUTH", "JWT_AUTH", null);

        assertThat(result).isFalse();
        verify(trainerDao, never()).findById(any());
    }

    @Test
    void findTrainerById_ShouldReturnTrainer_WhenExists() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> found = trainerService.findTrainerById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.trainer");
    }

    @Test
    void findTrainerById_ShouldReturnEmpty_WhenNotExists() {
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Trainer> found = trainerService.findTrainerById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findTrainerById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<Trainer> found = trainerService.findTrainerById(null);

        assertThat(found).isEmpty();
        verify(trainerDao, never()).findById(any());
    }

    @Test
    void findTrainerByUsername_ShouldReturnTrainer_WhenExists() {
        when(trainerDao.findByUsername("john.trainer")).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> found = trainerService.findTrainerByUsername("john.trainer");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.trainer");
    }

    @Test
    void findTrainerByUsername_ShouldReturnEmpty_WhenNotExists() {
        when(trainerDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<Trainer> found = trainerService.findTrainerByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void findTrainerByUsername_ShouldReturnEmpty_WhenUsernameIsNullOrEmpty() {
        Optional<Trainer> found1 = trainerService.findTrainerByUsername(null);
        Optional<Trainer> found2 = trainerService.findTrainerByUsername("");
        Optional<Trainer> found3 = trainerService.findTrainerByUsername("  ");

        assertThat(found1).isEmpty();
        assertThat(found2).isEmpty();
        assertThat(found3).isEmpty();
        verify(trainerDao, never()).findByUsername(any());
    }

    @Test
    void findAllTrainers_ShouldReturnAllTrainers() {
        List<Trainer> trainers = Arrays.asList(testTrainer, new Trainer("Jane", "Coach", testSpecialization));
        when(trainerDao.findAll()).thenReturn(trainers);

        List<Trainer> found = trainerService.findAllTrainers();

        assertThat(found).hasSize(2);
        assertThat(found).isEqualTo(trainers);
    }

    @Test
    void findTrainersBySpecialization_ShouldReturnMatchingTrainers() {
        Trainer cardioTrainer1 = new Trainer("Trainer1", "One", testSpecialization);
        Trainer cardioTrainer2 = new Trainer("Trainer2", "Two", testSpecialization);

        TrainingType strengthType = new TrainingType("Strength");
        strengthType.setId(2L);
        Trainer strengthTrainer = new Trainer("Trainer3", "Three", strengthType);

        when(trainerDao.findAll()).thenReturn(Arrays.asList(cardioTrainer1, cardioTrainer2, strengthTrainer));

        List<Trainer> found = trainerService.findTrainersBySpecialization(testSpecialization);

        assertThat(found).hasSize(2);
        assertThat(found).contains(cardioTrainer1, cardioTrainer2);
        assertThat(found).doesNotContain(strengthTrainer);
    }

    @Test
    void findTrainersBySpecialization_ShouldReturnEmpty_WhenSpecializationIsNull() {
        List<Trainer> found = trainerService.findTrainersBySpecialization(null);

        assertThat(found).isEmpty();
        verify(trainerDao, never()).findAll();
    }

    @Test
    void findTrainersBySpecialization_ShouldFilterOutNullSpecializations() {
        Trainer trainerWithSpec = new Trainer("With", "Spec", testSpecialization);
        Trainer trainerWithoutSpec = new Trainer("Without", "Spec", null);

        when(trainerDao.findAll()).thenReturn(Arrays.asList(trainerWithSpec, trainerWithoutSpec));

        List<Trainer> found = trainerService.findTrainersBySpecialization(testSpecialization);

        assertThat(found).hasSize(1);
        assertThat(found).contains(trainerWithSpec);
    }

    @Test
    void trainerExists_ShouldReturnTrue_WhenExists() {
        when(trainerDao.existsById(1L)).thenReturn(true);

        boolean exists = trainerService.trainerExists(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void trainerExists_ShouldReturnFalse_WhenNotExists() {
        when(trainerDao.existsById(999L)).thenReturn(false);

        boolean exists = trainerService.trainerExists(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void trainerExists_ShouldReturnFalse_WhenIdIsNull() {
        boolean exists = trainerService.trainerExists(null);

        assertThat(exists).isFalse();
    }

    @Test
    void changePassword_ShouldUpdatePassword() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("john.trainer");
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(passwordEncryption.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncryption.encode("newPassword")).thenReturn("newEncodedPassword");
        when(trainerDao.update(any(Trainer.class))).thenReturn(testTrainer);

        trainerService.changePassword(testTrainer, request);

        verify(passwordEncryption).matches("oldPassword", "encodedPassword");
        verify(passwordEncryption).encode("newPassword");
        verify(trainerDao).update(argThat(t -> t.getPassword().equals("newEncodedPassword")));
    }

    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordInvalid() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(passwordEncryption.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> trainerService.changePassword(testTrainer, request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid old password");
    }

    @Test
    void activateTrainer_ShouldHandleUpdateException() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenThrow(new RuntimeException("DB error"));

        boolean result = trainerService.activateTrainer("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isFalse();
    }

    @Test
    void deactivateTrainer_ShouldHandleUpdateException() {
        when(trainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));
        when(trainerDao.update(any(Trainer.class))).thenThrow(new RuntimeException("DB error"));

        boolean result = trainerService.deactivateTrainer("JWT_AUTH", "JWT_AUTH", 1L);

        assertThat(result).isFalse();
    }
}