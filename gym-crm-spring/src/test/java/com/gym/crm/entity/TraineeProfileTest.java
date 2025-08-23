package com.gym.crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraineeProfile Model Tests")
class TraineeProfileTest {

    private Trainee trainee;
    private List<Training> trainings;
    private TraineeProfile profile;

    @BeforeEach
    void setUp() {
        trainee = new Trainee("John", "Doe", LocalDate.of(1990, 5, 15), "123 Main St");
        trainee.setUserId(1L);
        trainee.setUsername("john.doe");
        trainee.setPassword("password123");
        trainee.setIsActive(true);

        TrainingType cardio = new TrainingType("Cardio");
        TrainingType strength = new TrainingType("Strength");

        Training pastTraining = new Training(1L, 1L, 2L, "Past Workout",
                cardio, LocalDate.now().minusDays(5), 60);
        Training futureTraining = new Training(2L, 1L, 3L, "Future Workout",
                strength, LocalDate.now().plusDays(3), 45);
        Training todayTraining = new Training(3L, 1L, 4L, "Today Workout",
                cardio, LocalDate.now(), 30);

        trainings = Arrays.asList(pastTraining, futureTraining, todayTraining);
        profile = new TraineeProfile(trainee, trainings);
    }

    @Test
    @DisplayName("Constructor should require non-null trainee")
    void constructor_WithNullTrainee_ShouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                new TraineeProfile(null, trainings));
    }

    @Test
    @DisplayName("Constructor should handle null trainings list")
    void constructor_WithNullTrainings_ShouldCreateEmptyList() {
        TraineeProfile profileWithNullTrainings = new TraineeProfile(trainee, null);

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
    @DisplayName("getTrainee should return the trainee")
    void getTrainee_ShouldReturnTrainee() {
        assertEquals(trainee, profile.getTrainee());
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
        long expectedDuration = 60 + 45 + 30; // 135 minutes
        assertEquals(expectedDuration, profile.getTotalTrainingDuration());
    }

    @Test
    @DisplayName("getTotalTrainingDuration should handle null durations")
    void getTotalTrainingDuration_WithNullDurations_ShouldIgnoreNulls() {
        Training trainingWithNullDuration = new Training(4L, 1L, 2L, "No Duration",
                new TrainingType("Yoga"), LocalDate.now(), null);

        List<Training> trainingsWithNull = Arrays.asList(
                trainings.get(0), // 60 minutes
                trainingWithNullDuration, // null duration - should be ignored
                trainings.get(2)  // 30 minutes
        );

        TraineeProfile profileWithNulls = new TraineeProfile(trainee, trainingsWithNull);
        assertEquals(90, profileWithNulls.getTotalTrainingDuration());
    }

    @Test
    @DisplayName("isActive should return trainee active status")
    void isActive_ShouldReturnTraineeActiveStatus() {
        assertTrue(profile.isActive());

        trainee.setIsActive(false);
        TraineeProfile inactiveProfile = new TraineeProfile(trainee, trainings);
        assertFalse(inactiveProfile.isActive());
    }

    @Test
    @DisplayName("getAge should return trainee age")
    void getAge_ShouldReturnTraineeAge() {
        assertEquals(trainee.getAge(), profile.getAge());
    }

    @Test
    @DisplayName("getDateOfBirth should return trainee date of birth")
    void getDateOfBirth_ShouldReturnTraineeDateOfBirth() {
        assertEquals(trainee.getDateOfBirth(), profile.getDateOfBirth());
    }

    @Test
    @DisplayName("getAddress should return trainee address")
    void getAddress_ShouldReturnTraineeAddress() {
        assertEquals(trainee.getAddress(), profile.getAddress());
    }

    @Test
    @DisplayName("getUpcomingTrainings should return future trainings")
    void getUpcomingTrainings_ShouldReturnFutureTrainings() {
        List<Training> upcoming = profile.getUpcomingTrainings();

        assertEquals(1, upcoming.size());
        assertEquals("Future Workout", upcoming.getFirst().getTrainingName());
    }

    @Test
    @DisplayName("getUpcomingTrainings should handle null dates")
    void getUpcomingTrainings_WithNullDates_ShouldIgnoreNulls() {
        Training trainingWithNullDate = new Training(5L, 1L, 2L, "Null Date",
                new TrainingType("Yoga"), null, 60);

        List<Training> trainingsWithNull = Arrays.asList(
                trainings.get(1), // future training
                trainingWithNullDate // null date - should be ignored
        );

        TraineeProfile profileWithNulls = new TraineeProfile(trainee, trainingsWithNull);
        assertEquals(1, profileWithNulls.getUpcomingTrainings().size());
    }

    @Test
    @DisplayName("getPastTrainings should return past trainings")
    void getPastTrainings_ShouldReturnPastTrainings() {
        List<Training> past = profile.getPastTrainings();

        assertEquals(1, past.size());
        assertEquals("Past Workout", past.getFirst().getTrainingName());
    }

    @Test
    @DisplayName("getPastTrainings should handle null dates")
    void getPastTrainings_WithNullDates_ShouldIgnoreNulls() {
        Training trainingWithNullDate = new Training(5L, 1L, 2L, "Null Date",
                new TrainingType("Yoga"), null, 60);

        List<Training> trainingsWithNull = Arrays.asList(
                trainings.getFirst(), // past training
                trainingWithNullDate // null date - should be ignored
        );

        TraineeProfile profileWithNulls = new TraineeProfile(trainee, trainingsWithNull);
        assertEquals(1, profileWithNulls.getPastTrainings().size());
    }

    @Test
    @DisplayName("equals should work correctly")
    void equals_ShouldWorkCorrectly() {
        TraineeProfile sameProfile = new TraineeProfile(trainee, trainings);
        TraineeProfile differentProfile = new TraineeProfile(
                new Trainee("Jane", "Smith"), trainings);

        assertEquals(profile, sameProfile);
        assertNotEquals(profile, differentProfile);
        assertNotEquals(null, profile);
    }

    @Test
    @DisplayName("hashCode should be consistent")
    void hashCode_ShouldBeConsistent() {
        TraineeProfile sameProfile = new TraineeProfile(trainee, trainings);
        assertEquals(profile.hashCode(), sameProfile.hashCode());
    }

    @Test
    @DisplayName("toString should include relevant information")
    void toString_ShouldIncludeRelevantInfo() {
        String result = profile.toString();

        assertAll(
                () -> assertTrue(result.contains("John Doe")),
                () -> assertTrue(result.contains("totalTrainings=3")),
                () -> assertTrue(result.contains("upcomingTrainings=1")),
                () -> assertTrue(result.contains("135 minutes"))
        );
    }
}