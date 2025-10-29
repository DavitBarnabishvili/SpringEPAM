package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeTrainerAssignmentDao;
import com.gym.crm.entity.TraineeTrainerAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TraineeTrainerAssignmentDaoImpl implements TraineeTrainerAssignmentDao {

    private static final Logger logger = LoggerFactory.getLogger(TraineeTrainerAssignmentDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TraineeTrainerAssignment create(TraineeTrainerAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }

        logger.debug("Creating assignment between trainee {} and trainer {}",
                assignment.getTraineeId(), assignment.getTrainerId());

        if (existsByTraineeIdAndTrainerId(assignment.getTraineeId(), assignment.getTrainerId())) {
            logger.warn("Assignment already exists between trainee {} and trainer {}",
                    assignment.getTraineeId(), assignment.getTrainerId());
            return findByTraineeIdAndTrainerId(assignment.getTraineeId(), assignment.getTrainerId()).get();
        }

        entityManager.persist(assignment);
        entityManager.flush();

        logger.info("Successfully created assignment with id: {} (trainee: {}, trainer: {})",
                assignment.getId(), assignment.getTraineeId(), assignment.getTrainerId());

        return assignment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraineeTrainerAssignment> findByTraineeId(Long traineeId) {
        if (traineeId == null) {
            return List.of();
        }

        logger.debug("Finding assignments for trainee: {}", traineeId);

        TypedQuery<TraineeTrainerAssignment> query = entityManager.createQuery(
                "SELECT a FROM TraineeTrainerAssignment a WHERE a.traineeId = :traineeId",
                TraineeTrainerAssignment.class);
        query.setParameter("traineeId", traineeId);

        List<TraineeTrainerAssignment> assignments = query.getResultList();
        logger.debug("Found {} assignments for trainee: {}", assignments.size(), traineeId);

        return assignments;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraineeTrainerAssignment> findByTrainerId(Long trainerId) {
        if (trainerId == null) {
            return List.of();
        }

        logger.debug("Finding assignments for trainer: {}", trainerId);

        TypedQuery<TraineeTrainerAssignment> query = entityManager.createQuery(
                "SELECT a FROM TraineeTrainerAssignment a WHERE a.trainerId = :trainerId",
                TraineeTrainerAssignment.class);
        query.setParameter("trainerId", trainerId);

        List<TraineeTrainerAssignment> assignments = query.getResultList();
        logger.debug("Found {} assignments for trainer: {}", assignments.size(), trainerId);

        return assignments;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeTrainerAssignment> findByTraineeIdAndTrainerId(Long traineeId, Long trainerId) {
        if (traineeId == null || trainerId == null) {
            return Optional.empty();
        }

        logger.debug("Finding assignment between trainee {} and trainer {}", traineeId, trainerId);

        try {
            TypedQuery<TraineeTrainerAssignment> query = entityManager.createQuery(
                    "SELECT a FROM TraineeTrainerAssignment a WHERE a.traineeId = :traineeId AND a.trainerId = :trainerId",
                    TraineeTrainerAssignment.class);
            query.setParameter("traineeId", traineeId);
            query.setParameter("trainerId", trainerId);

            TraineeTrainerAssignment assignment = query.getSingleResult();
            logger.debug("Found assignment between trainee {} and trainer {}", traineeId, trainerId);
            return Optional.of(assignment);

        } catch (NoResultException e) {
            logger.debug("No assignment found between trainee {} and trainer {}", traineeId, trainerId);
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }

        logger.debug("Deleting assignment with id: {}", id);

        TraineeTrainerAssignment assignment = entityManager.find(TraineeTrainerAssignment.class, id);
        if (assignment == null) {
            logger.debug("No assignment found with id: {}", id);
            return false;
        }

        entityManager.remove(assignment);
        logger.info("Successfully deleted assignment with id: {}", id);
        return true;
    }

    @Override
    public int deleteByTraineeId(Long traineeId) {
        if (traineeId == null) {
            return 0;
        }

        logger.debug("Deleting all assignments for trainee: {}", traineeId);

        int deletedCount = entityManager.createQuery(
                        "DELETE FROM TraineeTrainerAssignment a WHERE a.traineeId = :traineeId")
                .setParameter("traineeId", traineeId)
                .executeUpdate();

        logger.info("Deleted {} assignments for trainee: {}", deletedCount, traineeId);
        return deletedCount;
    }

    @Override
    public int deleteByTrainerId(Long trainerId) {
        if (trainerId == null) {
            return 0;
        }

        logger.debug("Deleting all assignments for trainer: {}", trainerId);

        int deletedCount = entityManager.createQuery(
                        "DELETE FROM TraineeTrainerAssignment a WHERE a.trainerId = :trainerId")
                .setParameter("trainerId", trainerId)
                .executeUpdate();

        logger.info("Deleted {} assignments for trainer: {}", deletedCount, trainerId);
        return deletedCount;
    }

    @Override
    public boolean deleteByTraineeIdAndTrainerId(Long traineeId, Long trainerId) {
        if (traineeId == null || trainerId == null) {
            return false;
        }

        logger.debug("Deleting assignment between trainee {} and trainer {}", traineeId, trainerId);

        int deletedCount = entityManager.createQuery(
                        "DELETE FROM TraineeTrainerAssignment a WHERE a.traineeId = :traineeId AND a.trainerId = :trainerId")
                .setParameter("traineeId", traineeId)
                .setParameter("trainerId", trainerId)
                .executeUpdate();

        if (deletedCount > 0) {
            logger.info("Successfully deleted assignment between trainee {} and trainer {}", traineeId, trainerId);
            return true;
        } else {
            logger.debug("No assignment found to delete between trainee {} and trainer {}", traineeId, trainerId);
            return false;
        }
    }

    @Override
    @Transactional
    public List<TraineeTrainerAssignment> replaceTraineeAssignments(Long traineeId, List<Long> trainerIds) {
        if (traineeId == null) {
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }

        logger.info("Replacing trainer assignments for trainee: {}", traineeId);

        // Delete existing assignments
        int deletedCount = deleteByTraineeId(traineeId);
        logger.debug("Deleted {} existing assignments for trainee: {}", deletedCount, traineeId);

        // Create new assignments
        List<TraineeTrainerAssignment> newAssignments = new ArrayList<>();
        if (trainerIds != null) {
            for (Long trainerId : trainerIds) {
                if (trainerId != null) {
                    TraineeTrainerAssignment assignment = new TraineeTrainerAssignment(
                            traineeId, trainerId, LocalDate.now());
                    newAssignments.add(create(assignment));
                }
            }
        }

        logger.info("Created {} new assignments for trainee: {}", newAssignments.size(), traineeId);
        return newAssignments;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTraineeIdAndTrainerId(Long traineeId, Long trainerId) {
        if (traineeId == null || trainerId == null) {
            return false;
        }

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(a) FROM TraineeTrainerAssignment a WHERE a.traineeId = :traineeId AND a.trainerId = :trainerId",
                Long.class);
        query.setParameter("traineeId", traineeId);
        query.setParameter("trainerId", trainerId);

        Long count = query.getSingleResult();
        return count != null && count > 0;
    }
}