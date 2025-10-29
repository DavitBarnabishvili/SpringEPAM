package com.gym.crm.util.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter traineeRegistrationCounter(MeterRegistry registry) {
        return Counter.builder("gym.trainee.registrations")
                .description("Number of trainee registrations")
                .register(registry);
    }

    @Bean
    public Counter trainerRegistrationCounter(MeterRegistry registry) {
        return Counter.builder("gym.trainer.registrations")
                .description("Number of trainer registrations")
                .register(registry);
    }

    @Bean
    public Counter trainingCreatedCounter(MeterRegistry registry) {
        return Counter.builder("gym.training.created")
                .description("Number of trainings created")
                .register(registry);
    }

    @Bean
    public Counter loginAttemptCounter(MeterRegistry registry) {
        return Counter.builder("gym.login.attempts")
                .description("Number of login attempts")
                .register(registry);
    }

    @Bean
    public Counter loginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("gym.login.success")
                .description("Number of successful logins")
                .register(registry);
    }

    @Bean
    public Timer authenticationTimer(MeterRegistry registry) {
        return Timer.builder("gym.authentication.time")
                .description("Time taken for authentication")
                .register(registry);
    }
}