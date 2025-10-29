package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.entity.Trainee;
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
@Import(TraineeDaoImpl.class)
@ActiveProfiles("test")
@Transactional
class TraineeDaoImplTest {

    @Autowired
    private TraineeDao traineeDao;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Test Street"
        );
        testTrainee.setUsername("john.doe");
        testTrainee.setPassword("password123");
        testTrainee.setIsActive(true);
    }

    @Test
    void create_ShouldPersistTrainee() {
        Trainee created = traineeDao.create(testTrainee);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getLastName()).isEqualTo("Doe");
        assertThat(created.getUsername()).isEqualTo("john.doe");
        assertThat(created.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(created.getAddress()).isEqualTo("123 Test Street");
    }

    @Test
    void create_ShouldThrowException_WhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeDao.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee cannot be null");
    }

    @Test
    void update_ShouldModifyExistingTrainee() {
        Trainee created = traineeDao.create(testTrainee);
        created.setFirstName("Jane");
        created.setAddress("456 New Street");

        Trainee updated = traineeDao.update(created);

        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getAddress()).isEqualTo("456 New Street");
        assertThat(updated.getId()).isEqualTo(created.getId());
    }

    @Test
    void update_ShouldThrowException_WhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeDao.update(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee cannot be null");
    }

    @Test
    void update_ShouldThrowException_WhenTraineeIdIsNull() {
        Trainee traineeWithoutId = new Trainee("Test", "User");

        assertThatThrownBy(() -> traineeDao.update(traineeWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee id cannot be null for update");
    }

    @Test
    void delete_ShouldRemoveTrainee() {
        Trainee created = traineeDao.create(testTrainee);
        Long id = created.getId();
        boolean deleted = traineeDao.delete(id);
        assertThat(deleted).isTrue();
        assertThat(traineeDao.findById(id)).isEmpty();
    }

    @Test
    void delete_ShouldReturnFalse_WhenTraineeNotFound() {
        boolean deleted = traineeDao.delete(999L);
        assertThat(deleted).isFalse();
    }

    @Test
    void delete_ShouldReturnFalse_WhenIdIsNull() {
        boolean deleted = traineeDao.delete(null);
        assertThat(deleted).isFalse();
    }

    @Test
    void findById_ShouldReturnTrainee_WhenExists() {
        Trainee created = traineeDao.create(testTrainee);
        Optional<Trainee> found = traineeDao.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<Trainee> found = traineeDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<Trainee> found = traineeDao.findById(null);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllTrainees() {
        traineeDao.create(testTrainee);

        Trainee trainee2 = new Trainee("Jane", "Smith");
        trainee2.setUsername("jane.smith");
        trainee2.setPassword("password456");
        traineeDao.create(trainee2);

        List<Trainee> all = traineeDao.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Trainee::getUsername)
                .containsExactlyInAnyOrder("john.doe", "jane.smith");
    }

    @Test
    void findByUsername_ShouldReturnTrainee_WhenExists() {
        traineeDao.create(testTrainee);
        Optional<Trainee> found = traineeDao.findByUsername("john.doe");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenNotExists() {
        Optional<Trainee> found = traineeDao.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameIsNull() {
        Optional<Trainee> found = traineeDao.findByUsername(null);
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameIsEmpty() {
        Optional<Trainee> found = traineeDao.findByUsername("");
        assertThat(found).isEmpty();
    }

    @Test
    void findAllActive_ShouldReturnOnlyActiveTrainees() {
        traineeDao.create(testTrainee);
        Trainee inactiveTrainee = new Trainee("Inactive", "User");
        inactiveTrainee.setUsername("inactive.user");
        inactiveTrainee.setPassword("password789");
        inactiveTrainee.setIsActive(false);
        traineeDao.create(inactiveTrainee);

        List<Trainee> activeTrainees = traineeDao.findAllActive();

        assertThat(activeTrainees).hasSize(1);
        assertThat(activeTrainees.getFirst().getUsername()).isEqualTo("john.doe");
        assertThat(activeTrainees.getFirst().getIsActive()).isTrue();
    }

    @Test
    void existsById_ShouldReturnTrue_WhenTraineeExists() {
        Trainee created = traineeDao.create(testTrainee);
        boolean exists = traineeDao.existsById(created.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenTraineeNotExists() {
        boolean exists = traineeDao.existsById(999L);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenIdIsNull() {
        boolean exists = traineeDao.existsById(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUsernameExists() {
        traineeDao.create(testTrainee);
        boolean exists = traineeDao.existsByUsername("john.doe");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameNotExists() {
        boolean exists = traineeDao.existsByUsername("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameIsNull() {
        boolean exists = traineeDao.existsByUsername(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameIsEmpty() {
        boolean exists = traineeDao.existsByUsername("");
        assertThat(exists).isFalse();
    }

    @Test
    void shouldTrimWhitespaceFromFields() {
        Trainee traineeWithSpaces = new Trainee(
                "  John  ",
                "  Doe  ",
                LocalDate.of(1990, 1, 1),
                "  123 Test Street  "
        );
        traineeWithSpaces.setUsername("john.doe");
        traineeWithSpaces.setPassword("password");
        Trainee created = traineeDao.create(traineeWithSpaces);
        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getLastName()).isEqualTo("Doe");
        assertThat(created.getAddress()).isEqualTo("123 Test Street");
    }
}