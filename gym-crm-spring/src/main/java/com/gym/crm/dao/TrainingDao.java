package com.gym.crm.dao;

import com.gym.crm.entity.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingDao {

    /**
     * Creates a new training session in storage.
     * Assigns a unique id if not already set.
     *
     * @param training Training to create
     * @return Created training with assigned id
     * @throws IllegalArgumentException if training is null
     */
    Training create(Training training);

    /**
     * Finds a training by its id.
     *
     * @param id Training's id
     * @return Optional containing the training if found, empty otherwise
     */
    Optional<Training> findById(Long id);

    /**
     * Finds all training sessions in storage.
     *
     * @return List of all training sessions
     */
    List<Training> findAll();

    /**
     * Finds all training sessions for a specific trainee.
     *
     * @param traineeId Trainee's userId
     * @return List of training sessions for the trainee
     */
    List<Training> findByTraineeId(Long traineeId);

    /**
     * Finds all training sessions for a specific trainer.
     *
     * @param trainerId Trainer's userId
     * @return List of training sessions for the trainer
     */
    List<Training> findByTrainerId(Long trainerId);

    /**
     * Finds all training sessions on a specific date.
     *
     * @param date Training date
     * @return List of training sessions on the specified date
     */
    List<Training> findByDate(LocalDate date);

    /**
     * Finds all training sessions between two dates (inclusive).
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of training sessions in the date range
     */
    List<Training> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Finds all training sessions for a trainee within a date range.
     *
     * @param traineeId Trainee's userId
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of training sessions for the trainee in the date range
     */
    List<Training> findByTraineeIdAndDateRange(Long traineeId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds all training sessions for a trainer within a date range.
     *
     * @param trainerId Trainer's userId
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of training sessions for the trainer in the date range
     */
    List<Training> findByTrainerIdAndDateRange(Long trainerId, LocalDate startDate, LocalDate endDate);

    /**
     * Checks if a training exists with the given id.
     *
     * @param id Training's id
     * @return true if training exists, false otherwise
     */
    boolean existsById(Long id);

    int deleteByTraineeId(Long traineeId);

    void delete(Training training);
}