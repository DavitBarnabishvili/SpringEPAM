package com.gym.crm.dao;

import com.gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {

    /**
     * Creates a new trainer in storage.
     * Assigns a unique userId if not already set.
     *
     * @param trainer Trainer to create
     * @return Created trainer with assigned userId
     * @throws IllegalArgumentException if trainer is null
     */
    Trainer create(Trainer trainer);

    /**
     * Updates an existing trainer in storage.
     *
     * @param trainer Trainer to update (must have valid userId)
     * @return Updated trainer
     * @throws IllegalArgumentException if trainer is null or userId is null
     * @throws RuntimeException if trainer not found
     */
    Trainer update(Trainer trainer);

    /**
     * Finds a trainer by their userId.
     *
     * @param userId Trainer's userId
     * @return Optional containing the trainer if found, empty otherwise
     */
    Optional<Trainer> findById(Long userId);

    /**
     * Finds all trainers in storage.
     *
     * @return List of all trainers
     */
    List<Trainer> findAll();

    /**
     * Finds a trainer by username.
     * Useful for checking username uniqueness and authentication.
     *
     * @param username Trainer's username
     * @return Optional containing the trainer if found, empty otherwise
     */
    Optional<Trainer> findByUsername(String username);

    /**
     * Checks if a trainer exists with the given userId.
     *
     * @param userId Trainer's userId
     * @return true if trainer exists, false otherwise
     */
    boolean existsById(Long userId);

    /**
     * Checks if a trainer exists with the given username.
     *
     * @param username Trainer's username
     * @return true if trainer exists, false otherwise
     */
    boolean existsByUsername(String username);
}
