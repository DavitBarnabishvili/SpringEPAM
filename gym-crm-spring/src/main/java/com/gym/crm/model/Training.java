package com.gym.crm.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name = "Training")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "Trainee Id", nullable = false)
    private Long traineeId;

    @Column(name = "Trainer Id", nullable = false)
    private Long trainerId;

    @Column(name = "Training Name", nullable = false)
    private String trainingName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Training Type Id", referencedColumnName = "ID", nullable = false)
    private TrainingType trainingType;

    @Column(name = "Training Date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "Training Duration", nullable = false)
    private Integer trainingDuration;

    public Training() {
    }

    public Training(Long traineeUserId, Long trainerUserId, String trainingName,
                    TrainingType trainingType, LocalDate trainingDate, Integer trainingDuration) {
        setTraineeId(traineeUserId);
        setTrainerId(trainerUserId);
        setTrainingName(trainingName);
        setTrainingType(trainingType);
        setTrainingDate(trainingDate);
        setTrainingDuration(trainingDuration);
    }

    // Full constructor
    public Training(Long id, Long traineeUserId, Long trainerUserId, String trainingName,
                    TrainingType trainingType, LocalDate trainingDate, Integer trainingDuration) {
        setId(id);
        setTraineeId(traineeUserId);
        setTrainerId(trainerUserId);
        setTrainingName(trainingName);
        setTrainingType(trainingType);
        setTrainingDate(trainingDate);
        setTrainingDuration(trainingDuration);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(Long traineeId) {
        this.traineeId = traineeId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName != null ? trainingName.trim() : null;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public String getTrainingTypeName() {
        return trainingType != null ? trainingType.getTrainingTypeName() : "Unknown";
    }

    public String getFormattedDate() {
        return trainingDate != null ? trainingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "No Date";
    }

    public String getFormattedDuration() {
        if (trainingDuration == null || trainingDuration <= 0) {
            return "No Duration";
        }
        int hours = trainingDuration / 60;
        int minutes = trainingDuration % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    public boolean isInPast() {
        return trainingDate != null && trainingDate.isBefore(LocalDate.now());
    }

    public boolean isInFuture() {
        return trainingDate != null && trainingDate.isAfter(LocalDate.now());
    }

    public boolean isToday() {
        return Objects.equals(trainingDate, LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Training training = (Training) o;

        if (this.id != null && training.id != null) {
            return Objects.equals(id, training.id);
        }

        return Objects.equals(traineeId, training.traineeId) &&
                Objects.equals(trainerId, training.trainerId) &&
                Objects.equals(trainingDate, training.trainingDate) &&
                Objects.equals(trainingName, training.trainingName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId, trainerId, trainingDate, trainingName);
    }

    @Override
    public String toString() {
        return "Training{" +
                "trainingName='" + trainingName + '\'' +
                ", traineeId=" + traineeId +
                ", trainerId=" + trainerId +
                ", trainingType=" + getTrainingTypeName() +
                ", trainingDate=" + getFormattedDate() +
                ", duration=" + getFormattedDuration() +
                '}';
    }
}