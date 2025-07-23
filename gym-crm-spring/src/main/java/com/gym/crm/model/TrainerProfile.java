package com.gym.crm.model;

import java.util.List;
import java.util.Objects;

public class TrainerProfile {

    private final Trainer trainer;
    private final List<Training> trainings;

    public TrainerProfile(Trainer trainer, List<Training> trainings) {
        this.trainer = Objects.requireNonNull(trainer, "Trainer cannot be null");
        this.trainings = trainings != null ? List.copyOf(trainings) : List.of();
    }

    public Trainer getTrainer() {
        return trainer;
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
        return trainer.isActive();
    }

    public String getSpecializationName() {
        return trainer.getSpecializationName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainerProfile that = (TrainerProfile) o;
        return Objects.equals(trainer, that.trainer) &&
                Objects.equals(trainings, that.trainings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainer, trainings);
    }

    @Override
    public String toString() {
        return "TrainerProfile{" +
                "trainer=" + trainer.getFullName() +
                ", totalTrainings=" + getTotalTrainings() +
                ", totalDuration=" + getTotalTrainingDuration() + " minutes" +
                ", specialization=" + getSpecializationName() +
                '}';
    }
}