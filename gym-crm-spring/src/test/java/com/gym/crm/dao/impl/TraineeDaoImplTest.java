package com.gym.crm.dao.impl;

import com.gym.crm.entity.Trainee;
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
@DisplayName("TraineeDaoImpl Tests")
class TraineeDaoImplTest {

    @Mock
    private InMemoryStorage mockStorage;

    private TraineeDaoImpl traineeDao;
    private Trainee testTrainee;
    private Map<Long, Trainee> mockTraineeMap;

    @BeforeEach
    void setUp() {
        traineeDao = new TraineeDaoImpl(mockStorage);
        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St");
        testTrainee.setUserId(1L);
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("password123");

        mockTraineeMap = new ConcurrentHashMap<>();
    }
    @Test
    @DisplayName("create should throw exception for null trainee")
    void create_WithNullTrainee_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.create(null));
    }

    @Test
    @DisplayName("create should generate ID when trainee has no ID")
    void create_WithoutId_ShouldGenerateId() {
        Trainee traineeWithoutId = new Trainee("Jane", "Smith");
        when(mockStorage.generateTraineeId()).thenReturn(2L);

        Trainee result = traineeDao.create(traineeWithoutId);

        assertEquals(2L, result.getUserId());
        verify(mockStorage).generateTraineeId();
        verify(mockStorage).storeTrainee(2L, traineeWithoutId);
    }

    @Test
    @DisplayName("create should use existing ID when trainee has ID")
    void create_WithExistingId_ShouldUseExistingId() {
        Trainee result = traineeDao.create(testTrainee);

        assertEquals(1L, result.getUserId());
        verify(mockStorage, never()).generateTraineeId();
        verify(mockStorage).storeTrainee(1L, testTrainee);
    }

    @Test
    @DisplayName("update should throw exception for null trainee")
    void update_WithNullTrainee_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.update(null));
    }

    @Test
    @DisplayName("update should throw exception when trainee has no ID")
    void update_WithoutId_ShouldThrowException() {
        Trainee traineeWithoutId = new Trainee("Jane", "Smith");

        assertThrows(IllegalArgumentException.class, () -> traineeDao.update(traineeWithoutId));
    }

    @Test
    @DisplayName("update should throw exception when trainee not found")
    void update_WhenTraineeNotFound_ShouldThrowException() {
        when(mockStorage.getTrainees()).thenReturn(mockTraineeMap);
        mockTraineeMap.clear();
        assertThrows(RuntimeException.class, () -> traineeDao.update(testTrainee));
    }

    @Test
    @DisplayName("update should update existing trainee")
    void update_WithExistingTrainee_ShouldUpdate() {
        when(mockStorage.getTrainees()).thenReturn(mockTraineeMap);
        mockTraineeMap.put(1L, testTrainee);

        Trainee result = traineeDao.update(testTrainee);

        assertEquals(testTrainee, result);
        verify(mockStorage).storeTrainee(1L, testTrainee);
    }

    @Test
    @DisplayName("delete should return false for null ID")
    void delete_WithNullId_ShouldReturnFalse() {
        assertFalse(traineeDao.delete(null));
    }

    @Test
    @DisplayName("delete should return false when trainee not found")
    void delete_WhenTraineeNotFound_ShouldReturnFalse() {
        when(mockStorage.getTrainee(999L)).thenReturn(null);
        assertFalse(traineeDao.delete(999L));
    }

    @Test
    @DisplayName("delete should return true when trainee deleted")
    void delete_WhenTraineeExists_ShouldReturnTrue() {
        when(mockStorage.getTrainee(1L)).thenReturn(testTrainee);
        boolean result = traineeDao.delete(1L);
        assertTrue(result);
        verify(mockStorage).removeTrainee(1L);
    }

    @Test
    @DisplayName("findById should return empty for null ID")
    void findById_WithNullId_ShouldReturnEmpty() {
        Optional<Trainee> result = traineeDao.findById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById should return empty when trainee not found")
    void findById_WhenTraineeNotFound_ShouldReturnEmpty() {
        when(mockStorage.getTrainee(999L)).thenReturn(null);
        Optional<Trainee> result = traineeDao.findById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById should return trainee when found")
    void findById_WhenTraineeExists_ShouldReturnTrainee() {
        when(mockStorage.getTrainee(1L)).thenReturn(testTrainee);
        Optional<Trainee> result = traineeDao.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    @DisplayName("findAll should return all trainees")
    void findAll_ShouldReturnAllTrainees() {
        List<Trainee> expectedTrainees = List.of(testTrainee);
        when(mockStorage.getAllTrainees()).thenReturn(expectedTrainees);

        List<Trainee> result = traineeDao.findAll();

        assertEquals(expectedTrainees, result);
    }

    @Test
    @DisplayName("findByUsername should return empty for null username")
    void findByUsername_WithNullUsername_ShouldReturnEmpty() {
        Optional<Trainee> result = traineeDao.findByUsername(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUsername should return empty for empty username")
    void findByUsername_WithEmptyUsername_ShouldReturnEmpty() {
        Optional<Trainee> result = traineeDao.findByUsername("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUsername should find trainee by username")
    void findByUsername_WhenTraineeExists_ShouldReturnTrainee() {
        when(mockStorage.getAllTrainees()).thenReturn(List.of(testTrainee));

        Optional<Trainee> result = traineeDao.findByUsername("john.doe");

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    @DisplayName("findByUsername should handle whitespace")
    void findByUsername_WithWhitespace_ShouldTrimAndFind() {
        when(mockStorage.getAllTrainees()).thenReturn(List.of(testTrainee));

        Optional<Trainee> result = traineeDao.findByUsername("  john.doe  ");

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    @DisplayName("findAllActive should return only active trainees")
    void findAllActive_ShouldReturnOnlyActiveTrainees() {
        Trainee activeTrainee = new Trainee("Active", "User");
        activeTrainee.setIsActive(true);

        Trainee inactiveTrainee = new Trainee("Inactive", "User");
        inactiveTrainee.setIsActive(false);

        when(mockStorage.getAllTrainees()).thenReturn(List.of(activeTrainee, inactiveTrainee));

        List<Trainee> result = traineeDao.findAllActive();

        assertEquals(1, result.size());
        assertEquals(activeTrainee, result.getFirst());
    }

    @Test
    @DisplayName("existsById should return false for null ID")
    void existsById_WithNullId_ShouldReturnFalse() {
        assertFalse(traineeDao.existsById(null));
    }

    @Test
    @DisplayName("existsById should check storage")
    void existsById_ShouldCheckStorage() {
        when(mockStorage.getTrainees()).thenReturn(mockTraineeMap);
        mockTraineeMap.put(1L, testTrainee);

        boolean result = traineeDao.existsById(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("existsByUsername should return false for null username")
    void existsByUsername_WithNullUsername_ShouldReturnFalse() {
        assertFalse(traineeDao.existsByUsername(null));
    }

    @Test
    @DisplayName("existsByUsername should return false for empty username")
    void existsByUsername_WithEmptyUsername_ShouldReturnFalse() {
        assertFalse(traineeDao.existsByUsername(""));
    }

    @Test
    @DisplayName("existsByUsername should check for username existence")
    void existsByUsername_ShouldCheckForUsernameExistence() {
        when(mockStorage.getAllTrainees()).thenReturn(List.of(testTrainee));

        assertTrue(traineeDao.existsByUsername("john.doe"));
        assertFalse(traineeDao.existsByUsername("nonexistent"));
    }
}
