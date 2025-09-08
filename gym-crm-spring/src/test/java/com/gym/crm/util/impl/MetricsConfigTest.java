package com.gym.crm.util.impl;

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

class MetricsConfigTest {

    private MetricsConfig metricsConfig;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        metricsConfig = new MetricsConfig();
        meterRegistry = new SimpleMeterRegistry();
    }

    @Nested
    @DisplayName("Counter Bean Creation Tests")
    class CounterBeanCreationTests {

        @Test
        @DisplayName("Should create trainee registration counter bean")
        void traineeRegistrationCounter_ShouldCreateValidCounter() {
            Counter counter = metricsConfig.traineeRegistrationCounter(meterRegistry);

            assertThat(counter).isNotNull();
            assertThat(counter.getId().getName()).isEqualTo("gym.trainee.registrations");
            assertThat(counter.getId().getDescription()).isEqualTo("Number of trainee registrations");
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create trainer registration counter bean")
        void trainerRegistrationCounter_ShouldCreateValidCounter() {
            Counter counter = metricsConfig.trainerRegistrationCounter(meterRegistry);

            assertThat(counter).isNotNull();
            assertThat(counter.getId().getName()).isEqualTo("gym.trainer.registrations");
            assertThat(counter.getId().getDescription()).isEqualTo("Number of trainer registrations");
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create training created counter bean")
        void trainingCreatedCounter_ShouldCreateValidCounter() {
            Counter counter = metricsConfig.trainingCreatedCounter(meterRegistry);

            assertThat(counter).isNotNull();
            assertThat(counter.getId().getName()).isEqualTo("gym.training.created");
            assertThat(counter.getId().getDescription()).isEqualTo("Number of trainings created");
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create login attempt counter bean")
        void loginAttemptCounter_ShouldCreateValidCounter() {
            Counter counter = metricsConfig.loginAttemptCounter(meterRegistry);

            assertThat(counter).isNotNull();
            assertThat(counter.getId().getName()).isEqualTo("gym.login.attempts");
            assertThat(counter.getId().getDescription()).isEqualTo("Number of login attempts");
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create login success counter bean")
        void loginSuccessCounter_ShouldCreateValidCounter() {
            Counter counter = metricsConfig.loginSuccessCounter(meterRegistry);

            assertThat(counter).isNotNull();
            assertThat(counter.getId().getName()).isEqualTo("gym.login.success");
            assertThat(counter.getId().getDescription()).isEqualTo("Number of successful logins");
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create multiple counters with same registry")
        void shouldCreateMultipleCountersWithSameRegistry() {
            Counter traineeCounter = metricsConfig.traineeRegistrationCounter(meterRegistry);
            Counter trainerCounter = metricsConfig.trainerRegistrationCounter(meterRegistry);
            Counter trainingCounter = metricsConfig.trainingCreatedCounter(meterRegistry);
            Counter loginAttemptCounter = metricsConfig.loginAttemptCounter(meterRegistry);
            Counter loginSuccessCounter = metricsConfig.loginSuccessCounter(meterRegistry);

            assertThat(meterRegistry.getMeters()).hasSize(5);

            assertThat(traineeCounter).isNotSameAs(trainerCounter);
            assertThat(trainerCounter).isNotSameAs(trainingCounter);
            assertThat(trainingCounter).isNotSameAs(loginAttemptCounter);
            assertThat(loginAttemptCounter).isNotSameAs(loginSuccessCounter);

            assertThat(traineeCounter.getId().getName()).isNotEqualTo(trainerCounter.getId().getName());
            assertThat(trainerCounter.getId().getName()).isNotEqualTo(trainingCounter.getId().getName());
            assertThat(trainingCounter.getId().getName()).isNotEqualTo(loginAttemptCounter.getId().getName());
            assertThat(loginAttemptCounter.getId().getName()).isNotEqualTo(loginSuccessCounter.getId().getName());
        }
    }

    @Nested
    @DisplayName("Timer Bean Creation Tests")
    class TimerBeanCreationTests {

        @Test
        @DisplayName("Should create authentication timer bean")
        void authenticationTimer_ShouldCreateValidTimer() {
            Timer timer = metricsConfig.authenticationTimer(meterRegistry);

            assertThat(timer).isNotNull();
            assertThat(timer.getId().getName()).isEqualTo("gym.authentication.time");
            assertThat(timer.getId().getDescription()).isEqualTo("Time taken for authentication");
            assertThat(timer.count()).isEqualTo(0);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create timer with same registry as counters")
        void authenticationTimer_ShouldUseSharedRegistry() {
            Counter counter = metricsConfig.traineeRegistrationCounter(meterRegistry);
            Timer timer = metricsConfig.authenticationTimer(meterRegistry);

            assertThat(meterRegistry.getMeters()).hasSize(2);
            assertThat(meterRegistry.getMeters()).contains(counter, timer);
        }

        @Test
        @DisplayName("Should create multiple timers with different registries")
        void authenticationTimer_ShouldWorkWithDifferentRegistries() {
            MeterRegistry registry1 = new SimpleMeterRegistry();
            MeterRegistry registry2 = new SimpleMeterRegistry();

            Timer timer1 = metricsConfig.authenticationTimer(registry1);
            Timer timer2 = metricsConfig.authenticationTimer(registry2);

            assertThat(timer1).isNotSameAs(timer2);
            assertThat(registry1.getMeters()).hasSize(1);
            assertThat(registry2.getMeters()).hasSize(1);
            assertThat(registry1.getMeters()).contains(timer1);
            assertThat(registry2.getMeters()).contains(timer2);
        }
    }

    @Nested
    @DisplayName("Metric Functionality Tests")
    class MetricFunctionalityTests {

        @Test
        @DisplayName("Should create functional counters that can be incremented")
        void counters_ShouldBeFunctional() {
            Counter traineeCounter = metricsConfig.traineeRegistrationCounter(meterRegistry);
            Counter trainerCounter = metricsConfig.trainerRegistrationCounter(meterRegistry);

            traineeCounter.increment();
            traineeCounter.increment();
            trainerCounter.increment();

            assertThat(traineeCounter.count()).isEqualTo(2.0);
            assertThat(trainerCounter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should create functional timer that can record time")
        void timer_ShouldBeFunctional() throws InterruptedException {
            Timer timer = metricsConfig.authenticationTimer(meterRegistry);

            Timer.Sample sample = Timer.start(meterRegistry);
            Thread.sleep(10);
            sample.stop(timer);

            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should maintain independent counter states")
        void counters_ShouldMaintainIndependentStates() {
            Counter traineeCounter = metricsConfig.traineeRegistrationCounter(meterRegistry);
            Counter trainerCounter = metricsConfig.trainerRegistrationCounter(meterRegistry);
            Counter trainingCounter = metricsConfig.trainingCreatedCounter(meterRegistry);

            traineeCounter.increment(5);
            trainerCounter.increment(3);
            trainingCounter.increment(7);

            assertThat(traineeCounter.count()).isEqualTo(5.0);
            assertThat(trainerCounter.count()).isEqualTo(3.0);
            assertThat(trainingCounter.count()).isEqualTo(7.0);
        }
    }

    @Nested
    @DisplayName("Registry Integration Tests")
    class RegistryIntegrationTests {

        @Test
        @DisplayName("Should register all metrics to provided registry")
        void shouldRegisterAllMetricsToProvidedRegistry() {
            metricsConfig.traineeRegistrationCounter(meterRegistry);
            metricsConfig.trainerRegistrationCounter(meterRegistry);
            metricsConfig.trainingCreatedCounter(meterRegistry);
            metricsConfig.loginAttemptCounter(meterRegistry);
            metricsConfig.loginSuccessCounter(meterRegistry);
            metricsConfig.authenticationTimer(meterRegistry);

            assertThat(meterRegistry.getMeters()).hasSize(6);
        }

        @Test
        @DisplayName("Should handle null registry")
        void shouldHandleNullRegistryGracefully() {
            try {
                Counter counter = metricsConfig.traineeRegistrationCounter(null);
            } catch (NullPointerException e) {
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
        }

        @Test
        @DisplayName("Should work with different meter registry implementations")
        void shouldWorkWithDifferentMeterRegistryImplementations() {
            SimpleMeterRegistry simpleRegistry = new SimpleMeterRegistry();

            Counter counter = metricsConfig.traineeRegistrationCounter(simpleRegistry);
            Timer timer = metricsConfig.authenticationTimer(simpleRegistry);

            assertThat(counter).isNotNull();
            assertThat(timer).isNotNull();
            assertThat(simpleRegistry.getMeters()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Metric Naming and Description Tests")
    class MetricNamingTests {

        @Test
        @DisplayName("Should use consistent naming convention")
        void shouldUseConsistentNamingConvention() {
            Counter traineeCounter = metricsConfig.traineeRegistrationCounter(meterRegistry);
            Counter trainerCounter = metricsConfig.trainerRegistrationCounter(meterRegistry);
            Counter trainingCounter = metricsConfig.trainingCreatedCounter(meterRegistry);
            Counter loginAttemptCounter = metricsConfig.loginAttemptCounter(meterRegistry);
            Counter loginSuccessCounter = metricsConfig.loginSuccessCounter(meterRegistry);
            Timer authTimer = metricsConfig.authenticationTimer(meterRegistry);

            assertThat(traineeCounter.getId().getDescription()).isNotBlank();
            assertThat(trainerCounter.getId().getDescription()).isNotBlank();
            assertThat(trainingCounter.getId().getDescription()).isNotBlank();
            assertThat(loginAttemptCounter.getId().getDescription()).isNotBlank();
            assertThat(loginSuccessCounter.getId().getDescription()).isNotBlank();
            assertThat(authTimer.getId().getDescription()).isNotBlank();

            assertThat(traineeCounter.getId().getDescription()).containsIgnoringCase("trainee");
            assertThat(trainerCounter.getId().getDescription()).containsIgnoringCase("trainer");
            assertThat(trainingCounter.getId().getDescription()).containsIgnoringCase("training");
            assertThat(loginAttemptCounter.getId().getDescription()).containsIgnoringCase("login");
            assertThat(loginSuccessCounter.getId().getDescription()).containsIgnoringCase("login");
            assertThat(authTimer.getId().getDescription()).containsIgnoringCase("authentication");
        }

        @Test
        @DisplayName("Should have unique metric names")
        void shouldHaveUniqueMetricNames() {
            Counter traineeCounter = metricsConfig.traineeRegistrationCounter(meterRegistry);
            Counter trainerCounter = metricsConfig.trainerRegistrationCounter(meterRegistry);
            Counter trainingCounter = metricsConfig.trainingCreatedCounter(meterRegistry);
            Counter loginAttemptCounter = metricsConfig.loginAttemptCounter(meterRegistry);
            Counter loginSuccessCounter = metricsConfig.loginSuccessCounter(meterRegistry);
            Timer authTimer = metricsConfig.authenticationTimer(meterRegistry);
            String[] names = {
                    traineeCounter.getId().getName(),
                    trainerCounter.getId().getName(),
                    trainingCounter.getId().getName(),
                    loginAttemptCounter.getId().getName(),
                    loginSuccessCounter.getId().getName(),
                    authTimer.getId().getName()
            };

            assertThat(names).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("Bean Registration Edge Cases")
    class BeanRegistrationEdgeCases {

        @Test
        @DisplayName("Should handle multiple calls with same registry")
        void shouldHandleMultipleCallsWithSameRegistry() {
            Counter counter1 = metricsConfig.traineeRegistrationCounter(meterRegistry);
            Counter counter2 = metricsConfig.traineeRegistrationCounter(meterRegistry);

            assertThat(counter1.getId().getName()).isEqualTo(counter2.getId().getName());
        }

        @Test
        @DisplayName("Should handle empty registry")
        void shouldHandleEmptyRegistry() {
            MeterRegistry emptyRegistry = new SimpleMeterRegistry();

            Counter counter = metricsConfig.traineeRegistrationCounter(emptyRegistry);

            assertThat(counter).isNotNull();
            assertThat(emptyRegistry.getMeters()).hasSize(1);
            assertThat(emptyRegistry.getMeters()).contains(counter);
        }

        @Test
        @DisplayName("Should create beans that persist across calls")
        void shouldCreateBeansThatPersistAcrossCalls() {
            Counter counter = metricsConfig.traineeRegistrationCounter(meterRegistry);

            counter.increment(5);

            Counter sameCounter = meterRegistry.get("gym.trainee.registrations").counter();

            assertThat(sameCounter.count()).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("Configuration Class Properties")
    class ConfigurationClassProperties {

        @Test
        @DisplayName("Should be properly annotated as Configuration")
        void shouldBeProperlyAnnotatedAsConfiguration() {
            assertThat(MetricsConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Should have all methods annotated as Bean")
        void shouldHaveAllMethodsAnnotatedAsBean() {
            java.lang.reflect.Method[] methods = MetricsConfig.class.getDeclaredMethods();

            for (java.lang.reflect.Method method : methods) {
                if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                    assertThat(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class))
                            .withFailMessage("Method %s should be annotated with @Bean", method.getName())
                            .isTrue();
                }
            }
        }

        @Test
        @DisplayName("Should have correct method return types")
        void shouldHaveCorrectMethodReturnTypes() throws NoSuchMethodException {
            assertThat(MetricsConfig.class.getMethod("traineeRegistrationCounter", MeterRegistry.class).getReturnType())
                    .isEqualTo(Counter.class);
            assertThat(MetricsConfig.class.getMethod("trainerRegistrationCounter", MeterRegistry.class).getReturnType())
                    .isEqualTo(Counter.class);
            assertThat(MetricsConfig.class.getMethod("trainingCreatedCounter", MeterRegistry.class).getReturnType())
                    .isEqualTo(Counter.class);
            assertThat(MetricsConfig.class.getMethod("loginAttemptCounter", MeterRegistry.class).getReturnType())
                    .isEqualTo(Counter.class);
            assertThat(MetricsConfig.class.getMethod("loginSuccessCounter", MeterRegistry.class).getReturnType())
                    .isEqualTo(Counter.class);
            assertThat(MetricsConfig.class.getMethod("authenticationTimer", MeterRegistry.class).getReturnType())
                    .isEqualTo(Timer.class);
        }

        @Test
        @DisplayName("Should have correct method parameter types")
        void shouldHaveCorrectMethodParameterTypes() throws NoSuchMethodException {
            java.lang.reflect.Method[] methods = {
                    MetricsConfig.class.getMethod("traineeRegistrationCounter", MeterRegistry.class),
                    MetricsConfig.class.getMethod("trainerRegistrationCounter", MeterRegistry.class),
                    MetricsConfig.class.getMethod("trainingCreatedCounter", MeterRegistry.class),
                    MetricsConfig.class.getMethod("loginAttemptCounter", MeterRegistry.class),
                    MetricsConfig.class.getMethod("loginSuccessCounter", MeterRegistry.class),
                    MetricsConfig.class.getMethod("authenticationTimer", MeterRegistry.class)
            };

            for (java.lang.reflect.Method method : methods) {
                Class<?>[] paramTypes = method.getParameterTypes();
                assertThat(paramTypes).hasSize(1);
                assertThat(paramTypes[0]).isEqualTo(MeterRegistry.class);
            }
        }
    }
}