package com.gym.crm.health;

import com.gym.crm.dao.TrainingTypeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceHealthIndicatorTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    private TrainingServiceHealthIndicator trainingServiceHealthIndicator;

    @BeforeEach
    void setUp() {
        trainingServiceHealthIndicator = new TrainingServiceHealthIndicator(trainingTypeDao);
    }

    @Nested
    @DisplayName("Healthy Training Service Tests")
    class HealthyTrainingServiceTests {

        @Test
        @DisplayName("Should return UP when training types exist")
        void health_ShouldReturnUp_WhenTrainingTypesExist() {
            when(trainingTypeDao.count()).thenReturn(5L);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("trainingTypes", 5L);
            assertThat(health.getDetails()).containsEntry("status", "Training types loaded");
        }

        @Test
        @DisplayName("Should return UP when only one training type exists")
        void health_ShouldReturnUp_WhenOneTrainingTypeExists() {
            when(trainingTypeDao.count()).thenReturn(1L);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("trainingTypes", 1L);
            assertThat(health.getDetails()).containsEntry("status", "Training types loaded");
        }

        @Test
        @DisplayName("Should return UP when many training types exist")
        void health_ShouldReturnUp_WhenManyTrainingTypesExist() {
            when(trainingTypeDao.count()).thenReturn(100L);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("trainingTypes", 100L);
            assertThat(health.getDetails()).containsEntry("status", "Training types loaded");
        }

        @Test
        @DisplayName("Should include correct count in health details")
        void health_ShouldIncludeCorrectCount_InHealthDetails() {
            long expectedCount = 42L;
            when(trainingTypeDao.count()).thenReturn(expectedCount);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getDetails()).containsEntry("trainingTypes", expectedCount);
            verify(trainingTypeDao).count();
        }
    }

    @Nested
    @DisplayName("Unhealthy Training Service Tests")
    class UnhealthyTrainingServiceTests {

        @Test
        @DisplayName("Should return DOWN when no training types exist")
        void health_ShouldReturnDown_WhenNoTrainingTypesExist() {
            when(trainingTypeDao.count()).thenReturn(0L);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "No training types found");
        }

        @Test
        @DisplayName("Should return DOWN when count throws exception")
        void health_ShouldReturnDown_WhenCountThrowsException() {
            RuntimeException exception = new RuntimeException("Database connection failed");
            when(trainingTypeDao.count()).thenThrow(exception);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "Database connection failed");
        }

        @Test
        @DisplayName("Should return DOWN when count throws SQL exception")
        void health_ShouldReturnDown_WhenCountThrowsSQLException() {
            RuntimeException sqlException = new RuntimeException("SQL syntax error");
            when(trainingTypeDao.count()).thenThrow(sqlException);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "SQL syntax error");
        }

        @Test
        @DisplayName("Should include exception message in error details")
        void health_ShouldIncludeExceptionMessage_InErrorDetails() {
            String errorMessage = "Connection timeout occurred";
            RuntimeException exception = new RuntimeException(errorMessage);
            when(trainingTypeDao.count()).thenThrow(exception);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", errorMessage);
        }

        @Test
        @DisplayName("Should handle generic exception types")
        void health_ShouldHandleGenericExceptionTypes() {
            IllegalStateException exception = new IllegalStateException("Invalid state");
            when(trainingTypeDao.count()).thenThrow(exception);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "Invalid state");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large training type counts")
        void health_ShouldHandleVeryLargeTrainingTypeCounts() {
            long largeCount = Long.MAX_VALUE;
            when(trainingTypeDao.count()).thenReturn(largeCount);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("trainingTypes", largeCount);
        }

        @Test
        @DisplayName("Should handle negative training type counts")
        void health_ShouldHandleNegativeTrainingTypeCounts() {
            when(trainingTypeDao.count()).thenReturn(-1L);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "No training types found");
        }

        @Test
        @DisplayName("Should handle null TrainingTypeDao gracefully")
        void health_ShouldHandleNullTrainingTypeDao() {
            TrainingServiceHealthIndicator nullDaoIndicator = new TrainingServiceHealthIndicator(null);

            Health health = nullDaoIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsKey("error");
        }

        @Test
        @DisplayName("Should handle multiple rapid health checks")
        void health_ShouldHandleMultipleRapidHealthChecks() {
            when(trainingTypeDao.count()).thenReturn(3L);

            for (int i = 0; i < 10; i++) {
                Health health = trainingServiceHealthIndicator.health();
                assertThat(health.getStatus()).isEqualTo(Status.UP);
                assertThat(health.getDetails()).containsEntry("trainingTypes", 3L);
            }

            verify(trainingTypeDao, times(10)).count();
        }

        @Test
        @DisplayName("Should handle very long exception messages")
        void health_ShouldHandleVeryLongExceptionMessages() {
            String longMessage = "Error: " + "A".repeat(1000);
            RuntimeException exception = new RuntimeException(longMessage);
            when(trainingTypeDao.count()).thenThrow(exception);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", longMessage);
        }
    }

    @Nested
    @DisplayName("Component and Implementation Tests")
    class ComponentTests {

        @Test
        @DisplayName("Should be properly annotated as Component")
        void shouldBeProperlyAnnotatedAsComponent() {
            assertThat(TrainingServiceHealthIndicator.class.isAnnotationPresent(
                    org.springframework.stereotype.Component.class)).isTrue();
        }

        @Test
        @DisplayName("Should implement HealthIndicator interface")
        void shouldImplementHealthIndicatorInterface() {
            assertThat(org.springframework.boot.actuate.health.HealthIndicator.class)
                    .isAssignableFrom(TrainingServiceHealthIndicator.class);
        }

        @Test
        @DisplayName("Should have correct constructor signature")
        void shouldHaveCorrectConstructorSignature() throws NoSuchMethodException {
            java.lang.reflect.Constructor<TrainingServiceHealthIndicator> constructor =
                    TrainingServiceHealthIndicator.class.getConstructor(TrainingTypeDao.class);

            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should have correct health method signature")
        void shouldHaveCorrectHealthMethodSignature() throws NoSuchMethodException {
            java.lang.reflect.Method healthMethod = TrainingServiceHealthIndicator.class.getMethod("health");

            assertThat(healthMethod).isNotNull();
            assertThat(healthMethod.getReturnType()).isEqualTo(Health.class);
            assertThat(healthMethod.getParameterCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Health Status Consistency Tests")
    class HealthStatusConsistencyTests {

        @Test
        @DisplayName("Should consistently return UP for positive counts")
        void health_ShouldConsistentlyReturnUp_ForPositiveCounts() {
            when(trainingTypeDao.count()).thenReturn(5L);

            Health health1 = trainingServiceHealthIndicator.health();
            Health health2 = trainingServiceHealthIndicator.health();
            Health health3 = trainingServiceHealthIndicator.health();

            assertThat(health1.getStatus()).isEqualTo(Status.UP);
            assertThat(health2.getStatus()).isEqualTo(Status.UP);
            assertThat(health3.getStatus()).isEqualTo(Status.UP);

            assertThat(health1.getDetails()).containsEntry("trainingTypes", 5L);
            assertThat(health2.getDetails()).containsEntry("trainingTypes", 5L);
            assertThat(health3.getDetails()).containsEntry("trainingTypes", 5L);
        }

        @Test
        @DisplayName("Should consistently return DOWN for zero count")
        void health_ShouldConsistentlyReturnDown_ForZeroCount() {
            when(trainingTypeDao.count()).thenReturn(0L);

            Health health1 = trainingServiceHealthIndicator.health();
            Health health2 = trainingServiceHealthIndicator.health();

            assertThat(health1.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health2.getStatus()).isEqualTo(Status.DOWN);

            assertThat(health1.getDetails()).containsEntry("error", "No training types found");
            assertThat(health2.getDetails()).containsEntry("error", "No training types found");
        }

        @Test
        @DisplayName("Should transition from DOWN to UP when training types are added")
        void health_ShouldTransitionFromDownToUp_WhenTrainingTypesAdded() {
            when(trainingTypeDao.count()).thenReturn(0L);
            Health downHealth = trainingServiceHealthIndicator.health();
            assertThat(downHealth.getStatus()).isEqualTo(Status.DOWN);

            when(trainingTypeDao.count()).thenReturn(3L);
            Health upHealth = trainingServiceHealthIndicator.health();
            assertThat(upHealth.getStatus()).isEqualTo(Status.UP);
            assertThat(upHealth.getDetails()).containsEntry("trainingTypes", 3L);
        }

        @Test
        @DisplayName("Should transition from UP to DOWN when training types are removed")
        void health_ShouldTransitionFromUpToDown_WhenTrainingTypesRemoved() {
            when(trainingTypeDao.count()).thenReturn(5L);
            Health upHealth = trainingServiceHealthIndicator.health();
            assertThat(upHealth.getStatus()).isEqualTo(Status.UP);

            when(trainingTypeDao.count()).thenReturn(0L);
            Health downHealth = trainingServiceHealthIndicator.health();
            assertThat(downHealth.getStatus()).isEqualTo(Status.DOWN);
        }

        @Test
        @DisplayName("Should transition from UP to DOWN when exception occurs")
        void health_ShouldTransitionFromUpToDown_WhenExceptionOccurs() {
            when(trainingTypeDao.count()).thenReturn(3L);
            Health upHealth = trainingServiceHealthIndicator.health();
            assertThat(upHealth.getStatus()).isEqualTo(Status.UP);

            when(trainingTypeDao.count()).thenThrow(new RuntimeException("Database error"));
            Health downHealth = trainingServiceHealthIndicator.health();
            assertThat(downHealth.getStatus()).isEqualTo(Status.DOWN);
            assertThat(downHealth.getDetails()).containsEntry("error", "Database error");
        }
    }

    @Nested
    @DisplayName("Training Type Count Validation Tests")
    class TrainingTypeCountValidationTests {

        @Test
        @DisplayName("Should treat boundary value 1 as UP")
        void health_ShouldTreatBoundaryValueOneAsUp() {
            when(trainingTypeDao.count()).thenReturn(1L);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("trainingTypes", 1L);
        }

        @Test
        @DisplayName("Should treat boundary value 0 as DOWN")
        void health_ShouldTreatBoundaryValueZeroAsDown() {
            when(trainingTypeDao.count()).thenReturn(0L);

            Health health = trainingServiceHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "No training types found");
        }

        @Test
        @DisplayName("Should handle count changes during monitoring")
        void health_ShouldHandleCountChangesDuringMonitoring() {
            when(trainingTypeDao.count())
                    .thenReturn(0L)
                    .thenReturn(1L)
                    .thenReturn(5L)
                    .thenReturn(3L)
                    .thenReturn(0L);

            Health health1 = trainingServiceHealthIndicator.health();
            assertThat(health1.getStatus()).isEqualTo(Status.DOWN);

            Health health2 = trainingServiceHealthIndicator.health();
            assertThat(health2.getStatus()).isEqualTo(Status.UP);
            assertThat(health2.getDetails()).containsEntry("trainingTypes", 1L);

            Health health3 = trainingServiceHealthIndicator.health();
            assertThat(health3.getStatus()).isEqualTo(Status.UP);
            assertThat(health3.getDetails()).containsEntry("trainingTypes", 5L);

            Health health4 = trainingServiceHealthIndicator.health();
            assertThat(health4.getStatus()).isEqualTo(Status.UP);
            assertThat(health4.getDetails()).containsEntry("trainingTypes", 3L);

            Health health5 = trainingServiceHealthIndicator.health();
            assertThat(health5.getStatus()).isEqualTo(Status.DOWN);
        }
    }
}