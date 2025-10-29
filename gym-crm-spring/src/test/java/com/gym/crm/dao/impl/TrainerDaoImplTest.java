package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({TrainerDaoImpl.class, TrainingTypeDaoImpl.class})
@ActiveProfiles("test")
@Transactional
class TrainerDaoImplTest {

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    private Trainer testTrainer;
    private TrainingType testSpecialization;

    @BeforeEach
    void setUp() {
        testSpecialization = new TrainingType("Cardio");
        testSpecialization = trainingTypeDao.create(testSpecialization);

        testTrainer = new Trainer("John", "Trainer", testSpecialization);
        testTrainer.setUsername("john.trainer");
        testTrainer.setPassword("password123");
        testTrainer.setIsActive(true);
    }

    @Test
    void create_ShouldPersistTrainer() {
        Trainer created = trainerDao.create(testTrainer);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getLastName()).isEqualTo("Trainer");
        assertThat(created.getUsername()).isEqualTo("john.trainer");
        assertThat(created.getSpecialization()).isEqualTo(testSpecialization);
        assertThat(created.getSpecializationName()).isEqualTo("Cardio");
    }

    @Test
    void create_ShouldThrowException_WhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerDao.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer cannot be null");
    }

    @Test
    void update_ShouldModifyExistingTrainer() {
        Trainer created = trainerDao.create(testTrainer);
        created.setFirstName("Jane");
        created.setIsActive(false);

        Trainer updated = trainerDao.update(created);
        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getSpecialization()).isEqualTo(testSpecialization);
    }

    @Test
    void update_ShouldThrowException_WhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerDao.update(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer cannot be null");
    }

    @Test
    void update_ShouldThrowException_WhenTrainerIdIsNull() {
        Trainer trainerWithoutId = new Trainer("Test", "User", testSpecialization);
        assertThatThrownBy(() -> trainerDao.update(trainerWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer id cannot be null for update");
    }

    @Test
    void findById_ShouldReturnTrainer_WhenExists() {
        Trainer created = trainerDao.create(testTrainer);
        Optional<Trainer> found = trainerDao.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getUsername()).isEqualTo("john.trainer");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<Trainer> found = trainerDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<Trainer> found = trainerDao.findById(null);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        trainerDao.create(testTrainer);

        TrainingType strengthType = trainingTypeDao.create(new TrainingType("Strength"));
        Trainer trainer2 = new Trainer("Jane", "Coach", strengthType);
        trainer2.setUsername("jane.coach");
        trainer2.setPassword("password456");
        trainerDao.create(trainer2);

        List<Trainer> all = trainerDao.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Trainer::getUsername)
                .containsExactlyInAnyOrder("john.trainer", "jane.coach");
    }

    @Test
    void findByUsername_ShouldReturnTrainer_WhenExists() {
        trainerDao.create(testTrainer);
        Optional<Trainer> found = trainerDao.findByUsername("john.trainer");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.trainer");
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenNotExists() {
        Optional<Trainer> found = trainerDao.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameIsNull() {
        Optional<Trainer> found = trainerDao.findByUsername(null);
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameIsEmpty() {
        Optional<Trainer> found = trainerDao.findByUsername("");
        assertThat(found).isEmpty();
    }

    @Test
    void existsById_ShouldReturnTrue_WhenTrainerExists() {
        Trainer created = trainerDao.create(testTrainer);
        boolean exists = trainerDao.existsById(created.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenTrainerNotExists() {
        boolean exists = trainerDao.existsById(999L);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenIdIsNull() {
        boolean exists = trainerDao.existsById(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUsernameExists() {
        trainerDao.create(testTrainer);
        boolean exists = trainerDao.existsByUsername("john.trainer");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameNotExists() {
        boolean exists = trainerDao.existsByUsername("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameIsNull() {
        boolean exists = trainerDao.existsByUsername(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameIsEmpty() {
        boolean exists = trainerDao.existsByUsername("");
        assertThat(exists).isFalse();
    }

    @Test
    void shouldTrimWhitespaceFromFields() {
        Trainer trainerWithSpaces = new Trainer(
                "  Jane  ",
                "  Coach  ",
                testSpecialization
        );
        trainerWithSpaces.setUsername("jane.coach");
        trainerWithSpaces.setPassword("password");
        Trainer created = trainerDao.create(trainerWithSpaces);

        assertThat(created.getFirstName()).isEqualTo("Jane");
        assertThat(created.getLastName()).isEqualTo("Coach");
    }
}