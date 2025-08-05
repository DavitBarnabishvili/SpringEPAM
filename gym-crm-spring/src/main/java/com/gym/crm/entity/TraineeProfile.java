package com.gym.crm.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


public class TraineeProfile {

    private final Trainee trainee;
    private final List<Training> trainings;

    public TraineeProfile(Trainee trainee, List<Training> trainings) {
        this.trainee = Objects.requireNonNull(trainee, "Trainee cannot be null");
        this.trainings = trainings != null ? List.copyOf(trainings) : List.of();
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public List<Training> getTrainings() {
        return trainings;
    }

    public int getTotalTrainings() {
        return trainings.size();
    }

    public long getTotalTrainingDuration() {
        return trainings.stream()
                .filter(training -> training.getTrainingDuration() != null)
                .mapToLong(Training::getTrainingDuration)
                .sum();
    }

    public boolean isActive() {
        return trainee.isActive();
    }

    public Integer getAge() {
        return trainee.getAge();
    }

    public LocalDate getDateOfBirth() {
        return trainee.getDateOfBirth();
    }

    public String getAddress() {
        return trainee.getAddress();
    }

    public List<Training> getUpcomingTrainings() {
        LocalDate today = LocalDate.now();
        return trainings.stream()
                .filter(training -> training.getTrainingDate() != null &&
                        training.getTrainingDate().isAfter(today))
                .toList();
    }

    public List<Training> getPastTrainings() {
        LocalDate today = LocalDate.now();
        return trainings.stream()
                .filter(training -> training.getTrainingDate() != null &&
                        training.getTrainingDate().isBefore(today))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraineeProfile that = (TraineeProfile) o;
        return Objects.equals(trainee, that.trainee) &&
                Objects.equals(trainings, that.trainings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainee, trainings);
    }

    @Override
    public String toString() {
        return "TraineeProfile{" +
                "trainee=" + trainee.getFullName() +
                ", age=" + getAge() +
                ", totalTrainings=" + getTotalTrainings() +
                ", upcomingTrainings=" + getUpcomingTrainings().size() +
                ", totalDuration=" + getTotalTrainingDuration() + " minutes" +
                '}';
    }
}