package com.gym.crm.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UpdateTrainerListRequest {

    @NotNull(message = "Trainer list is required")
    private List<String> trainerUsernames;

    public UpdateTrainerListRequest() {}

    public UpdateTrainerListRequest(List<String> trainerUsernames) {
        this.trainerUsernames = trainerUsernames;
    }

    public List<String> getTrainerUsernames() {
        return trainerUsernames;
    }

    public void setTrainerUsernames(List<String> trainerUsernames) {
        this.trainerUsernames = trainerUsernames;
    }
}