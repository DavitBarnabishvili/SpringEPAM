package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingTypeDao;
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
@Import(TrainingTypeDaoImpl.class)
@ActiveProfiles("test")
@Transactional
class TrainingTypeDaoImplTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        testTrainingType = new TrainingType("Cardio");
    }

    @Test
    void create_ShouldPersistTrainingType() {
        TrainingType created = trainingTypeDao.create(testTrainingType);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void create_ShouldThrowException_WhenTrainingTypeIsNull() {
        assertThatThrownBy(() -> trainingTypeDao.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TrainingType cannot be null");
    }

    @Test
    void findById_ShouldReturnTrainingType_WhenExists() {
        TrainingType created = trainingTypeDao.create(testTrainingType);
        Optional<TrainingType> found = trainingTypeDao.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<TrainingType> found = trainingTypeDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdIsNull() {
        Optional<TrainingType> found = trainingTypeDao.findById(null);
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_ShouldReturnTrainingType_WhenExists() {
        trainingTypeDao.create(testTrainingType);
        Optional<TrainingType> found = trainingTypeDao.findByName("Cardio");
        assertThat(found).isPresent();
        assertThat(found.get().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNotExists() {
        Optional<TrainingType> found = trainingTypeDao.findByName("NonExistent");
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameIsNull() {
        Optional<TrainingType> found = trainingTypeDao.findByName(null);
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameIsEmpty() {
        Optional<TrainingType> found = trainingTypeDao.findByName("");
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_ShouldTrimWhitespace() {
        trainingTypeDao.create(testTrainingType);
        Optional<TrainingType> found = trainingTypeDao.findByName("  Cardio  ");
        assertThat(found).isPresent();
        assertThat(found.get().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findAll_ShouldReturnAllTrainingTypes() {
        trainingTypeDao.create(testTrainingType);
        trainingTypeDao.create(new TrainingType("Strength"));
        trainingTypeDao.create(new TrainingType("Flexibility"));
        List<TrainingType> all = trainingTypeDao.findAll();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(TrainingType::getTrainingTypeName)
                .containsExactlyInAnyOrder("Cardio", "Strength", "Flexibility");
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoTrainingTypes() {
        List<TrainingType> all = trainingTypeDao.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void existsById_ShouldReturnTrue_WhenTrainingTypeExists() {
        TrainingType created = trainingTypeDao.create(testTrainingType);
        boolean exists = trainingTypeDao.existsById(created.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenTrainingTypeNotExists() {
        boolean exists = trainingTypeDao.existsById(999L);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenIdIsNull() {
        boolean exists = trainingTypeDao.existsById(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenNameExists() {
        trainingTypeDao.create(testTrainingType);
        boolean exists = trainingTypeDao.existsByName("Cardio");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNameNotExists() {
        boolean exists = trainingTypeDao.existsByName("NonExistent");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNameIsNull() {
        boolean exists = trainingTypeDao.existsByName(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNameIsEmpty() {
        boolean exists = trainingTypeDao.existsByName("");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_ShouldTrimWhitespace() {
        trainingTypeDao.create(testTrainingType);
        boolean exists = trainingTypeDao.existsByName("  Cardio  ");
        assertThat(exists).isTrue();
    }

    @Test
    void count_ShouldReturnCorrectNumber() {
        assertThat(trainingTypeDao.count()).isEqualTo(0);
        trainingTypeDao.create(testTrainingType);
        trainingTypeDao.create(new TrainingType("Strength"));
        trainingTypeDao.create(new TrainingType("Flexibility"));
        long count = trainingTypeDao.count();
        assertThat(count).isEqualTo(3);
    }

    @Test
    void count_ShouldReturnZero_WhenNoTrainingTypes() {
        long count = trainingTypeDao.count();
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldTrimTrainingTypeName() {
        TrainingType typeWithSpaces = new TrainingType("  Yoga  ");
        TrainingType created = trainingTypeDao.create(typeWithSpaces);
        assertThat(created.getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void shouldAllowMultipleTrainingTypesWithDifferentNames() {
        TrainingType type1 = trainingTypeDao.create(new TrainingType("Cardio"));
        TrainingType type2 = trainingTypeDao.create(new TrainingType("Strength"));
        TrainingType type3 = trainingTypeDao.create(new TrainingType("Flexibility"));

        assertThat(type1.getId()).isNotEqualTo(type2.getId());
        assertThat(type2.getId()).isNotEqualTo(type3.getId());
        assertThat(trainingTypeDao.findAll()).hasSize(3);
    }

    @Test
    void trainingType_ShouldImplementEqualsAndHashCode() {
        // Arrange
        TrainingType type1 = new TrainingType("Cardio");
        type1.setId(1L);

        TrainingType type2 = new TrainingType("Cardio");
        type2.setId(1L);

        TrainingType type3 = new TrainingType("Strength");
        type3.setId(2L);

        assertThat(type1).isEqualTo(type2);
        assertThat(type1.hashCode()).isEqualTo(type2.hashCode());
        assertThat(type1).isNotEqualTo(type3);
    }

    @Test
    void trainingType_ShouldHandleNullId() {
        TrainingType type1 = new TrainingType("Cardio");
        TrainingType type2 = new TrainingType("Cardio");

        assertThat(type1).isEqualTo(type2);
        assertThat(type1.hashCode()).isEqualTo(type2.hashCode());
    }
}