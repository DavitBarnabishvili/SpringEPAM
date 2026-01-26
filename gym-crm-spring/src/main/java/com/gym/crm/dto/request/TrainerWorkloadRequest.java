package com.gym.crm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequest {

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    private String trainerLastName;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    private Integer trainingDuration;

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    public enum ActionType {
        ADD,
        DELETE
    }
}