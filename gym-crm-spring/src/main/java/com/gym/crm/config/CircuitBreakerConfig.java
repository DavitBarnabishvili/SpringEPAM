package com.gym.crm.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfig {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerConfig.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerConfig(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void postConstruct() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("trainer-workload");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        logger.info("Circuit Breaker State Transition: {}", event))
                .onFailureRateExceeded(event ->
                        logger.warn("Circuit Breaker Failure Rate Exceeded: {}", event))
                .onCallNotPermitted(event ->
                        logger.warn("Circuit Breaker Call Not Permitted: {}", event))
                .onError(event ->
                        logger.error("Circuit Breaker Error: {}", event));
    }
}