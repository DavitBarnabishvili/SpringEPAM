package com.gym.crm.cucumber.steps.component;

import com.gym.crm.cucumber.support.*;
import com.gym.crm.dto.request.TrainerRegistrationRequest;
import com.gym.crm.dto.request.TrainerUpdateRequest;
import com.gym.crm.dto.response.RegistrationResponse;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerComponentSteps {

    @Autowired
    private RestAssuredClient restClient;

    @Autowired
    private TestContext testContext;

    @Autowired
    private TestDataBuilder dataBuilder;

    @When("a trainer registers with:")
    public void trainerRegistersWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        TrainerRegistrationRequest request = dataBuilder.buildTrainerRegistration(data);

        Response response = restClient.post("/api/trainers/register", request);
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            RegistrationResponse regResponse = response.as(RegistrationResponse.class);
            testContext.setCurrentUsername(regResponse.getUsername());
            testContext.setCurrentPassword(regResponse.getPassword());
            testContext.storeCredentials(regResponse.getUsername(), regResponse.getPassword());
        }
    }

    @When("a trainer registers with firstName {string} and lastName {string} and specialization {string}")
    public void trainerRegistersWithNames(String firstName, String lastName, String specialization) {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSpecializationId(1L);

        Response response = restClient.post("/api/trainers/register", request);
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            RegistrationResponse regResponse = response.as(RegistrationResponse.class);
            String actualUsername = regResponse.getUsername();
            testContext.setCurrentUsername(actualUsername);
            testContext.setCurrentPassword(regResponse.getPassword());
            testContext.storeCredentials(actualUsername, regResponse.getPassword());

            String lowercaseUsername = actualUsername.toLowerCase();
            testContext.mapUsernameAlias(lowercaseUsername, actualUsername);
        }
    }

    @When("a trainer registers with invalid specialization {string}")
    public void trainerRegistersWithInvalidSpecialization(String specialization) {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Test");
        request.setLastName("Trainer");
        request.setSpecializationId(999L);

        Response response = restClient.post("/api/trainers/register", request);
        testContext.setLastResponse(response);
    }

    @When("a trainer registers with empty firstName and lastName {string}")
    public void trainerRegistersWithEmptyFirstName(String lastName) {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("");
        request.setLastName(lastName);
        request.setSpecializationId(1L);

        Response response = restClient.post("/api/trainers/register", request);
        testContext.setLastResponse(response);
    }

    @Given("a trainer is registered and logged in")
    public void trainerRegisteredAndLoggedIn() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Test");
        request.setLastName("Trainer");
        request.setSpecializationId(1L);

        Response regResponse = restClient.post("/api/trainers/register", request);
        RegistrationResponse registration = regResponse.as(RegistrationResponse.class);

        testContext.setCurrentUsername(registration.getUsername());
        testContext.setCurrentPassword(registration.getPassword());
        testContext.storeCredentials(registration.getUsername(), registration.getPassword());

        Response loginResponse = restClient.get("/api/auth/login?username=" + registration.getUsername() +
            "&password=" + registration.getPassword());

        if (loginResponse.getStatusCode() == 200) {
            String token = loginResponse.jsonPath().getString("token");
            testContext.setCurrentToken(token);
            testContext.storeToken(registration.getUsername(), token);
        }
    }

    @Given("a trainer {string} is registered and logged in")
    public void trainerNamedIsRegisteredAndLoggedIn(String expectedUsername) {
        String[] parts = expectedUsername.split("\\.");
        String firstName = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
        String lastName = parts.length > 1 ?
            parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1) : "Trainer";

        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSpecializationId(1L);

        Response regResponse = restClient.post("/api/trainers/register", request);
        RegistrationResponse registration = regResponse.as(RegistrationResponse.class);

        String actualUsername = registration.getUsername();
        testContext.storeCredentials(actualUsername, registration.getPassword());

        testContext.mapUsernameAlias(expectedUsername, actualUsername);

        Response loginResponse = restClient.get("/api/auth/login?username=" + actualUsername +
            "&password=" + registration.getPassword());

        if (loginResponse.getStatusCode() == 200) {
            String token = loginResponse.jsonPath().getString("token");
            testContext.storeToken(actualUsername, token);
        }
    }

    @When("the trainer retrieves their profile")
    public void trainerRetrievesProfile() {
        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Response response = restClient.get("/api/trainers/profile/" + username, token);
        testContext.setLastResponse(response);
    }

    @When("the trainer updates profile with:")
    public void trainerUpdatesProfile(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        TrainerUpdateRequest request = dataBuilder.buildTrainerUpdateRequest(data);

        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Response response = restClient.put("/api/trainers/profile/" + username, request, token);
        testContext.setLastResponse(response);
    }

    @When("the trainer deactivates their profile")
    public void trainerDeactivatesProfile() {
        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("isActive", "false");

        Response response = restClient.patch("/api/trainers/profile/" + username + "/activate", queryParams, token);
        testContext.setLastResponse(response);
    }

    @When("the trainer activates their profile")
    public void trainerActivatesProfile() {
        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("isActive", "true");

        Response response = restClient.patch("/api/trainers/profile/" + username + "/activate", queryParams, token);
        testContext.setLastResponse(response);
    }

    @Then("the profile should contain trainer information")
    public void profileContainsTrainerInformation() {
        String body = testContext.getLastResponse().getBody().asString();
        assertThat(body).isNotNull();
        assertThat(body).contains("firstName");
    }
}
