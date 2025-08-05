package com.gym.crm.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Trainer")
public class Trainer extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "UserId", nullable = false)
    private Long userId;

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

    // Full constructor - for internal use
    public Trainer(Long userId, String firstName, String lastName, String username,
                   String password, Boolean isActive, TrainingType specialization) {
        super(firstName, lastName, username, password, isActive);
        setUserId(userId);
        setSpecialization(specialization);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
        Trainer trainer = (Trainer) o;

        if (this.getId() != null && trainer.getId() != null) {
            return Objects.equals(getId(), trainer.getId());
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }

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