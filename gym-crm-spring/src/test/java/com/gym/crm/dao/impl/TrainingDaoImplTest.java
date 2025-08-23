package com.gym.crm.dao.impl;

import com.gym.crm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingDaoImpl Tests")
class TrainingDaoImplTest {

    @Mock
    private InMemoryStorage mockStorage;

    private TrainingDaoImpl trainingDao;
    private com.gym.crm.entity.Training testTraining;
    private Map<Long, com.gym.crm.entity.Training> mockTrainingMap;

    @BeforeEach
    void setUp() {
        trainingDao = new TrainingDaoImpl(mockStorage);
        com.gym.crm.entity.TrainingType type = new com.gym.crm.entity.TrainingType("Cardio");
        testTraining = new com.gym.crm.entity.Training(1L, 1L, 2L, "Morning Run",
                type, LocalDate.now(), 60);

        mockTrainingMap = new ConcurrentHashMap<>();
    }

    @Test
    @DisplayName("create should throw exception for null training")
    void create_WithNullTraining_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.create(null));
    }

    @Test
    @DisplayName("create should generate ID when training has no ID")
    void create_WithoutId_ShouldGenerateId() {
        com.gym.crm.entity.Training trainingWithoutId = new com.gym.crm.entity.Training(
                1L, 2L, "Test", new com.gym.crm.entity.TrainingType("Yoga"), LocalDate.now(), 45);
        when(mockStorage.generateTrainingId()).thenReturn(2L);

        com.gym.crm.entity.Training result = trainingDao.create(trainingWithoutId);

        assertEquals(2L, result.getId());
        verify(mockStorage).generateTrainingId();
        verify(mockStorage).storeTraining(2L, trainingWithoutId);
    }

    @Test
    @DisplayName("create should use existing ID when training has ID")
    void create_WithExistingId_ShouldUseExistingId() {
        com.gym.crm.entity.Training result = trainingDao.create(testTraining);

        assertEquals(1L, result.getId());
        verify(mockStorage, never()).generateTrainingId();
        verify(mockStorage).storeTraining(1L, testTraining);
    }

    @Test
    @DisplayName("findById should return empty for null ID")
    void findById_WithNullId_ShouldReturnEmpty() {
        Optional<com.gym.crm.entity.Training> result = trainingDao.findById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById should return training when found")
    void findById_WhenTrainingExists_ShouldReturnTraining() {
        when(mockStorage.getTraining(1L)).thenReturn(testTraining);

        Optional<com.gym.crm.entity.Training> result = trainingDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTraining, result.get());
    }

    @Test
    @DisplayName("findAll should return all trainings")
    void findAll_ShouldReturnAllTrainings() {
        List<com.gym.crm.entity.Training> expectedTrainings = List.of(testTraining);
        when(mockStorage.getAllTrainings()).thenReturn(expectedTrainings);

        List<com.gym.crm.entity.Training> result = trainingDao.findAll();

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("findByTraineeId should return empty list for null ID")
    void findByTraineeId_WithNullId_ShouldReturnEmptyList() {
        List<com.gym.crm.entity.Training> result = trainingDao.findByTraineeId(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByTraineeId should filter by trainee ID")
    void findByTraineeId_ShouldFilterByTraineeId() {
        com.gym.crm.entity.Training training1 = new com.gym.crm.entity.Training(1L, 1L, 2L, "Training 1",
                new com.gym.crm.entity.TrainingType("Cardio"), LocalDate.now(), 60);
        com.gym.crm.entity.Training training2 = new com.gym.crm.entity.Training(2L, 2L, 3L, "Training 2",
                new com.gym.crm.entity.TrainingType("Strength"), LocalDate.now(), 45);

        when(mockStorage.getAllTrainings()).thenReturn(List.of(training1, training2));

        List<com.gym.crm.entity.Training> result = trainingDao.findByTraineeId(1L);

        assertEquals(1, result.size());
        assertEquals(training1, result.getFirst());
    }

    @Test
    @DisplayName("findByTrainerId should return empty list for null ID")
    void findByTrainerId_WithNullId_ShouldReturnEmptyList() {
        List<com.gym.crm.entity.Training> result = trainingDao.findByTrainerId(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByTrainerId should filter by trainer ID")
    void findByTrainerId_ShouldFilterByTrainerId() {
        com.gym.crm.entity.Training training1 = new com.gym.crm.entity.Training(1L, 1L, 2L, "Training 1",
                new com.gym.crm.entity.TrainingType("Cardio"), LocalDate.now(), 60);
        com.gym.crm.entity.Training training2 = new com.gym.crm.entity.Training(2L, 1L, 3L, "Training 2",
                new com.gym.crm.entity.TrainingType("Strength"), LocalDate.now(), 45);

        when(mockStorage.getAllTrainings()).thenReturn(List.of(training1, training2));

        List<com.gym.crm.entity.Training> result = trainingDao.findByTrainerId(2L);

        assertEquals(1, result.size());
        assertEquals(training1, result.getFirst());
    }

    @Test
    @DisplayName("findByDate should return empty list for null date")
    void findByDate_WithNullDate_ShouldReturnEmptyList() {
        List<com.gym.crm.entity.Training> result = trainingDao.findByDate(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByDate should filter by date")
    void findByDate_ShouldFilterByDate() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        com.gym.crm.entity.Training todayTraining = new com.gym.crm.entity.Training(1L, 1L, 2L, "Today",
                new com.gym.crm.entity.TrainingType("Cardio"), today, 60);
        com.gym.crm.entity.Training tomorrowTraining = new com.gym.crm.entity.Training(2L, 1L, 2L, "Tomorrow",
                new com.gym.crm.entity.TrainingType("Strength"), tomorrow, 45);

        when(mockStorage.getAllTrainings()).thenReturn(List.of(todayTraining, tomorrowTraining));

        List<com.gym.crm.entity.Training> result = trainingDao.findByDate(today);

        assertEquals(1, result.size());
        assertEquals(todayTraining, result.getFirst());
    }

    @Test
    @DisplayName("findByDateRange should return empty list for null dates")
    void findByDateRange_WithNullDates_ShouldReturnEmptyList() {
        assertTrue(trainingDao.findByDateRange(null, LocalDate.now()).isEmpty());
        assertTrue(trainingDao.findByDateRange(LocalDate.now(), null).isEmpty());
        assertTrue(trainingDao.findByDateRange(null, null).isEmpty());
    }

    @Test
    @DisplayName("findByDateRange should return empty list for invalid range")
    void findByDateRange_WithInvalidRange_ShouldReturnEmptyList() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        List<com.gym.crm.entity.Training> result = trainingDao.findByDateRange(start, end);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByDateRange should filter by date range")
    void findByDateRange_ShouldFilterByDateRange() {
        LocalDate start = LocalDate.now();
        LocalDate middle = start.plusDays(1);
        LocalDate end = start.plusDays(2);
        LocalDate outside = start.plusDays(5);

        com.gym.crm.entity.Training training1 = new com.gym.crm.entity.Training(1L, 1L, 2L, "In Range 1",
                new com.gym.crm.entity.TrainingType("Cardio"), start, 60);
        com.gym.crm.entity.Training training2 = new com.gym.crm.entity.Training(2L, 1L, 2L, "In Range 2",
                new com.gym.crm.entity.TrainingType("Strength"), middle, 45);
        com.gym.crm.entity.Training training3 = new com.gym.crm.entity.Training(3L, 1L, 2L, "Outside Range",
                new com.gym.crm.entity.TrainingType("Yoga"), outside, 30);

        when(mockStorage.getAllTrainings()).thenReturn(List.of(training1, training2, training3));

        List<com.gym.crm.entity.Training> result = trainingDao.findByDateRange(start, end);

        assertEquals(2, result.size());
        assertTrue(result.contains(training1));
        assertTrue(result.contains(training2));
        assertFalse(result.contains(training3));
    }

    @Test
    @DisplayName("findByTraineeIdAndDateRange should handle null parameters")
    void findByTraineeIdAndDateRange_WithNullParameters_ShouldReturnEmptyList() {
        LocalDate date = LocalDate.now();

        assertTrue(trainingDao.findByTraineeIdAndDateRange(null, date, date).isEmpty());
        assertTrue(trainingDao.findByTraineeIdAndDateRange(1L, null, date).isEmpty());
        assertTrue(trainingDao.findByTraineeIdAndDateRange(1L, date, null).isEmpty());
    }

    @Test
    @DisplayName("findByTraineeIdAndDateRange should filter by trainee and date range")
    void findByTraineeIdAndDateRange_ShouldFilterByTraineeAndDateRange() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2);

        com.gym.crm.entity.Training matchingTraining = new com.gym.crm.entity.Training(1L, 1L, 2L, "Match",
                new com.gym.crm.entity.TrainingType("Cardio"), start.plusDays(1), 60);
        com.gym.crm.entity.Training wrongTrainee = new com.gym.crm.entity.Training(2L, 2L, 2L, "Wrong Trainee",
                new com.gym.crm.entity.TrainingType("Strength"), start.plusDays(1), 45);
        com.gym.crm.entity.Training wrongDate = new com.gym.crm.entity.Training(3L, 1L, 2L, "Wrong Date",
                new com.gym.crm.entity.TrainingType("Yoga"), start.plusDays(5), 30);

        when(mockStorage.getAllTrainings()).thenReturn(List.of(matchingTraining, wrongTrainee, wrongDate));

        List<com.gym.crm.entity.Training> result = trainingDao.findByTraineeIdAndDateRange(1L, start, end);

        assertEquals(1, result.size());
        assertEquals(matchingTraining, result.getFirst());
    }

    @Test
    @DisplayName("findByTrainerIdAndDateRange should filter by trainer and date range")
    void findByTrainerIdAndDateRange_ShouldFilterByTrainerAndDateRange() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2);

        com.gym.crm.entity.Training matchingTraining = new com.gym.crm.entity.Training(1L, 1L, 2L, "Match",
                new com.gym.crm.entity.TrainingType("Cardio"), start.plusDays(1), 60);
        com.gym.crm.entity.Training wrongTrainer = new com.gym.crm.entity.Training(2L, 1L, 3L, "Wrong Trainer",
                new com.gym.crm.entity.TrainingType("Strength"), start.plusDays(1), 45);

        when(mockStorage.getAllTrainings()).thenReturn(List.of(matchingTraining, wrongTrainer));

        List<com.gym.crm.entity.Training> result = trainingDao.findByTrainerIdAndDateRange(2L, start, end);

        assertEquals(1, result.size());
        assertEquals(matchingTraining, result.getFirst());
    }

    @Test
    @DisplayName("existsById should return false for null ID")
    void existsById_WithNullId_ShouldReturnFalse() {
        assertFalse(trainingDao.existsById(null));
    }

    @Test
    @DisplayName("existsById should check storage")
    void existsById_ShouldCheckStorage() {
        when(mockStorage.getTrainings()).thenReturn(mockTrainingMap);
        mockTrainingMap.put(1L, testTraining);

        boolean result = trainingDao.existsById(1L);

        assertTrue(result);
    }
}
