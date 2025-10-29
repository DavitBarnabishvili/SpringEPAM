package com.gym.crm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JwtTokenInvalidationService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenInvalidationService.class);

    private final ConcurrentMap<String, Long> invalidatedTokens = new ConcurrentHashMap<>();

    public void invalidateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Attempted to invalidate null or empty token");
            return;
        }

        long expirationTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        invalidatedTokens.put(token, expirationTime);
        logger.debug("Token invalidated successfully");

        cleanupExpiredTokens();
    }

    public boolean isInvalidated(String token) {
        if (token == null) {
            return false;
        }

        Long expirationTime = invalidatedTokens.get(token);
        if (expirationTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > expirationTime) {
            invalidatedTokens.remove(token);
            logger.debug("Expired invalidated token removed");
            return false;
        }

        logger.debug("Token found in invalidation list");
        return true;
    }

    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        AtomicInteger removedCount = new AtomicInteger();

        invalidatedTokens.entrySet().removeIf(entry -> {
            boolean expired = currentTime > entry.getValue();
            if (expired) {
                removedCount.getAndIncrement();
            }
            return expired;
        });

        if (removedCount.get() > 0) {
            logger.debug("Cleaned up {} expired invalidated tokens", removedCount);
        }
    }

    public int getInvalidatedTokenCount() {
        cleanupExpiredTokens();
        return invalidatedTokens.size();
    }

    public void clearAllInvalidatedTokens() {
        invalidatedTokens.clear();
        logger.info("All invalidated tokens cleared");
    }
}