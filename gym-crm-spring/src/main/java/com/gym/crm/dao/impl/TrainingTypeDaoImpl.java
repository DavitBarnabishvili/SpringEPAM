package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.entity.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TrainingType create(TrainingType trainingType) {
        if (trainingType == null) {
            throw new IllegalArgumentException("TrainingType cannot be null");
        }

        logger.debug("Creating training type: {}", trainingType.getTrainingTypeName());

        entityManager.persist(trainingType);
        entityManager.flush();

        logger.info("Successfully created training type: {} with id: {}",
                trainingType.getTrainingTypeName(), trainingType.getId());

        return trainingType;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingType> findById(Long id) {
        if (id == null) {
            logger.debug("FindById called with null id");
            return Optional.empty();
        }

        logger.debug("Finding training type by id: {}", id);

        TrainingType trainingType = entityManager.find(TrainingType.class, id);

        if (trainingType != null) {
            logger.debug("Found training type: {}", trainingType.getTrainingTypeName());
            return Optional.of(trainingType);
        } else {
            logger.debug("No training type found with id: {}", id);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingType> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.debug("FindByName called with invalid name: {}", name);
            return Optional.empty();
        }

        String cleanName = name.trim();
        logger.debug("Finding training type by name: {}", cleanName);

        try {
            TypedQuery<TrainingType> query = entityManager.createQuery(
                    "SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class);
            query.setParameter("name", cleanName);

            TrainingType trainingType = query.getSingleResult();
            logger.debug("Found training type with name: {}", cleanName);
            return Optional.of(trainingType);

        } catch (NoResultException e) {
            logger.debug("No training type found with name: {}", cleanName);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> findAll() {
        logger.debug("Finding all training types");

        TypedQuery<TrainingType> query = entityManager.createQuery("SELECT t FROM TrainingType t", TrainingType.class);
        List<TrainingType> trainingTypes = query.getResultList();

        logger.debug("Found {} training types", trainingTypes.size());
        return trainingTypes;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(t) FROM TrainingType t WHERE t.id = :id", Long.class);
        query.setParameter("id", id);

        Long count = query.getSingleResult();
        boolean exists = count != null && count > 0;

        logger.debug("Training type exists check for id {}: {}", id, exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String cleanName = name.trim();

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(t) FROM TrainingType t WHERE t.trainingTypeName = :name", Long.class);
        query.setParameter("name", cleanName);

        Long count = query.getSingleResult();
        boolean exists = count != null && count > 0;

        logger.debug("Training type exists check for name '{}': {}", cleanName, exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(t) FROM TrainingType t", Long.class);
        Long count = query.getSingleResult();

        logger.debug("Total training types count: {}", count);
        return count != null ? count : 0L;
    }
}