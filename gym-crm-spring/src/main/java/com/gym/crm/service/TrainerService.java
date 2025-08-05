package com.gym.crm.service;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainerService {

    /**
     * Creates a new trainer with generated credentials.
     * Automatically generates username and password.
     *
     * @param trainer Trainer with firstName, lastName, and optional specialization
     * @return Created trainer with generated username, password, and assigned userId
     * @throws IllegalArgumentException if validation fails
     */
    Trainer createTrainer(Trainer trainer);

    /**
     * Updates an existing trainer's information.
     * Cannot update username or password.
     *
     * @param trainer Trainer with updated information (must have valid userId)
     * @return Updated trainer
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if trainer not found
     */
    Trainer updateTrainer(String username, String password, Trainer trainer);

    boolean activateTrainer(String username, String password, Long userId);

    boolean deactivateTrainer(String username, String password, Long userId);

    /**
     * Finds a trainer by their userId.
     *
     * @param userId Trainer's userId
     * @return Optional containing trainer if found, empty otherwise
     */
    Optional<Trainer> findTrainerById(Long userId);

    /**
     * Finds a trainer by their username.
     *
     * @param username Trainer's username
     * @return Optional containing trainer if found, empty otherwise
     */
    Optional<Trainer> findTrainerByUsername(String username);

    /**
     * Retrieves all trainers in the system.
     *
     * @return List of all trainers
     */
    List<Trainer> findAllTrainers();

    /**
     * Retrieves all trainers with a specific specialization.
     *
     * @param specialization Training type specialization
     * @return List of trainers with said specialization
     */
    List<Trainer> findTrainersBySpecialization(TrainingType specialization);

    /**
     * Checks if a trainer exists with the given userId.
     *
     * @param userId Trainer's userId
     * @return true if trainer exists, false otherwise
     */
    boolean trainerExists(Long userId);
}