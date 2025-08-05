package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
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
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainee create(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        logger.debug("Creating trainee: {}", trainee.getFullName());

        entityManager.persist(trainee);
        entityManager.flush();

        logger.info("Successfully created trainee: {} with id: {}",
                trainee.getFullName(), trainee.getId());

        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee cannot be null");
        }

        if (trainee.getId() == null) {
            throw new IllegalArgumentException("Trainee id cannot be null for update");
        }

        logger.debug("Updating trainee with id: {}", trainee.getId());

        Trainee updatedTrainee = entityManager.merge(trainee);

        logger.info("Successfully updated trainee: {} with id: {}",
                updatedTrainee.getFullName(), updatedTrainee.getId());

        return updatedTrainee;
    }

    @Override
    public boolean delete(Long userId) {
        if (userId == null) {
            logger.debug("Delete called with null userId");
            return false;
        }

        logger.debug("Deleting trainee with id: {}", userId);

        Trainee trainee = entityManager.find(Trainee.class, userId);
        if (trainee == null) {
            logger.debug("No trainee found with id: {} for deletion", userId);
            return false;
        }

        entityManager.remove(trainee);

        logger.info("Successfully deleted trainee: {} with id: {}",
                trainee.getFullName(), userId);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findById(Long userId) {
        if (userId == null) {
            logger.debug("FindById called with null userId");
            return Optional.empty();
        }

        logger.debug("Finding trainee by id: {}", userId);

        Trainee trainee = entityManager.find(Trainee.class, userId);

        if (trainee != null) {
            logger.debug("Found trainee: {}", trainee.getFullName());
            return Optional.of(trainee);
        } else {
            logger.debug("No trainee found with id: {}", userId);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> findAll() {
        logger.debug("Finding all trainees");

        TypedQuery<Trainee> query = entityManager.createQuery("SELECT t FROM Trainee t", Trainee.class);
        List<Trainee> trainees = query.getResultList();

        logger.debug("Found {} trainees", trainees.size());
        return trainees;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("FindByUsername called with invalid username: {}", username);
            return Optional.empty();
        }

        String cleanUsername = username.trim();
        logger.debug("Finding trainee by username: {}", cleanUsername);

        try {
            TypedQuery<Trainee> query = entityManager.createQuery(
                    "SELECT t FROM Trainee t WHERE t.username = :username", Trainee.class);
            query.setParameter("username", cleanUsername);

            Trainee trainee = query.getSingleResult();
            logger.debug("Found trainee with username: {}", cleanUsername);
            return Optional.of(trainee);

        } catch (NoResultException e) {
            logger.debug("No trainee found with username: {}", cleanUsername);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> findAllActive() {
        logger.debug("Finding all active trainees");

        TypedQuery<Trainee> query = entityManager.createQuery(
                "SELECT t FROM Trainee t WHERE t.isActive = true", Trainee.class);
        List<Trainee> activeTrainees = query.getResultList();

        logger.debug("Found {} active trainees", activeTrainees.size());
        return activeTrainees;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        if (userId == null) {
            return false;
        }

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(t) FROM Trainee t WHERE t.id = :id", Long.class);
        query.setParameter("id", userId);

        Long count = query.getSingleResult();
        boolean exists = count != null && count > 0;

        logger.debug("Trainee exists check for id {}: {}", userId, exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(t) FROM Trainee t WHERE t.username = :username", Long.class);
        query.setParameter("username", cleanUsername);

        Long count = query.getSingleResult();
        boolean exists = count != null && count > 0;

        logger.debug("Trainee exists check for username '{}': {}", cleanUsername, exists);
        return exists;
    }
}