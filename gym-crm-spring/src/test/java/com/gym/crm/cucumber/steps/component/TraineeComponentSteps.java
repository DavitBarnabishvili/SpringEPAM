package com.gym.crm.cucumber.steps.component;

import com.gym.crm.cucumber.support.*;
import com.gym.crm.dto.request.TraineeRegistrationRequest;
import com.gym.crm.dto.request.TraineeUpdateRequest;
import com.gym.crm.dto.response.RegistrationResponse;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TraineeComponentSteps {

    @Autowired
    private RestAssuredClient restClient;

    @Autowired
    private TestContext testContext;

    @Autowired
    private TestDataBuilder dataBuilder;

    @When("a trainee registers with:")
    public void traineeRegistersWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        TraineeRegistrationRequest request = dataBuilder.buildTraineeRegistration(data);

        Response response = restClient.post("/api/trainees/register", request);
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            RegistrationResponse regResponse = response.as(RegistrationResponse.class);
            testContext.setCurrentUsername(regResponse.getUsername());
            testContext.setCurrentPassword(regResponse.getPassword());
            testContext.storeCredentials(regResponse.getUsername(), regResponse.getPassword());
        }
    }

    @When("a trainee registers with firstName {string} and lastName {string}")
    public void traineeRegistersWithNames(String firstName, String lastName) {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);

        Response response = restClient.post("/api/trainees/register", request);
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

    @When("another trainee registers with firstName {string} and lastName {string}")
    public void anotherTraineeRegisters(String firstName, String lastName) {
        traineeRegistersWithNames(firstName, lastName);
    }

    @When("the trainee retrieves their profile")
    public void traineeRetrievesProfile() {
        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Response response = restClient.get("/api/trainees/profile/" + username, token);
        testContext.setLastResponse(response);
    }

    @When("the trainee updates profile with:")
    public void traineeUpdatesProfile(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        TraineeUpdateRequest request = dataBuilder.buildTraineeUpdateRequest(data);

        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Response response = restClient.put("/api/trainees/profile/" + username, request, token);
        testContext.setLastResponse(response);
    }

    @When("the trainee deactivates their profile")
    public void traineeDeactivatesProfile() {
        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("isActive", "false");

        Response response = restClient.patch("/api/trainees/profile/" + username + "/activate", queryParams, token);
        testContext.setLastResponse(response);
    }

    @When("the trainee activates their profile")
    public void traineeActivatesProfile() {
        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("isActive", "true");

        Response response = restClient.patch("/api/trainees/profile/" + username + "/activate", queryParams, token);
        testContext.setLastResponse(response);
    }

    @When("a trainee registers with empty firstName")
    public void traineeRegistersWithEmptyFirstName() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("");
        request.setLastName("Test");

        Response response = restClient.post("/api/trainees/register", request);
        testContext.setLastResponse(response);
    }

    @Given("a trainee {string} is registered and logged in")
    public void traineeNamedIsRegisteredAndLoggedIn(String expectedUsername) {
        String[] parts = expectedUsername.split("\\.");
        String firstName = parts[0].replaceAll("\\d", "").substring(0, 1).toUpperCase() + parts[0].replaceAll("\\d", "").substring(1);
        String lastName = parts[1].replaceAll("\\d", "").substring(0, 1).toUpperCase() + parts[1].replaceAll("\\d", "").substring(1);

        traineeRegistersWithNames(firstName, lastName);

        String actualUsername = testContext.getCurrentUsername();
        String password = testContext.getCurrentPassword();

        if (actualUsername != null && password != null) {
            testContext.mapUsernameAlias(expectedUsername, actualUsername);

            Response response = restClient.get("/api/auth/login?username=" + actualUsername + "&password=" + password);

            if (response.getStatusCode() == 200) {
                String token = response.jsonPath().getString("token");
                testContext.storeToken(actualUsername, token);
            }
        }
    }

    @Given("a trainee {string} is registered")
    public void traineeNamedIsRegistered(String username) {
        String[] parts = username.split("\\.");
        String firstName = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
        String lastName = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);

        traineeRegistersWithNames(firstName, lastName);
    }

    @When("trainee {string} attempts to retrieve profile of {string}")
    public void traineeAttemptsToRetrieveOtherProfile(String requester, String target) {
        String token = testContext.getToken(requester);
        String actualTarget = testContext.getActualUsername(target);

        Response response = restClient.get("/api/trainees/profile/" + actualTarget, token);
        testContext.setLastResponse(response);
    }

    @Then("the registration should be successful")
    public void registrationSuccessful() {
        int statusCode = testContext.getLastResponse().getStatusCode();
        assertThat(statusCode).isIn(200, 201);
    }

    @Then("the registration should fail with status {int}")
    public void registrationFailsWithStatus(int expectedStatus) {
        assertThat(testContext.getLastResponse().getStatusCode()).isEqualTo(expectedStatus);
    }

    @Then("credentials should be generated")
    public void credentialsGenerated() {
        assertThat(testContext.getCurrentUsername()).isNotNull();
        assertThat(testContext.getCurrentPassword()).isNotNull();
    }

    @Then("the username should match pattern {string}")
    public void usernameMatchesPattern(String pattern) {
        String username = testContext.getCurrentUsername();
        assertThat(username.toLowerCase()).matches(pattern.toLowerCase() + "\\d*");
    }

    @Then("the username should be {string}")
    public void usernameShouldBe(String expectedUsername) {
        assertThat(testContext.getCurrentUsername().toLowerCase()).isEqualTo(expectedUsername.toLowerCase());
    }

    @Then("a random password should be returned")
    public void randomPasswordReturned() {
        assertThat(testContext.getCurrentPassword()).isNotNull();
        assertThat(testContext.getCurrentPassword().length()).isGreaterThanOrEqualTo(10);
    }

    @Then("the new username should be {string}")
    public void newUsernameShouldBe(String expectedUsername) {
        assertThat(testContext.getCurrentUsername().toLowerCase()).isEqualTo(expectedUsername.toLowerCase());
    }

    @Then("the profile should contain personal information")
    public void profileContainsPersonalInformation() {
        String body = testContext.getLastResponse().getBody().asString();
        assertThat(body).isNotNull();
        assertThat(body).contains("firstName");
    }

    @Then("the profile update should be successful")
    public void profileUpdateSuccessful() {
        assertThat(testContext.getLastResponse().getStatusCode()).isEqualTo(200);
    }

    @Then("the updated profile should reflect new values")
    public void updatedProfileReflectsNewValues() {
        String body = testContext.getLastResponse().getBody().asString();
        assertThat(body).contains("Updated");
    }
}
