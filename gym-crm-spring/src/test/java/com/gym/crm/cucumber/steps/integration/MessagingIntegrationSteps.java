package com.gym.crm.cucumber.steps.integration;

import com.gym.crm.cucumber.support.*;
import com.gym.crm.dto.request.AddTrainingRequest;
import com.gym.crm.dto.request.TrainerWorkloadRequest;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


public class MessagingIntegrationSteps {

    private final ActiveMQTestConsumer testConsumer;
    private final TestContext testContext;
    private final RestAssuredClient restClient;
    private final TestDataBuilder dataBuilder;

    @Autowired
    public MessagingIntegrationSteps(
            ActiveMQTestConsumer testConsumer,
            TestContext testContext,
            RestAssuredClient restClient,
            TestDataBuilder dataBuilder
    ) {
        this.testConsumer = testConsumer;
        this.testContext = testContext;
        this.restClient = restClient;
        this.dataBuilder = dataBuilder;
    }

    @Given("the test message consumer is ready")
    public void testConsumerIsReady() {
        testConsumer.clearMessages();
    }

    @When("trainee {string} creates a training session:")
    public void traineeCreatesTrainingSession(String traineeUsername, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        AddTrainingRequest request = dataBuilder.buildTrainingRequest(data);

        request.setTraineeUsername(testContext.getActualUsername(request.getTraineeUsername()));
        request.setTrainerUsername(testContext.getActualUsername(request.getTrainerUsername()));

        String token = testContext.getToken(traineeUsername);

        Response response = restClient.post("/api/trainings", request, token);
        testContext.setLastResponse(response);

        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            System.err.println("TRAINING CREATION FAILED: Status=" + response.getStatusCode() + ", Body=" + response.getBody().asString());
        }
    }

    @When("trainee {string} creates {int} training sessions with trainer {string}")
    public void traineeCreatesMultipleTrainingSessions(String traineeUsername, int count, String trainerUsername) {
        String token = testContext.getToken(traineeUsername);

        for (int i = 0; i < count; i++) {
            AddTrainingRequest request = new AddTrainingRequest();
            request.setTrainingName("Training " + (i + 1));
            request.setTraineeUsername(testContext.getActualUsername(traineeUsername));
            request.setTrainerUsername(testContext.getActualUsername(trainerUsername));
            request.setTrainingDate(LocalDate.now().plusDays(i + 1));
            request.setTrainingDuration(60);

            Response response = restClient.post("/api/trainings", request, token);
            testContext.setLastResponse(response);
        }
    }

    @Then("the training should be created successfully")
    public void trainingCreatedSuccessfully() {
        int statusCode = testContext.getLastResponse().getStatusCode();
        assertThat(statusCode).isIn(200, 201);
    }

    @Then("within {int} seconds a message should be received")
    public void withinSecondsMessageReceived(int seconds) {
        await().atMost(seconds, TimeUnit.SECONDS)
               .until(() -> testConsumer.getMessageCount() > 0);
    }

    @Then("within {int} seconds exactly {int} messages should be received")
    public void withinSecondsExactlyNMessagesReceived(int seconds, int expectedCount) {
        await().atMost(seconds, TimeUnit.SECONDS)
               .until(() -> testConsumer.getMessageCount() >= expectedCount);

        assertThat(testConsumer.getMessageCount()).isGreaterThanOrEqualTo(expectedCount);
    }

    @Then("the message should have actionType {string}")
    public void messageHasActionType(String expectedActionType) {
        TrainerWorkloadRequest lastMessage = testConsumer.getLastMessage();
        assertThat(lastMessage).isNotNull();
        assertThat(lastMessage.getActionType().name()).isEqualTo(expectedActionType);
    }

    @Then("the message should contain trainerUsername {string}")
    public void messageContainsTrainerUsername(String expectedUsername) {
        TrainerWorkloadRequest lastMessage = testConsumer.getLastMessage();
        assertThat(lastMessage).isNotNull();
        String actualUsername = testContext.getActualUsername(expectedUsername);
        assertThat(lastMessage.getTrainerUsername()).isEqualTo(actualUsername);
    }

    @Then("the message should contain trainingDuration {int}")
    public void messageContainsTrainingDuration(int expectedDuration) {
        TrainerWorkloadRequest lastMessage = testConsumer.getLastMessage();
        assertThat(lastMessage).isNotNull();
        assertThat(lastMessage.getTrainingDuration()).isEqualTo(expectedDuration);
    }

    @Then("the message should have a valid transactionId")
    public void messageHasValidTransactionId() {
        TrainerWorkloadRequest lastMessage = testConsumer.getLastMessage();
        assertThat(lastMessage).isNotNull();
        assertThat(lastMessage.getTransactionId()).isNotNull();
    }

    @Then("all messages should have actionType {string}")
    public void allMessagesHaveActionType(String expectedActionType) {
        for (TrainerWorkloadRequest message : testConsumer.getReceivedMessages()) {
            assertThat(message.getActionType().name()).isEqualTo(expectedActionType);
        }
    }
}
