package com.gym.crm.health;

import com.gym.crm.util.impl.JwtUtil;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHealthIndicator implements HealthIndicator {

    private final JwtUtil jwtUtil;

    public AuthenticationHealthIndicator(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Health health() {
        try {
            String testToken = jwtUtil.generateToken("test.user", "TEST", 1L);
            boolean valid = jwtUtil.validateToken(testToken);

            if (valid) {
                return Health.up()
                        .withDetail("jwt", "JWT generation and validation working")
                        .withDetail("status", "Authentication service operational")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "JWT service error: " + e.getMessage())
                    .build();
        }
        return Health.down().build();
    }
}