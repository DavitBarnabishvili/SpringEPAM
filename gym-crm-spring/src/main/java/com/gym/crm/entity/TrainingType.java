package com.gym.crm.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "TrainingType")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "Training Type Name", nullable = false)
    private String trainingTypeName;

    public TrainingType() {
    }

    public TrainingType(String trainingTypeName) {
        setTrainingTypeName(trainingTypeName);
    }

    public TrainingType(Long id, String trainingTypeName) {
        setId(id);
        setTrainingTypeName(trainingTypeName);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName != null ? trainingTypeName.trim() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingType that = (TrainingType) o;

        if (this.id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }

        if (this.trainingTypeName != null && that.trainingTypeName != null) {
            return Objects.equals(trainingTypeName, that.trainingTypeName);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (trainingTypeName != null) {
            return trainingTypeName.hashCode();
        }
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TrainingType{" +
                "trainingTypeName='" + trainingTypeName + '\'' +
                '}';
    }
}