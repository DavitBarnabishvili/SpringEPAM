package com.gym.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TrainerRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @NotNull(message = "Specialization is required")
    private Long specializationId; // TrainingType ID

    public TrainerRegistrationRequest() {}

    public TrainerRegistrationRequest(String firstName, String lastName, Long specializationId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specializationId = specializationId;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getSpecializationId() { return specializationId; }
    public void setSpecializationId(Long specializationId) { this.specializationId = specializationId; }
}