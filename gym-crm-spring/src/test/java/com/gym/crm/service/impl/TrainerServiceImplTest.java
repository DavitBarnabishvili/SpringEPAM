package com.gym.crm.service.impl;


import com.gym.crm.entity.Trainer;
import com.gym.crm.util.CredentialsGeneratorService;
import com.gym.crm.util.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl Tests")
class TrainerServiceImplTest {

    @Mock
    private com.gym.crm.dao.TrainerDao mockTrainerDao;

    @Mock
    private CredentialsGeneratorService mockCredentialsGenerator;

    @Mock
    private ValidationService mockValidationService;

    private TrainerServiceImpl trainerService;
    private com.gym.crm.entity.Trainer testTrainer;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl(mockTrainerDao, mockCredentialsGenerator, mockValidationService);
        com.gym.crm.entity.TrainingType specialization = new com.gym.crm.entity.TrainingType("Cardio");
        testTrainer = new com.gym.crm.entity.Trainer("Jane", "Smith", specialization);
        testTrainer.setUserId(1L);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("password456");
    }

    @Test
    @DisplayName("createTrainer should throw exception for null trainer")
    void createTrainer_WithNullTrainer_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.createTrainer(null));
    }

    @Test
    @DisplayName("createTrainer should validate trainer")
    void createTrainer_ShouldValidateTrainer() {
        com.gym.crm.entity.Trainer newTrainer = new com.gym.crm.entity.Trainer("John", "Doe");
        when(mockCredentialsGenerator.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(mockCredentialsGenerator.generatePassword()).thenReturn("password123");
        when(mockTrainerDao.create(any(com.gym.crm.entity.Trainer.class))).thenReturn(newTrainer);

        trainerService.createTrainer(newTrainer);

        verify(mockValidationService).validateTrainer(newTrainer);
    }

    @Test
    @DisplayName("createTrainer should generate credentials")
    void createTrainer_ShouldGenerateCredentials() {
        com.gym.crm.entity.Trainer newTrainer = new com.gym.crm.entity.Trainer("John", "Doe");
        when(mockCredentialsGenerator.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(mockCredentialsGenerator.generatePassword()).thenReturn("password123");
        when(mockTrainerDao.create(any(com.gym.crm.entity.Trainer.class))).thenReturn(newTrainer);

        trainerService.createTrainer(newTrainer);

        verify(mockCredentialsGenerator).generateUsername("John", "Doe");
        verify(mockCredentialsGenerator).generatePassword();
        assertEquals("john.doe", newTrainer.getUsername());
        assertEquals("password123", newTrainer.getPassword());
        assertTrue(newTrainer.getIsActive());
    }

    @Test
    @DisplayName("createTrainer should save trainer via DAO")
    void createTrainer_ShouldSaveTrainerViaDao() {
        com.gym.crm.entity.Trainer newTrainer = new com.gym.crm.entity.Trainer("John", "Doe");
        when(mockCredentialsGenerator.generateUsername(anyString(), anyString())).thenReturn("john.doe");
        when(mockCredentialsGenerator.generatePassword()).thenReturn("password123");
        when(mockTrainerDao.create(newTrainer)).thenReturn(newTrainer);

        com.gym.crm.entity.Trainer result = trainerService.createTrainer(newTrainer);

        verify(mockTrainerDao).create(newTrainer);
        assertEquals(newTrainer, result);
    }

    @Test
    @DisplayName("updateTrainer should throw exception for null trainer")
    void updateTrainer_WithNullTrainer_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.updateTrainer(null));
    }

    @Test
    @DisplayName("updateTrainer should throw exception when trainer has no ID")
    void updateTrainer_WithoutId_ShouldThrowException() {
        com.gym.crm.entity.Trainer trainerWithoutId = new com.gym.crm.entity.Trainer("John", "Doe");

        assertThrows(IllegalArgumentException.class, () -> trainerService.updateTrainer(trainerWithoutId));
    }

    @Test
    @DisplayName("updateTrainer should throw exception when trainer not found")
    void updateTrainer_WhenTrainerNotFound_ShouldThrowException() {
        when(mockTrainerDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainerService.updateTrainer(testTrainer));
    }

    @Test
    @DisplayName("updateTrainer should preserve credentials")
    void updateTrainer_ShouldPreserveCredentials() {
        com.gym.crm.entity.Trainer existingTrainer = new com.gym.crm.entity.Trainer("Jane", "Smith");
        existingTrainer.setUserId(1L);
        existingTrainer.setUsername("jane.smith");
        existingTrainer.setPassword("original.password");

        com.gym.crm.entity.TrainingType newSpecialization = new com.gym.crm.entity.TrainingType("Strength");
        com.gym.crm.entity.Trainer updatedTrainer = new com.gym.crm.entity.Trainer("Jane", "Smith", newSpecialization);
        updatedTrainer.setUserId(1L);

        when(mockTrainerDao.findById(1L)).thenReturn(Optional.of(existingTrainer));
        when(mockTrainerDao.update(updatedTrainer)).thenReturn(updatedTrainer);

        trainerService.updateTrainer(updatedTrainer);

        assertEquals("jane.smith", updatedTrainer.getUsername());
        assertEquals("original.password", updatedTrainer.getPassword());
        verify(mockTrainerDao).update(updatedTrainer);
    }

    @Test
    @DisplayName("findTrainerById should return empty for null ID")
    void findTrainerById_WithNullId_ShouldReturnEmpty() {
        Optional<com.gym.crm.entity.Trainer> result = trainerService.findTrainerById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findTrainerById should delegate to DAO")
    void findTrainerById_ShouldDelegateToDao() {
        when(mockTrainerDao.findById(1L)).thenReturn(Optional.of(testTrainer));

        Optional<com.gym.crm.entity.Trainer> result = trainerService.findTrainerById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    @DisplayName("findTrainerByUsername should delegate to DAO")
    void findTrainerByUsername_ShouldDelegateToDao() {
        when(mockTrainerDao.findByUsername("jane.smith")).thenReturn(Optional.of(testTrainer));

        Optional<com.gym.crm.entity.Trainer> result = trainerService.findTrainerByUsername("jane.smith");

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    @DisplayName("findAllTrainers should delegate to DAO")
    void findAllTrainers_ShouldDelegateToDao() {
        List<Trainer> expectedTrainers = List.of(testTrainer);
        when(mockTrainerDao.findAll()).thenReturn(expectedTrainers);

        List<com.gym.crm.entity.Trainer> result = trainerService.findAllTrainers();

        assertEquals(expectedTrainers, result);
    }

    @Test
    @DisplayName("findTrainersBySpecialization should return empty for null specialization")
    void findTrainersBySpecialization_WithNullSpecialization_ShouldReturnEmpty() {
        List<com.gym.crm.entity.Trainer> result = trainerService.findTrainersBySpecialization(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findTrainersBySpecialization should filter by specialization")
    void findTrainersBySpecialization_ShouldFilterBySpecialization() {
        com.gym.crm.entity.TrainingType cardio = new com.gym.crm.entity.TrainingType("Cardio");
        com.gym.crm.entity.TrainingType strength = new com.gym.crm.entity.TrainingType("Strength");

        com.gym.crm.entity.Trainer cardioTrainer = new com.gym.crm.entity.Trainer("John", "Cardio", cardio);
        com.gym.crm.entity.Trainer strengthTrainer = new com.gym.crm.entity.Trainer("Jane", "Strength", strength);

        when(mockTrainerDao.findAll()).thenReturn(List.of(cardioTrainer, strengthTrainer));

        List<com.gym.crm.entity.Trainer> result = trainerService.findTrainersBySpecialization(cardio);

        assertEquals(1, result.size());
        assertEquals(cardioTrainer, result.getFirst());
    }

    @Test
    @DisplayName("trainerExists should return false for null ID")
    void trainerExists_WithNullId_ShouldReturnFalse() {
        assertFalse(trainerService.trainerExists(null));
    }

    @Test
    @DisplayName("trainerExists should delegate to DAO")
    void trainerExists_ShouldDelegateToDao() {
        when(mockTrainerDao.existsById(1L)).thenReturn(true);

        assertTrue(trainerService.trainerExists(1L));
        verify(mockTrainerDao).existsById(1L);
    }
}