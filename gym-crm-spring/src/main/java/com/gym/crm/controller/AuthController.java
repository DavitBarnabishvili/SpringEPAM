package com.gym.crm.controller;

import com.gym.crm.dto.request.ChangeLoginRequest;
import com.gym.crm.dto.response.LoginResponse;
import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.exception.InvalidCredentialsException;
import com.gym.crm.exception.UserNotFoundException;
import com.gym.crm.security.BruteForceProtectionService;
import com.gym.crm.security.JwtTokenInvalidationService;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.util.AuthenticationService;
import com.gym.crm.util.impl.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;
import com.gym.crm.service.impl.CustomMetricsService;
import io.micrometer.core.instrument.Timer;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and password management endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final JwtUtil jwtUtil;
    private final CustomMetricsService metricsService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final JwtTokenInvalidationService jwtTokenInvalidationService;

    public AuthController(AuthenticationService authenticationService,
                          TraineeService traineeService,
                          TrainerService trainerService,
                          JwtUtil jwtUtil,
                          CustomMetricsService metricsService,
                          BruteForceProtectionService bruteForceProtectionService,
                          JwtTokenInvalidationService jwtTokenInvalidationService) {
        this.authenticationService = authenticationService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.jwtUtil = jwtUtil;
        this.metricsService = metricsService;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.jwtTokenInvalidationService = jwtTokenInvalidationService;
    }

    @GetMapping("/login")
    @Operation(summary = "User login", description = "Authenticate trainee or trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account inactive"),
            @ApiResponse(responseCode = "423", description = "Account locked due to too many failed attempts")
    })
    public ResponseEntity<LoginResponse> login(
            @RequestParam String username,
            @RequestParam String password) {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        Timer.Sample authTimer = metricsService.startAuthenticationTimer();
        metricsService.incrementLoginAttempt();

        logger.info("Login attempt for username: {}", username);

        if (bruteForceProtectionService.isBlocked(username)) {
            long remainingMinutes = bruteForceProtectionService.getRemainingLockTimeMinutes(username);
            String message = String.format("Log In functionality is locked due to too many failed attempts. Try again in %d minutes.", remainingMinutes);
            logger.warn("Blocked login attempt for user: {} - {}", username, message);
            throw new LockedException(message);
        }

        try {
            String role;
            Long userId;

            try {
                Trainee trainee = authenticationService.authenticateTrainee(username, password);
                role = "TRAINEE";
                userId = trainee.getId();
            } catch (InvalidCredentialsException e) {
                Trainer trainer = authenticationService.authenticateTrainer(username, password);
                role = "TRAINER";
                userId = trainer.getId();
            }

            String token = jwtUtil.generateToken(username, role, userId);
            LoginResponse response = new LoginResponse(token);

            bruteForceProtectionService.loginSucceeded(username);
            metricsService.incrementLoginSuccess();

            logger.info("{} login successful for: {}", role, username);
            return ResponseEntity.ok(response);

        } catch (InvalidCredentialsException e) {
            bruteForceProtectionService.loginFailed(username);
            logger.warn("Login failed for username: {} - {}", username, e.getMessage());
            throw e;
        } finally {
            metricsService.stopAuthenticationTimer(authTimer);
            MDC.clear();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate JWT token")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @ApiResponse(responseCode = "401", description = "Invalid token")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtTokenInvalidationService.invalidateToken(token);

            String username = jwtUtil.extractUsername(token);
            logger.info("User {} logged out successfully", username);
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-login")
    @Operation(summary = "Change login", description = "Change user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequest request, HttpServletRequest httpRequest) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        String authenticatedUsername = (String) httpRequest.getAttribute("authenticatedUsername");
        if (authenticatedUsername == null) {
            logger.warn("Authentication principal not found for password change request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("Password change request for username: {}", request.getUsername());

        if (!request.getUsername().equals(authenticatedUsername)) {
            logger.warn("Unauthorized attempt to change password for user: {} by authenticated user: {}", request.getUsername(), authenticatedUsername);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Trainee> traineeOpt = traineeService.findTraineeByUsername(request.getUsername());
            Optional<Trainer> trainerOpt = trainerService.findTrainerByUsername(request.getUsername());

            if (traineeOpt.isPresent()) {
                traineeService.changePassword(traineeOpt.get(), request);
                logger.info("Password changed successfully for trainee: {}", request.getUsername());
            } else if (trainerOpt.isPresent()) {
                trainerService.changePassword(trainerOpt.get(), request);
                logger.info("Password changed successfully for trainer: {}", request.getUsername());
            } else {
                logger.warn("User not found for password change: {}", request.getUsername());
                throw new UserNotFoundException("User not found");
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Password change failed for username: {} - {}", request.getUsername(), e.getMessage());
            throw e;
        } finally {
            MDC.clear();
        }
    }
}