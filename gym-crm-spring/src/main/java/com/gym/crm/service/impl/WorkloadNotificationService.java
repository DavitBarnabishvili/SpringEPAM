package com.gym.crm.service.impl;

import com.gym.crm.client.TrainerWorkloadClient;
import com.gym.crm.dto.request.TrainerWorkloadRequest;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.service.TrainerService;
import com.gym.crm.util.impl.JwtUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Optional;

@Service
public class WorkloadNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadNotificationService.class);

    private final TrainerWorkloadClient trainerWorkloadClient;
    private final TrainerService trainerService;
    private final JwtUtil jwtUtil;

    public WorkloadNotificationService(TrainerWorkloadClient trainerWorkloadClient,
                                       TrainerService trainerService, JwtUtil jwtUtil) {
        this.trainerWorkloadClient = trainerWorkloadClient;
        this.trainerService = trainerService;
        this.jwtUtil = jwtUtil;
    }

    @CircuitBreaker(name = "trainer-workload", fallbackMethod = "fallbackNotifyWorkloadUpdate")
    public void notifyTrainingAdded(Training training) {
        logger.info("Notifying workload service about new training for trainer ID: {}",
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

        String token = getAuthorizationToken();

        try {
            ResponseEntity<Void> response = trainerWorkloadClient.updateTrainerWorkload(token, request); //it fails here, but i don't get how.
            logger.info("Successfully notified workload service. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to notify workload service", e);
            throw e; // circuit breaker should handle it
        }
    }

    @CircuitBreaker(name = "trainer-workload", fallbackMethod = "fallbackNotifyWorkloadUpdate")
    public void notifyTrainingDeleted(Training training) {
        logger.info("Notifying workload service about deleted training for trainer ID: {}",
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

        String token = getAuthorizationToken();

        try {
            ResponseEntity<Void> response = trainerWorkloadClient.updateTrainerWorkload(token, request);
            logger.info("Successfully notified workload service. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to notify workload service", e);
            throw e; // circuit breaker should handle it
        }
    }

    // Fallback method for circuit breaker
    public void fallbackNotifyWorkloadUpdate(Training training, Exception ex) {
        logger.error("Circuit breaker activated. Failed to notify workload service for training ID: {}. Error: {}",
                training.getId(), ex.getMessage());
    }

    private String getAuthorizationToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                return authHeader;
            }
        }

        return "Bearer " + generateServiceToken();
    }

    private String generateServiceToken() {
        return jwtUtil.generateServiceToken();
    }
}