package com.gym.crm.util.impl;

import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.util.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;

@Service
public class ValidationServiceImpl implements ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);

    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_TRAINING_DURATION = 15; //minutes
    private static final int MAX_TRAINING_DURATION = 480; //minutes, 8 hours
    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 116; // oldest living person according to google is 115, so...

    @Override
    public void validateTrainer(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        logger.debug("Validating trainer: {}", trainer.getFullName());

        validateName(trainer.getFirstName(), "First name");
        validateName(trainer.getLastName(), "Last name");

        if (trainer.getSpecialization() != null &&
                (trainer.getSpecialization().getTrainingTypeName() == null ||
                        trainer.getSpecialization().getTrainingTypeName().trim().isEmpty())) {
            throw new IllegalArgumentException("Specialization must have a valid training type name");
        }

        logger.debug("Trainer validation passed for: {}", trainer.getFullName());
    }

    @Override
    public void validateTrainee(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        logger.debug("Validating trainee: {}", trainee.getFullName());

        validateName(trainee.getFirstName(), "First name");
        validateName(trainee.getLastName(), "Last name");

        if (trainee.getDateOfBirth() != null) {
            validateDateOfBirth(trainee.getDateOfBirth());
        }

        if (trainee.getAddress() != null && trainee.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address (if provided) cannot be empty");
        }

        logger.debug("Trainee validation passed for: {}", trainee.getFullName());
    }

    @Override
    public void validateTraining(Training training) {
        if (training == null) {
            throw new IllegalArgumentException("Training cannot be null");
        }

        logger.debug("Validating training: {}", training.getTrainingName());

        if (training.getTraineeId() == null) {
            throw new IllegalArgumentException("Trainee ID is required");
        }

        if (training.getTrainerId() == null) {
            throw new IllegalArgumentException("Trainer ID is required");
        }

        if (training.getTrainingName() == null || training.getTrainingName().trim().isEmpty()) {
            throw new IllegalArgumentException("Training name is required");
        }

        if (training.getTrainingType() == null) {
            throw new IllegalArgumentException("Training type is required");
        }

        if (training.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date is required");
        }

        if (training.getTrainingDuration() != null) {
            validateTrainingDuration(training.getTrainingDuration());
        }

        logger.debug("Training validation passed for: {}", training.getTrainingName());
    }

    @Override
    public boolean validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String cleanName = name.trim();

        //Right now this is redundant, it's always false, but if MIN_NAME_LENGTH increases
        //in the future - it won't be redundant.
        if (cleanName.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException(fieldName + " must be at least " + MIN_NAME_LENGTH + " character long");
        }

        if (cleanName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(fieldName + " cannot be longer than " + MAX_NAME_LENGTH + " characters");
        }

        if (!cleanName.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException(fieldName + " can only contain letters, spaces, hyphens, and apostrophes");
        }

        return true;
    }

    @Override
    public boolean validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }

        LocalDate today = LocalDate.now();

        if (dateOfBirth.isAfter(today)) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        int age = today.getYear() - dateOfBirth.getYear();
        MonthDay birthMonthDay = MonthDay.from(dateOfBirth);
        MonthDay todayMonthDay = MonthDay.from(today);
        if (todayMonthDay.isBefore(birthMonthDay)) {
            age--;
        }

        if (age < MIN_AGE) {
            throw new IllegalArgumentException("Trainee must be at least " + MIN_AGE + " years old");
        }

        if (age > MAX_AGE) {
            throw new IllegalArgumentException("Age cannot exceed " + MAX_AGE + " years"); //vampires and ghosts aren't allowed
        }

        return true;
    }

    @Override
    public boolean validateTrainingDuration(Integer duration) {
        if (duration == null) {
            throw new IllegalArgumentException("Training duration cannot be null");
        }

        if (duration < MIN_TRAINING_DURATION) {
            throw new IllegalArgumentException("Training duration must be at least " + MIN_TRAINING_DURATION + " minutes");
        }

        if (duration > MAX_TRAINING_DURATION) {
            throw new IllegalArgumentException("Training duration cannot exceed " + MAX_TRAINING_DURATION + " minutes");
        }

        return true;
    }
}