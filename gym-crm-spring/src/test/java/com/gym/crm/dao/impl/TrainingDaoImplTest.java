package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.entity.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({TrainingDaoImpl.class, TraineeDaoImpl.class, TrainerDaoImpl.class, TrainingTypeDaoImpl.class})
@ActiveProfiles("test")
@Transactional
class TrainingDaoImplTest {

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    private Training testTraining;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Test St");
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("password123");
        testTrainee = traineeDao.create(testTrainee);

        testTrainingType = new TrainingType("Cardio");
        testTrainingType = trainingTypeDao.create(testTrainingType);

        testTrainer = new Trainer("Jane", "Smith", testTrainingType);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("password456");
        testTrainer = trainerDao.create(testTrainer);

        testTraining = new Training(
                testTrainee.getId(),
                testTrainer.getId(),
                "Morning Cardio",
                testTrainingType,
                LocalDate.now(),
                60
        );
    }

    @Test
    void create_ShouldPersistTraining() {
        Training created = trainingDao.create(testTraining);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTrainingName()).isEqualTo("Morning Cardio");
        assertThat(created.getTraineeId()).isEqualTo(testTrainee.getId());
        assertThat(created.getTrainerId()).isEqualTo(testTrainer.getId());
        assertThat(created.getTrainingType()).isEqualTo(testTrainingType);
        assertThat(created.getTrainingDate()).isEqualTo(LocalDate.now());
        assertThat(created.getTrainingDuration()).isEqualTo(60);
    }

    @Test
    void create_ShouldThrowException_WhenTrainingIsNull() {
        assertThatThrownBy(() -> trainingDao.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training cannot be null");
    }

    @Test
    void findById_ShouldReturnTraining_WhenExists() {
        Training created = trainingDao.create(testTraining);
        Optional<Training> found = trainingDao.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getTrainingName()).isEqualTo("Morning Cardio");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<Training> found = trainingDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<Training> found = trainingDao.findById(null);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        trainingDao.create(testTraining);

        Training training2 = new Training(
                testTrainee.getId(),
                testTrainer.getId(),
                "Evening Workout",
                testTrainingType,
                LocalDate.now().plusDays(1),
                45
        );
        trainingDao.create(training2);

        List<Training> all = trainingDao.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Morning Cardio", "Evening Workout");
    }

    @Test
    void findByTraineeId_ShouldReturnTraineeTrainings() {
        trainingDao.create(testTraining);
        Trainee anotherTrainee = new Trainee("Another", "Trainee");
        anotherTrainee.setUsername("another.trainee");
        anotherTrainee.setPassword("password789");
        anotherTrainee = traineeDao.create(anotherTrainee);

        Training anotherTraining = new Training(
                anotherTrainee.getId(),
                testTrainer.getId(),
                "Different Training",
                testTrainingType,
                LocalDate.now(),
                30
        );
        trainingDao.create(anotherTraining);

        List<Training> traineeTrainings = trainingDao.findByTraineeId(testTrainee.getId());

        assertThat(traineeTrainings).hasSize(1);
        assertThat(traineeTrainings.getFirst().getTrainingName()).isEqualTo("Morning Cardio");
    }

    @Test
    void findByTraineeId_ShouldReturnEmpty_WhenNoTrainings() {
        List<Training> trainings = trainingDao.findByTraineeId(999L);
        assertThat(trainings).isEmpty();
    }

    @Test
    void findByTraineeId_ShouldReturnEmpty_WhenIdIsNull() {
        List<Training> trainings = trainingDao.findByTraineeId(null);
        assertThat(trainings).isEmpty();
    }

    @Test
    void findByTrainerId_ShouldReturnTrainerTrainings() {
        trainingDao.create(testTraining);
        Trainer anotherTrainer = new Trainer("Another", "Trainer", testTrainingType);
        anotherTrainer.setUsername("another.trainer");
        anotherTrainer.setPassword("password789");
        anotherTrainer = trainerDao.create(anotherTrainer);

        Training anotherTraining = new Training(
                testTrainee.getId(),
                anotherTrainer.getId(),
                "Different Training",
                testTrainingType,
                LocalDate.now(),
                30
        );
        trainingDao.create(anotherTraining);

        List<Training> trainerTrainings = trainingDao.findByTrainerId(testTrainer.getId());

        assertThat(trainerTrainings).hasSize(1);
        assertThat(trainerTrainings.getFirst().getTrainingName()).isEqualTo("Morning Cardio");
    }

    @Test
    void findByTrainerId_ShouldReturnEmpty_WhenNoTrainings() {
        List<Training> trainings = trainingDao.findByTrainerId(999L);
        assertThat(trainings).isEmpty();
    }

    @Test
    void findByTrainerId_ShouldReturnEmpty_WhenIdIsNull() {
        List<Training> trainings = trainingDao.findByTrainerId(null);
        assertThat(trainings).isEmpty();
    }

    @Test
    void findByDate_ShouldReturnTrainingsOnSpecificDate() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        trainingDao.create(testTraining);

        Training tomorrowTraining = new Training(
                testTrainee.getId(),
                testTrainer.getId(),
                "Tomorrow Training",
                testTrainingType,
                tomorrow,
                45
        );
        trainingDao.create(tomorrowTraining);

        List<Training> todayTrainings = trainingDao.findByDate(today);

        assertThat(todayTrainings).hasSize(1);
        assertThat(todayTrainings.getFirst().getTrainingName()).isEqualTo("Morning Cardio");
    }

    @Test
    void findByDate_ShouldReturnEmpty_WhenNoTrainingsOnDate() {
        List<Training> trainings = trainingDao.findByDate(LocalDate.now().plusYears(1));
        assertThat(trainings).isEmpty();
    }

    @Test
    void findByDate_ShouldReturnEmpty_WhenDateIsNull() {
        List<Training> trainings = trainingDao.findByDate(null);
        assertThat(trainings).isEmpty();
    }

    @Test
    void findByDateRange_ShouldReturnTrainingsInRange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfter = today.plusDays(2);

        Training yesterdayTraining = new Training(
                testTrainee.getId(), testTrainer.getId(), "Yesterday",
                testTrainingType, yesterday, 30
        );
        trainingDao.create(yesterdayTraining);
        trainingDao.create(testTraining); // today

        Training tomorrowTraining = new Training(
                testTrainee.getId(), testTrainer.getId(), "Tomorrow",
                testTrainingType, tomorrow, 45
        );
        trainingDao.create(tomorrowTraining);

        Training dayAfterTraining = new Training(
                testTrainee.getId(), testTrainer.getId(), "Day After",
                testTrainingType, dayAfter, 60
        );
        trainingDao.create(dayAfterTraining);

        List<Training> rangeTrainings = trainingDao.findByDateRange(yesterday, tomorrow);

        assertThat(rangeTrainings).hasSize(3);
        assertThat(rangeTrainings).extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Yesterday", "Morning Cardio", "Tomorrow");
    }

    @Test
    void findByDateRange_ShouldReturnEmpty_WhenInvalidRange() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate today = LocalDate.now();

        List<Training> trainings = trainingDao.findByDateRange(tomorrow, today);

        assertThat(trainings).isEmpty();
    }

    @Test
    void findByDateRange_ShouldReturnEmpty_WhenNullDates() {
        List<Training> trainings1 = trainingDao.findByDateRange(null, LocalDate.now());
        List<Training> trainings2 = trainingDao.findByDateRange(LocalDate.now(), null);
        List<Training> trainings3 = trainingDao.findByDateRange(null, null);

        assertThat(trainings1).isEmpty();
        assertThat(trainings2).isEmpty();
        assertThat(trainings3).isEmpty();
    }

    @Test
    void findByTraineeIdAndDateRange_ShouldReturnFilteredTrainings() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        trainingDao.create(testTraining);

        Training tomorrowTraining = new Training(
                testTrainee.getId(), testTrainer.getId(), "Tomorrow Training",
                testTrainingType, tomorrow, 45
        );
        trainingDao.create(tomorrowTraining);

        Trainee anotherTrainee = new Trainee("Another", "Trainee");
        anotherTrainee.setUsername("another.trainee");
        anotherTrainee.setPassword("password");
        anotherTrainee = traineeDao.create(anotherTrainee);

        Training anotherTraineeTraining = new Training(
                anotherTrainee.getId(), testTrainer.getId(), "Other Trainee Training",
                testTrainingType, today, 30
        );
        trainingDao.create(anotherTraineeTraining);

        List<Training> traineeRangeTrainings = trainingDao.findByTraineeIdAndDateRange(
                testTrainee.getId(), today, tomorrow
        );

        assertThat(traineeRangeTrainings).hasSize(2);
        assertThat(traineeRangeTrainings).extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Morning Cardio", "Tomorrow Training");
    }

    @Test
    void findByTrainerIdAndDateRange_ShouldReturnFilteredTrainings() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        trainingDao.create(testTraining);

        Training tomorrowTraining = new Training(
                testTrainee.getId(), testTrainer.getId(), "Tomorrow Training",
                testTrainingType, tomorrow, 45
        );
        trainingDao.create(tomorrowTraining);

        Trainer anotherTrainer = new Trainer("Another", "Trainer", testTrainingType);
        anotherTrainer.setUsername("another.trainer");
        anotherTrainer.setPassword("password");
        anotherTrainer = trainerDao.create(anotherTrainer);

        Training anotherTrainerTraining = new Training(
                testTrainee.getId(), anotherTrainer.getId(), "Other Trainer Training",
                testTrainingType, today, 30
        );
        trainingDao.create(anotherTrainerTraining);

        List<Training> trainerRangeTrainings = trainingDao.findByTrainerIdAndDateRange(
                testTrainer.getId(), today, tomorrow
        );

        assertThat(trainerRangeTrainings).hasSize(2);
        assertThat(trainerRangeTrainings).extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Morning Cardio", "Tomorrow Training");
    }

    @Test
    void existsById_ShouldReturnTrue_WhenTrainingExists() {
        Training created = trainingDao.create(testTraining);
        boolean exists = trainingDao.existsById(created.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenTrainingNotExists() {
        boolean exists = trainingDao.existsById(999L);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenIdIsNull() {
        boolean exists = trainingDao.existsById(null);
        assertThat(exists).isFalse();
    }

    @Test
    void deleteByTraineeId_ShouldRemoveAllTraineeTrainings() {
        trainingDao.create(testTraining);

        Training training2 = new Training(
                testTrainee.getId(), testTrainer.getId(), "Another Training",
                testTrainingType, LocalDate.now().plusDays(1), 45
        );
        trainingDao.create(training2);
        Trainee anotherTrainee = new Trainee("Another", "Trainee");
        anotherTrainee.setUsername("another.trainee");
        anotherTrainee.setPassword("password");
        anotherTrainee = traineeDao.create(anotherTrainee);

        Training anotherTraineeTraining = new Training(
                anotherTrainee.getId(), testTrainer.getId(), "Keep This",
                testTrainingType, LocalDate.now(), 30
        );
        trainingDao.create(anotherTraineeTraining);
        int deletedCount = trainingDao.deleteByTraineeId(testTrainee.getId());
        assertThat(deletedCount).isEqualTo(2);
        assertThat(trainingDao.findByTraineeId(testTrainee.getId())).isEmpty();
        assertThat(trainingDao.findByTraineeId(anotherTrainee.getId())).hasSize(1);
    }

    @Test
    void deleteByTraineeId_ShouldReturnZero_WhenNoTrainings() {
        int deletedCount = trainingDao.deleteByTraineeId(999L);
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void deleteByTraineeId_ShouldReturnZero_WhenIdIsNull() {
        int deletedCount = trainingDao.deleteByTraineeId(null);
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void shouldTrimTrainingName() {
        Training trainingWithSpaces = new Training(
                testTrainee.getId(),
                testTrainer.getId(),
                "  Trimmed Training  ",
                testTrainingType,
                LocalDate.now(),
                60
        );

        Training created = trainingDao.create(trainingWithSpaces);
        assertThat(created.getTrainingName()).isEqualTo("Trimmed Training");
    }
}