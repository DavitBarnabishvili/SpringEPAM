package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TraineeTrainerAssignmentDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.TraineeTrainerAssignment;
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
@Import({TraineeTrainerAssignmentDaoImpl.class, TraineeDaoImpl.class, TrainerDaoImpl.class, TrainingTypeDaoImpl.class})
@ActiveProfiles("test")
@Transactional
class TraineeTrainerAssignmentDaoImplTest {

    @Autowired
    private TraineeTrainerAssignmentDao assignmentDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private TraineeTrainerAssignment testAssignment;

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Test St");
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("password123");
        testTrainee = traineeDao.create(testTrainee);

        TrainingType trainingType = trainingTypeDao.create(new TrainingType("Cardio"));
        testTrainer = new Trainer("Jane", "Smith", trainingType);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("password456");
        testTrainer = trainerDao.create(testTrainer);

        testAssignment = new TraineeTrainerAssignment(testTrainee.getId(), testTrainer.getId());
    }

    @Test
    void create_ShouldPersistAssignment() {
        TraineeTrainerAssignment created = assignmentDao.create(testAssignment);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTraineeId()).isEqualTo(testTrainee.getId());
        assertThat(created.getTrainerId()).isEqualTo(testTrainer.getId());
        assertThat(created.getAssignedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void create_ShouldThrowException_WhenAssignmentIsNull() {
        assertThatThrownBy(() -> assignmentDao.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assignment cannot be null");
    }

    @Test
    void create_ShouldReturnExisting_WhenDuplicateAssignment() {
        TraineeTrainerAssignment first = assignmentDao.create(testAssignment);

        TraineeTrainerAssignment duplicate = new TraineeTrainerAssignment(
                testTrainee.getId(), testTrainer.getId()
        );
        TraineeTrainerAssignment second = assignmentDao.create(duplicate);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(assignmentDao.findByTraineeId(testTrainee.getId())).hasSize(1);
    }

    @Test
    void create_ShouldAllowCustomAssignedDate() {
        LocalDate customDate = LocalDate.of(2023, 1, 1);
        TraineeTrainerAssignment customAssignment = new TraineeTrainerAssignment(
                testTrainee.getId(), testTrainer.getId(), customDate
        );

        TraineeTrainerAssignment created = assignmentDao.create(customAssignment);

        assertThat(created.getAssignedDate()).isEqualTo(customDate);
    }

    @Test
    void findByTraineeId_ShouldReturnTraineeAssignments() {
        assignmentDao.create(testAssignment);

        Trainer anotherTrainer = new Trainer("Another", "Trainer",
                trainingTypeDao.create(new TrainingType("Strength")));
        anotherTrainer.setUsername("another.trainer");
        anotherTrainer.setPassword("password789");
        anotherTrainer = trainerDao.create(anotherTrainer);

        TraineeTrainerAssignment anotherAssignment = new TraineeTrainerAssignment(
                testTrainee.getId(), anotherTrainer.getId()
        );
        assignmentDao.create(anotherAssignment);

        List<TraineeTrainerAssignment> assignments = assignmentDao.findByTraineeId(testTrainee.getId());

        assertThat(assignments).hasSize(2);
        assertThat(assignments).extracting(TraineeTrainerAssignment::getTrainerId)
                .containsExactlyInAnyOrder(testTrainer.getId(), anotherTrainer.getId());
    }

    @Test
    void findByTraineeId_ShouldReturnEmpty_WhenNoAssignments() {
        List<TraineeTrainerAssignment> assignments = assignmentDao.findByTraineeId(999L);

        assertThat(assignments).isEmpty();
    }

    @Test
    void findByTraineeId_ShouldReturnEmpty_WhenIdIsNull() {
        List<TraineeTrainerAssignment> assignments = assignmentDao.findByTraineeId(null);

        assertThat(assignments).isEmpty();
    }

    @Test
    void findByTrainerId_ShouldReturnTrainerAssignments() {
        assignmentDao.create(testAssignment);

        Trainee anotherTrainee = new Trainee("Another", "Trainee");
        anotherTrainee.setUsername("another.trainee");
        anotherTrainee.setPassword("password789");
        anotherTrainee = traineeDao.create(anotherTrainee);

        TraineeTrainerAssignment anotherAssignment = new TraineeTrainerAssignment(
                anotherTrainee.getId(), testTrainer.getId()
        );
        assignmentDao.create(anotherAssignment);

        List<TraineeTrainerAssignment> assignments = assignmentDao.findByTrainerId(testTrainer.getId());

        assertThat(assignments).hasSize(2);
        assertThat(assignments).extracting(TraineeTrainerAssignment::getTraineeId)
                .containsExactlyInAnyOrder(testTrainee.getId(), anotherTrainee.getId());
    }

    @Test
    void findByTrainerId_ShouldReturnEmpty_WhenNoAssignments() {
        List<TraineeTrainerAssignment> assignments = assignmentDao.findByTrainerId(999L);

        assertThat(assignments).isEmpty();
    }

    @Test
    void findByTrainerId_ShouldReturnEmpty_WhenIdIsNull() {
        List<TraineeTrainerAssignment> assignments = assignmentDao.findByTrainerId(null);

        assertThat(assignments).isEmpty();
    }

    @Test
    void findByTraineeIdAndTrainerId_ShouldReturnAssignment_WhenExists() {
        assignmentDao.create(testAssignment);

        Optional<TraineeTrainerAssignment> found = assignmentDao.findByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        );

        assertThat(found).isPresent();
        assertThat(found.get().getTraineeId()).isEqualTo(testTrainee.getId());
        assertThat(found.get().getTrainerId()).isEqualTo(testTrainer.getId());
    }

    @Test
    void findByTraineeIdAndTrainerId_ShouldReturnEmpty_WhenNotExists() {
        Optional<TraineeTrainerAssignment> found = assignmentDao.findByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        );

        assertThat(found).isEmpty();
    }

    @Test
    void findByTraineeIdAndTrainerId_ShouldReturnEmpty_WhenEitherIdIsNull() {
        Optional<TraineeTrainerAssignment> found1 = assignmentDao.findByTraineeIdAndTrainerId(
                null, testTrainer.getId()
        );
        Optional<TraineeTrainerAssignment> found2 = assignmentDao.findByTraineeIdAndTrainerId(
                testTrainee.getId(), null
        );
        Optional<TraineeTrainerAssignment> found3 = assignmentDao.findByTraineeIdAndTrainerId(
                null, null
        );

        assertThat(found1).isEmpty();
        assertThat(found2).isEmpty();
        assertThat(found3).isEmpty();
    }

    @Test
    void delete_ShouldRemoveAssignment() {
        TraineeTrainerAssignment created = assignmentDao.create(testAssignment);
        Long id = created.getId();

        boolean deleted = assignmentDao.delete(id);

        assertThat(deleted).isTrue();
        assertThat(assignmentDao.findByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        )).isEmpty();
    }

    @Test
    void delete_ShouldReturnFalse_WhenAssignmentNotFound() {
        boolean deleted = assignmentDao.delete(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void delete_ShouldReturnFalse_WhenIdIsNull() {
        boolean deleted = assignmentDao.delete(null);

        assertThat(deleted).isFalse();
    }

    @Test
    void deleteByTraineeId_ShouldRemoveAllTraineeAssignments() {
        assignmentDao.create(testAssignment);

        Trainer anotherTrainer = new Trainer("Another", "Trainer",
                trainingTypeDao.create(new TrainingType("Yoga")));
        anotherTrainer.setUsername("another.trainer");
        anotherTrainer.setPassword("password");
        anotherTrainer = trainerDao.create(anotherTrainer);

        TraineeTrainerAssignment anotherAssignment = new TraineeTrainerAssignment(
                testTrainee.getId(), anotherTrainer.getId()
        );
        assignmentDao.create(anotherAssignment);

        int deletedCount = assignmentDao.deleteByTraineeId(testTrainee.getId());

        assertThat(deletedCount).isEqualTo(2);
        assertThat(assignmentDao.findByTraineeId(testTrainee.getId())).isEmpty();
    }

    @Test
    void deleteByTraineeId_ShouldReturnZero_WhenNoAssignments() {
        int deletedCount = assignmentDao.deleteByTraineeId(999L);

        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void deleteByTraineeId_ShouldReturnZero_WhenIdIsNull() {
        int deletedCount = assignmentDao.deleteByTraineeId(null);

        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void deleteByTrainerId_ShouldRemoveAllTrainerAssignments() {
        assignmentDao.create(testAssignment);

        Trainee anotherTrainee = new Trainee("Another", "Trainee");
        anotherTrainee.setUsername("another.trainee");
        anotherTrainee.setPassword("password");
        anotherTrainee = traineeDao.create(anotherTrainee);

        TraineeTrainerAssignment anotherAssignment = new TraineeTrainerAssignment(
                anotherTrainee.getId(), testTrainer.getId()
        );
        assignmentDao.create(anotherAssignment);

        int deletedCount = assignmentDao.deleteByTrainerId(testTrainer.getId());

        assertThat(deletedCount).isEqualTo(2);
        assertThat(assignmentDao.findByTrainerId(testTrainer.getId())).isEmpty();
    }

    @Test
    void deleteByTrainerId_ShouldReturnZero_WhenNoAssignments() {
        int deletedCount = assignmentDao.deleteByTrainerId(999L);

        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void deleteByTrainerId_ShouldReturnZero_WhenIdIsNull() {
        int deletedCount = assignmentDao.deleteByTrainerId(null);

        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void deleteByTraineeIdAndTrainerId_ShouldRemoveSpecificAssignment() {
        assignmentDao.create(testAssignment);

        boolean deleted = assignmentDao.deleteByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        );

        assertThat(deleted).isTrue();
        assertThat(assignmentDao.findByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        )).isEmpty();
    }

    @Test
    void deleteByTraineeIdAndTrainerId_ShouldReturnFalse_WhenNotExists() {
        boolean deleted = assignmentDao.deleteByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        );

        assertThat(deleted).isFalse();
    }

    @Test
    void deleteByTraineeIdAndTrainerId_ShouldReturnFalse_WhenEitherIdIsNull() {
        boolean deleted1 = assignmentDao.deleteByTraineeIdAndTrainerId(null, testTrainer.getId());
        boolean deleted2 = assignmentDao.deleteByTraineeIdAndTrainerId(testTrainee.getId(), null);
        boolean deleted3 = assignmentDao.deleteByTraineeIdAndTrainerId(null, null);

        assertThat(deleted1).isFalse();
        assertThat(deleted2).isFalse();
        assertThat(deleted3).isFalse();
    }

    @Test
    void replaceTraineeAssignments_ShouldReplaceAllAssignments() {
        assignmentDao.create(testAssignment);

        Trainer trainer2 = new Trainer("Trainer2", "Two",
                trainingTypeDao.create(new TrainingType("Strength")));
        trainer2.setUsername("trainer2");
        trainer2.setPassword("password2");
        trainer2 = trainerDao.create(trainer2);

        Trainer trainer3 = new Trainer("Trainer3", "Three",
                trainingTypeDao.create(new TrainingType("Flexibility")));
        trainer3.setUsername("trainer3");
        trainer3.setPassword("password3");
        trainer3 = trainerDao.create(trainer3);

        List<Long> newTrainerIds = List.of(trainer2.getId(), trainer3.getId());
        List<TraineeTrainerAssignment> newAssignments = assignmentDao.replaceTraineeAssignments(
                testTrainee.getId(), newTrainerIds
        );

        assertThat(newAssignments).hasSize(2);
        assertThat(assignmentDao.findByTraineeId(testTrainee.getId())).hasSize(2);
        assertThat(assignmentDao.existsByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        )).isFalse();
        assertThat(assignmentDao.existsByTraineeIdAndTrainerId(
                testTrainee.getId(), trainer2.getId()
        )).isTrue();
        assertThat(assignmentDao.existsByTraineeIdAndTrainerId(
                testTrainee.getId(), trainer3.getId()
        )).isTrue();
    }

    @Test
    void replaceTraineeAssignments_ShouldClearAllAssignments_WhenEmptyList() {
        assignmentDao.create(testAssignment);

        List<TraineeTrainerAssignment> newAssignments = assignmentDao.replaceTraineeAssignments(
                testTrainee.getId(), List.of()
        );

        assertThat(newAssignments).isEmpty();
        assertThat(assignmentDao.findByTraineeId(testTrainee.getId())).isEmpty();
    }

    @Test
    void replaceTraineeAssignments_ShouldClearAllAssignments_WhenNullList() {
        assignmentDao.create(testAssignment);

        List<TraineeTrainerAssignment> newAssignments = assignmentDao.replaceTraineeAssignments(
                testTrainee.getId(), null
        );

        assertThat(newAssignments).isEmpty();
        assertThat(assignmentDao.findByTraineeId(testTrainee.getId())).isEmpty();
    }

    @Test
    void replaceTraineeAssignments_ShouldThrowException_WhenTraineeIdIsNull() {
        assertThatThrownBy(() -> assignmentDao.replaceTraineeAssignments(null, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee ID cannot be null");
    }

    @Test
    void replaceTraineeAssignments_ShouldIgnoreNullTrainerIds() {
        Trainer trainer2 = new Trainer("Trainer2", "Two",
                trainingTypeDao.create(new TrainingType("CrossFit")));
        trainer2.setUsername("trainer2");
        trainer2.setPassword("password2");
        trainer2 = trainerDao.create(trainer2);

        List<Long> trainerIdsWithNull = List.of(trainer2.getId());
        List<TraineeTrainerAssignment> newAssignments = assignmentDao.replaceTraineeAssignments(
                testTrainee.getId(), trainerIdsWithNull
        );

        assertThat(newAssignments).hasSize(1);
        assertThat(assignmentDao.findByTraineeId(testTrainee.getId())).hasSize(1);
    }

    @Test
    void existsByTraineeIdAndTrainerId_ShouldReturnTrue_WhenAssignmentExists() {
        assignmentDao.create(testAssignment);

        boolean exists = assignmentDao.existsByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByTraineeIdAndTrainerId_ShouldReturnFalse_WhenAssignmentNotExists() {
        boolean exists = assignmentDao.existsByTraineeIdAndTrainerId(
                testTrainee.getId(), testTrainer.getId()
        );

        assertThat(exists).isFalse();
    }

    @Test
    void existsByTraineeIdAndTrainerId_ShouldReturnFalse_WhenEitherIdIsNull() {
        boolean exists1 = assignmentDao.existsByTraineeIdAndTrainerId(null, testTrainer.getId());
        boolean exists2 = assignmentDao.existsByTraineeIdAndTrainerId(testTrainee.getId(), null);
        boolean exists3 = assignmentDao.existsByTraineeIdAndTrainerId(null, null);

        assertThat(exists1).isFalse();
        assertThat(exists2).isFalse();
        assertThat(exists3).isFalse();
    }

    @Test
    void assignment_ShouldImplementEqualsAndHashCode() {
        TraineeTrainerAssignment assignment1 = new TraineeTrainerAssignment(1L, 2L);
        TraineeTrainerAssignment assignment2 = new TraineeTrainerAssignment(1L, 2L);
        TraineeTrainerAssignment assignment3 = new TraineeTrainerAssignment(1L, 3L);

        assertThat(assignment1).isEqualTo(assignment2);
        assertThat(assignment1.hashCode()).isEqualTo(assignment2.hashCode());
        assertThat(assignment1).isNotEqualTo(assignment3);
    }
}