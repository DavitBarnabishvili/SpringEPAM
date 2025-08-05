package com.gym.crm.util.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.util.CredentialsGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class CredentialsGeneratorServiceImpl implements CredentialsGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialsGeneratorServiceImpl.class);
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final SecureRandom random = new SecureRandom();

    public CredentialsGeneratorServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Override
    public String generateUsername(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name are required for username generation");
        }

        String baseUsername = firstName.trim() + "." + lastName.trim();
        logger.debug("Generating username for: {} {}, base username: {}", firstName, lastName, baseUsername);

        if (isUsernameUnique(baseUsername)) {
            logger.debug("Username {} is unique", baseUsername);
            return baseUsername;
        }

        int counter = 1;
        String candidateUsername;
        do {
            candidateUsername = baseUsername + counter;
            logger.debug("Checking username candidate: {}", candidateUsername);
            counter++;
        } while (!isUsernameUnique(candidateUsername));

        logger.info("Generated unique username: {} (base: {})", candidateUsername, baseUsername);
        return candidateUsername;
    }

    @Override
    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(PASSWORD_CHARACTERS.length());
            password.append(PASSWORD_CHARACTERS.charAt(index));
        }
        String result = password.toString();
        logger.debug("Generated password of length: {}", result.length());
        return result;
    }

    @Override
    public boolean isUsernameUnique(String username) {
        if (username == null || username.trim().isEmpty()) return false;

        String clean = username.trim();
        boolean existsInTrainees = traineeDao.existsByUsername(clean);
        boolean existsInTrainers = trainerDao.existsByUsername(clean);
        boolean isUnique = !existsInTrainees && !existsInTrainers;

        logger.debug("Username '{}' uniqueness: {}, (in trainees: {}, in trainers: {})",
                clean, isUnique, existsInTrainees, existsInTrainers);

        return isUnique;
    }
}