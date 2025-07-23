package com.gym.crm.service.impl;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainerService;
import com.gym.crm.util.CredentialsGeneratorService;
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
    private final CredentialsGeneratorService credentialsGenerator;
    private final ValidationService validationService;


    public TrainerServiceImpl(TrainerDao trainerDao,
                              CredentialsGeneratorService credentialsGenerator,
                              ValidationService validationService) {
        this.trainerDao = trainerDao;
        this.credentialsGenerator = credentialsGenerator;
        this.validationService = validationService;
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
        String password = credentialsGenerator.generatePassword();

        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setIsActive(true);

        Trainer savedTrainer = trainerDao.create(trainer);

        logger.info("Successfully created trainer: {} with username: {} and userId: {}",
                savedTrainer.getFullName(), savedTrainer.getUsername(), savedTrainer.getUserId());

        return savedTrainer;
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        if (trainer.getUserId() == null) {
            throw new IllegalArgumentException("Trainer userId is required for update");
        }

        logger.info("Updating trainer with userId: {}", trainer.getUserId());

        validationService.validateTrainer(trainer);

        Optional<Trainer> existingTrainerOpt = trainerDao.findById(trainer.getUserId());
        if (existingTrainerOpt.isEmpty()) {
            throw new RuntimeException("Trainer not found with userId: " + trainer.getUserId());
        }

        Trainer existingTrainer = existingTrainerOpt.get();

        trainer.setUsername(existingTrainer.getUsername());
        trainer.setPassword(existingTrainer.getPassword());

        Trainer updatedTrainer = trainerDao.update(trainer);

        logger.info("Successfully updated trainer: {} with userId: {}",
                updatedTrainer.getFullName(), updatedTrainer.getUserId());

        return updatedTrainer;
    }

    @Override
    public Optional<Trainer> findTrainerById(Long userId) {
        if (userId == null) {
            logger.debug("FindTrainerById called with null userId");
            return Optional.empty();
        }

        logger.debug("Finding trainer by userId: {}", userId);

        Optional<Trainer> result = trainerDao.findById(userId);

        if (result.isPresent()) {
            logger.debug("Found trainer: {}", result.get().getFullName());
        } else {
            logger.debug("No trainer found with userId: {}", userId);
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
        logger.debug("Trainer exists check for userId {}: {}", userId, exists);

        return exists;
    }
}