package com.gym.crm.util.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncryption {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryption.class);

    private final BCryptPasswordEncoder bCryptEncoder;

    public PasswordEncryption() {
        this.bCryptEncoder = new BCryptPasswordEncoder(12);
    }

    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        String encoded = bCryptEncoder.encode(rawPassword);
        logger.debug("Password encoded successfully with BCrypt");
        return encoded;
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        boolean matches = bCryptEncoder.matches(rawPassword, encodedPassword);
        if (matches) {
            logger.debug("Password verified successfully with BCrypt");
        } else {
            logger.debug("Password verification failed with BCrypt");
        }
        return matches;
    }
}