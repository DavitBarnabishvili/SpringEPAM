package com.gym.crm.client;

import com.gym.crm.dto.request.TrainerWorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadClientFallback implements TrainerWorkloadClient {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadClientFallback.class);

    @Override
    public ResponseEntity<Void> updateTrainerWorkload(String token, TrainerWorkloadRequest request) {
        logger.error("Fallback: Failed to update trainer workload for trainer: {}",
                request.getTrainerUsername());

        return ResponseEntity.ok().build();
    }
}