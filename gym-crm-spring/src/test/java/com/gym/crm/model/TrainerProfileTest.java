package com.gym.crm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TrainerProfile Model Tests")
class TrainerProfileTest {

    private Trainer trainer;
    private List<Training> trainings;
    private TrainerProfile profile;

    @BeforeEach
    void setUp() {
        TrainingType specialization = new TrainingType("Cardio");
        trainer = new Trainer("Jane", "Smith", specialization);
        trainer.setUserId(2L);
        trainer.setUsername("jane.smith");
        trainer.setPassword("password456");
        trainer.setIsActive(true);

        Training training1 = new Training(1L, 1L, 2L, "Morning Session",
                specialization, LocalDate.now().minusDays(2), 90);
        Training training2 = new Training(2L, 3L, 2L, "Evening Session",
                specialization, LocalDate.now().plusDays(1), 60);
        Training training3 = new Training(3L, 4L, 2L, "Afternoon Session",
                specialization, LocalDate.now(), 45);

        trainings = Arrays.asList(training1, training2, training3);
        profile = new TrainerProfile(trainer, trainings);
    }

    @Test
    @DisplayName("Constructor should require non-null trainer")
    void constructor_WithNullTrainer_ShouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                new TrainerProfile(null, trainings));
    }

    @Test
    @DisplayName("Constructor should handle null trainings list")
    void constructor_WithNullTrainings_ShouldCreateEmptyList() {
        TrainerProfile profileWithNullTrainings = new TrainerProfile(trainer, null);

        assertEquals(0, profileWithNullTrainings.getTotalTrainings());
        assertNotNull(profileWithNullTrainings.getTrainings());
        assertTrue(profileWithNullTrainings.getTrainings().isEmpty());
    }

    @Test
    @DisplayName("Constructor should create immutable copy of trainings")
    void constructor_ShouldCreateImmutableCopy() {
        List<Training> originalTrainings = profile.getTrainings();

        assertThrows(UnsupportedOperationException.class, () ->
                originalTrainings.add(new Training()));
    }

    @Test
    @DisplayName("getTrainer should return the trainer")
    void getTrainer_ShouldReturnTrainer() {
        assertEquals(trainer, profile.getTrainer());
    }

    @Test
    @DisplayName("getTrainings should return all trainings")
    void getTrainings_ShouldReturnAllTrainings() {
        assertEquals(3, profile.getTrainings().size());
        assertEquals(trainings, profile.getTrainings());
    }

    @Test
    @DisplayName("getTotalTrainings should return correct count")
    void getTotalTrainings_ShouldReturnCorrectCount() {
        assertEquals(3, profile.getTotalTrainings());
    }

    @Test
    @DisplayName("getTotalTrainingDuration should sum all durations")
    void getTotalTrainingDuration_ShouldSumAllDurations() {
        long expectedDuration = 90 + 60 + 45; // 195 minutes
        assertEquals(expectedDuration, profile.getTotalTrainingDuration());
    }

    @Test
    @DisplayName("getTotalTrainingDuration should handle null durations")
    void getTotalTrainingDuration_WithNullDurations_ShouldIgnoreNulls() {
        Training trainingWithNullDuration = new Training(4L, 1L, 2L, "No Duration",
                new TrainingType("Strength"), LocalDate.now(), null);

        List<Training> trainingsWithNull = Arrays.asList(
                trainings.get(0), // 90 minutes
                trainingWithNullDuration, // null duration - should be ignored
                trainings.get(2)  // 45 minutes
        );

        TrainerProfile profileWithNulls = new TrainerProfile(trainer, trainingsWithNull);
        assertEquals(135, profileWithNulls.getTotalTrainingDuration());
    }

    @Test
    @DisplayName("isActive should return trainer active status")
    void isActive_ShouldReturnTrainerActiveStatus() {
        assertTrue(profile.isActive());

        trainer.setIsActive(false);
        TrainerProfile inactiveProfile = new TrainerProfile(trainer, trainings);
        assertFalse(inactiveProfile.isActive());
    }

    @Test
    @DisplayName("getSpecializationName should return trainer specialization")
    void getSpecializationName_ShouldReturnTrainerSpecialization() {
        assertEquals("Cardio", profile.getSpecializationName());
    }

    @Test
    @DisplayName("getSpecializationName should handle null specialization")
    void getSpecializationName_WithNullSpecialization_ShouldReturnDefault() {
        trainer.setSpecialization(null);
        TrainerProfile profileWithoutSpec = new TrainerProfile(trainer, trainings);
        assertEquals("No Specialization", profileWithoutSpec.getSpecializationName());
    }

    @Test
    @DisplayName("equals should work correctly")
    void equals_ShouldWorkCorrectly() {
        TrainerProfile sameProfile = new TrainerProfile(trainer, trainings);
        TrainerProfile differentProfile = new TrainerProfile(
                new Trainer("John", "Doe"), trainings);

        assertEquals(profile, sameProfile);
        assertNotEquals(profile, differentProfile);
        assertNotEquals(null, profile);
    }

    @Test
    @DisplayName("hashCode should be consistent")
    void hashCode_ShouldBeConsistent() {
        TrainerProfile sameProfile = new TrainerProfile(trainer, trainings);
        assertEquals(profile.hashCode(), sameProfile.hashCode());
    }

    @Test
    @DisplayName("toString should include relevant information")
    void toString_ShouldIncludeRelevantInfo() {
        String result = profile.toString();

        assertAll(
                () -> assertTrue(result.contains("Jane Smith")),
                () -> assertTrue(result.contains("totalTrainings=3")),
                () -> assertTrue(result.contains("195 minutes")),
                () -> assertTrue(result.contains("Cardio"))
        );
    }
}
