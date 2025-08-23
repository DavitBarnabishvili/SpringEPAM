package com.gym.crm.util;

import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;

import java.time.LocalDate;

/**
 * Service interface for validating domain objects and business rules.
 */
public interface ValidationService {

    /**
     * Validates a trainer object according to business rules.
     *
     * @param trainer Trainer to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateTrainer(Trainer trainer);

    /**
     * Validates a trainee object according to business rules.
     *
     * @param trainee Trainee to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateTrainee(Trainee trainee);

    /**
     * Validates a training object according to business rules.
     *
     * @param training Training to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateTraining(Training training);

    /**
     * Validates a name (first or last).
     *
     * @param name Name to validate
     * @param fieldName Field name for error messages
     * @return true if valid
     * @throws IllegalArgumentException if invalid
     */
    boolean validateName(String name, String fieldName);

    /**
     * Validates a date of birth.
     *
     * @param dateOfBirth Date to validate
     * @return true if valid
     * @throws IllegalArgumentException if invalid
     */
    boolean validateDateOfBirth(LocalDate dateOfBirth);

    /**
     * Validates training duration.
     *
     * @param duration Duration in minutes
     * @return true if valid
     * @throws IllegalArgumentException if invalid
     */
    boolean validateTrainingDuration(Integer duration);
}
