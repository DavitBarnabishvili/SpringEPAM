package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Training;
import com.gym.crm.util.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingServiceImpl Tests")
class TrainingServiceImplTest {

    @Mock
    private com.gym.crm.dao.TrainingDao mockTrainingDao;

    @Mock
    private com.gym.crm.dao.TrainerDao mockTrainerDao;

    @Mock
    private TraineeDao mockTraineeDao;

    @Mock
    private ValidationService mockValidationService;

    private TrainingServiceImpl trainingService;
    private com.gym.crm.entity.Training testTraining;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl(mockTrainingDao, mockTrainerDao, mockTraineeDao, mockValidationService);
        com.gym.crm.entity.TrainingType type = new com.gym.crm.entity.TrainingType("Cardio");
        testTraining = new com.gym.crm.entity.Training(1L, 2L, "Morning Run", type, LocalDate.now(), 60);
        testTraining.setId(1L);
    }

    @Test
    @DisplayName("createTraining should throw exception for null training")
    void createTraining_WithNullTraining_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(null));
    }

    @Test
    @DisplayName("createTraining should validate training")
    void createTraining_ShouldValidateTraining() {
        when(mockTraineeDao.existsById(1L)).thenReturn(true);
        when(mockTrainerDao.existsById(2L)).thenReturn(true);

        Trainee activeTrainee = new Trainee("John", "Doe");
        activeTrainee.setIsActive(true);
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(activeTrainee));

        com.gym.crm.entity.Trainer activeTrainer = new com.gym.crm.entity.Trainer("Jane", "Smith");
        activeTrainer.setIsActive(true);
        when(mockTrainerDao.findById(2L)).thenReturn(Optional.of(activeTrainer));

        when(mockTrainingDao.create(testTraining)).thenReturn(testTraining);

        trainingService.createTraining(testTraining);

        verify(mockValidationService).validateTraining(testTraining);
    }

    @Test
    @DisplayName("createTraining should throw exception when trainee not found")
    void createTraining_WhenTraineeNotFound_ShouldThrowException() {
        when(mockTraineeDao.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> trainingService.createTraining(testTraining));
    }

    @Test
    @DisplayName("createTraining should throw exception when trainer not found")
    void createTraining_WhenTrainerNotFound_ShouldThrowException() {
        when(mockTraineeDao.existsById(1L)).thenReturn(true);
        when(mockTrainerDao.existsById(2L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> trainingService.createTraining(testTraining));
    }

    @Test
    @DisplayName("createTraining should throw exception for inactive trainee")
    void createTraining_WithInactiveTrainee_ShouldThrowException() {
        when(mockTraineeDao.existsById(1L)).thenReturn(true);
        when(mockTrainerDao.existsById(2L)).thenReturn(true);

        Trainee inactiveTrainee = new Trainee("John", "Doe");
        inactiveTrainee.setIsActive(false);
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(inactiveTrainee));

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(testTraining));
    }

    @Test
    @DisplayName("createTraining should throw exception for inactive trainer")
    void createTraining_WithInactiveTrainer_ShouldThrowException() {
        when(mockTraineeDao.existsById(1L)).thenReturn(true);
        when(mockTrainerDao.existsById(2L)).thenReturn(true);

        Trainee activeTrainee = new Trainee("John", "Doe");
        activeTrainee.setIsActive(true);
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(activeTrainee));

        com.gym.crm.entity.Trainer inactiveTrainer = new com.gym.crm.entity.Trainer("Jane", "Smith");
        inactiveTrainer.setIsActive(false);
        when(mockTrainerDao.findById(2L)).thenReturn(Optional.of(inactiveTrainer));

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(testTraining));
    }

    @Test
    @DisplayName("createTraining should save training via DAO")
    void createTraining_ShouldSaveTrainingViaDao() {
        when(mockTraineeDao.existsById(1L)).thenReturn(true);
        when(mockTrainerDao.existsById(2L)).thenReturn(true);

        Trainee activeTrainee = new Trainee("John", "Doe");
        activeTrainee.setIsActive(true);
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(activeTrainee));

        com.gym.crm.entity.Trainer activeTrainer = new com.gym.crm.entity.Trainer("Jane", "Smith");
        activeTrainer.setIsActive(true);
        when(mockTrainerDao.findById(2L)).thenReturn(Optional.of(activeTrainer));

        when(mockTrainingDao.create(testTraining)).thenReturn(testTraining);

        com.gym.crm.entity.Training result = trainingService.createTraining(testTraining);

        verify(mockTrainingDao).create(testTraining);
        assertEquals(testTraining, result);
    }

    @Test
    @DisplayName("findTrainingById should return empty for null ID")
    void findTrainingById_WithNullId_ShouldReturnEmpty() {
        Optional<com.gym.crm.entity.Training> result = trainingService.findTrainingById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findTrainingById should delegate to DAO")
    void findTrainingById_ShouldDelegateToDao() {
        when(mockTrainingDao.findById(1L)).thenReturn(Optional.of(testTraining));

        Optional<com.gym.crm.entity.Training> result = trainingService.findTrainingById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTraining, result.get());
    }

    @Test
    @DisplayName("findAllTrainings should delegate to DAO")
    void findAllTrainings_ShouldDelegateToDao() {
        List<Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingDao.findAll()).thenReturn(expectedTrainings);

        List<com.gym.crm.entity.Training> result = trainingService.findAllTrainings();

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("findTrainingsByTraineeId should delegate to DAO")
    void findTrainingsByTraineeId_ShouldDelegateToDao() {
        List<com.gym.crm.entity.Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingDao.findByTraineeId(1L)).thenReturn(expectedTrainings);

        List<com.gym.crm.entity.Training> result = trainingService.findTrainingsByTraineeId(1L);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("findTrainingsByTrainerId should delegate to DAO")
    void findTrainingsByTrainerId_ShouldDelegateToDao() {
        List<com.gym.crm.entity.Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingDao.findByTrainerId(2L)).thenReturn(expectedTrainings);

        List<com.gym.crm.entity.Training> result = trainingService.findTrainingsByTrainerId(2L);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("findTrainingsByDate should delegate to DAO")
    void findTrainingsByDate_ShouldDelegateToDao() {
        LocalDate date = LocalDate.now();
        List<com.gym.crm.entity.Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingDao.findByDate(date)).thenReturn(expectedTrainings);

        List<com.gym.crm.entity.Training> result = trainingService.findTrainingsByDate(date);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("findTrainingsByDateRange should delegate to DAO")
    void findTrainingsByDateRange_ShouldDelegateToDao() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(7);
        List<com.gym.crm.entity.Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingDao.findByDateRange(start, end)).thenReturn(expectedTrainings);

        List<com.gym.crm.entity.Training> result = trainingService.findTrainingsByDateRange(start, end);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("trainingExists should delegate to DAO")
    void trainingExists_ShouldDelegateToDao() {
        when(mockTrainingDao.existsById(1L)).thenReturn(true);
        assertTrue(trainingService.trainingExists(1L));
        verify(mockTrainingDao).existsById(1L);
    }
}