package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private final InMemoryStorage storage;

    public TraineeDaoImpl(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public Trainee create(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        logger.debug("Creating trainee: {}", trainee.getFullName());

        Long id = trainee.getUserId();
        String fullName = trainee.getFullName();

        if (id == null) {
            id = storage.generateTraineeId();
            trainee.setUserId(id);
            logger.debug("Assigned userId {} to trainee {}", id, fullName);
        }

        storage.storeTrainee(id, trainee);

        logger.info("Successfully created trainee: {} with userId: {}",
                fullName, id);

        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        Long id = trainee.getUserId();

        if (id == null) {
            throw new IllegalArgumentException("Trainee userId cannot be null for update");
        }

        logger.debug("Updating trainee with userId: {}", id);

        if (!storage.getTrainees().containsKey(id)) {
            throw new RuntimeException("Trainee not found with userId: " + id);
        }

        storage.storeTrainee(id, trainee);

        logger.info("Successfully updated trainee: {} with userId: {}",
                trainee.getFullName(), id);

        return trainee;
    }

    @Override
    public boolean delete(Long userId) {
        if (userId == null) {
            logger.debug("Delete called with null userId");
            return false;
        }

        logger.debug("Deleting trainee with userId: {}", userId);

        Trainee existingTrainee = storage.getTrainee(userId);
        if (existingTrainee == null) {
            logger.debug("No trainee found with userId: {} for deletion", userId);
            return false;
        }

        storage.removeTrainee(userId);

        logger.info("Successfully deleted trainee: {} with userId: {}",
                existingTrainee.getFullName(), userId);

        return true;
    }

    @Override
    public Optional<Trainee> findById(Long userId) {
        if (userId == null) {
            logger.debug("FindById called with null userId");
            return Optional.empty();
        }

        logger.debug("Finding trainee by userId: {}", userId);

        Trainee trainee = storage.getTrainee(userId);

        if (trainee != null) {
            logger.debug("Found trainee: {}", trainee.getFullName());
            return Optional.of(trainee);
        } else {
            logger.debug("No trainee found with userId: {}", userId);
            return Optional.empty();
        }
    }

    @Override
    public List<Trainee> findAll() {
        logger.debug("Finding all trainees");

        List<Trainee> trainees = storage.getAllTrainees();

        logger.debug("Found {} trainees", trainees.size());

        return trainees;
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("FindByUsername called with invalid username: {}", username);
            return Optional.empty();
        }

        String cleanUsername = username.trim();
        logger.debug("Finding trainee by username: {}", cleanUsername);

        Optional<Trainee> result = storage.getAllTrainees().stream()
                .filter(trainee -> cleanUsername.equals(trainee.getUsername()))
                .findFirst();

        if (result.isPresent()) {
            logger.debug("Found trainee with username: {}", cleanUsername);
        } else {
            logger.debug("No trainee found with username: {}", cleanUsername);
        }

        return result;
    }

    @Override
    public List<Trainee> findAllActive() {
        logger.debug("Finding all active trainees");

        List<Trainee> activeTrainees = storage.getAllTrainees().stream()
                .filter(Trainee::isActive)
                .collect(Collectors.toList());

        logger.debug("Found {} active trainees out of {} total",
                activeTrainees.size(), storage.getAllTrainees().size());

        return activeTrainees;
    }

    @Override
    public boolean existsById(Long userId) {
        if (userId == null) {
            return false;
        }

        boolean exists = storage.getTrainees().containsKey(userId);
        logger.debug("Trainee exists check for userId {}: {}", userId, exists);

        return exists;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();
        boolean exists = storage.getAllTrainees().stream()
                .anyMatch(trainee -> cleanUsername.equals(trainee.getUsername()));

        logger.debug("Trainee exists check for username '{}': {}", cleanUsername, exists);

        return exists;
    }
}