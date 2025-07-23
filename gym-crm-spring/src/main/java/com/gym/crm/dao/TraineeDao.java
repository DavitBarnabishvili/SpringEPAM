package com.gym.crm.dao;

import com.gym.crm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {

    /**
     * Creates a new trainee in storage.
     * Assigns a unique userId if not already set.
     *
     * @param trainee Trainee to create
     * @return Created trainee with assigned userId
     * @throws IllegalArgumentException if trainee is null
     */
    Trainee create(Trainee trainee);

    /**
     * Updates an existing trainee in storage.
     *
     * @param trainee Trainee to update (must have valid userId)
     * @return Updated trainee
     * @throws IllegalArgumentException if trainee is null or userId is null
     * @throws RuntimeException if trainee not found
     */
    Trainee update(Trainee trainee);

    /**
     * Deletes a trainee from storage.
     *
     * @param userId Trainee's userId
     * @return true if trainee was deleted, false if not found
     */
    boolean delete(Long userId);

    /**
     * Finds a trainee by their userId.
     *
     * @param userId Trainee's userId
     * @return Optional containing the trainee if found, empty otherwise
     */
    Optional<Trainee> findById(Long userId);

    /**
     * Finds all trainees in storage.
     *
     * @return List of all trainees
     */
    List<Trainee> findAll();

    /**
     * Finds a trainee by username.
     * Useful for checking username uniqueness and authentication.
     *
     * @param username Trainee's username
     * @return Optional containing the trainee if found, empty otherwise
     */
    Optional<Trainee> findByUsername(String username);

    /**
     * Finds all active trainees.
     *
     * @return List of active trainees
     */
    List<Trainee> findAllActive();

    /**
     * Checks if a trainee exists with the given userId.
     *
     * @param userId Trainee's userId
     * @return true if trainee exists, false otherwise
     */
    boolean existsById(Long userId);

    /**
     * Checks if a trainee exists with the given username.
     *
     * @param username Trainee's username
     * @return true if trainee exists, false otherwise
     */
    boolean existsByUsername(String username);
}
