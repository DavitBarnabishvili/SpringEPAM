package com.gym.crm.facade;

import com.gym.crm.entity.*;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import com.gym.crm.storage.InMemoryStorage;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymFacade Tests")
class GymFacadeTest {

    @Mock
    private TrainerService mockTrainerService;

    @Mock
    private TraineeService mockTraineeService;

    @Mock
    private TrainingService mockTrainingService;

    @Mock
    private InMemoryStorage mockStorage;

    private GymFacade gymFacade;
    private TrainingType cardioType;
    private Trainer testTrainer;
    private Trainee testTrainee;
    private Training testTraining;

    @BeforeEach
    void setUp() {
        gymFacade = new GymFacade(mockTrainerService, mockTraineeService, mockTrainingService, mockStorage);

        cardioType = new TrainingType(1L, "Cardio");
        testTrainer = new Trainer("Jane", "Smith", cardioType);
        testTrainer.setUserId(1L);

        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St");
        testTrainee.setUserId(2L);

        testTraining = new Training(2L, 1L, "Morning Run", cardioType, LocalDate.now(), 60);
        testTraining.setId(3L);
    }

    @Test
    @DisplayName("createTrainer should create trainer with provided details")
    void createTrainer_ShouldCreateTrainerWithProvidedDetails() {
        when(mockTrainerService.createTrainer(any(Trainer.class))).thenReturn(testTrainer);
        Trainer result = gymFacade.createTrainer("Jane", "Smith", cardioType);
        assertNotNull(result);
        verify(mockTrainerService).createTrainer(argThat(trainer ->
                "Jane".equals(trainer.getFirstName()) &&
                        "Smith".equals(trainer.getLastName()) &&
                        cardioType.equals(trainer.getSpecialization())
        ));
    }

    @Test
    @DisplayName("updateTrainer should delegate to service")
    void updateTrainer_ShouldDelegateToService() {
        when(mockTrainerService.updateTrainer(testTrainer)).thenReturn(testTrainer);
        Trainer result = gymFacade.updateTrainer(testTrainer);

        assertEquals(testTrainer, result);
        verify(mockTrainerService).updateTrainer(testTrainer);
    }

