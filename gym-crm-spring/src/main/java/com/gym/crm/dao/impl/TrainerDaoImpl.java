package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDao;
import com.gym.crm.entity.Trainer;
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
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainer create(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        logger.debug("Creating trainer: {}", trainer.getFullName());

        entityManager.persist(trainer);
        entityManager.flush(); // Ensure ID is generated

        logger.info("Successfully created trainer: {} with id: {}",
                trainer.getFullName(), trainer.getId());

        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer cannot be null");
        }

        if (trainer.getId() == null) {
            throw new IllegalArgumentException("Trainer id cannot be null for update");
        }

        logger.debug("Updating trainer with id: {}", trainer.getId());

        Trainer updatedTrainer = entityManager.merge(trainer);

        logger.info("Successfully updated trainer: {} with id: {}",
                updatedTrainer.getFullName(), updatedTrainer.getId());

        return updatedTrainer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> findById(Long userId) {
        if (userId == null) {
            logger.debug("FindById called with null userId");
            return Optional.empty();
        }

        logger.debug("Finding trainer by id: {}", userId);

        Trainer trainer = entityManager.find(Trainer.class, userId);

        if (trainer != null) {
            logger.debug("Found trainer: {}", trainer.getFullName());
            return Optional.of(trainer);
        } else {
            logger.debug("No trainer found with id: {}", userId);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findAll() {
        logger.debug("Finding all trainers");

        TypedQuery<Trainer> query = entityManager.createQuery("SELECT t FROM Trainer t", Trainer.class);
        List<Trainer> trainers = query.getResultList();

        logger.debug("Found {} trainers", trainers.size());
        return trainers;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.debug("FindByUsername called with invalid username: {}", username);
            return Optional.empty();
        }

        String cleanUsername = username.trim();
        logger.debug("Finding trainer by username: {}", cleanUsername);

        try {
            TypedQuery<Trainer> query = entityManager.createQuery(
                    "SELECT t FROM Trainer t WHERE t.username = :username", Trainer.class);
            query.setParameter("username", cleanUsername);

            Trainer trainer = query.getSingleResult();
            logger.debug("Found trainer with username: {}", cleanUsername);
            return Optional.of(trainer);

        } catch (NoResultException e) {
            logger.debug("No trainer found with username: {}", cleanUsername);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        if (userId == null) {
            return false;
        }

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(t) FROM Trainer t WHERE t.id = :id", Long.class);
        query.setParameter("id", userId);

        Long count = query.getSingleResult();
        boolean exists = count != null && count > 0;

        logger.debug("Trainer exists check for id {}: {}", userId, exists);
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
                "SELECT COUNT(t) FROM Trainer t WHERE t.username = :username", Long.class);
        query.setParameter("username", cleanUsername);

        Long count = query.getSingleResult();
        boolean exists = count != null && count > 0;

        logger.debug("Trainer exists check for username '{}': {}", cleanUsername, exists);
        return exists;
    }
}