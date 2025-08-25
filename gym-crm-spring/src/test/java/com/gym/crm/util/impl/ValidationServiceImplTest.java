package com.gym.crm.util.impl;

import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.util.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationServiceImplTest {

    private ValidationService validationService;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private Training testTraining;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        validationService = new ValidationServiceImpl();

        testTrainee = new Trainee("John", "Doe", LocalDate.now().minusYears(25), "123 Test St");
        testTrainee.setId(1L);

        testTrainingType = new TrainingType("Cardio");
        testTrainingType.setId(1L);

        testTrainer = new Trainer("Jane", "Smith", testTrainingType);
        testTrainer.setId(2L);

        testTraining = new Training(1L, 2L, "Test Training", testTrainingType, LocalDate.now(), 60);
    }

    @Test
    void validateTrainer_ShouldPass_WhenTrainerValid() {
        validationService.validateTrainer(testTrainer);
    }

    @Test
    void validateTrainer_ShouldThrowException_WhenTrainerNull() {
        assertThatThrownBy(() -> validationService.validateTrainer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer cannot be null");
    }

    @Test
    void validateTrainer_ShouldThrowException_WhenFirstNameNull() {
        testTrainer.setFirstName(null);

        assertThatThrownBy(() -> validationService.validateTrainer(testTrainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name is required");
    }

    @Test
    void validateTrainer_ShouldThrowException_WhenLastNameNull() {
        testTrainer.setLastName(null);

        assertThatThrownBy(() -> validationService.validateTrainer(testTrainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name is required");
    }

    @Test
    void validateTrainer_ShouldThrowException_WhenSpecializationInvalid() {
        TrainingType invalidType = new TrainingType();
        invalidType.setTrainingTypeName(null);
        testTrainer.setSpecialization(invalidType);

        assertThatThrownBy(() -> validationService.validateTrainer(testTrainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Specialization must have a valid training type name");
    }

    @Test
    void validateTrainee_ShouldPass_WhenTraineeValid() {
        validationService.validateTrainee(testTrainee);
    }

    @Test
    void validateTrainee_ShouldThrowException_WhenTraineeNull() {
        assertThatThrownBy(() -> validationService.validateTrainee(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee cannot be null");
    }

    @Test
    void validateTrainee_ShouldThrowException_WhenFirstNameNull() {
        testTrainee.setFirstName(null);

        assertThatThrownBy(() -> validationService.validateTrainee(testTrainee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name is required");
    }

    @Test
    void validateTrainee_ShouldThrowException_WhenLastNameNull() {
        testTrainee.setLastName(null);

        assertThatThrownBy(() -> validationService.validateTrainee(testTrainee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name is required");
    }

    @Test
    void validateTrainee_ShouldThrowException_WhenAddressEmpty() {
        testTrainee.setAddress("  ");

        assertThatThrownBy(() -> validationService.validateTrainee(testTrainee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Address (if provided) cannot be empty");
    }

    @Test
    void validateTrainee_ShouldPass_WhenDateOfBirthValid() {
        testTrainee.setDateOfBirth(LocalDate.now().minusYears(20));

        validationService.validateTrainee(testTrainee);
    }

    @Test
    void validateTrainee_ShouldThrowException_WhenTooYoung() {
        testTrainee.setDateOfBirth(LocalDate.now().minusYears(15));

        assertThatThrownBy(() -> validationService.validateTrainee(testTrainee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee must be at least 16 years old");
    }

    @Test
    void validateTrainee_ShouldThrowException_WhenTooOld() {
        testTrainee.setDateOfBirth(LocalDate.now().minusYears(117));

        assertThatThrownBy(() -> validationService.validateTrainee(testTrainee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Age cannot exceed 116 years");
    }

    @Test
    void validateTraining_ShouldPass_WhenTrainingValid() {
        validationService.validateTraining(testTraining);
    }

    @Test
    void validateTraining_ShouldThrowException_WhenTrainingNull() {
        assertThatThrownBy(() -> validationService.validateTraining(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training cannot be null");
    }

    @Test
    void validateTraining_ShouldThrowException_WhenTraineeIdNull() {
        testTraining.setTraineeId(null);

        assertThatThrownBy(() -> validationService.validateTraining(testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee ID is required");
    }

    @Test
    void validateTraining_ShouldThrowException_WhenTrainerIdNull() {
        testTraining.setTrainerId(null);

        assertThatThrownBy(() -> validationService.validateTraining(testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer ID is required");
    }

    @Test
    void validateTraining_ShouldThrowException_WhenTrainingNameNull() {
        testTraining.setTrainingName(null);

        assertThatThrownBy(() -> validationService.validateTraining(testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training name is required");
    }

    @Test
    void validateTraining_ShouldThrowException_WhenTrainingNameEmpty() {
        testTraining.setTrainingName("  ");

        assertThatThrownBy(() -> validationService.validateTraining(testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training name is required");
    }

    @Test
    void validateTraining_ShouldThrowException_WhenTrainingTypeNull() {
        testTraining.setTrainingType(null);

        assertThatThrownBy(() -> validationService.validateTraining(testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training type is required");
    }

    @Test
    void validateTraining_ShouldThrowException_WhenTrainingDateNull() {
        testTraining.setTrainingDate(null);

        assertThatThrownBy(() -> validationService.validateTraining(testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training date is required");
    }

    @Test
    void validateName_ShouldReturnTrue_WhenNameValid() {
        boolean valid = validationService.validateName("John", "First name");

        assertThat(valid).isTrue();
    }

    @Test
    void validateName_ShouldReturnTrue_WhenNameHasValidSpecialChars() {
        assertThat(validationService.validateName("Mary-Jane", "First name")).isTrue();
        assertThat(validationService.validateName("O'Brien", "Last name")).isTrue();
        assertThat(validationService.validateName("John Smith", "Full name")).isTrue();
    }

    @Test
    void validateName_ShouldThrowException_WhenNameNull() {
        assertThatThrownBy(() -> validationService.validateName(null, "First name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name is required");
    }

    @Test
    void validateName_ShouldThrowException_WhenNameEmpty() {
        assertThatThrownBy(() -> validationService.validateName("", "First name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name is required");
    }

    @Test
    void validateName_ShouldThrowException_WhenNameTooLong() {
        String longName = "A".repeat(51);

        assertThatThrownBy(() -> validationService.validateName(longName, "First name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name cannot be longer than 50 characters");
    }

    @Test
    void validateName_ShouldThrowException_WhenNameHasInvalidChars() {
        assertThatThrownBy(() -> validationService.validateName("John123", "First name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name can only contain letters, spaces, hyphens, and apostrophes");
    }

    @Test
    void validateDateOfBirth_ShouldReturnTrue_WhenDateValid() {
        boolean valid = validationService.validateDateOfBirth(LocalDate.now().minusYears(25));

        assertThat(valid).isTrue();
    }

    @Test
    void validateDateOfBirth_ShouldThrowException_WhenDateNull() {
        assertThatThrownBy(() -> validationService.validateDateOfBirth(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Date of birth cannot be null");
    }

    @Test
    void validateDateOfBirth_ShouldThrowException_WhenDateInFuture() {
        assertThatThrownBy(() -> validationService.validateDateOfBirth(LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Date of birth cannot be in the future");
    }

    @Test
    void validateDateOfBirth_ShouldThrowException_WhenAgeLessThan16() {
        assertThatThrownBy(() -> validationService.validateDateOfBirth(LocalDate.now().minusYears(15)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee must be at least 16 years old");
    }

    @Test
    void validateDateOfBirth_ShouldThrowException_WhenAgeMoreThan116() {
        assertThatThrownBy(() -> validationService.validateDateOfBirth(LocalDate.now().minusYears(117)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Age cannot exceed 116 years");
    }

    @Test
    void validateTrainingDuration_ShouldReturnTrue_WhenDurationValid() {
        assertThat(validationService.validateTrainingDuration(30)).isTrue();
        assertThat(validationService.validateTrainingDuration(60)).isTrue();
        assertThat(validationService.validateTrainingDuration(480)).isTrue();
    }

    @Test
    void validateTrainingDuration_ShouldThrowException_WhenDurationNull() {
        assertThatThrownBy(() -> validationService.validateTrainingDuration(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training duration cannot be null");
    }

    @Test
    void validateTrainingDuration_ShouldThrowException_WhenDurationTooShort() {
        assertThatThrownBy(() -> validationService.validateTrainingDuration(14))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training duration must be at least 15 minutes");
    }

    @Test
    void validateTrainingDuration_ShouldThrowException_WhenDurationTooLong() {
        assertThatThrownBy(() -> validationService.validateTrainingDuration(481))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training duration cannot exceed 480 minutes");
    }

    @Test
    void validateTraining_ShouldValidateDuration_WhenDurationNotNull() {
        testTraining.setTrainingDuration(10);

        assertThatThrownBy(() -> validationService.validateTraining(testTraining))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training duration must be at least 15 minutes");
    }

    @Test
    void validateDateOfBirth_ShouldHandleLeapYear() {
        LocalDate leapYearBirthdate = LocalDate.of(2000, 2, 29);
        testTrainee.setDateOfBirth(leapYearBirthdate);

        validationService.validateTrainee(testTrainee);
    }

    @Test
    void validateName_ShouldAcceptMaxLength() {
        String maxLengthName = "A".repeat(50);

        boolean valid = validationService.validateName(maxLengthName, "Name");

        assertThat(valid).isTrue();
    }
}