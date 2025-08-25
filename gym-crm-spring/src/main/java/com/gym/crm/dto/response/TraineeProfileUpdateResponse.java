package com.gym.crm.dto.response;

import java.time.LocalDate;
import java.util.List;

public class TraineeProfileUpdateResponse {
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;
    private List<TrainerSummary> trainers;

    public TraineeProfileUpdateResponse() {}

    public TraineeProfileUpdateResponse(String firstName, String lastName,
                                  LocalDate dateOfBirth, String address,
                                  Boolean isActive, List<TrainerSummary> trainers) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.isActive = isActive;
        this.trainers = trainers;
    }

    public TraineeProfileUpdateResponse(String username, String firstName, String lastName,
                                  LocalDate dateOfBirth, String address,
                                  Boolean isActive, List<TrainerSummary> trainers) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.isActive = isActive;
        this.trainers = trainers;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<TrainerSummary> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<TrainerSummary> trainers) {
        this.trainers = trainers;
    }
}