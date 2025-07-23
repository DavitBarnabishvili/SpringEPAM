package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private final InMemoryStorage storage;

    public TrainingDaoImpl(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public Training create(Training training) {
        if (training == null) {
            throw new IllegalArgumentException("Training cannot be null");
        }

        logger.debug("Creating training: {}", training.getTrainingName());

        Long id = training.getId();

        if (training.getId() == null) {
            id = storage.generateTrainingId();
            training.setId(id);
            logger.debug("Assigned id {} to training '{}'", id, training.getTrainingName());
        }

        storage.storeTraining(id, training);

        logger.info("Successfully created training: '{}' with id: {} (trainee: {}, trainer: {})",
                training.getTrainingName(), id,
                training.getTraineeId(), training.getTrainerId());

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        if (id == null) {
            logger.debug("FindById called with null id");
            return Optional.empty();
        }

        logger.debug("Finding training by id: {}", id);

        Training training = storage.getTraining(id);

        if (training != null) {
            logger.debug("Found training: '{}'", training.getTrainingName());
            return Optional.of(training);
        } else {
            logger.debug("No training found with id: {}", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Training> findAll() {
        logger.debug("Finding all trainings");

        List<Training> trainings = storage.getAllTrainings();

        logger.debug("Found {} trainings", trainings.size());

        return trainings;
    }

    @Override
    public List<Training> findByTraineeId(Long traineeId) {
        if (traineeId == null) {
            logger.debug("FindByTraineeId called with null traineeId");
            return List.of();
        }

        logger.debug("Finding trainings for trainee: {}", traineeId);

        List<Training> trainings = storage.getAllTrainings().stream()
                .filter(training -> traineeId.equals(training.getTraineeId()))
                .collect(Collectors.toList());

        logger.debug("Found {} trainings for trainee: {}", trainings.size(), traineeId);

        return trainings;
    }

    @Override
    public List<Training> findByTrainerId(Long trainerId) {
        if (trainerId == null) {
            logger.debug("FindByTrainerId called with null trainerId");
            return List.of();
        }

        logger.debug("Finding trainings for trainer: {}", trainerId);

        List<Training> trainings = storage.getAllTrainings().stream()
                .filter(training -> trainerId.equals(training.getTrainerId()))
                .collect(Collectors.toList());

        logger.debug("Found {} trainings for trainer: {}", trainings.size(), trainerId);

        return trainings;
    }

    @Override
    public List<Training> findByDate(LocalDate date) {
        if (date == null) {
            logger.debug("FindByDate called with null date");
            return List.of();
        }

        logger.debug("Finding trainings for date: {}", date);

        List<Training> trainings = storage.getAllTrainings().stream()
                .filter(training -> date.equals(training.getTrainingDate()))
                .collect(Collectors.toList());

        logger.debug("Found {} trainings for date: {}", trainings.size(), date);

        return trainings;
    }

    @Override
    public List<Training> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            logger.debug("FindByDateRange called with null dates: start={}, end={}", startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindByDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings between {} and {}", startDate, endDate);

        List<Training> trainings = storage.getAllTrainings().stream()
                .filter(training -> {
                    LocalDate trainingDate = training.getTrainingDate();
                    return trainingDate != null &&
                            !trainingDate.isBefore(startDate) &&
                            !trainingDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        logger.debug("Found {} trainings between {} and {}", trainings.size(), startDate, endDate);

        return trainings;
    }

    @Override
    public List<Training> findByTraineeIdAndDateRange(Long traineeId, LocalDate startDate, LocalDate endDate) {
        if (traineeId == null || startDate == null || endDate == null) {
            logger.debug("FindByTraineeIdAndDateRange called with null parameters: traineeId={}, start={}, end={}",
                    traineeId, startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindByTraineeIdAndDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings for trainee {} between {} and {}", traineeId, startDate, endDate);

        List<Training> trainings = storage.getAllTrainings().stream()
                .filter(training -> {
                    LocalDate trainingDate = training.getTrainingDate();
                    return traineeId.equals(training.getTraineeId()) &&
                            trainingDate != null &&
                            !trainingDate.isBefore(startDate) &&
                            !trainingDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        logger.debug("Found {} trainings for trainee {} between {} and {}",
                trainings.size(), traineeId, startDate, endDate);

        return trainings;
    }

    @Override
    public List<Training> findByTrainerIdAndDateRange(Long trainerId, LocalDate startDate, LocalDate endDate) {
        if (trainerId == null || startDate == null || endDate == null) {
            logger.debug("FindByTrainerIdAndDateRange called with null parameters: trainerId={}, start={}, end={}",
                    trainerId, startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindByTrainerIdAndDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings for trainer {} between {} and {}", trainerId, startDate, endDate);

        List<Training> trainings = storage.getAllTrainings().stream()
                .filter(training -> {
                    LocalDate trainingDate = training.getTrainingDate();
                    return trainerId.equals(training.getTrainerId()) &&
                            trainingDate != null &&
                            !trainingDate.isBefore(startDate) &&
                            !trainingDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        logger.debug("Found {} trainings for trainer {} between {} and {}",
                trainings.size(), trainerId, startDate, endDate);

        return trainings;
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }

        boolean exists = storage.getTrainings().containsKey(id);
        logger.debug("Training exists check for id {}: {}", id, exists);

        return exists;
    }
}