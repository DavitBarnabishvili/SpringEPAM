package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.service.TrainingService;
import com.gym.crm.util.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);
    private final TrainingDao trainingDao;
    private final TrainerDao trainerDao;
    private final TraineeDao traineeDao;
    private final ValidationService validationService;


    public TrainingServiceImpl(TrainingDao trainingDao,
                               TrainerDao trainerDao,
                               TraineeDao traineeDao,
                               ValidationService validationService) {
        this.trainingDao = trainingDao;
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
        this.validationService = validationService;
    }

    @Override
    public Training createTraining(Training training) {
        if (training == null) {
            throw new IllegalArgumentException("Training cannot be null");
        }

        logger.info("Creating training: '{}' for trainee {} and trainer {}",
                training.getTrainingName(), training.getTraineeId(), training.getTrainerId());

        validationService.validateTraining(training);

        if (!traineeDao.existsById(training.getTraineeId())) {
            throw new RuntimeException("Trainee not found with userId: " + training.getTraineeId());
        }

        if (!trainerDao.existsById(training.getTrainerId())) {
            throw new RuntimeException("Trainer not found with userId: " + training.getTrainerId());
        }

        Optional<com.gym.crm.model.Trainee> traineeOpt = traineeDao.findById(training.getTraineeId());
        if (traineeOpt.isPresent() && !traineeOpt.get().isActive()) {
            throw new IllegalArgumentException("Cannot create training for inactive trainee");
        }

        Optional<com.gym.crm.model.Trainer> trainerOpt = trainerDao.findById(training.getTrainerId());
        if (trainerOpt.isPresent() && !trainerOpt.get().isActive()) {
            throw new IllegalArgumentException("Cannot create training for inactive trainer");
        }

        Training savedTraining = trainingDao.create(training);

        logger.info("Successfully created training: '{}' with id: {} (trainee: {}, trainer: {})",
                savedTraining.getTrainingName(), savedTraining.getId(),
                savedTraining.getTraineeId(), savedTraining.getTrainerId());

        return savedTraining;
    }

    @Override
    public Optional<Training> findTrainingById(Long id) {
        if (id == null) {
            logger.debug("FindTrainingById called with null id");
            return Optional.empty();
        }

        logger.debug("Finding training by id: {}", id);

        Optional<Training> result = trainingDao.findById(id);

        if (result.isPresent()) {
            logger.debug("Found training: '{}'", result.get().getTrainingName());
        } else {
            logger.debug("No training found with id: {}", id);
        }

        return result;
    }

    @Override
    public List<Training> findAllTrainings() {
        logger.debug("Finding all trainings");

        List<Training> trainings = trainingDao.findAll();

        logger.debug("Found {} trainings", trainings.size());

        return trainings;
    }

    @Override
    public List<Training> findTrainingsByTraineeId(Long traineeId) {
        if (traineeId == null) {
            logger.debug("FindTrainingsByTraineeId called with null traineeId");
            return List.of();
        }

        logger.debug("Finding trainings for trainee: {}", traineeId);

        List<Training> trainings = trainingDao.findByTraineeId(traineeId);

        logger.debug("Found {} trainings for trainee: {}", trainings.size(), traineeId);

        return trainings;
    }

    @Override
    public List<Training> findTrainingsByTrainerId(Long trainerId) {
        if (trainerId == null) {
            logger.debug("FindTrainingsByTrainerId called with null trainerId");
            return List.of();
        }

        logger.debug("Finding trainings for trainer: {}", trainerId);

        List<Training> trainings = trainingDao.findByTrainerId(trainerId);

        logger.debug("Found {} trainings for trainer: {}", trainings.size(), trainerId);

        return trainings;
    }

    @Override
    public List<Training> findTrainingsByDate(LocalDate date) {
        if (date == null) {
            logger.debug("FindTrainingsByDate called with null date");
            return List.of();
        }

        logger.debug("Finding trainings for date: {}", date);

        List<Training> trainings = trainingDao.findByDate(date);

        logger.debug("Found {} trainings for date: {}", trainings.size(), date);

        return trainings;
    }

    @Override
    public List<Training> findTrainingsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            logger.debug("FindTrainingsByDateRange called with null dates: start={}, end={}", startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindTrainingsByDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings between {} and {}", startDate, endDate);

        List<Training> trainings = trainingDao.findByDateRange(startDate, endDate);

        logger.debug("Found {} trainings between {} and {}", trainings.size(), startDate, endDate);

        return trainings;
    }

    @Override
    public List<Training> findTraineeTrainingsByDateRange(Long traineeId, LocalDate startDate, LocalDate endDate) {
        if (traineeId == null || startDate == null || endDate == null) {
            logger.debug("FindTraineeTrainingsByDateRange called with null parameters: traineeId={}, start={}, end={}",
                    traineeId, startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindTraineeTrainingsByDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings for trainee {} between {} and {}", traineeId, startDate, endDate);

        List<Training> trainings = trainingDao.findByTraineeIdAndDateRange(traineeId, startDate, endDate);

        logger.debug("Found {} trainings for trainee {} between {} and {}",
                trainings.size(), traineeId, startDate, endDate);

        return trainings;
    }

    @Override
    public List<Training> findTrainerTrainingsByDateRange(Long trainerId, LocalDate startDate, LocalDate endDate) {
        if (trainerId == null || startDate == null || endDate == null) {
            logger.debug("FindTrainerTrainingsByDateRange called with null parameters: trainerId={}, start={}, end={}",
                    trainerId, startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindTrainerTrainingsByDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings for trainer {} between {} and {}", trainerId, startDate, endDate);

        List<Training> trainings = trainingDao.findByTrainerIdAndDateRange(trainerId, startDate, endDate);

        logger.debug("Found {} trainings for trainer {} between {} and {}",
                trainings.size(), trainerId, startDate, endDate);

        return trainings;
    }

    @Override
    public boolean trainingExists(Long id) {
        if (id == null) {
            return false;
        }

        boolean exists = trainingDao.existsById(id);
        logger.debug("Training exists check for id {}: {}", id, exists);

        return exists;
    }
}