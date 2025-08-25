package com.gym.crm.dao;

import com.gym.crm.entity.TraineeTrainerAssignment;
import java.util.List;
import java.util.Optional;

public interface TraineeTrainerAssignmentDao {

    /**
     * Creates a new trainee-trainer assignment.
     */
    TraineeTrainerAssignment create(TraineeTrainerAssignment assignment);

    /**
     * Finds all assignments for a specific trainee.
     */
    List<TraineeTrainerAssignment> findByTraineeId(Long traineeId);

    /**
     * Finds all assignments for a specific trainer.
     */
    List<TraineeTrainerAssignment> findByTrainerId(Long trainerId);

    /**
     * Finds a specific assignment between trainee and trainer.
     */
    Optional<TraineeTrainerAssignment> findByTraineeIdAndTrainerId(Long traineeId, Long trainerId);

    /**
     * Deletes a specific assignment.
     */
    boolean delete(Long id);

    /**
     * Deletes all assignments for a specific trainee.
     */
    int deleteByTraineeId(Long traineeId);

    /**
     * Deletes all assignments for a specific trainer.
     */
    int deleteByTrainerId(Long trainerId);

    /**
     * Deletes a specific trainee-trainer assignment.
     */
    boolean deleteByTraineeIdAndTrainerId(Long traineeId, Long trainerId);

    /**
     * Replaces all trainer assignments for a trainee.
     * Deletes existing assignments and creates new ones.
     */
    List<TraineeTrainerAssignment> replaceTraineeAssignments(Long traineeId, List<Long> trainerIds);

    /**
     * Checks if an assignment exists.
     */
    boolean existsByTraineeIdAndTrainerId(Long traineeId, Long trainerId);
}