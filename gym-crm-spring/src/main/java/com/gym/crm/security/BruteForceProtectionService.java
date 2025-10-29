package com.gym.crm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BruteForceProtectionService {

    private static final Logger logger = LoggerFactory.getLogger(BruteForceProtectionService.class);

    @Value("${security.brute-force.max-attempts:3}")
    private int maxAttempts;

    @Value("${security.brute-force.lock-duration:300000}")
    private long lockDurationMillis;

    private final ConcurrentMap<String, AttemptRecord> loginAttempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        AttemptRecord record = loginAttempts.get(username);

        if (record == null) {
            return false;
        }

        if (!record.isLocked) {
            return false;
        }

        LocalDateTime unlockTime = record.lockTime.plusNanos(lockDurationMillis * 1_000_000);
        if (LocalDateTime.now().isAfter(unlockTime)) {
            loginAttempts.remove(username);
            logger.info("User {} unlocked after lock period expired", username);
            return false;
        }

        logger.debug("User {} is currently blocked due to failed login attempts", username);
        return true;
    }

    public void loginFailed(String username) {
        AttemptRecord record = loginAttempts.computeIfAbsent(username, k -> new AttemptRecord());

        record.attempts++;
        record.lastAttempt = LocalDateTime.now();

        if (record.attempts >= maxAttempts) {
            record.isLocked = true;
            record.lockTime = LocalDateTime.now();
            logger.warn("User {} locked due to {} failed login attempts", username, record.attempts);
        } else {
            logger.debug("Failed login attempt {} for user {}", record.attempts, username);
        }
    }

    public void loginSucceeded(String username) {
        AttemptRecord removed = loginAttempts.remove(username);
        if (removed != null) {
            logger.debug("Login attempts cleared for user {} after successful login", username);
        }
    }

    public long getRemainingLockTimeMinutes(String username) {
        AttemptRecord record = loginAttempts.get(username);

        if (record == null || !record.isLocked || record.lockTime == null) {
            return 0;
        }

        LocalDateTime unlockTime = record.lockTime.plusNanos(lockDurationMillis * 1_000_000);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(unlockTime)) {
            return 0;
        }

        return java.time.Duration.between(now, unlockTime).toMinutes();
    }

    private static class AttemptRecord {
        int attempts = 0;
        LocalDateTime lastAttempt;
        boolean isLocked = false;
        LocalDateTime lockTime;
    }
}