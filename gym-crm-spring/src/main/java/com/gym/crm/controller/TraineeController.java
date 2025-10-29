package com.gym.crm.controller;

import com.gym.crm.dao.TraineeTrainerAssignmentDao;
import com.gym.crm.dto.request.TraineeRegistrationRequest;
import com.gym.crm.dto.request.TraineeUpdateRequest;
import com.gym.crm.dto.request.UpdateTrainerListRequest;
import com.gym.crm.dto.response.RegistrationResponse;
import com.gym.crm.dto.response.TraineeProfileResponse;
import com.gym.crm.dto.response.TraineeProfileUpdateResponse;
import com.gym.crm.dto.response.TrainerSummary;
import com.gym.crm.entity.*;
import com.gym.crm.exception.UnauthorizedAccessException;
import com.gym.crm.exception.UserNotFoundException;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
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
import com.gym.crm.service.impl.CustomMetricsService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee Management", description = "Trainee registration and profile management")
public class TraineeController {

    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TraineeTrainerAssignmentDao assignmentDao;
    private final CustomMetricsService metricsService;

    public TraineeController(TraineeService traineeService,
                             TrainerService trainerService,
                             TraineeTrainerAssignmentDao assignmentDao,
                             CustomMetricsService metricsService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.assignmentDao = assignmentDao;
        this.metricsService = metricsService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new trainee", description = "Create a new trainee profile with auto-generated credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Registering new trainee: {} {}", request.getFirstName(), request.getLastName());

        try {
            Trainee trainee = new Trainee(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getDateOfBirth(),
                    request.getAddress()
            );

            Trainee created = traineeService.createTrainee(trainee);
            metricsService.incrementTraineeRegistration();

            RegistrationResponse response = new RegistrationResponse(
                    created.getUsername(),
                    created.getPassword()
            );

            logger.info("Trainee registered successfully with username: {}", created.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/profile/{username}")
    @Operation(summary = "Get trainee profile", description = "Retrieve trainee profile by username")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @PathVariable String username,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Fetching profile for trainee: {}", username);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");
            String authenticatedRole = (String) request.getAttribute("authenticatedRole");

            // Users can only view their own profile
            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to view profile of {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only view your own profile");
            }

            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(username);
            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found");
            }

            Trainee trainee = traineeOpt.get();

            List<TrainerSummary> trainers = assignmentDao.findByTraineeId(trainee.getId()).stream()
                    .map(assignment -> trainerService.findTrainerById(assignment.getTrainerId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(trainer -> new TrainerSummary(
                            trainer.getUsername(),
                            trainer.getFirstName(),
                            trainer.getLastName(),
                            trainer.getSpecializationName()
                    ))
                    .collect(Collectors.toList());

            TraineeProfileResponse response = new TraineeProfileResponse(
                    trainee.getFirstName(),
                    trainee.getLastName(),
                    trainee.getDateOfBirth(),
                    trainee.getAddress(),
                    trainee.getIsActive(),
                    trainers
            );

            logger.info("Profile retrieved successfully for trainee: {}", username);
            return ResponseEntity.ok(response);

        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/profile/{username}")
    @Operation(summary = "Update trainee profile", description = "Update trainee profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TraineeProfileUpdateResponse> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest updateRequest,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Updating profile for trainee: {}", username);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to update profile of {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only update your own profile");
            }

            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(username);
            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found");
            }

            Trainee trainee = traineeOpt.get();
            trainee.setFirstName(updateRequest.getFirstName());
            trainee.setLastName(updateRequest.getLastName());
            trainee.setDateOfBirth(updateRequest.getDateOfBirth());
            trainee.setAddress(updateRequest.getAddress());
            trainee.setIsActive(updateRequest.getIsActive());

            // Note: I'm passing dummy auth credentials since user is already authenticated via JWT
            //service needs refactoring, but it works
            Trainee updated = traineeService.updateTrainee(authenticatedUsername, "JWT_AUTH", trainee);

            List<TrainerSummary> trainers = assignmentDao.findByTraineeId(updated.getId()).stream()
                    .map(assignment -> trainerService.findTrainerById(assignment.getTrainerId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(trainer -> new TrainerSummary(
                            trainer.getUsername(),
                            trainer.getFirstName(),
                            trainer.getLastName(),
                            trainer.getSpecializationName()
                    ))
                    .collect(Collectors.toList());

            TraineeProfileUpdateResponse response = new TraineeProfileUpdateResponse(
                    updated.getUsername(),
                    updated.getFirstName(),
                    updated.getLastName(),
                    updated.getDateOfBirth(),
                    updated.getAddress(),
                    updated.getIsActive(),
                    trainers
            );

            logger.info("Profile updated successfully for trainee: {}", username);
            return ResponseEntity.ok(response);

        } finally {
            MDC.clear();
        }
    }

    @DeleteMapping("/profile/{username}")
    @Operation(summary = "Delete trainee profile", description = "Delete trainee profile and cascade delete trainings")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteProfile(
            @PathVariable String username,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Deleting profile for trainee: {}", username);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to delete profile of {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only delete your own profile");
            }

            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(username);
            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found");
            }

