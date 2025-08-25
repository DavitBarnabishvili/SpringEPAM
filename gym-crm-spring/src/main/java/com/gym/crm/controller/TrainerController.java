package com.gym.crm.controller;

import com.gym.crm.dao.TraineeTrainerAssignmentDao;
import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.dto.request.TrainerRegistrationRequest;
import com.gym.crm.dto.request.TrainerUpdateRequest;
import com.gym.crm.dto.response.RegistrationResponse;
import com.gym.crm.dto.response.TrainerProfileResponse;
import com.gym.crm.dto.response.TraineeSummary;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.exception.UnauthorizedAccessException;
import com.gym.crm.exception.UserNotFoundException;
import com.gym.crm.exception.ValidationException;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer Management", description = "Trainer registration and profile management")
public class TrainerController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingTypeDao trainingTypeDao;
    private final TraineeTrainerAssignmentDao assignmentDao;

    public TrainerController(TrainerService trainerService,
                             TraineeService traineeService,
                             TrainingTypeDao trainingTypeDao,
                             TraineeTrainerAssignmentDao assignmentDao) {
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingTypeDao = trainingTypeDao;
        this.assignmentDao = assignmentDao;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new trainer", description = "Create a new trainer profile with auto-generated credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Registering new trainer: {} {}", request.getFirstName(), request.getLastName());

        try {
            Optional<TrainingType> specializationOpt = trainingTypeDao.findById(request.getSpecializationId());
            if (specializationOpt.isEmpty()) {
                throw new ValidationException("Invalid specialization ID");
            }

            Trainer trainer = new Trainer(
                    request.getFirstName(),
                    request.getLastName(),
                    specializationOpt.get()
            );

            Trainer created = trainerService.createTrainer(trainer);

            RegistrationResponse response = new RegistrationResponse(
                    created.getUsername(),
                    created.getPassword()
            );

            logger.info("Trainer registered successfully with username: {}", created.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/profile/{username}")
    @Operation(summary = "Get trainer profile", description = "Retrieve trainer profile by username")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @PathVariable String username,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Fetching profile for trainer: {}", username);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");
            String authenticatedRole = (String) request.getAttribute("authenticatedRole");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to view profile of {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only view your own profile");
            }

            Optional<Trainer> trainerOpt = trainerService.findTrainerByUsername(username);
            if (trainerOpt.isEmpty()) {
                throw new UserNotFoundException("Trainer not found");
            }

            Trainer trainer = trainerOpt.get();

            List<TraineeSummary> trainees = assignmentDao.findByTrainerId(trainer.getId()).stream()
                    .map(assignment -> traineeService.findTraineeById(assignment.getTraineeId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(trainee -> new TraineeSummary(
                            trainee.getUsername(),
                            trainee.getFirstName(),
                            trainee.getLastName()
                    ))
                    .collect(Collectors.toList());

            TrainerProfileResponse response = new TrainerProfileResponse(
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.getSpecializationName(),
                    trainer.getIsActive(),
                    trainees
            );

            logger.info("Profile retrieved successfully for trainer: {}", username);
            return ResponseEntity.ok(response);

        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/profile/{username}")
    @Operation(summary = "Update trainer profile", description = "Update trainer profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequest updateRequest,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Updating profile for trainer: {}", username);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to update profile of {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only update your own profile");
            }

            Optional<Trainer> trainerOpt = trainerService.findTrainerByUsername(username);
            if (trainerOpt.isEmpty()) {
                throw new UserNotFoundException("Trainer not found");
            }

            Trainer trainer = trainerOpt.get();
            trainer.setFirstName(updateRequest.getFirstName());
            trainer.setLastName(updateRequest.getLastName());
            trainer.setIsActive(updateRequest.getIsActive());

            // using dummy auth since user is already authenticated via JWT and service needs refactoring
            Trainer updated = trainerService.updateTrainer(authenticatedUsername, "JWT_AUTH", trainer);

            List<TraineeSummary> trainees = assignmentDao.findByTrainerId(updated.getId()).stream()
                    .map(assignment -> traineeService.findTraineeById(assignment.getTraineeId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(trainee -> new TraineeSummary(
                            trainee.getUsername(),
                            trainee.getFirstName(),
                            trainee.getLastName()
                    ))
                    .collect(Collectors.toList());

            TrainerProfileResponse response = new TrainerProfileResponse(
                    updated.getUsername(),
                    updated.getFirstName(),
                    updated.getLastName(),
                    updated.getSpecializationName(),
                    updated.getIsActive(),
                    trainees
            );

            logger.info("Profile updated successfully for trainer: {}", username);
            return ResponseEntity.ok(response);

        } finally {
            MDC.clear();
        }
    }

    @PatchMapping("/profile/{username}/activate")
    @Operation(summary = "Activate/Deactivate trainer", description = "Change trainer active status")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> changeActiveStatus(
            @PathVariable String username,
            @RequestParam boolean isActive,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Changing active status for trainer: {} to: {}", username, isActive);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to change status of {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only modify your own profile");
            }

            Optional<Trainer> trainerOpt = trainerService.findTrainerByUsername(username);
            if (trainerOpt.isEmpty()) {
                throw new UserNotFoundException("Trainer not found");
            }

            boolean result;
            if (isActive) {
                result = trainerService.activateTrainer(authenticatedUsername, "JWT_AUTH", trainerOpt.get().getId());
            } else {
                result = trainerService.deactivateTrainer(authenticatedUsername, "JWT_AUTH", trainerOpt.get().getId());
            }

            logger.info("Active status changed to {} for trainer: {}", isActive, username);
            return ResponseEntity.ok().build();

        } finally {
            MDC.clear();
        }
    }
}