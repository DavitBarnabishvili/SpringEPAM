package com.gym.crm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Training Model Tests")
class TrainingTest {

    private Training training;
    private LocalDate trainingDate;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        trainingDate = LocalDate.now().plusDays(1);
        trainingType = new TrainingType("Strength");
        training = new Training(1L, 2L, "Morning Workout", trainingType, trainingDate, 60);
    }

    @Test
    @DisplayName("Default constructor should create empty training")
    void defaultConstructor_ShouldCreateEmptyTraining() {
        Training newTraining = new Training();
        assertAll(
                () -> assertNull(newTraining.getId()),
                () -> assertNull(newTraining.getTraineeId()),
                () -> assertNull(newTraining.getTrainerId()),
                () -> assertNull(newTraining.getTrainingName()),
                () -> assertNull(newTraining.getTrainingType()),
                () -> assertNull(newTraining.getTrainingDate()),
                () -> assertNull(newTraining.getTrainingDuration())
        );
    }

    @Test
    @DisplayName("Constructor without ID should set all properties except ID")
    void constructorWithoutId_ShouldSetAllProperties() {
        Training newTraining = new Training(1L, 2L, "Evening Run", trainingType, trainingDate, 45);

        assertAll(
                () -> assertNull(newTraining.getId()),
                () -> assertEquals(1L, newTraining.getTraineeId()),
                () -> assertEquals(2L, newTraining.getTrainerId()),
                () -> assertEquals("Evening Run", newTraining.getTrainingName()),
                () -> assertEquals(trainingType, newTraining.getTrainingType()),
                () -> assertEquals(trainingDate, newTraining.getTrainingDate()),
                () -> assertEquals(45, newTraining.getTrainingDuration())
        );
    }

    @Test
    @DisplayName("Full constructor should set all properties including ID")
    void fullConstructor_ShouldSetAllProperties() {
        Training fullTraining = new Training(5L, 1L, 2L, "Cardio Session", trainingType, trainingDate, 90);

        assertAll(
                () -> assertEquals(5L, fullTraining.getId()),
                () -> assertEquals(1L, fullTraining.getTraineeId()),
                () -> assertEquals(2L, fullTraining.getTrainerId()),
                () -> assertEquals("Cardio Session", fullTraining.getTrainingName()),
                () -> assertEquals(trainingType, fullTraining.getTrainingType()),
                () -> assertEquals(trainingDate, fullTraining.getTrainingDate()),
                () -> assertEquals(90, fullTraining.getTrainingDuration())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"  Morning Workout  ", "\tEvening Session\n"})
    @DisplayName("Training name should be trimmed when set")
    void setTrainingName_ShouldTrimWhitespace(String name) {
        training.setTrainingName(name);
        assertEquals(name.trim(), training.getTrainingName());
    }

    @Test
    @DisplayName("Training name should be null when set to null")
    void setTrainingName_WhenNull_ShouldBeNull() {
        training.setTrainingName(null);
        assertNull(training.getTrainingName());
    }

    @Test
    @DisplayName("getTrainingTypeName should return type name when type exists")
    void getTrainingTypeName_WhenTypeExists_ShouldReturnName() {
        assertEquals("Strength", training.getTrainingTypeName());
    }

    @Test
    @DisplayName("getTrainingTypeName should return Unknown when type is null")
    void getTrainingTypeName_WhenTypeIsNull_ShouldReturnUnknown() {
        training.setTrainingType(null);
        assertEquals("Unknown", training.getTrainingTypeName());
    }

    @Test
    @DisplayName("getFormattedDate should format date correctly")
    void getFormattedDate_ShouldFormatCorrectly() {
        LocalDate testDate = LocalDate.of(2024, 12, 25);
        training.setTrainingDate(testDate);
        assertEquals("2024-12-25", training.getFormattedDate());
    }

    @Test
    @DisplayName("getFormattedDate should return No Date when date is null")
    void getFormattedDate_WhenDateIsNull_ShouldReturnNoDate() {
        training.setTrainingDate(null);
        assertEquals("No Date", training.getFormattedDate());
    }

    @ParameterizedTest
    @MethodSource("durationTestCases")
    @DisplayName("getFormattedDuration should format duration correctly")
    void getFormattedDuration_ShouldFormatCorrectly(Integer duration, String expected) {
        training.setTrainingDuration(duration);
        assertEquals(expected, training.getFormattedDuration());
    }

    static Stream<Arguments> durationTestCases() {
        return Stream.of(
                Arguments.of(null, "No Duration"),
                Arguments.of(0, "No Duration"),
                Arguments.of(-5, "No Duration"),
                Arguments.of(30, "30m"),
                Arguments.of(60, "1h 0m"),
                Arguments.of(90, "1h 30m"),
                Arguments.of(150, "2h 30m")
        );
    }

    @Test
    @DisplayName("isInPast should return true for past dates")
    void isInPast_WithPastDate_ShouldReturnTrue() {
        training.setTrainingDate(LocalDate.now().minusDays(1));
        assertTrue(training.isInPast());
    }

    @Test
    @DisplayName("isInPast should return false for future dates")
    void isInPast_WithFutureDate_ShouldReturnFalse() {
        training.setTrainingDate(LocalDate.now().plusDays(1));
        assertFalse(training.isInPast());
    }

    @Test
    @DisplayName("isInFuture should return true for future dates")
    void isInFuture_WithFutureDate_ShouldReturnTrue() {
        training.setTrainingDate(LocalDate.now().plusDays(1));
        assertTrue(training.isInFuture());
    }

    @Test
    @DisplayName("isInFuture should return false for past dates")
    void isInFuture_WithPastDate_ShouldReturnFalse() {
        training.setTrainingDate(LocalDate.now().minusDays(1));
        assertFalse(training.isInFuture());
    }

    @Test
    @DisplayName("isToday should return true for today's date")
    void isToday_WithTodaysDate_ShouldReturnTrue() {
        training.setTrainingDate(LocalDate.now());
        assertTrue(training.isToday());
    }

    @Test
    @DisplayName("isToday should return false for other dates")
    void isToday_WithOtherDate_ShouldReturnFalse() {
        training.setTrainingDate(LocalDate.now().plusDays(1));
        assertFalse(training.isToday());
    }

    @Test
    @DisplayName("Equals should work correctly with IDs")
    void equals_WithIds_ShouldWorkCorrectly() {
        Training training1 = new Training();
        Training training2 = new Training();

        training1.setId(1L);
        training2.setId(1L);

        assertEquals(training1, training2);
    }

    @Test
    @DisplayName("Equals should work with business keys when no ID")
    void equals_WithoutId_ShouldUseBusinessKeys() {
        Training training1 = new Training(1L, 2L, "Test", trainingType, trainingDate, 60);
        Training training2 = new Training(1L, 2L, "Test", trainingType, trainingDate, 60);

        assertEquals(training1, training2);
    }

    @Test
    @DisplayName("HashCode should use business keys")
    void hashCode_ShouldUseBusinessKeys() {
        int expectedHash = training.hashCode();

        Training sameTraining = new Training(1L, 2L, "Morning Workout", trainingType, trainingDate, 60);
        assertEquals(expectedHash, sameTraining.hashCode());
    }

    @Test
    @DisplayName("ToString should include all relevant information")
    void toString_ShouldIncludeAllRelevantInfo() {
        String result = training.toString();

        assertAll(
                () -> assertTrue(result.contains("Morning Workout")),
                () -> assertTrue(result.contains("1")),
                () -> assertTrue(result.contains("2")),
                () -> assertTrue(result.contains("Strength")),
                () -> assertTrue(result.contains("1h 0m"))
        );
    }
}