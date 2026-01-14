package com.gym.crm.cucumber.steps.common;

import com.gym.crm.cucumber.support.*;
import com.gym.crm.dto.request.ChangeLoginRequest;
import com.gym.crm.dto.request.TraineeRegistrationRequest;
import com.gym.crm.dto.request.TrainerRegistrationRequest;
import com.gym.crm.dto.response.LoginResponse;
import com.gym.crm.dto.response.RegistrationResponse;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationSteps {

    @Autowired
    private RestAssuredClient restClient;

    @Autowired
    private TestContext testContext;

    @Autowired
    private TestDataBuilder dataBuilder;

    @Given("a trainee is registered with firstName {string} and lastName {string}")
    public void traineeRegisteredWithNames(String firstName, String lastName) {
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

    @Given("a trainer is registered with firstName {string} and lastName {string} and specialization {string}")
    public void trainerRegisteredWithNamesAndSpecialization(String firstName, String lastName, String specialization) {
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

    @Given("a trainee is registered and logged in")
    public void traineeRegisteredAndLoggedIn() {
        traineeRegisteredWithNames("Test", "User");
        traineeLogsInWithCorrectCredentials();
    }

    @When("the trainee logs in with correct credentials")
    public void traineeLogsInWithCorrectCredentials() {
        String username = testContext.getCurrentUsername();
        String password = testContext.getCurrentPassword();

        Response response = restClient.get("/api/auth/login?username=" + username + "&password=" + password);
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 200) {
            LoginResponse loginResponse = response.as(LoginResponse.class);
            testContext.setCurrentToken(loginResponse.getToken());
            testContext.storeToken(username, loginResponse.getToken());
        }
    }

    @When("the trainer logs in with correct credentials")
    public void trainerLogsInWithCorrectCredentials() {
        traineeLogsInWithCorrectCredentials();
    }

    @When("the trainee attempts to login with username {string} and incorrect password {string}")
    public void traineeAttemptsLoginWithIncorrectPassword(String username, String password) {
        String actualUsername = testContext.getActualUsername(username);
        Response response = restClient.get("/api/auth/login?username=" + actualUsername + "&password=" + password);
        testContext.setLastResponse(response);
    }

    @When("a user attempts to login with username {string} and password {string}")
    public void userAttemptsLogin(String username, String password) {
        String actualUsername = testContext.getActualUsername(username);
        Response response = restClient.get("/api/auth/login?username=" + actualUsername + "&password=" + password);
        testContext.setLastResponse(response);
    }

    @When("the trainee changes password to {string}")
    public void traineeChangesPassword(String newPassword) {
        String username = testContext.getCurrentUsername();
        String oldPassword = testContext.getCurrentPassword();
        String token = testContext.getCurrentToken();

        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername(username);
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);

        Response response = restClient.put("/api/auth/change-login", request, token);
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 200) {
            testContext.setCurrentPassword(newPassword);
            testContext.storeCredentials(username, newPassword);
        }
    }

    @When("the trainee logs out")
    public void traineeLogsOut() {
        String token = testContext.getCurrentToken();
        Response response = restClient.post("/api/auth/logout", "", token);
        testContext.setLastResponse(response);
    }

    @When("attempting to access protected endpoint with the same token")
    public void attemptingToAccessProtectedEndpointWithSameToken() {
        String username = testContext.getCurrentUsername();
        String token = testContext.getCurrentToken();

        Response response = restClient.get("/api/trainees/profile/" + username, token);
        testContext.setLastResponse(response);
    }

    @When("the trainee fails to login {int} times with wrong password")
    public void traineeFailsToLoginMultipleTimes(int attempts) {
        String username = testContext.getCurrentUsername();

        for (int i = 0; i < attempts; i++) {
            Response response = restClient.get("/api/auth/login?username=" + username + "&password=wrongPassword" + i);
            testContext.setLastResponse(response);
        }
    }

    @Then("the login should be successful")
    public void loginSuccessful() {
        assertThat(testContext.getLastResponse().getStatusCode()).isEqualTo(200);
    }

    @Then("the login should fail with status {int}")
    public void loginFailsWithStatus(int expectedStatus) {
        assertThat(testContext.getLastResponse().getStatusCode()).isEqualTo(expectedStatus);
    }

    @Then("a JWT token should be returned")
    public void jwtTokenReturned() {
        assertThat(testContext.getCurrentToken()).isNotNull();
        assertThat(testContext.getCurrentToken()).isNotEmpty();
    }

    @Then("the password change should be successful")
    public void passwordChangeSuccessful() {
        assertThat(testContext.getLastResponse().getStatusCode()).isEqualTo(200);
    }

    @Then("the trainee should be able to login with the new password")
    public void traineeCanLoginWithNewPassword() {
        String username = testContext.getCurrentUsername();
        String newPassword = testContext.getCurrentPassword();

        Response response = restClient.get("/api/auth/login?username=" + username + "&password=" + newPassword);
        testContext.setLastResponse(response);

        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Then("the logout should be successful")
    public void logoutSuccessful() {
        assertThat(testContext.getLastResponse().getStatusCode()).isEqualTo(200);
    }

    @Then("subsequent login attempt should fail with status {int}")
    public void subsequentLoginAttemptFails(int expectedStatus) {
        String username = testContext.getCurrentUsername();
        String password = testContext.getPassword(username);

        Response response = restClient.get("/api/auth/login?username=" + username + "&password=" + password);
        testContext.setLastResponse(response);

        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }
}
