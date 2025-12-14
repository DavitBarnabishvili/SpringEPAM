package com.gym.crm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequest {

    @NotBlank(message = "Trainer username is required")
    @Size(min = 3, max = 50, message = "Trainer username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Trainer username must contain only alphanumeric characters, dots, underscores, or hyphens")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    @Size(min = 1, max = 50, message = "Trainer first name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Trainer first name must contain only letters")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    @Size(min = 1, max = 50, message = "Trainer last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Trainer last name must contain only letters")
    private String trainerLastName;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    @PastOrPresent(message = "Training date cannot be in the future")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Min(value = 1, message = "Training duration must be at least 1 minute")
    @Max(value = 480, message = "Training duration cannot exceed 480 minutes (8 hours)")
    private Integer trainingDuration;

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    private String transactionId; // For end-to-end tracing across services

    public enum ActionType {
        ADD,
        DELETE
    }
}