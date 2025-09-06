package com.gym.crm.controller;

import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.dto.request.AddTrainingRequest;
import com.gym.crm.dto.response.TrainingResponse;
import com.gym.crm.dto.response.TrainingTypeResponse;
import com.gym.crm.entity.Training;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.exception.UserNotFoundException;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gym.crm.service.impl.CustomMetricsService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Training Management", description = "Training sessions and training types")
@SecurityRequirement(name = "Bearer Authentication")
public class TrainingController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingTypeDao trainingTypeDao;
    private final CustomMetricsService metricsService;

    public TrainingController(TrainingService trainingService, TraineeService traineeService,
                              TrainingTypeDao trainingTypeDao, TrainerService trainerService,
                              CustomMetricsService metricsService) {
        this.trainingService = trainingService;
        this.trainerService = trainerService;
        this.trainingTypeDao = trainingTypeDao;
        this.traineeService = traineeService;
        this.metricsService = metricsService;
    }

    @PostMapping("/trainings")
    @Operation(summary = "Add training", description = "Create a new training session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest request,
            HttpServletRequest httpRequest) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        String authenticatedUsername = (String) httpRequest.getAttribute("authenticatedUsername");
        String authenticatedRole = (String) httpRequest.getAttribute("authenticatedRole");

        logger.info("Adding training: {} for trainee: {} and trainer: {} by user: {}",
                request.getTrainingName(), request.getTraineeUsername(),
                request.getTrainerUsername(), authenticatedUsername);

        try {
            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(request.getTraineeUsername());
            Optional<Trainer> trainerOpt = trainerService.findTrainerByUsername(request.getTrainerUsername());

            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found: " + request.getTraineeUsername());
            }
            if (trainerOpt.isEmpty()) {
                throw new UserNotFoundException("Trainer not found: " + request.getTrainerUsername());
            }

            Trainee trainee = traineeOpt.get();
            Trainer trainer = trainerOpt.get();

            if (!authenticatedUsername.equals(request.getTraineeUsername()) &&
                    !authenticatedUsername.equals(request.getTrainerUsername())) {
                logger.warn("Access denied: {} attempted to create training for trainee {} and trainer {}",
                        authenticatedUsername, request.getTraineeUsername(), request.getTrainerUsername());
                throw new UserNotFoundException("You can only create trainings where you are involved");
            }

            Training training = new Training(
                    trainee.getId(),
                    trainer.getId(),
                    request.getTrainingName(),
                    trainer.getSpecialization(),
                    request.getTrainingDate(),
                    request.getTrainingDuration()
            );

            trainingService.createTraining(authenticatedUsername, training);
            metricsService.incrementTrainingCreated();

            logger.info("Training added successfully: {}", request.getTrainingName());
            return ResponseEntity.ok().build();

        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/trainees/{username}/trainings")
    @Operation(summary = "Get trainee trainings", description = "Get trainee's training sessions with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<TrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType,
            HttpServletRequest httpRequest) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        String authenticatedUsername = (String) httpRequest.getAttribute("authenticatedUsername");
        String authenticatedRole = (String) httpRequest.getAttribute("authenticatedRole");

        logger.info("Getting trainings for trainee: {} by user: {}", username, authenticatedUsername);

        try {
            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to view trainings for {}",
                        authenticatedUsername, username);
                throw new UserNotFoundException("You can only view your own trainings");
            }

            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(username);
            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found");
            }

            Trainee trainee = traineeOpt.get();

            List<Training> trainings;
            if (periodFrom != null && periodTo != null) {
                trainings = trainingService.findTraineeTrainingsByDateRange(
                        username, trainee.getId(), periodFrom, periodTo
                );
            } else {
                trainings = trainingService.findTrainingsByTraineeId(
                        username, trainee.getId()
                );
            }

            if (trainerName != null && !trainerName.isEmpty()) {
                trainings = filterByTrainerName(trainings, trainerName);
            }

            if (trainingType != null && !trainingType.isEmpty()) {
                trainings = trainings.stream()
                        .filter(t -> t.getTrainingTypeName().equalsIgnoreCase(trainingType))
                        .collect(Collectors.toList());
            }

            List<TrainingResponse> response = trainings.stream()
                    .map(t -> {
                        Optional<Trainer> trainer = trainerService.findTrainerById(t.getTrainerId());
                        String trainerFullName = trainer.map(tr -> tr.getFirstName() + " " + tr.getLastName())
                                .orElse("Unknown");
                        return new TrainingResponse(
                                t.getTrainingName(),
                                t.getTrainingDate(),
                                t.getTrainingTypeName(),
                                t.getTrainingDuration(),
                                trainerFullName
                        );
                    })
                    .collect(Collectors.toList());

            logger.info("Retrieved {} trainings for trainee: {}", response.size(), username);
            return ResponseEntity.ok(response);

        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/trainers/{username}/trainings")
    @Operation(summary = "Get trainer trainings", description = "Get trainer's training sessions with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<TrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName,
            HttpServletRequest httpRequest) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        String authenticatedUsername = (String) httpRequest.getAttribute("authenticatedUsername");
        String authenticatedRole = (String) httpRequest.getAttribute("authenticatedRole");

        logger.info("Getting trainings for trainer: {} by user: {}", username, authenticatedUsername);

        try {
            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to view trainings for {}",
                        authenticatedUsername, username);
                throw new UserNotFoundException("You can only view your own trainings");
            }

            Optional<Trainer> trainerOpt = trainerService.findTrainerByUsername(username);
            if (trainerOpt.isEmpty()) {
                throw new UserNotFoundException("Trainer not found");
            }

            Trainer trainer = trainerOpt.get();

            List<Training> trainings;
            if (periodFrom != null && periodTo != null) {
                trainings = trainingService.findTrainerTrainingsByDateRange(
                        username, trainer.getId(), periodFrom, periodTo
                );
            } else {
                trainings = trainingService.findTrainingsByTrainerId(
                        username, trainer.getId()
                );
            }

            if (traineeName != null && !traineeName.isEmpty()) {
                trainings = filterByTraineeName(trainings, traineeName);
            }

            List<TrainingResponse> response = trainings.stream()
                    .map(t -> {
                        Optional<Trainee> trainee = traineeService.findTraineeById(t.getTraineeId());
                        String traineeFullName = trainee.map(tr -> tr.getFirstName() + " " + tr.getLastName())
                                .orElse("Unknown");
                        return new TrainingResponse(
                                t.getTrainingName(),
                                t.getTrainingDate(),
                                t.getTrainingTypeName(),
                                t.getTrainingDuration(),
                                traineeFullName
                        );
                    })
                    .collect(Collectors.toList());

            logger.info("Retrieved {} trainings for trainer: {}", response.size(), username);
            return ResponseEntity.ok(response);

        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/training-types")
    @Operation(summary = "Get training types", description = "Get all available training types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training types retrieved successfully")
    })
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        logger.info("Getting all training types");

        List<TrainingType> types = trainingTypeDao.findAll();

        List<TrainingTypeResponse> response = types.stream()
                .map(t -> new TrainingTypeResponse(t.getId(), t.getTrainingTypeName()))
                .collect(Collectors.toList());

        logger.info("Retrieved {} training types", response.size());
        return ResponseEntity.ok(response);
    }

    private List<Training> filterByTrainerName(List<Training> trainings, String trainerName) {
        return trainings.stream()
                .filter(t -> {
                    Optional<Trainer> trainer = trainerService.findTrainerById(t.getTrainerId());
                    if (trainer.isPresent()) {
                        String fullName = trainer.get().getFirstName() + " " + trainer.get().getLastName();
                        return fullName.toLowerCase().contains(trainerName.toLowerCase());
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    private List<Training> filterByTraineeName(List<Training> trainings, String traineeName) {
        return trainings.stream()
                .filter(t -> {
                    Optional<Trainee> trainee = traineeService.findTraineeById(t.getTraineeId());
                    if (trainee.isPresent()) {
                        String fullName = trainee.get().getFirstName() + " " + trainee.get().getLastName();
                        return fullName.toLowerCase().contains(traineeName.toLowerCase());
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}