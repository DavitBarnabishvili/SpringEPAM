package com.gym.crm.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private DatabaseHealthIndicator databaseHealthIndicator;

    @BeforeEach
    void setUp() {
        databaseHealthIndicator = new DatabaseHealthIndicator(dataSource);
    }

    @Nested
    @DisplayName("Healthy Database Tests")
    class HealthyDatabaseTests {

        @Test
        @DisplayName("Should return UP when database connection is valid")
        void health_ShouldReturnUp_WhenConnectionValid() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("database", "H2");
            assertThat(health.getDetails()).containsEntry("status", "Connection successful");
        }

        @Test
        @DisplayName("Should close connection after successful check")
        void health_ShouldCloseConnection_WhenSuccessful() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);

            databaseHealthIndicator.health();

            verify(connection).close();
        }

        @Test
        @DisplayName("Should validate connection with 1 second timeout")
        void health_ShouldValidateConnectionWithTimeout() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);

            databaseHealthIndicator.health();

            verify(connection).isValid(1);
        }

        @Test
        @DisplayName("Should include H2 database details")
        void health_ShouldIncludeH2DatabaseDetails() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getDetails()).containsEntry("database", "H2");
            assertThat(health.getDetails()).containsEntry("status", "Connection successful");
        }
    }

    @Nested
    @DisplayName("Unhealthy Database Tests")
    class UnhealthyDatabaseTests {

        @Test
        @DisplayName("Should return DOWN when connection is invalid")
        void health_ShouldReturnDown_WhenConnectionInvalid() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(false);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }

        @Test
        @DisplayName("Should return DOWN when getting connection throws SQLException")
        void health_ShouldReturnDown_WhenConnectionThrowsSQLException() throws SQLException {
            SQLException sqlException = new SQLException("Connection refused");
            when(dataSource.getConnection()).thenThrow(sqlException);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "Connection refused");
        }

        @Test
        @DisplayName("Should return DOWN when isValid throws SQLException")
        void health_ShouldReturnDown_WhenIsValidThrowsSQLException() throws SQLException {
            SQLException sqlException = new SQLException("Database error");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenThrow(sqlException);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "Database error");
        }

        @Test
        @DisplayName("Should return DOWN when general exception occurs")
        void health_ShouldReturnDown_WhenGeneralExceptionOccurs() throws SQLException {
            RuntimeException runtimeException = new RuntimeException("Unexpected error");
            when(dataSource.getConnection()).thenThrow(runtimeException);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "Unexpected error");
        }

        @Test
        @DisplayName("Should include exception message in error details")
        void health_ShouldIncludeExceptionMessage_InErrorDetails() throws SQLException {
            String errorMessage = "Database server not responding";
            SQLException sqlException = new SQLException(errorMessage);
            when(dataSource.getConnection()).thenThrow(sqlException);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", errorMessage);
        }
    }

    @Nested
    @DisplayName("Connection Resource Management Tests")
    class ConnectionResourceManagementTests {

        @Test
        @DisplayName("Should close connection even when isValid throws exception")
        void health_ShouldCloseConnection_WhenIsValidThrowsException() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenThrow(new SQLException("Validation error"));

            databaseHealthIndicator.health();

            verify(connection).close();
        }

        @Test
        @DisplayName("Should close connection when validation returns false")
        void health_ShouldCloseConnection_WhenValidationReturnsFalse() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(false);

            databaseHealthIndicator.health();

            verify(connection).close();
        }

        @Test
        @DisplayName("Should not attempt to close connection when dataSource throws exception")
        void health_ShouldNotCloseConnection_WhenDataSourceThrowsException() throws SQLException {
            when(dataSource.getConnection()).thenThrow(new SQLException("DataSource error"));

            databaseHealthIndicator.health();

            verify(connection, never()).close();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null DataSource gracefully")
        void health_ShouldHandleNullDataSource() {
            DatabaseHealthIndicator nullDataSourceIndicator = new DatabaseHealthIndicator(null);

            Health health = nullDataSourceIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsKey("error");
        }

        @Test
        @DisplayName("Should handle null connection from DataSource")
        void health_ShouldHandleNullConnection() throws SQLException {
            when(dataSource.getConnection()).thenReturn(null);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }

        @Test
        @DisplayName("Should handle connection timeout scenarios")
        void health_ShouldHandleConnectionTimeout() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenThrow(new SQLException("Connection timeout"));

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", "Connection timeout");
        }

        @Test
        @DisplayName("Should handle very long exception messages")
        void health_ShouldHandleVeryLongExceptionMessages() throws SQLException {
            String longMessage = "A".repeat(1000);
            SQLException sqlException = new SQLException(longMessage);
            when(dataSource.getConnection()).thenThrow(sqlException);

            Health health = databaseHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("error", longMessage);
        }

        @Test
        @DisplayName("Should handle multiple rapid health checks")
        void health_ShouldHandleMultipleRapidHealthChecks() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);

            for (int i = 0; i < 10; i++) {
                Health health = databaseHealthIndicator.health();
                assertThat(health.getStatus()).isEqualTo(Status.UP);
            }

            verify(dataSource, times(10)).getConnection();
            verify(connection, times(10)).isValid(1);
            verify(connection, times(10)).close();
        }
    }

    @Nested
    @DisplayName("Component and Implementation Tests")
    class ComponentTests {

        @Test
        @DisplayName("Should be properly annotated as Component")
        void shouldBeProperlyAnnotatedAsComponent() {
            assertThat(DatabaseHealthIndicator.class.isAnnotationPresent(
                    org.springframework.stereotype.Component.class)).isTrue();
        }

        @Test
        @DisplayName("Should implement HealthIndicator interface")
        void shouldImplementHealthIndicatorInterface() {
            assertThat(org.springframework.boot.actuate.health.HealthIndicator.class)
                    .isAssignableFrom(DatabaseHealthIndicator.class);
        }

        @Test
        @DisplayName("Should have correct constructor signature")
        void shouldHaveCorrectConstructorSignature() throws NoSuchMethodException {
            java.lang.reflect.Constructor<DatabaseHealthIndicator> constructor =
                    DatabaseHealthIndicator.class.getConstructor(DataSource.class);

            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should have correct health method signature")
        void shouldHaveCorrectHealthMethodSignature() throws NoSuchMethodException {
            java.lang.reflect.Method healthMethod = DatabaseHealthIndicator.class.getMethod("health");

            assertThat(healthMethod).isNotNull();
            assertThat(healthMethod.getReturnType()).isEqualTo(Health.class);
            assertThat(healthMethod.getParameterCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Health Status Consistency Tests")
    class HealthStatusConsistencyTests {

        @Test
        @DisplayName("Should consistently return UP for valid connections")
        void health_ShouldConsistentlyReturnUp_ForValidConnections() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);

            Health health1 = databaseHealthIndicator.health();
            Health health2 = databaseHealthIndicator.health();
            Health health3 = databaseHealthIndicator.health();

            assertThat(health1.getStatus()).isEqualTo(Status.UP);
            assertThat(health2.getStatus()).isEqualTo(Status.UP);
            assertThat(health3.getStatus()).isEqualTo(Status.UP);

            assertThat(health1.getDetails()).containsEntry("database", "H2");
            assertThat(health2.getDetails()).containsEntry("database", "H2");
            assertThat(health3.getDetails()).containsEntry("database", "H2");
        }

        @Test
        @DisplayName("Should consistently return DOWN for invalid connections")
        void health_ShouldConsistentlyReturnDown_ForInvalidConnections() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(false);

            Health health1 = databaseHealthIndicator.health();
            Health health2 = databaseHealthIndicator.health();

            assertThat(health1.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health2.getStatus()).isEqualTo(Status.DOWN);
        }

        @Test
        @DisplayName("Should transition from DOWN to UP when connection recovers")
        void health_ShouldTransitionFromDownToUp_WhenConnectionRecovers() throws SQLException {
            when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
            Health downHealth = databaseHealthIndicator.health();
            assertThat(downHealth.getStatus()).isEqualTo(Status.DOWN);

            reset(dataSource);

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);
            Health upHealth = databaseHealthIndicator.health();
            assertThat(upHealth.getStatus()).isEqualTo(Status.UP);
        }

        @Test
        @DisplayName("Should transition from UP to DOWN when connection fails")
        void health_ShouldTransitionFromUpToDown_WhenConnectionFails() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(1)).thenReturn(true);
            Health upHealth = databaseHealthIndicator.health();
            assertThat(upHealth.getStatus()).isEqualTo(Status.UP);

            when(connection.isValid(1)).thenReturn(false);
            Health downHealth = databaseHealthIndicator.health();
            assertThat(downHealth.getStatus()).isEqualTo(Status.DOWN);
        }
    }
}