package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.entity.Trainee;
import com.gym.crm.util.CredentialsGeneratorService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeServiceImpl Tests")
class TraineeServiceImplTest {

    @Mock
    private TraineeDao mockTraineeDao;

    @Mock
    private CredentialsGeneratorService mockCredentialsGenerator;

    @Mock
    private ValidationService mockValidationService;

    private TraineeServiceImpl traineeService;
    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl(mockTraineeDao, mockCredentialsGenerator, mockValidationService);
        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St");
        testTrainee.setUserId(1L);
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("password123");
    }

    @Test
    @DisplayName("createTrainee should throw exception for null trainee")
    void createTrainee_WithNullTrainee_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.createTrainee(null));
    }

    @Test
    @DisplayName("createTrainee should validate trainee")
    void createTrainee_ShouldValidateTrainee() {
        Trainee newTrainee = new Trainee("Jane", "Smith");
        when(mockCredentialsGenerator.generateUsername("Jane", "Smith")).thenReturn("jane.smith");
        when(mockCredentialsGenerator.generatePassword()).thenReturn("password123");
        when(mockTraineeDao.create(any(Trainee.class))).thenReturn(newTrainee);

        traineeService.createTrainee(newTrainee);

        verify(mockValidationService).validateTrainee(newTrainee);
    }

    @Test
    @DisplayName("createTrainee should generate credentials")
    void createTrainee_ShouldGenerateCredentials() {
        Trainee newTrainee = new Trainee("Jane", "Smith");
        when(mockCredentialsGenerator.generateUsername("Jane", "Smith")).thenReturn("jane.smith");
        when(mockCredentialsGenerator.generatePassword()).thenReturn("password123");
        when(mockTraineeDao.create(any(Trainee.class))).thenReturn(newTrainee);

        traineeService.createTrainee(newTrainee);

        verify(mockCredentialsGenerator).generateUsername("Jane", "Smith");
        verify(mockCredentialsGenerator).generatePassword();
        assertEquals("jane.smith", newTrainee.getUsername());
        assertEquals("password123", newTrainee.getPassword());
        assertTrue(newTrainee.getIsActive());
    }

    @Test
    @DisplayName("createTrainee should override existing credentials")
    void createTrainee_WithExistingCredentials_ShouldOverrideCredentials() {
        Trainee traineeWithCredentials = new Trainee("Jane", "Smith");
        traineeWithCredentials.setUsername("existing.username");
        traineeWithCredentials.setPassword("existing.password");

        when(mockCredentialsGenerator.generateUsername("Jane", "Smith")).thenReturn("jane.smith");
        when(mockCredentialsGenerator.generatePassword()).thenReturn("new.password");
        when(mockTraineeDao.create(any(Trainee.class))).thenReturn(traineeWithCredentials);

        traineeService.createTrainee(traineeWithCredentials);

        assertEquals("jane.smith", traineeWithCredentials.getUsername());
        assertEquals("new.password", traineeWithCredentials.getPassword());
    }

    @Test
    @DisplayName("createTrainee should save trainee via DAO")
    void createTrainee_ShouldSaveTraineeViaDao() {
        Trainee newTrainee = new Trainee("Jane", "Smith");
        when(mockCredentialsGenerator.generateUsername(anyString(), anyString())).thenReturn("jane.smith");
        when(mockCredentialsGenerator.generatePassword()).thenReturn("password123");
        when(mockTraineeDao.create(newTrainee)).thenReturn(newTrainee);

        Trainee result = traineeService.createTrainee(newTrainee);

        verify(mockTraineeDao).create(newTrainee);
        assertEquals(newTrainee, result);
    }
    @Test
    @DisplayName("updateTrainee should throw exception for null trainee")
    void updateTrainee_WithNullTrainee_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.updateTrainee(null));
    }

    @Test
    @DisplayName("updateTrainee should throw exception when trainee has no ID")
    void updateTrainee_WithoutId_ShouldThrowException() {
        Trainee traineeWithoutId = new Trainee("Jane", "Smith");

        assertThrows(IllegalArgumentException.class, () -> traineeService.updateTrainee(traineeWithoutId));
    }

    @Test
    @DisplayName("updateTrainee should throw exception when trainee not found")
    void updateTrainee_WhenTraineeNotFound_ShouldThrowException() {
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> traineeService.updateTrainee(testTrainee));
    }

    @Test
    @DisplayName("updateTrainee should preserve credentials")
    void updateTrainee_ShouldPreserveCredentials() {
        Trainee existingTrainee = new Trainee("John", "Doe");
        existingTrainee.setUserId(1L);
        existingTrainee.setUsername("john.doe");
        existingTrainee.setPassword("original.password");

        Trainee updatedTrainee = new Trainee("John", "Smith"); // Changed last name
        updatedTrainee.setUserId(1L);

        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(existingTrainee));
        when(mockTraineeDao.update(updatedTrainee)).thenReturn(updatedTrainee);

        traineeService.updateTrainee(updatedTrainee);

        assertEquals("john.doe", updatedTrainee.getUsername());
        assertEquals("original.password", updatedTrainee.getPassword());
        verify(mockTraineeDao).update(updatedTrainee);
    }

    @Test
    @DisplayName("deleteTrainee should return false for null ID")
    void deleteTrainee_WithNullId_ShouldReturnFalse() {
        assertFalse(traineeService.deleteTrainee(null));
        verify(mockTraineeDao, never()).delete(any());
    }

    @Test
    @DisplayName("deleteTrainee should delegate to DAO")
    void deleteTrainee_ShouldDelegateToDao() {
        when(mockTraineeDao.delete(1L)).thenReturn(true);

        boolean result = traineeService.deleteTrainee(1L);

        assertTrue(result);
        verify(mockTraineeDao).delete(1L);
    }

    @Test
    @DisplayName("findTraineeById should return empty for null ID")
    void findTraineeById_WithNullId_ShouldReturnEmpty() {
        Optional<Trainee> result = traineeService.findTraineeById(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findTraineeById should delegate to DAO")
    void findTraineeById_ShouldDelegateToDao() {
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = traineeService.findTraineeById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    @DisplayName("findTraineeByUsername should return empty for invalid username")
    void findTraineeByUsername_WithInvalidUsername_ShouldReturnEmpty() {
        assertTrue(traineeService.findTraineeByUsername(null).isEmpty());
        assertTrue(traineeService.findTraineeByUsername("").isEmpty());
        assertTrue(traineeService.findTraineeByUsername("   ").isEmpty());
    }

    @Test
    @DisplayName("findTraineeByUsername should delegate to DAO")
    void findTraineeByUsername_ShouldDelegateToDao() {
        when(mockTraineeDao.findByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = traineeService.findTraineeByUsername("john.doe");

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    @DisplayName("findAllTrainees should delegate to DAO")
    void findAllTrainees_ShouldDelegateToDao() {
        List<Trainee> expectedTrainees = List.of(testTrainee);
        when(mockTraineeDao.findAll()).thenReturn(expectedTrainees);

        List<Trainee> result = traineeService.findAllTrainees();

        assertEquals(expectedTrainees, result);
    }

    @Test
    @DisplayName("findAllActiveTrainees should delegate to DAO")
    void findAllActiveTrainees_ShouldDelegateToDao() {
        List<Trainee> expectedTrainees = List.of(testTrainee);
        when(mockTraineeDao.findAllActive()).thenReturn(expectedTrainees);

        List<Trainee> result = traineeService.findAllActiveTrainees();

        assertEquals(expectedTrainees, result);
    }

    @Test
    @DisplayName("findTraineesByAgeRange should return empty for invalid range")
    void findTraineesByAgeRange_WithInvalidRange_ShouldReturnEmpty() {
        assertTrue(traineeService.findTraineesByAgeRange(-1, 25).isEmpty());
        assertTrue(traineeService.findTraineesByAgeRange(25, -1).isEmpty());
        assertTrue(traineeService.findTraineesByAgeRange(30, 25).isEmpty());
    }

    @Test
    @DisplayName("findTraineesByAgeRange should filter by age")
    void findTraineesByAgeRange_ShouldFilterByAge() {
        Trainee youngTrainee = new Trainee("Young", "Person", LocalDate.now().minusYears(20), "Address");
        Trainee oldTrainee = new Trainee("Old", "Person", LocalDate.now().minusYears(50), "Address");

        when(mockTraineeDao.findAll()).thenReturn(List.of(youngTrainee, oldTrainee));

        List<Trainee> result = traineeService.findTraineesByAgeRange(18, 30);

        assertEquals(1, result.size());
        assertEquals(youngTrainee, result.getFirst());
    }

    @Test
    @DisplayName("activateTrainee should return false for null ID")
    void activateTrainee_WithNullId_ShouldReturnFalse() {
        assertFalse(traineeService.activateTrainee(null));
    }

    @Test
    @DisplayName("activateTrainee should return false when trainee not found")
    void activateTrainee_WhenTraineeNotFound_ShouldReturnFalse() {
        when(mockTraineeDao.findById(999L)).thenReturn(Optional.empty());

        assertFalse(traineeService.activateTrainee(999L));
    }

    @Test
    @DisplayName("activateTrainee should activate trainee")
    void activateTrainee_ShouldActivateTrainee() {
        testTrainee.setIsActive(false);
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(mockTraineeDao.update(testTrainee)).thenReturn(testTrainee);

        boolean result = traineeService.activateTrainee(1L);

        assertTrue(result);
        assertTrue(testTrainee.getIsActive());
        verify(mockTraineeDao).update(testTrainee);
    }

    @Test
    @DisplayName("deactivateTrainee should deactivate trainee")
    void deactivateTrainee_ShouldDeactivateTrainee() {
        testTrainee.setIsActive(true);
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(mockTraineeDao.update(testTrainee)).thenReturn(testTrainee);

        boolean result = traineeService.deactivateTrainee(1L);

        assertTrue(result);
        assertFalse(testTrainee.getIsActive());
        verify(mockTraineeDao).update(testTrainee);
    }

    @Test
    @DisplayName("activateTrainee should handle update exceptions")
    void activateTrainee_WithUpdateException_ShouldReturnFalse() {
        when(mockTraineeDao.findById(1L)).thenReturn(Optional.of(testTrainee));
        when(mockTraineeDao.update(testTrainee)).thenThrow(new RuntimeException("Update failed"));

        boolean result = traineeService.activateTrainee(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("traineeExists should return false for null ID")
    void traineeExists_WithNullId_ShouldReturnFalse() {
        assertFalse(traineeService.traineeExists(null));
    }

    @Test
    @DisplayName("traineeExists should delegate to DAO")
    void traineeExists_ShouldDelegateToDao() {
        when(mockTraineeDao.existsById(1L)).thenReturn(true);
        assertTrue(traineeService.traineeExists(1L));
        verify(mockTraineeDao).existsById(1L);
    }
}