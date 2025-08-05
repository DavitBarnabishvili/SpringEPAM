package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.TraineeProfile;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainerProfile;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import com.gym.crm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final InMemoryStorage storage;

    public GymFacade(TrainerService trainerService,
                     TraineeService traineeService,
                     TrainingService trainingService,
                     InMemoryStorage storage) {
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.storage = storage;
        logger.info("GymFacade initialized with all services");
    }

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        logger.info("Creating trainer via facade: {} {}", firstName, lastName);

        Trainer trainer = new Trainer(firstName, lastName, specialization);
        return trainerService.createTrainer(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Updating trainer via facade: {}", trainer.getFullName());
        return trainerService.updateTrainer(trainer);
    }

    public Optional<Trainer> findTrainerById(Long userId) {
        return trainerService.findTrainerById(userId);
    }

    public Optional<Trainer> findTrainerByUsername(String username) {
        return trainerService.findTrainerByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.findAllTrainers();
    }

    public List<Trainer> getTrainersBySpecialization(TrainingType specialization) {
        return trainerService.findTrainersBySpecialization(specialization);
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        logger.info("Creating trainee via facade: {} {}", firstName, lastName);

        Trainee trainee = new Trainee(firstName, lastName, dateOfBirth, address);
        return traineeService.createTrainee(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Updating trainee via facade: {}", trainee.getFullName());
        return traineeService.updateTrainee(trainee);
    }

    public boolean deleteTrainee(Long userId) {
        logger.info("Deleting trainee via facade: userId {}", userId);
        return traineeService.deleteTrainee(userId);
    }

    public Optional<Trainee> findTraineeById(Long userId) {
        return traineeService.findTraineeById(userId);
    }

    public Optional<Trainee> findTraineeByUsername(String username) {
        return traineeService.findTraineeByUsername(username);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.findAllTrainees();
    }

    public List<Trainee> getAllActiveTrainees() {
        return traineeService.findAllActiveTrainees();
    }

    public boolean activateTrainee(Long userId) {
        logger.info("Activating trainee via facade: userId {}", userId);
        return traineeService.activateTrainee(userId);
    }

    public boolean deactivateTrainee(Long userId) {
        logger.info("Deactivating trainee via facade: userId {}", userId);
        return traineeService.deactivateTrainee(userId);
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate, Integer duration) {
        logger.info("Creating training via facade: '{}' for trainee {} and trainer {}",
                trainingName, traineeId, trainerId);

        Training training = new Training(traineeId, trainerId, trainingName, trainingType, trainingDate, duration);
        return trainingService.createTraining(training);
    }

    public Optional<Training> findTrainingById(Long id) {
        return trainingService.findTrainingById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.findAllTrainings();
    }

    public List<Training> getTraineeTrainings(Long traineeId) {
        return trainingService.findTrainingsByTraineeId(traineeId);
    }

    public List<Training> getTrainerTrainings(Long trainerId) {
        return trainingService.findTrainingsByTrainerId(trainerId);
    }

    public List<Training> getTrainingsByDate(LocalDate date) {
        return trainingService.findTrainingsByDate(date);
    }

    public List<Training> getTrainingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return trainingService.findTrainingsByDateRange(startDate, endDate);
    }

    public List<TrainingType> getAvailableTrainingTypes() {
        return storage.getAllTrainingTypes();
    }

    public TrainingType getTrainingTypeById(Long id) {
        return storage.getTrainingType(id);
    }

    public TrainingType getTrainingTypeByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return storage.getAllTrainingTypes().stream()
                .filter(tt -> name.trim().equalsIgnoreCase(tt.getTrainingTypeName()))
                .findFirst()
                .orElse(null);
    }

    public TrainerProfile getTrainerProfile(Long trainerId) {
        Optional<Trainer> trainerOpt = trainerService.findTrainerById(trainerId);
        if (trainerOpt.isEmpty()) {
            return null;
        }

        Trainer trainer = trainerOpt.get();
        List<Training> trainings = trainingService.findTrainingsByTrainerId(trainerId);

        return new TrainerProfile(trainer, trainings);
    }

    public TraineeProfile getTraineeProfile(Long traineeId) {
        Optional<Trainee> traineeOpt = traineeService.findTraineeById(traineeId);
        if (traineeOpt.isEmpty()) {
            return null;
        }

        Trainee trainee = traineeOpt.get();
        List<Training> trainings = trainingService.findTrainingsByTraineeId(traineeId);

        return new TraineeProfile(trainee, trainings);
    }

    public boolean canCreateTraining(Long traineeId, Long trainerId) {
        Optional<Trainee> traineeOpt = traineeService.findTraineeById(traineeId);
        Optional<Trainer> trainerOpt = trainerService.findTrainerById(trainerId);

        return traineeOpt.isPresent() && traineeOpt.get().isActive() &&
                trainerOpt.isPresent() && trainerOpt.get().isActive();
    }
}