package com.gym.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "trainee_trainer_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trainee_id", "trainer_id"}))
public class TraineeTrainerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "trainee_id", nullable = false)
    private Long traineeId;

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    public TraineeTrainerAssignment() {
        this.assignedDate = LocalDate.now();
    }

    public TraineeTrainerAssignment(Long traineeId, Long trainerId) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.assignedDate = LocalDate.now();
    }

    public TraineeTrainerAssignment(Long traineeId, Long trainerId, LocalDate assignedDate) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.assignedDate = assignedDate;
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

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraineeTrainerAssignment that = (TraineeTrainerAssignment) o;
        return Objects.equals(traineeId, that.traineeId) &&
                Objects.equals(trainerId, that.trainerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId, trainerId);
    }

    @Override
    public String toString() {
        return "TraineeTrainerAssignment{" +
                "id=" + id +
                ", traineeId=" + traineeId +
                ", trainerId=" + trainerId +
                ", assignedDate=" + assignedDate +
                '}';
    }
}