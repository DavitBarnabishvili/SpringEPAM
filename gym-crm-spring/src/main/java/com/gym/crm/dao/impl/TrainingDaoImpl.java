package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TrainingDaoImpl implements TrainingDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Training create(Training training) {
        if (training == null) {
            throw new IllegalArgumentException("Training cannot be null");
        }

        logger.debug("Creating training: {}", training.getTrainingName());

        entityManager.persist(training);
        entityManager.flush(); // Ensure ID is generated

        logger.info("Successfully created training: '{}' with id: {} (trainee: {}, trainer: {})",
                training.getTrainingName(), training.getId(),
                training.getTraineeId(), training.getTrainerId());

        return training;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> findById(Long id) {
        if (id == null) {
            logger.debug("FindById called with null id");
            return Optional.empty();
        }

        logger.debug("Finding training by id: {}", id);

        Training training = entityManager.find(Training.class, id);

        if (training != null) {
            logger.debug("Found training: '{}'", training.getTrainingName());
            return Optional.of(training);
        } else {
            logger.debug("No training found with id: {}", id);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findAll() {
        logger.debug("Finding all trainings");

        TypedQuery<Training> query = entityManager.createQuery("SELECT t FROM Training t", Training.class);
        List<Training> trainings = query.getResultList();

        logger.debug("Found {} trainings", trainings.size());
        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByTraineeId(Long traineeId) {
        if (traineeId == null) {
            logger.debug("FindByTraineeId called with null traineeId");
            return List.of();
        }

        logger.debug("Finding trainings for trainee: {}", traineeId);

        TypedQuery<Training> query = entityManager.createQuery(
                "SELECT t FROM Training t WHERE t.traineeId = :traineeId", Training.class);
        query.setParameter("traineeId", traineeId);

        List<Training> trainings = query.getResultList();
        logger.debug("Found {} trainings for trainee: {}", trainings.size(), traineeId);

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByTrainerId(Long trainerId) {
        if (trainerId == null) {
            logger.debug("FindByTrainerId called with null trainerId");
            return List.of();
        }

        logger.debug("Finding trainings for trainer: {}", trainerId);

        TypedQuery<Training> query = entityManager.createQuery(
                "SELECT t FROM Training t WHERE t.trainerId = :trainerId", Training.class);
        query.setParameter("trainerId", trainerId);

        List<Training> trainings = query.getResultList();
        logger.debug("Found {} trainings for trainer: {}", trainings.size(), trainerId);

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByDate(LocalDate date) {
        if (date == null) {
            logger.debug("FindByDate called with null date");
            return List.of();
        }

        logger.debug("Finding trainings for date: {}", date);

        TypedQuery<Training> query = entityManager.createQuery(
                "SELECT t FROM Training t WHERE t.trainingDate = :date", Training.class);
        query.setParameter("date", date);

        List<Training> trainings = query.getResultList();
        logger.debug("Found {} trainings for date: {}", trainings.size(), date);

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            logger.debug("FindByDateRange called with null dates: start={}, end={}", startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindByDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings between {} and {}", startDate, endDate);

        TypedQuery<Training> query = entityManager.createQuery(
                "SELECT t FROM Training t WHERE t.trainingDate >= :startDate AND t.trainingDate <= :endDate",
                Training.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Training> trainings = query.getResultList();
        logger.debug("Found {} trainings between {} and {}", trainings.size(), startDate, endDate);

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByTraineeIdAndDateRange(Long traineeId, LocalDate startDate, LocalDate endDate) {
        if (traineeId == null || startDate == null || endDate == null) {
            logger.debug("FindByTraineeIdAndDateRange called with null parameters: traineeId={}, start={}, end={}",
                    traineeId, startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindByTraineeIdAndDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings for trainee {} between {} and {}", traineeId, startDate, endDate);

        TypedQuery<Training> query = entityManager.createQuery(
                "SELECT t FROM Training t WHERE t.traineeId = :traineeId " +
                        "AND t.trainingDate >= :startDate AND t.trainingDate <= :endDate",
                Training.class);
        query.setParameter("traineeId", traineeId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Training> trainings = query.getResultList();
        logger.debug("Found {} trainings for trainee {} between {} and {}",
                trainings.size(), traineeId, startDate, endDate);

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByTrainerIdAndDateRange(Long trainerId, LocalDate startDate, LocalDate endDate) {
        if (trainerId == null || startDate == null || endDate == null) {
            logger.debug("FindByTrainerIdAndDateRange called with null parameters: trainerId={}, start={}, end={}",
                    trainerId, startDate, endDate);
            return List.of();
        }

        if (startDate.isAfter(endDate)) {
            logger.debug("FindByTrainerIdAndDateRange called with invalid range: start={}, end={}", startDate, endDate);
            return List.of();
        }

        logger.debug("Finding trainings for trainer {} between {} and {}", trainerId, startDate, endDate);

        TypedQuery<Training> query = entityManager.createQuery(
                "SELECT t FROM Training t WHERE t.trainerId = :trainerId " +
                        "AND t.trainingDate >= :startDate AND t.trainingDate <= :endDate",
                Training.class);
        query.setParameter("trainerId", trainerId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Training> trainings = query.getResultList();
        logger.debug("Found {} trainings for trainer {} between {} and {}",
                trainings.size(), trainerId, startDate, endDate);

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(t) FROM Training t WHERE t.id = :id", Long.class);
        query.setParameter("id", id);

        Long count = query.getSingleResult();
        boolean exists = count != null && count > 0;

        logger.debug("Training exists check for id {}: {}", id, exists);
        return exists;
    }

    // Add method for cascade delete (needed for trainee deletion)
    public int deleteByTraineeId(Long traineeId) {
        if (traineeId == null) {
            return 0;
        }

        logger.debug("Deleting all trainings for trainee: {}", traineeId);

        int deletedCount = entityManager.createQuery(
                        "DELETE FROM Training t WHERE t.traineeId = :traineeId")
                .setParameter("traineeId", traineeId)
                .executeUpdate();

        logger.info("Deleted {} trainings for trainee: {}", deletedCount, traineeId);

        return deletedCount;
    }
}