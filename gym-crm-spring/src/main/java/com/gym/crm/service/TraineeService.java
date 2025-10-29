package com.gym.crm.service;

import com.gym.crm.dto.request.ChangeLoginRequest;
import com.gym.crm.entity.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {

    /**
     * Creates a new trainee with generated credentials.
     * Automatically generates username and password.
     *
     * @param trainee Trainee with firstName, lastName, and optional personal details
     * @return Created trainee with generated username, password, and assigned userId
     * @throws IllegalArgumentException if validation fails
     */
    Trainee createTrainee(Trainee trainee);

    /**
     * Updates an existing trainee's information.
     * Cannot update username or password.
     *
     * @param trainee Trainee with updated information (must have valid userId)
     * @return Updated trainee
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if trainee not found
     */
    Trainee updateTrainee(String username, String password, Trainee trainee);

    /**
     * Deletes a trainee from the system.
     *
     * @param userId Trainee's userId
     * @return true if trainee was deleted, false if not found
     */
    boolean deleteTrainee(String username, String password, Long userId);

    /**
     * Finds a trainee by their userId.
     *
     * @param userId Trainee's userId
     * @return Optional containing trainee if found, empty otherwise
     */
    Optional<Trainee> findTraineeById(Long userId);

    /**
     * Finds a trainee by their username.
     *
     * @param username Trainee's username
     * @return Optional containing trainee if found, empty otherwise
     */
    Optional<Trainee> findTraineeByUsername(String username);

    /**
     * Retrieves all trainees in the system.
     *
     * @return List of all trainees
     */
    List<Trainee> findAllTrainees();

    /**
     * Retrieves all active trainees.
     *
     * @return List of active trainees
     */
    List<Trainee> findAllActiveTrainees();

    /**
     * Retrieves trainees within a specific age range.
     *
     * @param minAge Minimum age (inclusive)
     * @param maxAge Maximum age (inclusive)
     * @return List of trainees within the age range
     */
    List<Trainee> findTraineesByAgeRange(int minAge, int maxAge);

    /**
     * Activates a trainee (sets isActive to true).
     *
     * @param userId Trainee's userId
     * @return true if trainee was activated, false if not found
     */
    boolean activateTrainee(String username, String password, Long userId);

    /**
     * Deactivates a trainee (sets isActive to false).
     *
     * @param userId Trainee's userId
     * @return true if trainee was deactivated, false if not found
     */
    boolean deactivateTrainee(String username, String password, Long userId);

    /**
     * Checks if a trainee exists with the given userId.
     *
     * @param userId Trainee's userId
     * @return true if trainee exists, false otherwise
     */
    boolean traineeExists(Long userId);

    void changePassword(Trainee trainee, ChangeLoginRequest request);
}
