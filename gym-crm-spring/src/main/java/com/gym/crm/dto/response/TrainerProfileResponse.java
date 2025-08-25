package com.gym.crm.dto.response;

import java.util.List;

public class TrainerProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;
    private List<TraineeSummary> trainees;

    public TrainerProfileResponse() {}

    public TrainerProfileResponse(String firstName, String lastName, String specialization,
                                  Boolean isActive, List<TraineeSummary> trainees) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.isActive = isActive;
        this.trainees = trainees;
    }

    public TrainerProfileResponse(String username, String firstName, String lastName,
                                  String specialization, Boolean isActive, List<TraineeSummary> trainees) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.isActive = isActive;
        this.trainees = trainees;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public List<TraineeSummary> getTrainees() { return trainees; }
    public void setTrainees(List<TraineeSummary> trainees) { this.trainees = trainees; }
}