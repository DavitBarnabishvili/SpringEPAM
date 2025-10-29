package com.gym.crm.health;

import com.gym.crm.util.impl.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationHealthIndicatorTest {

    @Mock
    private JwtUtil jwtUtil;

    private AuthenticationHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new AuthenticationHealthIndicator(jwtUtil);
    }

    @Test
    void health_ShouldReturnUp_WhenJwtGenerationAndValidationWork() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L)).thenReturn("valid.jwt.token");
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("jwt", "JWT generation and validation working")
                .containsEntry("status", "Authentication service operational");
    }

    @Test
    void health_ShouldReturnDown_WhenJwtValidationFails() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L)).thenReturn("invalid.jwt.token");
        when(jwtUtil.validateToken("invalid.jwt.token")).thenReturn(false);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).doesNotContainKey("jwt");
    }

    @Test
    void health_ShouldReturnDown_WhenJwtGenerationThrowsException() {
        when(jwtUtil.generateToken(anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("JWT generation failed"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("error", "JWT service error: JWT generation failed");
    }

    @Test
    void health_ShouldReturnDown_WhenJwtValidationThrowsException() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L)).thenReturn("valid.jwt.token");
        when(jwtUtil.validateToken("valid.jwt.token"))
                .thenThrow(new RuntimeException("JWT validation failed"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("error", "JWT service error: JWT validation failed");
    }

    @Test
    void health_ShouldReturnDown_WhenJwtGenerationReturnsNull() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L)).thenReturn(null);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void health_ShouldUseCorrectTestParameters() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L)).thenReturn("token");
        when(jwtUtil.validateToken("token")).thenReturn(true);

        healthIndicator.health();

        org.mockito.Mockito.verify(jwtUtil).generateToken("test.user", "TEST", 1L);
        org.mockito.Mockito.verify(jwtUtil).validateToken("token");
    }

    @Test
    void health_ShouldHandleNullPointerException() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L))
                .thenThrow(new NullPointerException("Null secret key"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("error", "JWT service error: Null secret key");
    }

    @Test
    void health_ShouldHandleIllegalArgumentException() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("error", "JWT service error: Invalid parameters");
    }

    @Test
    void health_ShouldReturnCorrectDetailsStructure_WhenHealthy() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L)).thenReturn("token");
        when(jwtUtil.validateToken("token")).thenReturn(true);

        Health health = healthIndicator.health();

        assertThat(health.getDetails()).hasSize(2);
        assertThat(health.getDetails()).containsOnlyKeys("jwt", "status");
    }

    @Test
    void health_ShouldReturnCorrectDetailsStructure_WhenUnhealthy() {
        when(jwtUtil.generateToken("test.user", "TEST", 1L))
                .thenThrow(new RuntimeException("Error"));

        Health health = healthIndicator.health();

        assertThat(health.getDetails()).hasSize(1);
        assertThat(health.getDetails()).containsOnlyKeys("error");
    }
}