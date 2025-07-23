package com.gym.crm.util.impl;

import com.gym.crm.storage.InMemoryStorage;
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

    private final InMemoryStorage storage;
    private final SecureRandom random = new SecureRandom();

    public CredentialsGeneratorServiceImpl(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public String generateUsername(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name are required for username generation");
        }

        String cleanFirstName = firstName.trim();
        String cleanLastName = lastName.trim();
        String baseUsername = cleanFirstName + "." + cleanLastName;

        logger.debug("Generating username for: {} {}, base username: {}",
                cleanFirstName, cleanLastName, baseUsername);

        if (isUsernameUnique(baseUsername)) {
            logger.debug("Username {} is unique", baseUsername);
            return baseUsername;
        }

        int counter = 1;
        String candidateUsername;
        do {
            candidateUsername = baseUsername + counter;
            counter++;
            logger.debug("Checking username candidate: {}", candidateUsername);
        } while (!isUsernameUnique(candidateUsername));

        logger.info("Generated unique username: {} (base: {})", candidateUsername, baseUsername);
        return candidateUsername;
    }

    @Override
    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHARACTERS.length());
            password.append(PASSWORD_CHARACTERS.charAt(randomIndex));
        }

        String generatedPassword = password.toString();
        logger.debug("Generated password of length: {}", generatedPassword.length());
        return generatedPassword;
    }

    @Override
    public boolean isUsernameUnique(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim();

        boolean existsInTrainers = storage.getAllTrainers().stream()
                .anyMatch(trainer -> cleanUsername.equals(trainer.getUsername()));

        boolean existsInTrainees = storage.getAllTrainees().stream()
                .anyMatch(trainee -> cleanUsername.equals(trainee.getUsername()));

        boolean isUnique = !existsInTrainers && !existsInTrainees;

        logger.debug("Username '{}' uniqueness check: {} (trainers: {}, trainees: {})",
                cleanUsername, isUnique, !existsInTrainers, !existsInTrainees);

        return isUnique;
    }
}
