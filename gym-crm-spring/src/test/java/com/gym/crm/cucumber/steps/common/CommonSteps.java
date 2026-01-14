package com.gym.crm.cucumber.steps.common;

import com.gym.crm.cucumber.support.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    @Autowired
    private TestContext testContext;

    @Given("the system is ready for testing")
    public void systemIsReady() {
        assertThat(testContext).isNotNull();
    }

    @Then("the request should be successful with status {int}")
    public void requestSuccessfulWithStatus(int expectedStatus) {
        assertThat(testContext.getLastResponse().getStatusCode())
            .isEqualTo(expectedStatus);
    }

    @Then("the request should fail with status {int}")
    public void requestFailsWithStatus(int expectedStatus) {
        assertThat(testContext.getLastResponse().getStatusCode())
            .isEqualTo(expectedStatus);
    }

    @Then("the error message should contain {string}")
    public void errorMessageContains(String expectedMessage) {
        String body = testContext.getLastResponse().getBody().asString();
        assertThat(body).containsIgnoringCase(expectedMessage);
    }

    @Then("the response should contain username {string}")
    public void responseContainsUsername(String expectedUsername) {
        String body = testContext.getLastResponse().getBody().asString();
        assertThat(body).containsIgnoringCase(expectedUsername);
    }
}
