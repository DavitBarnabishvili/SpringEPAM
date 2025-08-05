package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.service.TraineeService;
import com.gym.crm.util.AuthenticationService;
import com.gym.crm.util.CredentialsGeneratorService;
import com.gym.crm.util.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainingDao trainingDao;
    private final AuthenticationService authenticationService;
    private final CredentialsGeneratorService credentialsGenerator;
    private final ValidationService validationService;

    public TraineeServiceImpl(TraineeDao traineeDao,
                              TrainingDao trainingDao,
                              AuthenticationService authenticationService,
                              CredentialsGeneratorService credentialsGenerator,
                              ValidationService validationService) {
        this.traineeDao = traineeDao;
        this.trainingDao = trainingDao;
        this.authenticationService = authenticationService;
        this.credentialsGenerator = credentialsGenerator;
        this.validationService = validationService;
    }

    @Override
    public Trainee createTrainee(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        logger.info("Creating trainee: {} {}", trainee.getFirstName(), trainee.getLastName());

        validationService.validateTrainee(trainee);

        if (trainee.getUsername() != null || trainee.getPassword() != null) {
            logger.warn("Trainee creation attempted with pre-set credentials. Overriding with generated credentials.");
        }

        String username = credentialsGenerator.generateUsername(trainee.getFirstName(), trainee.getLastName());
        String password = credentialsGenerator.generatePassword();

        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setIsActive(true);

        Trainee savedTrainee = traineeDao.create(trainee);

        logger.info("Successfully created trainee: {} with username: {} and id: {}",
                savedTrainee.getFullName(), savedTrainee.getUsername(), savedTrainee.getId());

        return savedTrainee;
    }

    @Override
    public Trainee updateTrainee(String username, String password, Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        if (trainee.getId() == null) {
            throw new IllegalArgumentException("Trainee id is required for update");
        }

        logger.info("Updating trainee with id: {}", trainee.getId());

        authenticationService.authenticateTrainee(username, password);
        authenticationService.validateTraineeAccess(username, trainee.getId());

        validationService.validateTrainee(trainee);

        Optional<Trainee> existingTraineeOpt = traineeDao.findById(trainee.getId());
        if (existingTraineeOpt.isEmpty()) {
            throw new RuntimeException("Trainee not found with id: " + trainee.getId());
        }

        Trainee existingTrainee = existingTraineeOpt.get();

        trainee.setUsername(existingTrainee.getUsername());
        trainee.setPassword(existingTrainee.getPassword());

        Trainee updatedTrainee = traineeDao.update(trainee);

        logger.info("Successfully updated trainee: {} with id: {}",
                updatedTrainee.getFullName(), updatedTrainee.getId());

        return updatedTrainee;
    }

    @Override
    public boolean deleteTrainee(String username, String password, Long userId) {
        if (userId == null) {
            logger.debug("DeleteTrainee called with null userId");
            return false;
        }

        logger.info("Deleting trainee with id: {}", userId);

        authenticationService.authenticateTrainee(username, password);
        authenticationService.validateTraineeAccess(username, userId);

        // CASCADE DELETE: First delete all related trainings
        int deletedTrainings = trainingDao.deleteByTraineeId(userId);
        logger.info("Cascade deleted {} trainings for trainee: {}", deletedTrainings, userId);

        boolean deleted = traineeDao.delete(userId);

        if (deleted) {
            logger.info("Successfully deleted trainee with id: {} and {} related trainings", userId, deletedTrainings);
        } else {
            logger.debug("No trainee found with id: {} for deletion", userId);
        }

        return deleted;
    }

    @Override
    public Optional<Trainee> findTraineeById(Long userId) {
        if (userId == null) {
            logger.debug("FindTraineeById called with null userId");
            return Optional.empty();
        }

        logger.debug("Finding trainee by id: {}", userId);

        Optional<Trainee> result = traineeDao.findById(userId);

        if (result.isPresent()) {
            logger.debug("Found trainee: {}", result.get().getFullName());
        } else {
            logger.debug("No trainee found with id: {}", userId);
        }

        return result;
    }

    @Override
    public Optional<Trainee> findTraineeByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("FindTraineeByUsername called with invalid username: {}", username);
            return Optional.empty();
        }

        logger.debug("Finding trainee by username: {}", username);

        Optional<Trainee> result = traineeDao.findByUsername(username.trim());

        if (result.isPresent()) {
            logger.debug("Found trainee with username: {}", username);
        } else {
            logger.debug("No trainee found with username: {}", username);
        }

        return result;
    }

    @Override
    public List<Trainee> findAllTrainees() {
        logger.debug("Finding all trainees");

        List<Trainee> trainees = traineeDao.findAll();

        logger.debug("Found {} trainees", trainees.size());

        return trainees;
    }

    @Override
    public List<Trainee> findAllActiveTrainees() {
        logger.debug("Finding all active trainees");

        List<Trainee> activeTrainees = traineeDao.findAllActive();

        logger.debug("Found {} active trainees", activeTrainees.size());

        return activeTrainees;
    }

    @Override
    public List<Trainee> findTraineesByAgeRange(int minAge, int maxAge) {
        if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
            logger.debug("Invalid age range: min={}, max={}", minAge, maxAge);
            return List.of();
        }

        logger.debug("Finding trainees with age between {} and {}", minAge, maxAge);

        LocalDate today = LocalDate.now();
        LocalDate maxBirthDate = today.minusYears(minAge);
        LocalDate minBirthDate = today.minusYears(maxAge + 1);

        List<Trainee> trainees = traineeDao.findAll().stream()
                .filter(trainee -> {
                    if (trainee.getDateOfBirth() == null) {
                        return false;
                    }
                    LocalDate birthDate = trainee.getDateOfBirth();
                    return !birthDate.isBefore(minBirthDate) && !birthDate.isAfter(maxBirthDate);
                })
                .collect(Collectors.toList());

        logger.debug("Found {} trainees with age between {} and {}", trainees.size(), minAge, maxAge);

        return trainees;
    }

    @Override
    public boolean activateTrainee(String username, String password, Long userId) {
        authenticationService.authenticateTrainee(username, password);
        authenticationService.validateTraineeAccess(username, userId);

        return updateTraineeActiveStatus(userId, true);
    }

    @Override
    public boolean deactivateTrainee(String username, String password, Long userId) {
        authenticationService.authenticateTrainee(username, password);
        authenticationService.validateTraineeAccess(username, userId);

        return updateTraineeActiveStatus(userId, false);
    }

    private boolean updateTraineeActiveStatus(Long userId, boolean isActive) {
        if (userId == null) {
            logger.debug("UpdateTraineeActiveStatus called with null userId");
            return false;
        }

        String action = isActive ? "Activating" : "Deactivating";
        logger.info("{} trainee with id: {}", action, userId);

        Optional<Trainee> traineeOpt = traineeDao.findById(userId);
        if (traineeOpt.isEmpty()) {
            logger.debug("No trainee found with id: {} for status update", userId);
            return false;
        }

        Trainee trainee = traineeOpt.get();

        if (trainee.isActive() == isActive) {
            String currentState = isActive ? "already active" : "already inactive";
            logger.warn("Trainee {} is {}, operation not performed", trainee.getFullName(), currentState);
            return false; // Not idempotent - return false if already in desired state
        }

        trainee.setIsActive(isActive);

        try {
            traineeDao.update(trainee);
            action = isActive ? "Activated" : "Deactivated";
            logger.info("Successfully {} trainee: {}", action.toLowerCase(), trainee.getFullName());
            return true;
        } catch (Exception e) {
            action = isActive ? "Activate" : "Deactivate";
            logger.error("Failed to {} trainee with id: {}", action.toLowerCase(), userId, e);
            return false;
        }
    }

    @Override
    public boolean traineeExists(Long userId) {
        if (userId == null) {
            return false;
        }

        boolean exists = traineeDao.existsById(userId);
        logger.debug("Trainee exists check for id {}: {}", userId, exists);

        return exists;
    }
}