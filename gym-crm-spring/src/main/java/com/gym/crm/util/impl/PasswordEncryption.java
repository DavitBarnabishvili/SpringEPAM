package com.gym.crm.util.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password encoder using SHA-256 with salt for secure password storage.
 * This is a custom implementation without using Spring Security.
 */
@Component
public class PasswordEncryption {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryption.class);
    private static final String ALGORITHM = "SHA-256";
    private static final String SEPARATOR = ":";
    private static final int SALT_LENGTH = 16;

    private final SecureRandom random = new SecureRandom();

    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            String hashedPassword = hashWithSalt(rawPassword, salt);

            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String result = encodedSalt + SEPARATOR + hashedPassword;

            logger.debug("Password encoded successfully");
            return result;

        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available", e);
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        try {
            String[] parts = encodedPassword.split(SEPARATOR);
            if (parts.length != 2) {
                logger.warn("Invalid encoded password format");
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String storedHash = parts[1];

            String computedHash = hashWithSalt(rawPassword, salt);

            byte[] storedHashBytes = Base64.getDecoder().decode(storedHash);
            byte[] computedHashBytes = Base64.getDecoder().decode(computedHash);

            boolean matches = MessageDigest.isEqual(storedHashBytes, computedHashBytes);

            if (matches) {
                logger.debug("Password verification successful");
            } else {
                logger.debug("Password verification failed");
            }

            return matches;

        } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    private String hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.update(salt);
        byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    public String migratePassword(String plainPassword) {
        logger.info("Migrating plain text password to encrypted format");
        return encode(plainPassword);
    }

    public boolean isEncoded(String password) {
        return password != null && password.contains(SEPARATOR);
    }
}