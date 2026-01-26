package com.gym.crm.service.impl;

import com.gym.crm.dto.request.TrainerWorkloadRequest;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.messaging.WorkloadMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkloadNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadNotificationService.class);
    private final TrainerService trainerService;
    private final WorkloadMessageProducer messageProducer;

    public WorkloadNotificationService(WorkloadMessageProducer messageProducer,
                                       TrainerService trainerService){
        this.messageProducer = messageProducer;
        this.trainerService = trainerService;
    }

    public void notifyTrainingAdded(Training training) {
        logger.info("Preparing to notify workload service about new training for trainer ID: {}",
                training.getTrainerId());

        Optional<Trainer> trainerOpt = trainerService.findTrainerById(training.getTrainerId());
        if (trainerOpt.isEmpty()) {
            logger.error("Trainer not found with ID: {}", training.getTrainerId());
            return;
        }

        Trainer trainer = trainerOpt.get();

        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(trainer.getUsername())
                .trainerFirstName(trainer.getFirstName())
                .trainerLastName(trainer.getLastName())
                .isActive(trainer.getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();

        // Send message asynchronously
        messageProducer.sendWorkloadUpdate(request);
    }


    public void notifyTrainingDeleted(Training training) {
        logger.info("Preparing to notify workload service about deleted training for trainer ID: {}",
                training.getTrainerId());

        Optional<Trainer> trainerOpt = trainerService.findTrainerById(training.getTrainerId());
        if (trainerOpt.isEmpty()) {
            logger.error("Trainer not found with ID: {}", training.getTrainerId());
            return;
        }

        Trainer trainer = trainerOpt.get();

        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(trainer.getUsername())
                .trainerFirstName(trainer.getFirstName())
                .trainerLastName(trainer.getLastName())
                .isActive(trainer.getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(TrainerWorkloadRequest.ActionType.DELETE)
                .build();

        messageProducer.sendWorkloadUpdate(request);
    }
}