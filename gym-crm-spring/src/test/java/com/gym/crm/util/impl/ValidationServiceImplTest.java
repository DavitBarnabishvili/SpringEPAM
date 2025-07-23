package com.gym.crm.util.impl;

import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationServiceImpl Tests")
class ValidationServiceImplTest {

    private ValidationServiceImpl validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationServiceImpl();
    }

    @Test
    @DisplayName("validateTrainer should throw exception for null trainer")
    void validateTrainer_WithNullTrainer_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainer(null));
    }

    @Test
    @DisplayName("validateTrainer should validate first name")
    void validateTrainer_WithInvalidFirstName_ShouldThrowException() {
        com.gym.crm.model.Trainer trainer = new com.gym.crm.model.Trainer(null, "Doe");

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainer(trainer));
    }

    @Test
    @DisplayName("validateTrainer should validate last name")
    void validateTrainer_WithInvalidLastName_ShouldThrowException() {
        com.gym.crm.model.Trainer trainer = new com.gym.crm.model.Trainer("John", "");

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainer(trainer));
    }

    @Test
    @DisplayName("validateTrainer should validate specialization")
    void validateTrainer_WithInvalidSpecialization_ShouldThrowException() {
        com.gym.crm.model.TrainingType invalidType = new com.gym.crm.model.TrainingType("");
        com.gym.crm.model.Trainer trainer = new com.gym.crm.model.Trainer("John", "Doe", invalidType);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainer(trainer));
    }

    @Test
    @DisplayName("validateTrainer should pass for valid trainer")
    void validateTrainer_WithValidTrainer_ShouldPass() {
        com.gym.crm.model.TrainingType validType = new com.gym.crm.model.TrainingType("Cardio");
        com.gym.crm.model.Trainer trainer = new com.gym.crm.model.Trainer("John", "Doe", validType);

        assertDoesNotThrow(() -> validationService.validateTrainer(trainer));
    }

    @Test
    @DisplayName("validateTrainer should allow null specialization")
    void validateTrainer_WithNullSpecialization_ShouldPass() {
        com.gym.crm.model.Trainer trainer = new com.gym.crm.model.Trainer("John", "Doe");

        assertDoesNotThrow(() -> validationService.validateTrainer(trainer));
    }

    @Test
    @DisplayName("validateTrainee should throw exception for null trainee")
    void validateTrainee_WithNullTrainee_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainee(null));
    }

    @Test
    @DisplayName("validateTrainee should validate first name")
    void validateTrainee_WithInvalidFirstName_ShouldThrowException() {
        Trainee trainee = new Trainee(null, "Doe");

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainee(trainee));
    }

    @Test
    @DisplayName("validateTrainee should validate last name")
    void validateTrainee_WithInvalidLastName_ShouldThrowException() {
        Trainee trainee = new Trainee("John", "");

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainee(trainee));
    }

    @Test
    @DisplayName("validateTrainee should validate date of birth")
    void validateTrainee_WithInvalidDateOfBirth_ShouldThrowException() {
        Trainee trainee = new Trainee("John", "Doe",
                java.time.LocalDate.now().plusDays(1), "123 Main St");

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainee(trainee));
    }

    @Test
    @DisplayName("validateTrainee should reject empty address")
    void validateTrainee_WithEmptyAddress_ShouldThrowException() {
        Trainee trainee = new Trainee("John", "Doe",
                java.time.LocalDate.of(1990, 1, 1), "   ");

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainee(trainee));
    }

    @Test
    @DisplayName("validateTrainee should pass for valid trainee")
    void validateTrainee_WithValidTrainee_ShouldPass() {
        Trainee trainee = new Trainee("John", "Doe",
                java.time.LocalDate.of(1990, 1, 1), "123 Main St");

        assertDoesNotThrow(() -> validationService.validateTrainee(trainee));
    }

    @Test
    @DisplayName("validateTrainee should allow null date of birth and address")
    void validateTrainee_WithNullOptionalFields_ShouldPass() {
        Trainee trainee = new Trainee("John", "Doe", null, null);

        assertDoesNotThrow(() -> validationService.validateTrainee(trainee));
    }

    @Test
    @DisplayName("validateTraining should throw exception for null training")
    void validateTraining_WithNullTraining_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTraining(null));
    }

    @Test
    @DisplayName("validateTraining should require trainee ID")
    void validateTraining_WithNullTraineeId_ShouldThrowException() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                null, 2L, "Test", new com.gym.crm.model.TrainingType("Cardio"),
                java.time.LocalDate.now(), 60);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTraining(training));
    }

    @Test
    @DisplayName("validateTraining should require trainer ID")
    void validateTraining_WithNullTrainerId_ShouldThrowException() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                1L, null, "Test", new com.gym.crm.model.TrainingType("Cardio"),
                java.time.LocalDate.now(), 60);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTraining(training));
    }

    @Test
    @DisplayName("validateTraining should require training name")
    void validateTraining_WithNullTrainingName_ShouldThrowException() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                1L, 2L, null, new com.gym.crm.model.TrainingType("Cardio"),
                java.time.LocalDate.now(), 60);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTraining(training));
    }

    @Test
    @DisplayName("validateTraining should require training type")
    void validateTraining_WithNullTrainingType_ShouldThrowException() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                1L, 2L, "Test", null, java.time.LocalDate.now(), 60);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTraining(training));
    }

    @Test
    @DisplayName("validateTraining should require training date")
    void validateTraining_WithNullTrainingDate_ShouldThrowException() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                1L, 2L, "Test", new com.gym.crm.model.TrainingType("Cardio"), null, 60);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTraining(training));
    }

    @Test
    @DisplayName("validateTraining should validate duration when provided")
    void validateTraining_WithInvalidDuration_ShouldThrowException() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                1L, 2L, "Test", new com.gym.crm.model.TrainingType("Cardio"),
                java.time.LocalDate.now(), 5);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTraining(training));
    }

    @Test
    @DisplayName("validateTraining should pass for valid training")
    void validateTraining_WithValidTraining_ShouldPass() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                1L, 2L, "Test", new com.gym.crm.model.TrainingType("Cardio"),
                java.time.LocalDate.now(), 60);

        assertDoesNotThrow(() -> validationService.validateTraining(training));
    }

    @Test
    @DisplayName("validateTraining should allow null duration")
    void validateTraining_WithNullDuration_ShouldPass() {
        com.gym.crm.model.Training training = new com.gym.crm.model.Training(
                1L, 2L, "Test", new com.gym.crm.model.TrainingType("Cardio"),
                java.time.LocalDate.now(), null);

        assertDoesNotThrow(() -> validationService.validateTraining(training));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    @DisplayName("validateName should throw exception for invalid names")
    void validateName_WithInvalidName_ShouldThrowException(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateName(name, "Test Field"));
    }

    @Test
    @DisplayName("validateName should throw exception for too long name")
    void validateName_WithTooLongName_ShouldThrowException() {
        String longName = "a".repeat(51); // 51 characters

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateName(longName, "Test Field"));
    }

    @Test
    @DisplayName("validateName should throw exception for invalid characters")
    void validateName_WithInvalidCharacters_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateName("John123", "Test Field"));
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateName("John@Doe", "Test Field"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"John", "Mary-Jane", "O'Connor", "Jean Marie", "Anne-Marie O'Sullivan"})
    @DisplayName("validateName should pass for valid names")
    void validateName_WithValidNames_ShouldPass(String name) {
        assertDoesNotThrow(() -> validationService.validateName(name, "Test Field"));
    }

    @Test
    @DisplayName("validateDateOfBirth should throw exception for null date")
    void validateDateOfBirth_WithNullDate_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateDateOfBirth(null));
    }

    @Test
    @DisplayName("validateDateOfBirth should throw exception for future date")
    void validateDateOfBirth_WithFutureDate_ShouldThrowException() {
        java.time.LocalDate futureDate = java.time.LocalDate.now().plusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateDateOfBirth(futureDate));
    }

    @Test
    @DisplayName("validateDateOfBirth should throw exception for too young")
    void validateDateOfBirth_WithTooYoung_ShouldThrowException() {
        java.time.LocalDate tooYoung = java.time.LocalDate.now().minusYears(15);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateDateOfBirth(tooYoung));
    }

    @Test
    @DisplayName("validateDateOfBirth should throw exception for too old")
    void validateDateOfBirth_WithTooOld_ShouldThrowException() {
        java.time.LocalDate tooOld = java.time.LocalDate.now().minusYears(117);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateDateOfBirth(tooOld));
    }

    @Test
    @DisplayName("validateDateOfBirth should pass for valid age")
    void validateDateOfBirth_WithValidAge_ShouldPass() {
        java.time.LocalDate validDate = java.time.LocalDate.now().minusYears(25);

        assertDoesNotThrow(() -> validationService.validateDateOfBirth(validDate));
    }

    @Test
    @DisplayName("validateDateOfBirth should pass for minimum age")
    void validateDateOfBirth_WithMinimumAge_ShouldPass() {
        java.time.LocalDate minAge = java.time.LocalDate.now().minusYears(16);

        assertDoesNotThrow(() -> validationService.validateDateOfBirth(minAge));
    }

    @Test
    @DisplayName("validateDateOfBirth should pass for maximum age")
    void validateDateOfBirth_WithMaximumAge_ShouldPass() {
        java.time.LocalDate maxAge = java.time.LocalDate.now().minusYears(116);

        assertDoesNotThrow(() -> validationService.validateDateOfBirth(maxAge));
    }

    @Test
    @DisplayName("validateTrainingDuration should throw exception for null duration")
    void validateTrainingDuration_WithNullDuration_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainingDuration(null));
    }

    @ParameterizedTest
    @MethodSource("invalidDurations")
    @DisplayName("validateTrainingDuration should throw exception for invalid durations")
    void validateTrainingDuration_WithInvalidDuration_ShouldThrowException(Integer duration) {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateTrainingDuration(duration));
    }

    static Stream<Arguments> invalidDurations() {
        return Stream.of(
                Arguments.of(14),  // Too short
                Arguments.of(0),   // Zero
                Arguments.of(-5),  // Negative
                Arguments.of(481)  // Too long
        );
    }

    @ParameterizedTest
    @MethodSource("validDurations")
    @DisplayName("validateTrainingDuration should pass for valid durations")
    void validateTrainingDuration_WithValidDuration_ShouldPass(Integer duration) {
        assertDoesNotThrow(() -> validationService.validateTrainingDuration(duration));
    }

    static Stream<Arguments> validDurations() {
        return Stream.of(
                Arguments.of(15),   // Minimum
                Arguments.of(30),   // Short session
                Arguments.of(60),   // Standard session
                Arguments.of(120),  // Long session
                Arguments.of(480)   // Maximum
        );
    }
}