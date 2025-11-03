package com.gym.crm.client;

import com.gym.crm.dto.request.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "trainer-workload-service",
        fallback = TrainerWorkloadClientFallback.class
)
public interface TrainerWorkloadClient {

    @PostMapping("/api/trainer/workload")
    ResponseEntity<Void> updateTrainerWorkload(
            @RequestHeader("Authorization") String token,
            @RequestBody TrainerWorkloadRequest request
    );
}