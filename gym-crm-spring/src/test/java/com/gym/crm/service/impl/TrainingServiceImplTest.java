package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.exception.UnauthorizedAccessException;
import com.gym.crm.service.TrainingService;
import com.gym.crm.util.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private ValidationService validationService;

    private TrainingService trainingService;
    private Training testTraining;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl(
                trainingDao,
                trainerDao,
                traineeDao,
                validationService
        );

        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Test St");
        testTrainee.setId(1L);
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("password");
        testTrainee.setIsActive(true);

        testTrainingType = new TrainingType("Cardio");
        testTrainingType.setId(1L);

        testTrainer = new Trainer("Jane", "Smith", testTrainingType);
        testTrainer.setId(2L);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("password");
        testTrainer.setIsActive(true);

        testTraining = new Training(
                testTrainee.getId(),
                testTrainer.getId(),
                "Morning Workout",
                testTrainingType,
                LocalDate.now(),
                60
        );
        testTraining.setId(1L);
    }

    @Test
    void createTraining_ShouldPersistTraining_WhenUserIsTrainee() {
        when(traineeDao.existsById(testTrainee.getId())).thenReturn(true);
        when(trainerDao.existsById(testTrainer.getId())).thenReturn(true);
        when(traineeDao.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));
        when(trainerDao.findById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));
        when(trainingDao.create(any(Training.class))).thenReturn(testTraining);

        Training created = trainingService.createTraining("john.doe", testTraining);

        assertThat(created).isNotNull();
        assertThat(created.getTrainingName()).isEqualTo("Morning Workout");
        verify(validationService).validateTraining(testTraining);
        verify(trainingDao).create(testTraining);
    }

    @Test
    void createTraining_ShouldPersistTraining_WhenUserIsTrainer() {
        when(traineeDao.existsById(testTrainee.getId())).thenReturn(true);
        when(trainerDao.existsById(testTrainer.getId())).thenReturn(true);
        when(traineeDao.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));
        when(trainerDao.findById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));
        when(trainingDao.create(any(Training.class))).thenReturn(testTraining);

        Training created = trainingService.createTraining("jane.smith", testTraining);

        assertThat(created).isNotNull();
        verify(trainingDao).create(testTraining);
    }

    @Test
    void createTraining_ShouldThrowException_WhenTrainingIsNull() {
        assertThatThrownBy(() -> trainingService.createTraining("john.doe", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training cannot be null");
    }

    @Test
    void createTraining_ShouldThrowException_WhenTraineeInactive() {
        testTrainee.setIsActive(false);
        when(traineeDao.existsById(testTrainee.getId())).thenReturn(true);
        when(trainerDao.existsById(testTrainer.getId())).thenReturn(true);
        when(traineeDao.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));

        assertThatThrownBy(() -> trainingService.createTraining("john.doe", testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create training for inactive trainee");
    }

    @Test
    void createTraining_ShouldThrowException_WhenTrainerInactive() {
        testTrainer.setIsActive(false);
        when(traineeDao.existsById(testTrainee.getId())).thenReturn(true);
        when(trainerDao.existsById(testTrainer.getId())).thenReturn(true);
        when(traineeDao.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));
        when(trainerDao.findById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));

        assertThatThrownBy(() -> trainingService.createTraining("john.doe", testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create training for inactive trainer");
    }

    @Test
    void findTrainingById_ShouldReturnTraining_WhenExists() {
        when(trainingDao.findById(1L)).thenReturn(Optional.of(testTraining));

        Optional<Training> found = trainingService.findTrainingById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getTrainingName()).isEqualTo("Morning Workout");
    }

    @Test
    void findTrainingById_ShouldReturnEmpty_WhenNotExists() {
        when(trainingDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Training> found = trainingService.findTrainingById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findTrainingById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<Training> found = trainingService.findTrainingById(null);

        assertThat(found).isEmpty();
        verify(trainingDao, never()).findById(any());
    }

    @Test
    void findAllTrainings_ShouldReturnAllTrainings() {
        List<Training> trainings = Arrays.asList(testTraining, new Training());
        when(trainingDao.findAll()).thenReturn(trainings);

        List<Training> found = trainingService.findAllTrainings();

        assertThat(found).hasSize(2);
        assertThat(found).isEqualTo(trainings);
    }

    @Test
    void findTrainingsByTraineeId_ShouldReturnTrainings_WhenAuthorized() {
        when(traineeDao.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));
        when(trainingDao.findByTraineeId(testTrainee.getId())).thenReturn(Arrays.asList(testTraining));

        List<Training> found = trainingService.findTrainingsByTraineeId("john.doe", testTrainee.getId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0)).isEqualTo(testTraining);
    }

    @Test
    void findTrainingsByTraineeId_ShouldThrowException_WhenUnauthorized() {
        Trainee otherTrainee = new Trainee("Other", "User");
        otherTrainee.setId(99L);
        otherTrainee.setUsername("other.user");

        when(traineeDao.findById(otherTrainee.getId())).thenReturn(Optional.of(otherTrainee));

        assertThatThrownBy(() -> trainingService.findTrainingsByTraineeId("john.doe", otherTrainee.getId()))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Users can only access their own data");
    }

    @Test
    void findTrainingsByTraineeId_ShouldReturnEmpty_WhenIdIsNull() {
        List<Training> found = trainingService.findTrainingsByTraineeId("john.doe", null);

        assertThat(found).isEmpty();
        verify(trainingDao, never()).findByTraineeId(any());
    }

    @Test
    void findTrainingsByTrainerId_ShouldReturnTrainings_WhenAuthorized() {
        when(trainerDao.findById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));
        when(trainingDao.findByTrainerId(testTrainer.getId())).thenReturn(Arrays.asList(testTraining));

        List<Training> found = trainingService.findTrainingsByTrainerId("jane.smith", testTrainer.getId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0)).isEqualTo(testTraining);
    }

    @Test
    void findTrainingsByTrainerId_ShouldThrowException_WhenUnauthorized() {
        Trainer otherTrainer = new Trainer("Other", "Trainer", testTrainingType);
        otherTrainer.setId(99L);
        otherTrainer.setUsername("other.trainer");

        when(trainerDao.findById(otherTrainer.getId())).thenReturn(Optional.of(otherTrainer));

        assertThatThrownBy(() -> trainingService.findTrainingsByTrainerId("jane.smith", otherTrainer.getId()))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Users can only access their own data");
    }

    @Test
    void findTrainingsByTrainerId_ShouldReturnEmpty_WhenIdIsNull() {
        List<Training> found = trainingService.findTrainingsByTrainerId("jane.smith", null);

        assertThat(found).isEmpty();
        verify(trainingDao, never()).findByTrainerId(any());
    }

    @Test
    void findTrainingsByDate_ShouldReturnTrainings() {
        LocalDate date = LocalDate.now();
        when(trainingDao.findByDate(date)).thenReturn(Arrays.asList(testTraining));

        List<Training> found = trainingService.findTrainingsByDate(date);

        assertThat(found).hasSize(1);
        assertThat(found.get(0)).isEqualTo(testTraining);
    }

    @Test
    void findTrainingsByDate_ShouldReturnEmpty_WhenDateIsNull() {
        List<Training> found = trainingService.findTrainingsByDate(null);

        assertThat(found).isEmpty();
        verify(trainingDao, never()).findByDate(any());
    }

    @Test
    void findTrainingsByDateRange_ShouldReturnTrainings() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(7);
        when(trainingDao.findByDateRange(start, end)).thenReturn(Arrays.asList(testTraining));

        List<Training> found = trainingService.findTrainingsByDateRange(start, end);

        assertThat(found).hasSize(1);
        assertThat(found.get(0)).isEqualTo(testTraining);
    }

    @Test
    void findTrainingsByDateRange_ShouldReturnEmpty_WhenNullDates() {
        List<Training> found1 = trainingService.findTrainingsByDateRange(null, LocalDate.now());
        List<Training> found2 = trainingService.findTrainingsByDateRange(LocalDate.now(), null);

        assertThat(found1).isEmpty();
        assertThat(found2).isEmpty();
        verify(trainingDao, never()).findByDateRange(any(), any());
    }

    @Test
    void findTrainingsByDateRange_ShouldReturnEmpty_WhenInvalidRange() {
        LocalDate end = LocalDate.now();
        LocalDate start = LocalDate.now().plusDays(1);

        List<Training> found = trainingService.findTrainingsByDateRange(start, end);

        assertThat(found).isEmpty();
        verify(trainingDao, never()).findByDateRange(any(), any());
    }

    @Test
    void findTraineeTrainingsByDateRange_ShouldReturnTrainings_WhenAuthorized() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(7);

        when(traineeDao.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));
        when(trainingDao.findByTraineeIdAndDateRange(testTrainee.getId(), start, end))
                .thenReturn(Arrays.asList(testTraining));

        List<Training> found = trainingService.findTraineeTrainingsByDateRange(
                "john.doe", testTrainee.getId(), start, end);

        assertThat(found).hasSize(1);
        assertThat(found.get(0)).isEqualTo(testTraining);
    }

    @Test
    void findTraineeTrainingsByDateRange_ShouldThrowException_WhenUnauthorized() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(7);

        Trainee otherTrainee = new Trainee("Other", "User");
        otherTrainee.setId(99L);
        otherTrainee.setUsername("other.user");

        when(traineeDao.findById(otherTrainee.getId())).thenReturn(Optional.of(otherTrainee));

        assertThatThrownBy(() -> trainingService.findTraineeTrainingsByDateRange(
                "john.doe", otherTrainee.getId(), start, end))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Users can only access their own data");
    }

    @Test
    void findTrainerTrainingsByDateRange_ShouldReturnTrainings_WhenAuthorized() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(7);

        when(trainerDao.findById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));
        when(trainingDao.findByTrainerIdAndDateRange(testTrainer.getId(), start, end))
                .thenReturn(Arrays.asList(testTraining));

        List<Training> found = trainingService.findTrainerTrainingsByDateRange(
                "jane.smith", testTrainer.getId(), start, end);

        assertThat(found).hasSize(1);
        assertThat(found.get(0)).isEqualTo(testTraining);
    }

    @Test
    void trainingExists_ShouldReturnTrue_WhenExists() {
        when(trainingDao.existsById(1L)).thenReturn(true);

        boolean exists = trainingService.trainingExists(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void trainingExists_ShouldReturnFalse_WhenNotExists() {
        when(trainingDao.existsById(999L)).thenReturn(false);

        boolean exists = trainingService.trainingExists(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void trainingExists_ShouldReturnFalse_WhenIdIsNull() {
        boolean exists = trainingService.trainingExists(null);

        assertThat(exists).isFalse();
    }
}