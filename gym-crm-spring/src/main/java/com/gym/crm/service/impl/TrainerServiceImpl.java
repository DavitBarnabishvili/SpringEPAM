package com.gym.crm.service.impl;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dto.request.ChangeLoginRequest;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.exception.InvalidCredentialsException;
import com.gym.crm.service.TrainerService;
import com.gym.crm.util.AuthenticationService;
import com.gym.crm.util.CredentialsGeneratorService;
import com.gym.crm.util.PasswordEncryption;
import com.gym.crm.util.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final AuthenticationService authenticationService;
    private final CredentialsGeneratorService credentialsGenerator;
    private final ValidationService validationService;
    private final PasswordEncryption passwordEncryption;

    public TrainerServiceImpl(TrainerDao trainerDao,
                              AuthenticationService authenticationService,
                              CredentialsGeneratorService credentialsGenerator,
                              ValidationService validationService,
                              PasswordEncryption passwordEncryption) {
        this.trainerDao = trainerDao;
        this.authenticationService = authenticationService;
        this.credentialsGenerator = credentialsGenerator;
        this.validationService = validationService;
        this.passwordEncryption = passwordEncryption;
    }

    @Override
    public Trainer createTrainer(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        logger.info("Creating trainer: {} {}", trainer.getFirstName(), trainer.getLastName());

        validationService.validateTrainer(trainer);

        if (trainer.getUsername() != null || trainer.getPassword() != null) {
            logger.warn("Trainer creation attempted with pre-set credentials. Overriding with generated credentials.");
        }

        String username = credentialsGenerator.generateUsername(trainer.getFirstName(), trainer.getLastName());
        String rawPassword = credentialsGenerator.generatePassword();

        trainer.setUsername(username);
        String tempRawPassword = rawPassword;
        trainer.setPassword(passwordEncryption.encode(rawPassword));
        trainer.setIsActive(true);

        Trainer savedTrainer = trainerDao.create(trainer);

        logger.info("Successfully created trainer: {} with username: {} and id: {}",
                savedTrainer.getFullName(), savedTrainer.getUsername(), savedTrainer.getId());

        savedTrainer.setPassword(tempRawPassword);

        return savedTrainer;
    }

    @Override
    public Trainer updateTrainer(String username, String password, Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        if (trainer.getId() == null) {
            throw new IllegalArgumentException("Trainer id is required for update");
        }

        logger.info("Updating trainer with id: {}", trainer.getId());

        if (!"JWT_AUTH".equals(password)) {
            authenticationService.authenticateTrainer(username, password);
            authenticationService.validateTrainerAccess(username, trainer.getId());
        }

        validationService.validateTrainer(trainer);

        Optional<Trainer> existingTrainerOpt = trainerDao.findById(trainer.getId());
        if (existingTrainerOpt.isEmpty()) {
            throw new RuntimeException("Trainer not found with id: " + trainer.getId());
        }

        Trainer existingTrainer = existingTrainerOpt.get();

        trainer.setUsername(existingTrainer.getUsername());
        trainer.setPassword(existingTrainer.getPassword());
        trainer.setSpecialization(existingTrainer.getSpecialization());

        Trainer updatedTrainer = trainerDao.update(trainer);

        logger.info("Successfully updated trainer: {} with id: {}",
                updatedTrainer.getFullName(), updatedTrainer.getId());

        return updatedTrainer;
    }

    @Override
    public boolean activateTrainer(String username, String password, Long userId) {
        if (!"JWT_AUTH".equals(password)) {
            authenticationService.authenticateTrainer(username, password);
            authenticationService.validateTrainerAccess(username, userId);
        }

        return updateTrainerActiveStatus(userId, true);
    }

    @Override
    public boolean deactivateTrainer(String username, String password, Long userId) {
        if (!"JWT_AUTH".equals(password)) {
            authenticationService.authenticateTrainer(username, password);
            authenticationService.validateTrainerAccess(username, userId);
        }

        return updateTrainerActiveStatus(userId, false);
    }

    private boolean updateTrainerActiveStatus(Long userId, boolean isActive) {
        if (userId == null) {
            logger.debug("UpdateTrainerActiveStatus called with null userId");
            return false;
        }

        String action = isActive ? "Activating" : "Deactivating";
        logger.info("{} trainer with id: {}", action, userId);

        Optional<Trainer> trainerOpt = trainerDao.findById(userId);
        if (trainerOpt.isEmpty()) {
            logger.debug("No trainer found with id: {} for status update", userId);
            return false;
        }

        Trainer trainer = trainerOpt.get();

        // activate/deactivate is not idempotent
        // Always perform the action, even if already in target state
        trainer.setIsActive(isActive);

        try {
            trainerDao.update(trainer);
            action = isActive ? "Activated" : "Deactivated";
            logger.info("Successfully {} trainer: {} (was previously {})",
                    action.toLowerCase(), trainer.getFullName(), !isActive ? "active" : "inactive");
            return true;
        } catch (Exception e) {
            action = isActive ? "Activate" : "Deactivate";
            logger.error("Failed to {} trainer with id: {}", action.toLowerCase(), userId, e);
            return false;
        }
    }

    @Override
    public Optional<Trainer> findTrainerById(Long userId) {
        if (userId == null) {
            logger.debug("FindTrainerById called with null userId");
            return Optional.empty();
        }

        logger.debug("Finding trainer by id: {}", userId);

        Optional<Trainer> result = trainerDao.findById(userId);

        if (result.isPresent()) {
            logger.debug("Found trainer: {}", result.get().getFullName());
        } else {
            logger.debug("No trainer found with id: {}", userId);
        }

        return result;
    }

    @Override
    public Optional<Trainer> findTrainerByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("FindTrainerByUsername called with invalid username: {}", username);
            return Optional.empty();
        }

        logger.debug("Finding trainer by username: {}", username);

        Optional<Trainer> result = trainerDao.findByUsername(username.trim());

        if (result.isPresent()) {
            logger.debug("Found trainer with username: {}", username);
        } else {
            logger.debug("No trainer found with username: {}", username);
        }

        return result;
    }

    @Override
    public List<Trainer> findAllTrainers() {
        logger.debug("Finding all trainers");

        List<Trainer> trainers = trainerDao.findAll();

        logger.debug("Found {} trainers", trainers.size());

        return trainers;
    }

    @Override
    public List<Trainer> findTrainersBySpecialization(TrainingType specialization) {
        if (specialization == null) {
            logger.debug("FindTrainersBySpecialization called with null specialization");
            return List.of();
        }

        logger.debug("Finding trainers with specialization: {}", specialization.getTrainingTypeName());

        List<Trainer> trainers = trainerDao.findAll().stream()
                .filter(trainer -> trainer.getSpecialization() != null &&
                        trainer.getSpecialization().equals(specialization))
                .collect(Collectors.toList());

        logger.debug("Found {} trainers with specialization: {}",
                trainers.size(), specialization.getTrainingTypeName());

        return trainers;
    }

    @Override
    public boolean trainerExists(Long userId) {
        if (userId == null) {
            return false;
        }

        boolean exists = trainerDao.existsById(userId);
        logger.debug("Trainer exists check for id {}: {}", userId, exists);

        return exists;
    }

    @Override
    public void changePassword(Trainer trainer, ChangeLoginRequest request) {
        boolean oldPasswordValid = passwordEncryption.matches(request.getOldPassword(), trainer.getPassword());
        if (!oldPasswordValid) {
            throw new InvalidCredentialsException("Invalid old password");
        }

        String encodedPassword = passwordEncryption.encode(request.getNewPassword());
        trainer.setPassword(encodedPassword);

        trainerDao.update(trainer);
    }
}