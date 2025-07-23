package com.gym.crm.storage;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory storage component for the gym CRM system.
 * Provides separate storage namespaces for each entity type.
 * Thread-safe implementation using ConcurrentHashMap.
 */
@Component
public class InMemoryStorage {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStorage.class);

    // Storage maps for each entity
    private final Map<Long, Trainer> trainers = new ConcurrentHashMap<>();
    private final Map<Long, Trainee> trainees = new ConcurrentHashMap<>();
    private final Map<Long, Training> trainings = new ConcurrentHashMap<>();
    private final Map<Long, TrainingType> trainingTypes = new ConcurrentHashMap<>();

    // ID generators for each entity
    private final AtomicLong trainerIdGenerator = new AtomicLong(1);
    private final AtomicLong traineeIdGenerator = new AtomicLong(1);
    private final AtomicLong trainingIdGenerator = new AtomicLong(1);
    private final AtomicLong trainingTypeIdGenerator = new AtomicLong(1);

    // Trainer operations
    public Map<Long, Trainer> getTrainers() {
        return trainers;
    }

    public Long generateTrainerId() {
        return trainerIdGenerator.getAndIncrement();
    }

    public void storeTrainer(Long id, Trainer trainer) {
        trainers.put(id, trainer);
        logger.debug("Stored trainer: {} (username: {}, specialization: {})",
                trainer.getFullName(), trainer.getUsername(), trainer.getSpecializationName());
    }

    public Trainer getTrainer(Long id) {
        Trainer trainer = trainers.get(id);
        if (trainer != null) {
            logger.debug("Retrieved trainer: {} (username: {})", trainer.getFullName(), trainer.getUsername());
        } else {
            logger.debug("Trainer not found with ID: {}", id);
        }
        return trainer;
    }

    public List<Trainer> getAllTrainers() {
        List<Trainer> allTrainers = new ArrayList<>(trainers.values());
        logger.debug("Retrieved {} trainers from storage", allTrainers.size());
        return allTrainers;
    }

    public void removeTrainer(Long id) {
        Trainer removed = trainers.remove(id);
        if (removed != null) {
            logger.debug("Removed trainer: {} (username: {})", removed.getFullName(), removed.getUsername());
        } else {
            logger.debug("Attempted to remove non-existent trainer with ID: {}", id);
        }
    }

    // Trainee operations
    public Map<Long, Trainee> getTrainees() {
        return trainees;
    }

    public Long generateTraineeId() {
        return traineeIdGenerator.getAndIncrement();
    }

    public void storeTrainee(Long id, Trainee trainee) {
        trainees.put(id, trainee);
        logger.debug("Stored trainee: {} (username: {}, age: {})",
                trainee.getFullName(), trainee.getUsername(),
                trainee.getAge() != null ? trainee.getAge() + " years" : "unknown");
    }

    public Trainee getTrainee(Long id) {
        Trainee trainee = trainees.get(id);
        if (trainee != null) {
            logger.debug("Retrieved trainee: {} (username: {})", trainee.getFullName(), trainee.getUsername());
        } else {
            logger.debug("Trainee not found with ID: {}", id);
        }
        return trainee;
    }

    public List<Trainee> getAllTrainees() {
        List<Trainee> allTrainees = new ArrayList<>(trainees.values());
        logger.debug("Retrieved {} trainees from storage", allTrainees.size());
        return allTrainees;
    }

    public void removeTrainee(Long id) {
        Trainee removed = trainees.remove(id);
        if (removed != null) {
            logger.debug("Removed trainee: {} (username: {})", removed.getFullName(), removed.getUsername());
        } else {
            logger.debug("Attempted to remove non-existent trainee with ID: {}", id);
        }
    }

    // Training operations
    public Map<Long, Training> getTrainings() {
        return trainings;
    }

    public Long generateTrainingId() {
        return trainingIdGenerator.getAndIncrement();
    }

    public void storeTraining(Long id, Training training) {
        trainings.put(id, training);
        logger.debug("Stored training: '{}' (trainee ID: {}, trainer ID: {}, date: {})",
                training.getTrainingName(), training.getTraineeId(),
                training.getTrainerId(), training.getFormattedDate());
    }

    public Training getTraining(Long id) {
        Training training = trainings.get(id);
        if (training != null) {
            logger.debug("Retrieved training: '{}' on {}", training.getTrainingName(), training.getFormattedDate());
        } else {
            logger.debug("Training not found with ID: {}", id);
        }
        return training;
    }

    public List<Training> getAllTrainings() {
        List<Training> allTrainings = new ArrayList<>(trainings.values());
        logger.debug("Retrieved {} trainings from storage", allTrainings.size());
        return allTrainings;
    }

    public void removeTraining(Long id) {
        Training removed = trainings.remove(id);
        if (removed != null) {
            logger.debug("Removed training: '{}' scheduled for {}",
                    removed.getTrainingName(), removed.getFormattedDate());
        } else {
            logger.debug("Attempted to remove non-existent training with ID: {}", id);
        }
    }

    // TrainingType operations
    public Map<Long, TrainingType> getTrainingTypes() {
        return trainingTypes;
    }

    public Long generateTrainingTypeId() {
        return trainingTypeIdGenerator.getAndIncrement();
    }

    public void storeTrainingType(Long id, TrainingType trainingType) {
        trainingTypes.put(id, trainingType);
        logger.debug("Stored training type: '{}'", trainingType.getTrainingTypeName());
    }

    public TrainingType getTrainingType(Long id) {
        TrainingType trainingType = trainingTypes.get(id);
        if (trainingType != null) {
            logger.debug("Retrieved training type: '{}'", trainingType.getTrainingTypeName());
        } else {
            logger.debug("Training type with ID: {} not found", id);
        }
        return trainingType;
    }

    public List<TrainingType> getAllTrainingTypes() {
        List<TrainingType> allTypes = new ArrayList<>(trainingTypes.values());
        logger.debug("Retrieved {} training types from storage", allTypes.size());
        return allTypes;
    }

    // Utility methods
    public void clear() {
        trainers.clear();
        trainees.clear();
        trainings.clear();
        trainingTypes.clear();

        trainerIdGenerator.set(1);
        traineeIdGenerator.set(1);
        trainingIdGenerator.set(1);
        trainingTypeIdGenerator.set(1);

        logger.info("Cleared storage");
    }

    public int getTotalEntities() {
        return trainers.size() + trainees.size() + trainings.size() + trainingTypes.size();
    }

    public void logStorageStatus() {
        logger.info("Storage status - Trainers: {}, Trainees: {}, Trainings: {}, TrainingTypes: {}",
                trainers.size(), trainees.size(), trainings.size(), trainingTypes.size());
    }
}