    @Test
    @DisplayName("findTrainerById should delegate to service")
    void findTrainerById_ShouldDelegateToService() {
        when(mockTrainerService.findTrainerById(1L)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = gymFacade.findTrainerById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    @DisplayName("getAllTrainers should delegate to service")
    void getAllTrainers_ShouldDelegateToService() {
        List<Trainer> expectedTrainers = List.of(testTrainer);
        when(mockTrainerService.findAllTrainers()).thenReturn(expectedTrainers);

        List<Trainer> result = gymFacade.getAllTrainers();

        assertEquals(expectedTrainers, result);
    }

    @Test
    @DisplayName("getTrainersBySpecialization should delegate to service")
    void getTrainersBySpecialization_ShouldDelegateToService() {
        List<Trainer> expectedTrainers = List.of(testTrainer);
        when(mockTrainerService.findTrainersBySpecialization(cardioType)).thenReturn(expectedTrainers);

        List<Trainer> result = gymFacade.getTrainersBySpecialization(cardioType);

        assertEquals(expectedTrainers, result);
    }

    @Test
    @DisplayName("createTrainee should create trainee with provided details")
    void createTrainee_ShouldCreateTraineeWithProvidedDetails() {
        when(mockTraineeService.createTrainee(any(Trainee.class))).thenReturn(testTrainee);

        Trainee result = gymFacade.createTrainee("John", "Doe",
                LocalDate.of(1990, 1, 1), "123 Main St");

        assertNotNull(result);
        verify(mockTraineeService).createTrainee(argThat(trainee ->
                "John".equals(trainee.getFirstName()) &&
                        "Doe".equals(trainee.getLastName()) &&
                        LocalDate.of(1990, 1, 1).equals(trainee.getDateOfBirth()) &&
                        "123 Main St".equals(trainee.getAddress())
        ));
    }

    @Test
    @DisplayName("updateTrainee should delegate to service")
    void updateTrainee_ShouldDelegateToService() {
        when(mockTraineeService.updateTrainee(testTrainee)).thenReturn(testTrainee);

        Trainee result = gymFacade.updateTrainee(testTrainee);

        assertEquals(testTrainee, result);
        verify(mockTraineeService).updateTrainee(testTrainee);
    }

    @Test
    @DisplayName("deleteTrainee should delegate to service")
    void deleteTrainee_ShouldDelegateToService() {
        when(mockTraineeService.deleteTrainee(2L)).thenReturn(true);

        boolean result = gymFacade.deleteTrainee(2L);

        assertTrue(result);
        verify(mockTraineeService).deleteTrainee(2L);
    }

    @Test
    @DisplayName("getAllActiveTrainees should delegate to service")
    void getAllActiveTrainees_ShouldDelegateToService() {
        List<Trainee> expectedTrainees = List.of(testTrainee);
        when(mockTraineeService.findAllActiveTrainees()).thenReturn(expectedTrainees);

        List<Trainee> result = gymFacade.getAllActiveTrainees();

        assertEquals(expectedTrainees, result);
    }

    @Test
    @DisplayName("activateTrainee should delegate to service")
    void activateTrainee_ShouldDelegateToService() {
        when(mockTraineeService.activateTrainee(2L)).thenReturn(true);

        boolean result = gymFacade.activateTrainee(2L);

        assertTrue(result);
        verify(mockTraineeService).activateTrainee(2L);
    }

    @Test
    @DisplayName("deactivateTrainee should delegate to service")
    void deactivateTrainee_ShouldDelegateToService() {
        when(mockTraineeService.deactivateTrainee(2L)).thenReturn(true);

        boolean result = gymFacade.deactivateTrainee(2L);

        assertTrue(result);
        verify(mockTraineeService).deactivateTrainee(2L);
    }

    @Test
    @DisplayName("createTraining should create training with provided details")
    void createTraining_ShouldCreateTrainingWithProvidedDetails() {
        LocalDate trainingDate = LocalDate.now().plusDays(1);
        when(mockTrainingService.createTraining(any(Training.class))).thenReturn(testTraining);

        Training result = gymFacade.createTraining(2L, 1L, "Morning Run",
                cardioType, trainingDate, 60);

        assertNotNull(result);
        verify(mockTrainingService).createTraining(argThat(training ->
                Long.valueOf(2L).equals(training.getTraineeId()) &&
                        Long.valueOf(1L).equals(training.getTrainerId()) &&
                        "Morning Run".equals(training.getTrainingName()) &&
                        cardioType.equals(training.getTrainingType()) &&
                        trainingDate.equals(training.getTrainingDate()) &&
                        Integer.valueOf(60).equals(training.getTrainingDuration())
        ));
    }

    @Test
    @DisplayName("getAllTrainings should delegate to service")
    void getAllTrainings_ShouldDelegateToService() {
        List<Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingService.findAllTrainings()).thenReturn(expectedTrainings);

        List<Training> result = gymFacade.getAllTrainings();

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("getTraineeTrainings should delegate to service")
    void getTraineeTrainings_ShouldDelegateToService() {
        List<Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingService.findTrainingsByTraineeId(2L)).thenReturn(expectedTrainings);

        List<Training> result = gymFacade.getTraineeTrainings(2L);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("getTrainerTrainings should delegate to service")
    void getTrainerTrainings_ShouldDelegateToService() {
        List<Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingService.findTrainingsByTrainerId(1L)).thenReturn(expectedTrainings);

        List<Training> result = gymFacade.getTrainerTrainings(1L);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("getTrainingsByDate should delegate to service")
    void getTrainingsByDate_ShouldDelegateToService() {
        LocalDate date = LocalDate.now();
        List<Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingService.findTrainingsByDate(date)).thenReturn(expectedTrainings);

        List<Training> result = gymFacade.getTrainingsByDate(date);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("getTrainingsByDateRange should delegate to service")
    void getTrainingsByDateRange_ShouldDelegateToService() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(7);
        List<Training> expectedTrainings = List.of(testTraining);
        when(mockTrainingService.findTrainingsByDateRange(start, end)).thenReturn(expectedTrainings);

        List<Training> result = gymFacade.getTrainingsByDateRange(start, end);

        assertEquals(expectedTrainings, result);
    }

    @Test
    @DisplayName("getAvailableTrainingTypes should delegate to storage")
    void getAvailableTrainingTypes_ShouldDelegateToStorage() {
        List<TrainingType> expectedTypes = List.of(cardioType);
        when(mockStorage.getAllTrainingTypes()).thenReturn(expectedTypes);

        List<TrainingType> result = gymFacade.getAvailableTrainingTypes();

        assertEquals(expectedTypes, result);
    }

    @Test
    @DisplayName("getTrainingTypeById should delegate to storage")
    void getTrainingTypeById_ShouldDelegateToStorage() {
        when(mockStorage.getTrainingType(1L)).thenReturn(cardioType);
        TrainingType result = gymFacade.getTrainingTypeById(1L);
        assertEquals(cardioType, result);
    }

    @Test
    @DisplayName("getTrainingTypeByName should find by name")
    void getTrainingTypeByName_ShouldFindByName() {
        List<TrainingType> types = List.of(cardioType, new TrainingType(2L, "Strength"));
        when(mockStorage.getAllTrainingTypes()).thenReturn(types);

        TrainingType result = gymFacade.getTrainingTypeByName("Cardio");

        assertEquals(cardioType, result);
    }

    @Test
    @DisplayName("getTrainingTypeByName should be case insensitive")
    void getTrainingTypeByName_ShouldBeCaseInsensitive() {
        List<TrainingType> types = List.of(cardioType);
        when(mockStorage.getAllTrainingTypes()).thenReturn(types);

        TrainingType result = gymFacade.getTrainingTypeByName("CARDIO");

        assertEquals(cardioType, result);
    }

    @Test
    @DisplayName("getTrainingTypeByName should return null for invalid name")
    void getTrainingTypeByName_WithInvalidName_ShouldReturnNull() {
        assertNull(gymFacade.getTrainingTypeByName(null));
        assertNull(gymFacade.getTrainingTypeByName(""));
        assertNull(gymFacade.getTrainingTypeByName("   "));
    }

    @Test
    @DisplayName("getTrainerProfile should create profile with trainer and trainings")
    void getTrainerProfile_ShouldCreateProfileWithTrainerAndTrainings() {
        List<Training> trainings = List.of(testTraining);
        when(mockTrainerService.findTrainerById(1L)).thenReturn(Optional.of(testTrainer));
        when(mockTrainingService.findTrainingsByTrainerId(1L)).thenReturn(trainings);

        TrainerProfile result = gymFacade.getTrainerProfile(1L);

        assertNotNull(result);
        assertEquals(testTrainer, result.getTrainer());
        assertEquals(trainings, result.getTrainings());
    }

    @Test
    @DisplayName("getTrainerProfile should return null when trainer not found")
    void getTrainerProfile_WhenTrainerNotFound_ShouldReturnNull() {
        when(mockTrainerService.findTrainerById(999L)).thenReturn(Optional.empty());
        TrainerProfile result = gymFacade.getTrainerProfile(999L);
        assertNull(result);
    }

    @Test
    @DisplayName("getTraineeProfile should create profile with trainee and trainings")
    void getTraineeProfile_ShouldCreateProfileWithTraineeAndTrainings() {
        List<Training> trainings = List.of(testTraining);
        when(mockTraineeService.findTraineeById(2L)).thenReturn(Optional.of(testTrainee));
        when(mockTrainingService.findTrainingsByTraineeId(2L)).thenReturn(trainings);

        TraineeProfile result = gymFacade.getTraineeProfile(2L);

        assertNotNull(result);
        assertEquals(testTrainee, result.getTrainee());
        assertEquals(trainings, result.getTrainings());
    }

    @Test
    @DisplayName("getTraineeProfile should return null when trainee not found")
    void getTraineeProfile_WhenTraineeNotFound_ShouldReturnNull() {
        when(mockTraineeService.findTraineeById(999L)).thenReturn(Optional.empty());
        TraineeProfile result = gymFacade.getTraineeProfile(999L);
        assertNull(result);
    }

    @Test
    @DisplayName("canCreateTraining should return true when both are active")
    void canCreateTraining_WithActiveUsers_ShouldReturnTrue() {
        testTrainee.setIsActive(true);
        testTrainer.setIsActive(true);

        when(mockTraineeService.findTraineeById(2L)).thenReturn(Optional.of(testTrainee));
        when(mockTrainerService.findTrainerById(1L)).thenReturn(Optional.of(testTrainer));

        boolean result = gymFacade.canCreateTraining(2L, 1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("canCreateTraining should return false when trainee is inactive")
    void canCreateTraining_WithInactiveTrainee_ShouldReturnFalse() {
        testTrainee.setIsActive(false);
        testTrainer.setIsActive(true);

        when(mockTraineeService.findTraineeById(2L)).thenReturn(Optional.of(testTrainee));
        when(mockTrainerService.findTrainerById(1L)).thenReturn(Optional.of(testTrainer));

        boolean result = gymFacade.canCreateTraining(2L, 1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("canCreateTraining should return false when trainer is inactive")
    void canCreateTraining_WithInactiveTrainer_ShouldReturnFalse() {
        testTrainee.setIsActive(true);
        testTrainer.setIsActive(false);

        when(mockTraineeService.findTraineeById(2L)).thenReturn(Optional.of(testTrainee));
        when(mockTrainerService.findTrainerById(1L)).thenReturn(Optional.of(testTrainer));

        boolean result = gymFacade.canCreateTraining(2L, 1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("canCreateTraining should return false when trainee not found")
    void canCreateTraining_WhenTraineeNotFound_ShouldReturnFalse() {
        when(mockTraineeService.findTraineeById(999L)).thenReturn(Optional.empty());
        when(mockTrainerService.findTrainerById(1L)).thenReturn(Optional.of(testTrainer));

        boolean result = gymFacade.canCreateTraining(999L, 1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("canCreateTraining should return false when trainer not found")
    void canCreateTraining_WhenTrainerNotFound_ShouldReturnFalse() {
        when(mockTraineeService.findTraineeById(2L)).thenReturn(Optional.of(testTrainee));
        when(mockTrainerService.findTrainerById(999L)).thenReturn(Optional.empty());

        boolean result = gymFacade.canCreateTraining(2L, 999L);

        assertFalse(result);
    }
}