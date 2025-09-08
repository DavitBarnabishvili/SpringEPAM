package com.gym.crm.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CustomMetricsServiceTest {

    private CustomMetricsService customMetricsService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        customMetricsService = new CustomMetricsService(meterRegistry);
    }

    @Nested
    @DisplayName("Counter Tests")
    class CounterTests {

        @Test
        @DisplayName("Should increment trainee registration counter")
        void incrementTraineeRegistration_ShouldIncrementCounter() {
            Counter counter = meterRegistry.get("gym.trainee.registrations").counter();
            assertThat(counter.count()).isEqualTo(0.0);

            customMetricsService.incrementTraineeRegistration();
            assertThat(counter.count()).isEqualTo(1.0);

            customMetricsService.incrementTraineeRegistration();
            customMetricsService.incrementTraineeRegistration();
            assertThat(counter.count()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Should increment trainer registration counter")
        void incrementTrainerRegistration_ShouldIncrementCounter() {
            Counter counter = meterRegistry.get("gym.trainer.registrations").counter();
            assertThat(counter.count()).isEqualTo(0.0);

            customMetricsService.incrementTrainerRegistration();
            assertThat(counter.count()).isEqualTo(1.0);

            customMetricsService.incrementTrainerRegistration();
            customMetricsService.incrementTrainerRegistration();
            customMetricsService.incrementTrainerRegistration();
            assertThat(counter.count()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("Should increment training created counter")
        void incrementTrainingCreated_ShouldIncrementCounter() {
            Counter counter = meterRegistry.get("gym.training.created").counter();
            assertThat(counter.count()).isEqualTo(0.0);

            customMetricsService.incrementTrainingCreated();
            assertThat(counter.count()).isEqualTo(1.0);

            for (int i = 0; i < 10; i++) {
                customMetricsService.incrementTrainingCreated();
            }
            assertThat(counter.count()).isEqualTo(11.0);
        }

        @Test
        @DisplayName("Should increment login attempt counter")
        void incrementLoginAttempt_ShouldIncrementCounter() {
            Counter counter = meterRegistry.get("gym.login.attempts").counter();
            assertThat(counter.count()).isEqualTo(0.0);

            customMetricsService.incrementLoginAttempt();
            assertThat(counter.count()).isEqualTo(1.0);

            customMetricsService.incrementLoginAttempt();
            assertThat(counter.count()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Should increment login success counter")
        void incrementLoginSuccess_ShouldIncrementCounter() {
            Counter counter = meterRegistry.get("gym.login.success").counter();
            assertThat(counter.count()).isEqualTo(0.0);

            customMetricsService.incrementLoginSuccess();
            assertThat(counter.count()).isEqualTo(1.0);

            customMetricsService.incrementLoginSuccess();
            customMetricsService.incrementLoginSuccess();
            assertThat(counter.count()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Should handle multiple counter increments independently")
        void shouldHandleMultipleCountersIndependently() {
            customMetricsService.incrementTraineeRegistration();
            customMetricsService.incrementTraineeRegistration();

            customMetricsService.incrementTrainerRegistration();

            customMetricsService.incrementTrainingCreated();
            customMetricsService.incrementTrainingCreated();
            customMetricsService.incrementTrainingCreated();

            customMetricsService.incrementLoginAttempt();
            customMetricsService.incrementLoginAttempt();
            customMetricsService.incrementLoginAttempt();
            customMetricsService.incrementLoginAttempt();

            customMetricsService.incrementLoginSuccess();

            assertThat(meterRegistry.get("gym.trainee.registrations").counter().count()).isEqualTo(2.0);
            assertThat(meterRegistry.get("gym.trainer.registrations").counter().count()).isEqualTo(1.0);
            assertThat(meterRegistry.get("gym.training.created").counter().count()).isEqualTo(3.0);
            assertThat(meterRegistry.get("gym.login.attempts").counter().count()).isEqualTo(4.0);
            assertThat(meterRegistry.get("gym.login.success").counter().count()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("Timer Tests")
    class TimerTests {

        @Test
        @DisplayName("Should start and stop authentication timer")
        void authenticationTimer_ShouldStartAndStop() throws InterruptedException {
            Timer timer = meterRegistry.get("gym.authentication.time").timer();

            assertThat(timer.count()).isEqualTo(0);
            Timer.Sample sample = customMetricsService.startAuthenticationTimer();
            assertThat(sample).isNotNull();

            Thread.sleep(10);

            customMetricsService.stopAuthenticationTimer(sample);

            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle multiple timer measurements")
        void authenticationTimer_ShouldHandleMultipleMeasurements() throws InterruptedException {
            Timer timer = meterRegistry.get("gym.authentication.time").timer();

            Timer.Sample sample1 = customMetricsService.startAuthenticationTimer();
            Thread.sleep(5);
            customMetricsService.stopAuthenticationTimer(sample1);

            Timer.Sample sample2 = customMetricsService.startAuthenticationTimer();
            Thread.sleep(5);
            customMetricsService.stopAuthenticationTimer(sample2);

            Timer.Sample sample3 = customMetricsService.startAuthenticationTimer();
            Thread.sleep(5);
            customMetricsService.stopAuthenticationTimer(sample3);

            assertThat(timer.count()).isEqualTo(3);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle concurrent timer measurements")
        void authenticationTimer_ShouldHandleConcurrentMeasurements() throws InterruptedException {
            Timer timer = meterRegistry.get("gym.authentication.time").timer();

            Timer.Sample sample1 = customMetricsService.startAuthenticationTimer();
            Timer.Sample sample2 = customMetricsService.startAuthenticationTimer();
            Timer.Sample sample3 = customMetricsService.startAuthenticationTimer();

            Thread.sleep(10);

            customMetricsService.stopAuthenticationTimer(sample2);
            customMetricsService.stopAuthenticationTimer(sample1);
            customMetricsService.stopAuthenticationTimer(sample3);

            assertThat(timer.count()).isEqualTo(3);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Metric Registration Tests")
    class MetricRegistrationTests {

        @Test
        @DisplayName("Should register all required counters")
        void shouldRegisterAllRequiredCounters() {
            assertThat(meterRegistry.get("gym.trainee.registrations").counter()).isNotNull();
            assertThat(meterRegistry.get("gym.trainer.registrations").counter()).isNotNull();
            assertThat(meterRegistry.get("gym.training.created").counter()).isNotNull();
            assertThat(meterRegistry.get("gym.login.attempts").counter()).isNotNull();
            assertThat(meterRegistry.get("gym.login.success").counter()).isNotNull();
        }

        @Test
        @DisplayName("Should register authentication timer")
        void shouldRegisterAuthenticationTimer() {
            assertThat(meterRegistry.get("gym.authentication.time").timer()).isNotNull();
        }

        @Test
        @DisplayName("Should have correct counter descriptions")
        void shouldHaveCorrectCounterDescriptions() {
            assertThat(meterRegistry.getMeters()).hasSize(6); // 5 counters + 1 timer
        }

        @Test
        @DisplayName("Should initialize all counters to zero")
        void shouldInitializeAllCountersToZero() {
            assertThat(meterRegistry.get("gym.trainee.registrations").counter().count()).isEqualTo(0.0);
            assertThat(meterRegistry.get("gym.trainer.registrations").counter().count()).isEqualTo(0.0);
            assertThat(meterRegistry.get("gym.training.created").counter().count()).isEqualTo(0.0);
            assertThat(meterRegistry.get("gym.login.attempts").counter().count()).isEqualTo(0.0);
            assertThat(meterRegistry.get("gym.login.success").counter().count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should initialize timer with zero count")
        void shouldInitializeTimerWithZeroCount() {
            Timer timer = meterRegistry.get("gym.authentication.time").timer();
            assertThat(timer.count()).isEqualTo(0);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle realistic workout scenario")
        void shouldHandleRealisticWorkoutScenario() throws InterruptedException {
            customMetricsService.incrementTraineeRegistration();
            customMetricsService.incrementTrainerRegistration();
            Timer.Sample authSample1 = customMetricsService.startAuthenticationTimer();
            Thread.sleep(5);
            customMetricsService.incrementLoginAttempt();
            customMetricsService.incrementLoginSuccess();
            customMetricsService.stopAuthenticationTimer(authSample1);

            Timer.Sample authSample2 = customMetricsService.startAuthenticationTimer();
            Thread.sleep(3);
            customMetricsService.incrementLoginAttempt();
            customMetricsService.stopAuthenticationTimer(authSample2);

            Timer.Sample authSample3 = customMetricsService.startAuthenticationTimer();
            Thread.sleep(7);
            customMetricsService.incrementLoginAttempt();
            customMetricsService.incrementLoginSuccess();
            customMetricsService.stopAuthenticationTimer(authSample3);

            customMetricsService.incrementTrainingCreated();
            customMetricsService.incrementTrainingCreated();

            assertThat(meterRegistry.get("gym.trainee.registrations").counter().count()).isEqualTo(1.0);
            assertThat(meterRegistry.get("gym.trainer.registrations").counter().count()).isEqualTo(1.0);
            assertThat(meterRegistry.get("gym.training.created").counter().count()).isEqualTo(2.0);
            assertThat(meterRegistry.get("gym.login.attempts").counter().count()).isEqualTo(3.0);
            assertThat(meterRegistry.get("gym.login.success").counter().count()).isEqualTo(2.0);
            assertThat(meterRegistry.get("gym.authentication.time").timer().count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle high volume metrics")
        void shouldHandleHighVolumeMetrics() {
            int volume = 1000;

            for (int i = 0; i < volume; i++) {
                customMetricsService.incrementTraineeRegistration();
                customMetricsService.incrementTrainerRegistration();
                customMetricsService.incrementTrainingCreated();
                customMetricsService.incrementLoginAttempt();
                customMetricsService.incrementLoginSuccess();
            }

            assertThat(meterRegistry.get("gym.trainee.registrations").counter().count()).isEqualTo((double) volume);
            assertThat(meterRegistry.get("gym.trainer.registrations").counter().count()).isEqualTo((double) volume);
            assertThat(meterRegistry.get("gym.training.created").counter().count()).isEqualTo((double) volume);
            assertThat(meterRegistry.get("gym.login.attempts").counter().count()).isEqualTo((double) volume);
            assertThat(meterRegistry.get("gym.login.success").counter().count()).isEqualTo((double) volume);
        }

        @Test
        @DisplayName("Should maintain metric state across multiple operations")
        void shouldMaintainMetricStateAcrossMultipleOperations() {
            customMetricsService.incrementTraineeRegistration();
            customMetricsService.incrementLoginAttempt();

            double traineeCount1 = meterRegistry.get("gym.trainee.registrations").counter().count();
            double loginAttemptCount1 = meterRegistry.get("gym.login.attempts").counter().count();

            customMetricsService.incrementTrainerRegistration();
            customMetricsService.incrementTrainingCreated();

            assertThat(meterRegistry.get("gym.trainee.registrations").counter().count()).isEqualTo(traineeCount1);
            assertThat(meterRegistry.get("gym.login.attempts").counter().count()).isEqualTo(loginAttemptCount1);

            assertThat(meterRegistry.get("gym.trainer.registrations").counter().count()).isEqualTo(1.0);
            assertThat(meterRegistry.get("gym.training.created").counter().count()).isEqualTo(1.0);
        }
    }
}