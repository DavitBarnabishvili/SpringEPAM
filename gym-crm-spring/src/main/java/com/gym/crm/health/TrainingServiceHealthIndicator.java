package com.gym.crm.health;

import com.gym.crm.dao.TrainingTypeDao;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainingServiceHealthIndicator implements HealthIndicator {

    private final TrainingTypeDao trainingTypeDao;

    public TrainingServiceHealthIndicator(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public Health health() {
        try {
            long count = trainingTypeDao.count();
            if (count > 0) {
                return Health.up()
                        .withDetail("trainingTypes", count)
                        .withDetail("status", "Training types loaded")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "No training types found")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}