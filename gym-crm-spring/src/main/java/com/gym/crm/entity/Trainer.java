package com.gym.crm.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Trainer")
public class Trainer extends User {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Specialization", referencedColumnName = "ID", nullable = false)
    private TrainingType specialization;

    public Trainer() {
        super();
    }

    public Trainer(String firstName, String lastName) {
        super(firstName, lastName);
    }

    public Trainer(String firstName, String lastName, TrainingType specialization) {
        super(firstName, lastName);
        setSpecialization(specialization);
    }

    public Trainer(String firstName, String lastName, String username,
                   String password, Boolean isActive, TrainingType specialization) {
        super(firstName, lastName, username, password, isActive);
        setSpecialization(specialization);
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public String getSpecializationName() {
        return specialization != null ? specialization.getTrainingTypeName() : "No Specialization";
    }

    public boolean hasSpecialization() {
        return specialization != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", specialization='" + getSpecializationName() + '\'' +
                ", isActive=" + getIsActive() +
                '}';
    }
}