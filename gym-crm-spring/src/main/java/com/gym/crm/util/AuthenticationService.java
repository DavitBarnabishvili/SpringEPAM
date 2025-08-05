package com.gym.crm.util;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;

/**
 * Service interface for user authentication and authorization.
 */
public interface AuthenticationService {

    /**
     * Authenticates a trainee with username and password.
     *
     * @param username Trainee's username
     * @param password Trainee's password
     * @return Authenticated trainee
     * @throws SecurityException if authentication fails
     */
    Trainee authenticateTrainee(String username, String password);

    /**
     * Authenticates a trainer with username and password.
     *
     * @param username Trainer's username
     * @param password Trainer's password
     * @return Authenticated trainer
     * @throws SecurityException if authentication fails
     */
    Trainer authenticateTrainer(String username, String password);

    /**
     * Validates that the authenticated user can modify the target trainee profile.
     *
     * @param authenticatedUsername Username of authenticated user
     * @param targetTraineeId ID of trainee being modified
     * @throws SecurityException if user cannot modify the target profile
     */
    void validateTraineeAccess(String authenticatedUsername, Long targetTraineeId);

    /**
     * Validates that the authenticated user can modify the target trainer profile.
     *
     * @param authenticatedUsername Username of authenticated user
     * @param targetTrainerId ID of trainer being modified
     * @throws SecurityException if user cannot modify the target profile
     */
    void validateTrainerAccess(String authenticatedUsername, Long targetTrainerId);

    /**
     * Simple boolean check for authentication without throwing exceptions.
     *
     * @param username Username to check
     * @param password Password to check
     * @return true if trainee credentials are valid and account is active
     */
    boolean isValidTraineeCredentials(String username, String password);

    /**
     * Simple boolean check for authentication without throwing exceptions.
     *
     * @param username Username to check
     * @param password Password to check
     * @return true if trainer credentials are valid and account is active
     */
    boolean isValidTrainerCredentials(String username, String password);
}