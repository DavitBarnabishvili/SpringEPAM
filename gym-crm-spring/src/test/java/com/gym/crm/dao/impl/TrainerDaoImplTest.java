package com.gym.crm.dao.impl;

import com.gym.crm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerDaoImpl Tests")
class TrainerDaoImplTest {

    @Mock
    private InMemoryStorage mockStorage;

    private TrainerDaoImpl trainerDao;
    private com.gym.crm.entity.Trainer testTrainer;
    private Map<Long, com.gym.crm.entity.Trainer> mockTrainerMap;

    @BeforeEach
    void setUp() {
        trainerDao = new TrainerDaoImpl(mockStorage);
        com.gym.crm.entity.TrainingType specialization = new com.gym.crm.entity.TrainingType("Cardio");
        testTrainer = new com.gym.crm.entity.Trainer("Jane", "Smith", specialization);
        testTrainer.setUserId(1L);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("password456");

        mockTrainerMap = new ConcurrentHashMap<>();
    }

    @Test
    @DisplayName("create should throw exception for null trainer")
    void create_WithNullTrainer_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.create(null));
    }

    @Test
    @DisplayName("create should generate ID when trainer has no ID")
    void create_WithoutId_ShouldGenerateId() {
        com.gym.crm.entity.Trainer trainerWithoutId = new com.gym.crm.entity.Trainer("John", "Doe");
        when(mockStorage.generateTrainerId()).thenReturn(2L);

        com.gym.crm.entity.Trainer result = trainerDao.create(trainerWithoutId);

        assertEquals(2L, result.getUserId());
        verify(mockStorage).generateTrainerId();
        verify(mockStorage).storeTrainer(2L, trainerWithoutId);
    }

    @Test
    @DisplayName("create should use existing ID when trainer has ID")
    void create_WithExistingId_ShouldUseExistingId() {
        com.gym.crm.entity.Trainer result = trainerDao.create(testTrainer);

        assertEquals(1L, result.getUserId());
        verify(mockStorage, never()).generateTrainerId();
        verify(mockStorage).storeTrainer(1L, testTrainer);
    }

    @Test
    @DisplayName("update should throw exception for null trainer")
    void update_WithNullTrainer_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.update(null));
    }

    @Test
    @DisplayName("update should throw exception when trainer has no ID")
    void update_WithoutId_ShouldThrowException() {
        com.gym.crm.entity.Trainer trainerWithoutId = new com.gym.crm.entity.Trainer("John", "Doe");

        assertThrows(IllegalArgumentException.class, () -> trainerDao.update(trainerWithoutId));
    }

    @Test
    @DisplayName("update should throw exception when trainer not found")
    void update_WhenTrainerNotFound_ShouldThrowException() {
        when(mockStorage.getTrainers()).thenReturn(mockTrainerMap);
        mockTrainerMap.clear();

        assertThrows(RuntimeException.class, () -> trainerDao.update(testTrainer));
    }

    @Test
    @DisplayName("update should update existing trainer")
    void update_WithExistingTrainer_ShouldUpdate() {
        when(mockStorage.getTrainers()).thenReturn(mockTrainerMap);
        mockTrainerMap.put(1L, testTrainer);

        com.gym.crm.entity.Trainer result = trainerDao.update(testTrainer);

        assertEquals(testTrainer, result);
        verify(mockStorage).storeTrainer(1L, testTrainer);
    }

    @Test
    @DisplayName("findById should return empty for null ID")
    void findById_WithNullId_ShouldReturnEmpty() {
        Optional<com.gym.crm.entity.Trainer> result = trainerDao.findById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById should return empty when trainer not found")
    void findById_WhenTrainerNotFound_ShouldReturnEmpty() {
        when(mockStorage.getTrainer(999L)).thenReturn(null);

        Optional<com.gym.crm.entity.Trainer> result = trainerDao.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById should return trainer when found")
    void findById_WhenTrainerExists_ShouldReturnTrainer() {
        when(mockStorage.getTrainer(1L)).thenReturn(testTrainer);

        Optional<com.gym.crm.entity.Trainer> result = trainerDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    @DisplayName("findAll should return all trainers")
    void findAll_ShouldReturnAllTrainers() {
        List<com.gym.crm.entity.Trainer> expectedTrainers = List.of(testTrainer);
        when(mockStorage.getAllTrainers()).thenReturn(expectedTrainers);

        List<com.gym.crm.entity.Trainer> result = trainerDao.findAll();

        assertEquals(expectedTrainers, result);
    }

    @Test
    @DisplayName("findByUsername should return empty for null username")
    void findByUsername_WithNullUsername_ShouldReturnEmpty() {
        Optional<com.gym.crm.entity.Trainer> result = trainerDao.findByUsername(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUsername should find trainer by username")
    void findByUsername_WhenTrainerExists_ShouldReturnTrainer() {
        when(mockStorage.getAllTrainers()).thenReturn(List.of(testTrainer));

        Optional<com.gym.crm.entity.Trainer> result = trainerDao.findByUsername("jane.smith");

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    @DisplayName("existsById should return false for null ID")
    void existsById_WithNullId_ShouldReturnFalse() {
        assertFalse(trainerDao.existsById(null));
    }

    @Test
    @DisplayName("existsById should check storage")
    void existsById_ShouldCheckStorage() {
        when(mockStorage.getTrainers()).thenReturn(mockTrainerMap);
        mockTrainerMap.put(1L, testTrainer);

        boolean result = trainerDao.existsById(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("existsByUsername should check for username existence")
    void existsByUsername_ShouldCheckForUsernameExistence() {
        when(mockStorage.getAllTrainers()).thenReturn(List.of(testTrainer));

        assertTrue(trainerDao.existsByUsername("jane.smith"));
        assertFalse(trainerDao.existsByUsername("nonexistent"));
    }
}