            // Delete assignments first
            assignmentDao.deleteByTraineeId(traineeOpt.get().getId());

            // Then delete trainee (this will cascade delete trainings)
            boolean deleted = traineeService.deleteTrainee(authenticatedUsername, "JWT_AUTH", traineeOpt.get().getId());

            if (deleted) {
                logger.info("Profile deleted successfully for trainee: {}", username);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }

        } finally {
            MDC.clear();
        }
    }

    @PatchMapping("/profile/{username}/activate")
    @Operation(summary = "Activate/Deactivate trainee", description = "Change trainee active status")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> changeActiveStatus(
            @PathVariable String username,
            @RequestParam boolean isActive,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Changing active status for trainee: {} to: {}", username, isActive);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to change status of {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only modify your own profile");
            }

            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(username);
            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found");
            }

            boolean result;
            if (isActive) {
                result = traineeService.activateTrainee(authenticatedUsername, "JWT_AUTH", traineeOpt.get().getId());
            } else {
                result = traineeService.deactivateTrainee(authenticatedUsername, "JWT_AUTH", traineeOpt.get().getId());
            }

            logger.info("Active status changed to {} for trainee: {}", isActive, username);
            return ResponseEntity.ok().build();

        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{username}/not-assigned-trainers")
    @Operation(summary = "Get not assigned active trainers", description = "Get list of active trainers not assigned to the trainee")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<TrainerSummary>> getNotAssignedActiveTrainers(
            @PathVariable String username,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Getting not assigned active trainers for trainee: {}", username);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to view trainers for {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only view your own trainer options");
            }

            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(username);
            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found");
            }

            Trainee trainee = traineeOpt.get();

            Set<Long> assignedTrainerIds = assignmentDao.findByTraineeId(trainee.getId()).stream()
                    .map(TraineeTrainerAssignment::getTrainerId)
                    .collect(Collectors.toSet());

            List<TrainerSummary> notAssignedTrainers = trainerService.findAllTrainers().stream()
                    .filter(User::getIsActive)
                    .filter(trainer -> !assignedTrainerIds.contains(trainer.getId()))
                    .map(trainer -> new TrainerSummary(
                            trainer.getUsername(),
                            trainer.getFirstName(),
                            trainer.getLastName(),
                            trainer.getSpecializationName()
                    ))
                    .collect(Collectors.toList());

            logger.info("Found {} not assigned active trainers for trainee: {}",
                    notAssignedTrainers.size(), username);
            return ResponseEntity.ok(notAssignedTrainers);

        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's trainer list", description = "Update the list of trainers assigned to a trainee")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer list updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<TrainerSummary>> updateTrainerList(
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerListRequest updateRequest,
            HttpServletRequest request) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Updating trainer list for trainee: {}", username);

        try {
            String authenticatedUsername = (String) request.getAttribute("authenticatedUsername");

            if (!username.equals(authenticatedUsername)) {
                logger.warn("Access denied: {} attempted to update trainers for {}",
                        authenticatedUsername, username);
                throw new UnauthorizedAccessException("You can only update your own trainer list");
            }

            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(username);
            if (traineeOpt.isEmpty()) {
                throw new UserNotFoundException("Trainee not found");
            }

            Trainee trainee = traineeOpt.get();

            List<Long> trainerIds = new ArrayList<>();
            List<Trainer> newTrainers = new ArrayList<>();

            for (String trainerUsername : updateRequest.getTrainerUsernames()) {
                Optional<Trainer> trainerOpt = trainerService.findTrainerByUsername(trainerUsername);
                if (trainerOpt.isEmpty()) {
                    throw new UserNotFoundException("Trainer not found: " + trainerUsername);
                }
                Trainer trainer = trainerOpt.get();
                trainerIds.add(trainer.getId());
                newTrainers.add(trainer);
            }

            assignmentDao.replaceTraineeAssignments(trainee.getId(), trainerIds);

            List<TrainerSummary> updatedTrainers = newTrainers.stream()
                    .map(trainer -> new TrainerSummary(
                            trainer.getUsername(),
                            trainer.getFirstName(),
                            trainer.getLastName(),
                            trainer.getSpecializationName()
                    ))
                    .collect(Collectors.toList());

            logger.info("Updated trainer list for trainee: {} with {} trainers",
                    username, updatedTrainers.size());
            return ResponseEntity.ok(updatedTrainers);

        } finally {
            MDC.clear();
        }
    }
}