package com.gym.crm.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class CustomMetricsService {

    private final MeterRegistry meterRegistry;
    private final Counter traineeRegistrationCounter;
    private final Counter trainerRegistrationCounter;
    private final Counter trainingCreatedCounter;
    private final Counter loginAttemptCounter;
    private final Counter loginSuccessCounter;
    private final Timer authenticationTimer;

    public CustomMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.traineeRegistrationCounter = Counter.builder("gym.trainee.registrations")
                .description("Number of trainee registrations")
                .register(meterRegistry);

        this.trainerRegistrationCounter = Counter.builder("gym.trainer.registrations")
                .description("Number of trainer registrations")
                .register(meterRegistry);

        this.trainingCreatedCounter = Counter.builder("gym.training.created")
                .description("Number of trainings created")
                .register(meterRegistry);

        this.loginAttemptCounter = Counter.builder("gym.login.attempts")
                .description("Number of login attempts")
                .register(meterRegistry);

        this.loginSuccessCounter = Counter.builder("gym.login.success")
                .description("Number of successful logins")
                .register(meterRegistry);

        this.authenticationTimer = Timer.builder("gym.authentication.time")
                .description("Time taken for authentication")
                .register(meterRegistry);
    }

    public void incrementTraineeRegistration() {
        traineeRegistrationCounter.increment();
    }

    public void incrementTrainerRegistration() {
        trainerRegistrationCounter.increment();
    }

    public void incrementTrainingCreated() {
        trainingCreatedCounter.increment();
    }

    public void incrementLoginAttempt() {
        loginAttemptCounter.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public Timer.Sample startAuthenticationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopAuthenticationTimer(Timer.Sample sample) {
        sample.stop(authenticationTimer);
    }
}