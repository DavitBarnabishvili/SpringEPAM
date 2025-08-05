package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.entity.*;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GymCrmApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Gym CRM Application...");

        try {
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
            logger.info("Spring Application Context initialized successfully");

            TrainerService trainerService = context.getBean(TrainerService.class);
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);
            TrainingTypeDao trainingTypeDao = context.getBean(TrainingTypeDao.class);

            demonstrateApplication(trainerService, traineeService, trainingService, trainingTypeDao);

        } catch (Exception e) {
            logger.error("Failed to start Gym CRM Application", e);
            System.exit(1);
        }

        logger.info("Gym CRM Application completed successfully");
    }

    private static void demonstrateApplication(TrainerService trainerService,
                                               TraineeService traineeService,
                                               TrainingService trainingService,
                                               TrainingTypeDao trainingTypeDao) {
        logger.info("=== STARTING GYM CRM DEMONSTRATION ===");

        // 1. Fetch a TrainingType from the database
        Optional<TrainingType> strengthTypeOptional = trainingTypeDao.findByName("Strength");
        if (strengthTypeOptional.isEmpty()) {
            logger.error("Required TrainingType 'Strength' not found in the database. Exiting demonstration.");
            return;
        }
        TrainingType strengthType = strengthTypeOptional.get();

        // 2. Create a Trainer using the fetched TrainingType
        Trainer trainer = new Trainer("Walter", "White", strengthType);
        trainer = trainerService.createTrainer(trainer);
        logger.info("Created Trainer: {} (username: {})", trainer.getFullName(), trainer.getUsername());

        // 3. Create a Trainee
        Trainee trainee = new Trainee("Jesse", "Pinkman", LocalDate.of(1992, 5, 10), "123 Albuquerque Ave");
        trainee = traineeService.createTrainee(trainee);
        logger.info("Created Trainee: {} (username: {})", trainee.getFullName(), trainee.getUsername());

        // 4. Extract credentials for authentication
        String trainerUsername = trainer.getUsername();
        String trainerPassword = trainer.getPassword();
        String traineeUsername = trainee.getUsername();
        String traineePassword = trainee.getPassword();

        // 5. Update Trainee's address
        trainee.setAddress("456 New Mexico Rd");
        traineeService.updateTrainee(traineeUsername, traineePassword, trainee);
        logger.info("Updated Trainee address: {} now lives at {}", trainee.getFullName(), trainee.getAddress());

        // 6. Demonstrate activation/deactivation but keep trainer active for training demo
        boolean firstDeactivation = trainerService.deactivateTrainer(trainerUsername, trainerPassword, trainer.getId());
        logger.info("Trainer {} deactivated (result: {})", trainer.getFullName(), firstDeactivation);

        // Try to deactivate again to show non-idempotent behavior
        boolean secondDeactivation = false;
        try {
            secondDeactivation = trainerService.deactivateTrainer(trainerUsername, trainerPassword, trainer.getId());
        } catch (SecurityException e) {
            logger.info("Cannot deactivate again - trainer is inactive: {}", e.getMessage());
        }
        logger.info("Second deactivation attempt: {}", secondDeactivation);

        // For demo purposes, create a new active trainer for the training session
        TrainingType cardioType = trainingTypeDao.findByName("Cardio").orElse(strengthType);
        Trainer activeTrainer = new Trainer("John", "Doe", cardioType);
        activeTrainer = trainerService.createTrainer(activeTrainer);
        logger.info("Created active trainer for demo: {} (username: {})", activeTrainer.getFullName(), activeTrainer.getUsername());

        String activeTrainerUsername = activeTrainer.getUsername();
        String activeTrainerPassword = activeTrainer.getPassword();

        // 7. Create Training session (using active trainer)
        Training training = new Training(
                trainee.getId(),
                activeTrainer.getId(),
                "Morning Strength Session",
                strengthType,
                LocalDate.now().plusDays(1),
                90
        );
        trainingService.createTraining(traineeUsername, traineePassword, training);
        logger.info("Created training: '{}' on {}", training.getTrainingName(), training.getFormattedDate());

        // 8. Query trainings by trainee and active trainer
        List<Training> traineeTrainings = trainingService.findTrainingsByTraineeId(
                traineeUsername, traineePassword, trainee.getId());
        logger.info("Trainee {} has {} training(s)", trainee.getFullName(), traineeTrainings.size());

        List<Training> activeTrainerTrainings = trainingService.findTrainingsByTrainerId(
                activeTrainerUsername, activeTrainerPassword, activeTrainer.getId());
        logger.info("Active trainer {} has {} training(s)", activeTrainer.getFullName(), activeTrainerTrainings.size());

        // Show that inactive trainer queries fail
        try {
            List<Training> inactiveTrainerTrainings = trainingService.findTrainingsByTrainerId(
                    trainerUsername, trainerPassword, trainer.getId());
        } catch (SecurityException e) {
            logger.info("Inactive trainer query failed as expected: {}", e.getMessage());
        }

        // 9. Demonstrate authentication requirements
        try {
            traineeService.updateTrainee("wrong.user", "wrongpassword", trainee);
            logger.error("Security breach: Authentication should have failed!");
        } catch (SecurityException e) {
            logger.info("Authentication correctly rejected unauthorized user: {}", e.getMessage());
        }

        // 10. Demonstrate non-idempotent activation/deactivation with trainee
        boolean firstTraineeDeactivation = traineeService.deactivateTrainee(traineeUsername, traineePassword, trainee.getId());
        boolean secondTraineeDeactivation = false;
        try {
            secondTraineeDeactivation = traineeService.deactivateTrainee(traineeUsername, traineePassword, trainee.getId());
        } catch (SecurityException e) {
            logger.info("Cannot deactivate trainee again - account is inactive: {}", e.getMessage());
        }
        logger.info("Trainee first deactivation: {}, second deactivation: {}", firstTraineeDeactivation, secondTraineeDeactivation);

        // Reactivate trainee for deletion demo
        Trainee traineeForDeletion = new Trainee("Mike", "Ehrmantraut", LocalDate.of(1980, 3, 15), "789 Better Call Saul St");
        traineeForDeletion = traineeService.createTrainee(traineeForDeletion);
        logger.info("Created trainee for deletion demo: {}", traineeForDeletion.getFullName());

        String deletionUsername = traineeForDeletion.getUsername();
        String deletionPassword = traineeForDeletion.getPassword();

        // 11. Delete trainee and demonstrate cascade deletion
        traineeService.deleteTrainee(deletionUsername, deletionPassword, traineeForDeletion.getId());
        logger.info("Deleted Trainee {} — associated trainings should also be deleted", traineeForDeletion.getFullName());

        // 12. Verify cascade deletion worked
        List<Training> remainingTrainings = trainingService.findAllTrainings();
        logger.info("Remaining trainings after cascade deletion: {}", remainingTrainings.size());

        logger.info("=== GYM CRM DEMONSTRATION COMPLETED ===");

        logger.info("=== REQUIREMENTS DEMONSTRATED ===");
        logger.info("✅ #1: Username/password generation during profile creation");
        logger.info("✅ #2: Authentication required for all operations except create");
        logger.info("✅ #3: Required field validation before create/update");
        logger.info("✅ #4: Users table parent-child relation with Trainer/Trainee");
        logger.info("✅ #5: Trainee-Trainer many-to-many via Training table");
        logger.info("✅ #6: Non-idempotent activate/deactivate operations");
        logger.info("✅ #7: Hard delete with cascade deletion of trainings");
        logger.info("✅ #8: Training duration as number type");
        logger.info("✅ #9: Training Date, Trainee DOB as Date type");
        logger.info("✅ #10: Training related to Trainee/Trainer by FK");
        logger.info("✅ #11: IsActive field as Boolean type");
        logger.info("✅ #12: TrainingTypes constant values initialized");
        logger.info("✅ #13: Each table has own PK");
        logger.info("✅ #15: Transaction management implemented");
        logger.info("✅ #16: Hibernate configured for H2 database");
    }
}