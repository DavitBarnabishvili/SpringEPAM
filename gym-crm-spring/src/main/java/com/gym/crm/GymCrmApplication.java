package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.TraineeProfile;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainerProfile;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class GymCrmApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Gym CRM Application...");

        try {
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
            logger.info("Spring Application Context initialized successfully");

            GymFacade gymFacade = context.getBean(GymFacade.class);
            logger.info("GymFacade obtained from Spring context");

            demonstrateApplication(gymFacade);

        } catch (Exception e) {
            logger.error("Failed to start Gym CRM Application", e);
            System.exit(1);
        }

        logger.info("Gym CRM Application completed successfully");
    }

    /**
     * Demonstrates the functionality of the Gym CRM system.
     */
    private static void demonstrateApplication(GymFacade gymFacade) {
        logger.info("=== STARTING GYM CRM DEMONSTRATION ===");

        try {
            // Show available training types (loaded from initial data)
            demonstrateTrainingTypes(gymFacade);

            // Create and manage trainers
            demonstrateTrainerOperations(gymFacade);

            // Create and manage trainees
            demonstrateTraineeOperations(gymFacade);

            // Create and manage training sessions
            demonstrateTrainingOperations(gymFacade);

            // Demonstrate complex operations
            demonstrateComplexOperations(gymFacade);

        } catch (Exception e) {
            logger.error("Error during application demonstration", e);
        }

        logger.info("=== GYM CRM DEMONSTRATION COMPLETED ===");
    }

    private static void demonstrateTrainingTypes(GymFacade gymFacade) {
        logger.info("--- Demonstrating Training Types ---");

        List<TrainingType> trainingTypes = gymFacade.getAvailableTrainingTypes();
        logger.info("Available training types: {}", trainingTypes.size());

        for (TrainingType type : trainingTypes) {
            logger.info("Training Type: {} (ID: {})", type.getTrainingTypeName(), type.getId());
        }
    }

    private static void demonstrateTrainerOperations(GymFacade gymFacade) {
        logger.info("--- Demonstrating Trainer Operations ---");

        TrainingType cardioType = gymFacade.getTrainingTypeByName("Cardio");
        TrainingType strengthType = gymFacade.getTrainingTypeByName("Strength");

        Trainer trainer1 = gymFacade.createTrainer("Walter", "White", cardioType);
        Trainer trainer2 = gymFacade.createTrainer("Jesse", "Pinkman", strengthType);
        Trainer trainer3 = gymFacade.createTrainer("Mike", "Ehrmantraut", cardioType);

        logger.info("Created trainer: {} with username: {}", trainer1.getFullName(), trainer1.getUsername());
        logger.info("Created trainer: {} with username: {}", trainer2.getFullName(), trainer2.getUsername());
        logger.info("Created trainer: {} with username: {}", trainer3.getFullName(), trainer3.getUsername());

        trainer1.setSpecialization(strengthType);
        Trainer updatedTrainer = gymFacade.updateTrainer(trainer1);
        logger.info("Updated trainer specialization: {} now specializes in {}",
                updatedTrainer.getFullName(), updatedTrainer.getSpecializationName());

        List<Trainer> allTrainers = gymFacade.getAllTrainers();
        logger.info("Total trainers in system: {}", allTrainers.size());

        List<Trainer> cardioTrainers = gymFacade.getTrainersBySpecialization(cardioType);
        logger.info("Cardio trainers: {}", cardioTrainers.size());
    }

    private static void demonstrateTraineeOperations(GymFacade gymFacade) {
        logger.info("--- Demonstrating Trainee Operations ---");

        Trainee trainee1 = gymFacade.createTrainee("Gregory", "House",
                LocalDate.of(1990, 5, 15), "123 Grove St");
        Trainee trainee2 = gymFacade.createTrainee("Alan", "Turing",
                LocalDate.of(1985, 10, 22), "456 Rustaveli Ave");
        Trainee trainee3 = gymFacade.createTrainee("Guram", "Sherozia",
                LocalDate.of(1992, 3, 8), null);

        logger.info("Created trainee: {} (age: {}) with username: {}",
                trainee1.getFullName(), trainee1.getAge(), trainee1.getUsername());
        logger.info("Created trainee: {} (age: {}) with username: {}",
                trainee2.getFullName(), trainee2.getAge(), trainee2.getUsername());
        logger.info("Created trainee: {} (age: {}) with username: {}",
                trainee3.getFullName(), trainee3.getAge(), trainee3.getUsername());

        trainee1.setAddress("789 New St");
        Trainee updatedTrainee = gymFacade.updateTrainee(trainee1);
        logger.info("Updated trainee address: {} now lives at {}",
                updatedTrainee.getFullName(), updatedTrainee.getAddress());

        boolean deactivated = gymFacade.deactivateTrainee(trainee2.getUserId());
        logger.info("Trainee deactivation: {}", deactivated ? "SUCCESS" : "FAILED");

        boolean activated = gymFacade.activateTrainee(trainee2.getUserId());
        logger.info("Trainee reactivation: {}", activated ? "SUCCESS" : "FAILED");

        List<Trainee> allTrainees = gymFacade.getAllTrainees();
        List<Trainee> activeTrainees = gymFacade.getAllActiveTrainees();
        logger.info("Total trainees: {}, Active trainees: {}", allTrainees.size(), activeTrainees.size());
    }

    private static void demonstrateTrainingOperations(GymFacade gymFacade) {
        logger.info("--- Demonstrating Training Operations ---");

        List<Trainer> trainers = gymFacade.getAllTrainers();
        List<Trainee> trainees = gymFacade.getAllTrainees();

        if (trainers.isEmpty() || trainees.isEmpty()) {
            logger.warn("No trainers or trainees available for training creation");
            return;
        }

        Trainer trainer = trainers.getFirst();
        Trainee trainee = trainees.getFirst();
        TrainingType cardioType = gymFacade.getTrainingTypeByName("Cardio");

        Training training1 = gymFacade.createTraining(
                trainee.getUserId(), trainer.getUserId(),
                "Morning Cardio Session", cardioType, LocalDate.now().plusDays(1), 60
        );

        Training training2 = gymFacade.createTraining(
                trainee.getUserId(), trainer.getUserId(),
                "Evening Cardio Session", cardioType, LocalDate.now().plusDays(2), 45
        );

        logger.info("Created training: '{}' scheduled for {}",
                training1.getTrainingName(), training1.getFormattedDate());
        logger.info("Created training: '{}' scheduled for {}",
                training2.getTrainingName(), training2.getFormattedDate());

        List<Training> allTrainings = gymFacade.getAllTrainings();
        List<Training> traineeTrainings = gymFacade.getTraineeTrainings(trainee.getUserId());
        List<Training> trainerTrainings = gymFacade.getTrainerTrainings(trainer.getUserId());

        logger.info("Total trainings: {}", allTrainings.size());
        logger.info("Trainings for {}: {}", trainee.getFullName(), traineeTrainings.size());
        logger.info("Trainings for {}: {}", trainer.getFullName(), trainerTrainings.size());
    }

    private static void demonstrateComplexOperations(GymFacade gymFacade) {
        logger.info("--- Demonstrating Complex Operations ---");

        List<Trainer> trainers = gymFacade.getAllTrainers();
        List<Trainee> trainees = gymFacade.getAllTrainees();

        if (!trainers.isEmpty()) {
            TrainerProfile trainerProfile = gymFacade.getTrainerProfile(trainers.getFirst().getUserId());
            if (trainerProfile != null) {
                logger.info("Trainer Profile: {} has {} training sessions",
                        trainerProfile.getTrainer().getFullName(),
                        trainerProfile.getTotalTrainings());
            }
        }

        if (!trainees.isEmpty()) {
            TraineeProfile traineeProfile = gymFacade.getTraineeProfile(trainees.get(0).getUserId());
            if (traineeProfile != null) {
                logger.info("Trainee Profile: {} has {} training sessions",
                        traineeProfile.getTrainee().getFullName(),
                        traineeProfile.getTotalTrainings());
            }
        }

        if (!trainers.isEmpty() && !trainees.isEmpty()) {
            boolean canCreate = gymFacade.canCreateTraining(trainees.getFirst().getUserId(),
                    trainers.getFirst().getUserId());
            logger.info("Can create training between {} and {}: {}",
                    trainees.getFirst().getFullName(), trainers.getFirst().getFullName(), canCreate);
        }
    }
}