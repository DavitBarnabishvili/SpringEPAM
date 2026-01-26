package com.gym.crm.cucumber.support;

import com.gym.crm.dto.request.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class TestDataBuilder {

    public TraineeRegistrationRequest buildTraineeRegistration(Map<String, String> data) {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName(data.get("firstName"));
        request.setLastName(data.get("lastName"));

        if (data.containsKey("dateOfBirth")) {
            request.setDateOfBirth(LocalDate.parse(data.get("dateOfBirth")));
        }
        if (data.containsKey("address")) {
            request.setAddress(data.get("address"));
        }

        return request;
    }

    public TrainerRegistrationRequest buildTrainerRegistration(Map<String, String> data) {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName(data.get("firstName"));
        request.setLastName(data.get("lastName"));

        if (data.containsKey("specialization")) {
            request.setSpecializationId(1L);
        }

        return request;
    }

    public AddTrainingRequest buildTrainingRequest(Map<String, String> data) {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTrainingName(data.get("trainingName"));
        request.setTraineeUsername(data.get("traineeUsername"));
        request.setTrainerUsername(data.get("trainerUsername"));
        request.setTrainingDate(LocalDate.parse(data.get("trainingDate")));
        request.setTrainingDuration(Integer.parseInt(data.get("trainingDuration")));

        return request;
    }

    public LoginRequest buildLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    public TraineeUpdateRequest buildTraineeUpdateRequest(Map<String, String> data) {
        TraineeUpdateRequest request = new TraineeUpdateRequest();

        if (data.containsKey("firstName")) {
            request.setFirstName(data.get("firstName"));
        }
        if (data.containsKey("lastName")) {
            request.setLastName(data.get("lastName"));
        }
        if (data.containsKey("dateOfBirth")) {
            request.setDateOfBirth(LocalDate.parse(data.get("dateOfBirth")));
        }
        if (data.containsKey("address")) {
            request.setAddress(data.get("address"));
        }
        if (data.containsKey("isActive")) {
            request.setIsActive(Boolean.parseBoolean(data.get("isActive")));
        }

        return request;
    }

    public TrainerUpdateRequest buildTrainerUpdateRequest(Map<String, String> data) {
        TrainerUpdateRequest request = new TrainerUpdateRequest();

        if (data.containsKey("firstName")) {
            request.setFirstName(data.get("firstName"));
        }
        if (data.containsKey("lastName")) {
            request.setLastName(data.get("lastName"));
        }
        if (data.containsKey("isActive")) {
            request.setIsActive(Boolean.parseBoolean(data.get("isActive")));
        }

        return request;
    }
}
