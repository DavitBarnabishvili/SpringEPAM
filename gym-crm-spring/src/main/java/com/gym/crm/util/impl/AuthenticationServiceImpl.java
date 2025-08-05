package com.gym.crm.util.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.util.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public AuthenticationServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Override
    public Trainee authenticateTrainee(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Trainee authentication failed: username is null or empty");
            throw new SecurityException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Trainee authentication failed: password is null or empty for username: {}", username);
            throw new SecurityException("Password is required");
        }

        logger.debug("Authenticating trainee with username: {}", username);

        Optional<Trainee> traineeOpt = traineeDao.findByUsername(username.trim());

        if (traineeOpt.isEmpty()) {
            logger.warn("Trainee authentication failed: username not found: {}", username);
            throw new SecurityException("Invalid username or password");
        }

        Trainee trainee = traineeOpt.get();

        if (!password.equals(trainee.getPassword())) {
            logger.warn("Trainee authentication failed: invalid password for username: {}", username);
            throw new SecurityException("Invalid username or password");
        }

        if (!trainee.isActive()) {
            logger.warn("Trainee authentication failed: account is inactive for username: {}", username);
            throw new SecurityException("Account is inactive");
        }

        logger.info("Trainee authenticated successfully: {}", username);
        return trainee;
    }

    @Override
    public Trainer authenticateTrainer(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Trainer authentication failed: username is null or empty");
            throw new SecurityException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Trainer authentication failed: password is null or empty for username: {}", username);
            throw new SecurityException("Password is required");
        }

        logger.debug("Authenticating trainer with username: {}", username);

        Optional<Trainer> trainerOpt = trainerDao.findByUsername(username.trim());

        if (trainerOpt.isEmpty()) {
            logger.warn("Trainer authentication failed: username not found: {}", username);
            throw new SecurityException("Invalid username or password");
        }

        Trainer trainer = trainerOpt.get();

        if (!password.equals(trainer.getPassword())) {
            logger.warn("Trainer authentication failed: invalid password for username: {}", username);
            throw new SecurityException("Invalid username or password");
        }

        if (!trainer.isActive()) {
            logger.warn("Trainer authentication failed: account is inactive for username: {}", username);
            throw new SecurityException("Account is inactive");
        }

        logger.info("Trainer authenticated successfully: {}", username);
        return trainer;
    }

    @Override
    public void validateTraineeAccess(String authenticatedUsername, Long targetTraineeId) {
        if (authenticatedUsername == null || targetTraineeId == null) {
            throw new SecurityException("Invalid access validation parameters");
        }

        Optional<Trainee> targetTraineeOpt = traineeDao.findById(targetTraineeId);
        if (targetTraineeOpt.isEmpty()) {
            throw new SecurityException("Target trainee not found");
        }

        Trainee targetTrainee = targetTraineeOpt.get();

        if (!authenticatedUsername.equals(targetTrainee.getUsername())) {
            logger.warn("Access denied: {} attempted to modify trainee: {}",
                    authenticatedUsername, targetTrainee.getUsername());
            throw new SecurityException("Users can only modify their own profile");
        }

        logger.debug("Access validated: {} can modify trainee profile", authenticatedUsername);
    }

    @Override
    public void validateTrainerAccess(String authenticatedUsername, Long targetTrainerId) {
        if (authenticatedUsername == null || targetTrainerId == null) {
            throw new SecurityException("Invalid access validation parameters");
        }

        Optional<Trainer> targetTrainerOpt = trainerDao.findById(targetTrainerId);
        if (targetTrainerOpt.isEmpty()) {
            throw new SecurityException("Target trainer not found");
        }

        Trainer targetTrainer = targetTrainerOpt.get();

        if (!authenticatedUsername.equals(targetTrainer.getUsername())) {
            logger.warn("Access denied: {} attempted to modify trainer: {}",
                    authenticatedUsername, targetTrainer.getUsername());
            throw new SecurityException("Users can only modify their own profile");
        }

        logger.debug("Access validated: {} can modify trainer profile", authenticatedUsername);
    }

    @Override
    public boolean isValidTraineeCredentials(String username, String password) {
        try {
            authenticateTrainee(username, password);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    @Override
    public boolean isValidTrainerCredentials(String username, String password) {
        try {
            authenticateTrainer(username, password);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
}