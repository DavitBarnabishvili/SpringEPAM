package com.gym.crm.util.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.exception.InactiveAccountException;
import com.gym.crm.exception.InvalidCredentialsException;
import com.gym.crm.exception.UnauthorizedAccessException;
import com.gym.crm.util.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final PasswordEncryption passwordEncryption;

    @Autowired
    public AuthenticationServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao,
                                     PasswordEncryption passwordEncryption) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.passwordEncryption = passwordEncryption;
    }


    @Override
    public Trainee authenticateTrainee(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Trainee authentication failed: username is null or empty");
            throw new InvalidCredentialsException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Trainee authentication failed: password is null or empty for username: {}", username);
            throw new InvalidCredentialsException("Password is required");
        }

        logger.debug("Authenticating trainee with username: {}", username);

        Optional<Trainee> traineeOpt = traineeDao.findByUsername(username.trim());

        if (traineeOpt.isEmpty()) {
            logger.warn("Trainee authentication failed: username not found: {}", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        Trainee trainee = traineeOpt.get();

        boolean passwordMatches;
        if (passwordEncryption.isEncoded(trainee.getPassword())) {
            passwordMatches = passwordEncryption.matches(password, trainee.getPassword());
        } else {
            // If we have an unencrypted password from the past, we should handle it
            passwordMatches = password.equals(trainee.getPassword());
            if (passwordMatches) {
                // Migrate to encrypted password on successful login
                logger.info("Migrating plain text password for trainee: {}", username);
                trainee.setPassword(passwordEncryption.encode(password));
                traineeDao.update(trainee);
            }
        }

        if (!passwordMatches) {
            logger.warn("Trainee authentication failed: invalid password for username: {}", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!trainee.isActive()) {
            logger.warn("Trainee authentication failed: account is inactive for username: {}", username);
            throw new InactiveAccountException("This account is inactive");
        }

        logger.info("Trainee authenticated successfully: {}", username);
        return trainee;
    }

    @Override
    public Trainer authenticateTrainer(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Trainer authentication failed: username is null or empty");
            throw new InvalidCredentialsException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Trainer authentication failed: password is null or empty for username: {}", username);
            throw new InvalidCredentialsException("Password is required");
        }

        logger.debug("Authenticating trainer with username: {}", username);

        Optional<Trainer> trainerOpt = trainerDao.findByUsername(username.trim());

        if (trainerOpt.isEmpty()) {
            logger.warn("Trainer authentication failed: username not found: {}", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        Trainer trainer = trainerOpt.get();

        boolean passwordMatches;
        if (passwordEncryption.isEncoded(trainer.getPassword())) {
            passwordMatches = passwordEncryption.matches(password, trainer.getPassword());
        } else {
            passwordMatches = password.equals(trainer.getPassword());
            if (passwordMatches) {
                logger.info("Migrating plain text password for trainer: {}", username);
                trainer.setPassword(passwordEncryption.encode(password));
                trainerDao.update(trainer);
            }
        }

        if (!passwordMatches) {
            logger.warn("Trainer authentication failed: invalid password for username: {}", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!trainer.isActive()) {
            logger.warn("Trainer authentication failed: account is inactive for username: {}", username);
            throw new InactiveAccountException("This account is inactive");
        }

        logger.info("Trainer authenticated successfully: {}", username);
        return trainer;
    }

    @Override
    public void validateTraineeAccess(String authenticatedUsername, Long targetTraineeId) {
        if (authenticatedUsername == null || targetTraineeId == null) {
            throw new UnauthorizedAccessException("Invalid access validation parameters");
        }

        Optional<Trainee> targetTraineeOpt = traineeDao.findById(targetTraineeId);
        if (targetTraineeOpt.isEmpty()) {
            throw new UnauthorizedAccessException("Target trainee not found");
        }

        Trainee targetTrainee = targetTraineeOpt.get();

        if (!authenticatedUsername.equals(targetTrainee.getUsername())) {
            logger.warn("Access denied: {} attempted to modify trainee: {}",
                    authenticatedUsername, targetTrainee.getUsername());
            throw new UnauthorizedAccessException("Users can only modify their own profile");
        }

        logger.debug("Access validated: {} can modify trainee profile", authenticatedUsername);
    }

    @Override
    public void validateTrainerAccess(String authenticatedUsername, Long targetTrainerId) {
        if (authenticatedUsername == null || targetTrainerId == null) {
            throw new UnauthorizedAccessException("Invalid access validation parameters");
        }

        Optional<Trainer> targetTrainerOpt = trainerDao.findById(targetTrainerId);
        if (targetTrainerOpt.isEmpty()) {
            throw new UnauthorizedAccessException("Target trainer not found");
        }

        Trainer targetTrainer = targetTrainerOpt.get();

        if (!authenticatedUsername.equals(targetTrainer.getUsername())) {
            logger.warn("Access denied: {} attempted to modify trainer: {}",
                    authenticatedUsername, targetTrainer.getUsername());
            throw new UnauthorizedAccessException("Users can only modify their own profile");
        }

        logger.debug("Access validated: {} can modify trainer profile", authenticatedUsername);
    }

    @Override
    public boolean isValidTraineeCredentials(String username, String password) {
        try {
            authenticateTrainee(username, password);
            return true;
        } catch (InvalidCredentialsException e) {
            return false;
        }
    }

    @Override
    public boolean isValidTrainerCredentials(String username, String password) {
        try {
            authenticateTrainer(username, password);
            return true;
        } catch (InvalidCredentialsException e) {
            return false;
        }
    }
}