package com.gym.crm.service;

import com.gym.crm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {

    /**
     * Creates a new training session.
     * Makes sure both trainee and trainer exist.
     *
     * @param training Training session details
     * @return Created training with assigned id
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if trainee or trainer not found
     */
    Training createTraining(Training training);

    /**
     * Finds a training session by id.
     *
     * @param id Training's id
     * @return Optional containing training if found, empty otherwise
     */
    Optional<Training> findTrainingById(Long id);

    /**
     * Retrieves all training sessions in the system.
     *
     * @return List of all training sessions
     */
    List<Training> findAllTrainings();

    /**
     * Retrieves all training sessions for a specific trainee.
     *
     * @param traineeId Trainee's userId
     * @return List of training sessions for the trainee
     */
    List<Training> findTrainingsByTraineeId(Long traineeId);

    /**
     * Retrieves all training sessions for a specific trainer.
     *
     * @param trainerId Trainer's userId
     * @return List of training sessions for the trainer
     */
    List<Training> findTrainingsByTrainerId(Long trainerId);

    /**
     * Retrieves all training sessions on a specific date.
     *
     * @param date Training date
     * @return List of training sessions on the specified date
     */
    List<Training> findTrainingsByDate(LocalDate date);

    /**
     * Retrieves training sessions within a date range.
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of training sessions in the date range
     */
    List<Training> findTrainingsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves training sessions for a trainee within a date range.
     *
     * @param traineeId Trainee's userId
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of training sessions for the trainee in the date range
     */
    List<Training> findTraineeTrainingsByDateRange(Long traineeId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves training sessions for a trainer within a date range.
     *
     * @param trainerId Trainer's userId
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of training sessions for the trainer in the date range
     */
    List<Training> findTrainerTrainingsByDateRange(Long trainerId, LocalDate startDate, LocalDate endDate);

    /**
     * Checks if a training session exists with the given id.
     *
     * @param id Training's id
     * @return true if training exists, false otherwise
     */
    boolean trainingExists(Long id);
}