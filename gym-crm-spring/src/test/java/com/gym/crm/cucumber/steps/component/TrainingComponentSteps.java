package com.gym.crm.cucumber.steps.component;

import com.gym.crm.cucumber.support.*;
import com.gym.crm.dto.request.AddTrainingRequest;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingComponentSteps {

    @Autowired
    private RestAssuredClient restClient;

    @Autowired
    private TestContext testContext;

    @Autowired
    private TestDataBuilder dataBuilder;

    @When("trainee {string} creates a training with:")
    public void traineeCreatesTrainingWith(String traineeUsername, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        AddTrainingRequest request = dataBuilder.buildTrainingRequest(data);

        request.setTraineeUsername(testContext.getActualUsername(request.getTraineeUsername()));
        request.setTrainerUsername(testContext.getActualUsername(request.getTrainerUsername()));

        String token = testContext.getToken(traineeUsername);

        Response response = restClient.post("/api/trainings", request, token);
        testContext.setLastResponse(response);
    }

    @Given("trainee {string} has created a training with trainer {string}")
    public void traineeHasCreatedTraining(String traineeUsername, String trainerUsername) {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTrainingName("Test Training");
        request.setTraineeUsername(testContext.getActualUsername(traineeUsername));
        request.setTrainerUsername(testContext.getActualUsername(trainerUsername));
        request.setTrainingDate(LocalDate.now().plusDays(1));
        request.setTrainingDuration(60);

        String token = testContext.getToken(traineeUsername);

        Response response = restClient.post("/api/trainings", request, token);
        testContext.setLastResponse(response);
    }

    @When("trainee {string} retrieves their trainings")
    public void traineeRetrievesTrainings(String traineeUsername) {
        String token = testContext.getToken(traineeUsername);
        String actualUsername = testContext.getActualUsername(traineeUsername);

        Response response = restClient.get("/api/trainees/" + actualUsername + "/trainings", token);
        testContext.setLastResponse(response);
    }

    @When("trainee creates a training with duration {int} minutes")
    public void traineeCreatesTrainingWithDuration(int duration) {
        String traineeUsername = testContext.getUserCredentials().keySet().stream()
            .filter(username -> testContext.getToken(username) != null)
            .findFirst()
            .orElse(testContext.getCurrentUsername());

        AddTrainingRequest request = new AddTrainingRequest();
        request.setTrainingName("Test Training");
        request.setTraineeUsername(testContext.getActualUsername(traineeUsername));
        request.setTrainerUsername(testContext.getActualUsername("jane.trainer"));
        request.setTrainingDate(LocalDate.now().plusDays(1));
        request.setTrainingDuration(duration);

        String token = testContext.getToken(traineeUsername);

        Response response = restClient.post("/api/trainings", request, token);
        testContext.setLastResponse(response);
    }

    @When("an unauthenticated user attempts to create a training")
    public void unauthenticatedUserAttemptsToCreateTraining() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTrainingName("Test Training");
        request.setTraineeUsername("john.doe");
        request.setTrainerUsername("jane.trainer");
        request.setTrainingDate(LocalDate.now().plusDays(1));
        request.setTrainingDuration(60);

        Response response = restClient.post("/api/trainings", request);
        testContext.setLastResponse(response);
    }

    @When("a user retrieves the list of training types")
    public void userRetrievesTrainingTypes() {
        Response response = restClient.get("/api/training-types");
        testContext.setLastResponse(response);
    }

    @Then("the training creation should be successful")
    public void trainingCreationSuccessful() {
        int statusCode = testContext.getLastResponse().getStatusCode();
        assertThat(statusCode).isIn(200, 201);
    }

    @Then("the training creation should fail with status {int}")
    public void trainingCreationFailsWithStatus(int expectedStatus) {
        assertThat(testContext.getLastResponse().getStatusCode()).isEqualTo(expectedStatus);
    }

    @Then("the training list should not be empty")
    public void trainingListNotEmpty() {
        String body = testContext.getLastResponse().getBody().asString();
        assertThat(body).isNotNull();
        assertThat(body).contains("trainingName");
    }

    @Then("the training types should include {string}")
    public void trainingTypesInclude(String typeName) {
        String body = testContext.getLastResponse().getBody().asString();
        assertThat(body).contains(typeName);
    }
}
