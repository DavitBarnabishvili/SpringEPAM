package com.gym.crm.cucumber.steps.component;

import com.gym.crm.cucumber.support.*;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

public class SecurityComponentSteps {

    @Autowired
    private RestAssuredClient restClient;

    @Autowired
    private TestContext testContext;

    @When("trainee {string} accesses their own profile")
    public void traineeAccessesOwnProfile(String username) {
        String token = testContext.getToken(username);
        String actualUsername = testContext.getActualUsername(username);

        Response response = restClient.get("/api/trainees/profile/" + actualUsername, token);
        testContext.setLastResponse(response);
    }

    @When("trainer {string} accesses their own profile")
    public void trainerAccessesOwnProfile(String username) {
        String token = testContext.getToken(username);
        String actualUsername = testContext.getActualUsername(username);

        Response response = restClient.get("/api/trainers/profile/" + actualUsername, token);
        testContext.setLastResponse(response);
    }

    @When("an unauthenticated user accesses {string}")
    public void unauthenticatedUserAccessesEndpoint(String endpoint) {
        Response response = restClient.get(endpoint);
        testContext.setLastResponse(response);
    }
}
