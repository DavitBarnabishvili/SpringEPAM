package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private final InMemoryStorage storage;


    public TrainerDaoImpl(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public Trainer create(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        logger.debug("Creating trainer: {}", trainer.getFullName());

        Long id = trainer.getUserId();
        String fullName = trainer.getFullName();

        if (id == null) {
            id = storage.generateTrainerId();
            trainer.setUserId(id);
            logger.debug("Assigned userId {} to trainer {}", id, fullName);
        }

        storage.storeTrainer(id, trainer);

        logger.info("Successfully created trainer: {} with userId: {}",
                fullName, id);

        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        Long id = trainer.getUserId();

        if (trainer.getUserId() == null) {
            throw new IllegalArgumentException("Trainer userId cannot be null for update");
        }

        logger.debug("Updating trainer with userId: {}", trainer.getUserId());

        if (!storage.getTrainers().containsKey(id)) {
            throw new RuntimeException("Trainer not found with userId: " + id);
        }

        storage.storeTrainer(id, trainer);

        logger.info("Successfully updated trainer: {} with userId: {}",
                trainer.getFullName(), id);

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long userId) {
        if (userId == null) {
            logger.debug("FindById called with null userId");
            return Optional.empty();
        }

        logger.debug("Finding trainer by userId: {}", userId);

        Trainer trainer = storage.getTrainer(userId);

        if (trainer != null) {
            logger.debug("Found trainer: {}", trainer.getFullName());
            return Optional.of(trainer);
        } else {
            logger.debug("No trainer found with userId: {}", userId);
            return Optional.empty();
        }
    }

    @Override
    public List<Trainer> findAll() {
        logger.debug("Finding all trainers");

        List<Trainer> trainers = storage.getAllTrainers();

        logger.debug("Found {} trainers", trainers.size());

        return trainers;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("FindByUsername called with invalid username: {}", username);
            return Optional.empty();
        }

        String cleanUsername = username.trim();
        logger.debug("Finding trainer by username: {}", cleanUsername);

        Optional<Trainer> result = storage.getAllTrainers().stream()
                .filter(trainer -> cleanUsername.equals(trainer.getUsername()))
                .findFirst();

        if (result.isPresent()) {
            logger.debug("Found trainer with username: {}", cleanUsername);
        } else {
            logger.debug("No trainer found with username: {}", cleanUsername);
        }

        return result;
    }

    @Override
    public boolean existsById(Long userId) {
        if (userId == null) {
            return false;
        }

        boolean exists = storage.getTrainers().containsKey(userId);
        logger.debug("Trainer exists check for userId {}: {}", userId, exists);

        return exists;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();
        boolean exists = storage.getAllTrainers().stream()
                .anyMatch(trainer -> cleanUsername.equals(trainer.getUsername()));

        logger.debug("Trainer exists check for username '{}': {}", cleanUsername, exists);

        return exists;
    }
}