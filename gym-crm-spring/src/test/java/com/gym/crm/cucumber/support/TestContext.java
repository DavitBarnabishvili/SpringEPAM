package com.gym.crm.cucumber.support;

import io.restassured.response.Response;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Component
public class TestContext {

    private Response lastResponse;
    private Map<String, Response> namedResponses = new HashMap<>();

    private String currentUsername;
    private String currentPassword;
    private String currentToken;
    private Map<String, String> userCredentials = new HashMap<>();
    private Map<String, String> userTokens = new HashMap<>();
    private Map<String, String> usernameAliases = new HashMap<>(); // Maps expected username -> actual username

    private Long lastTraineeId;
    private Long lastTrainerId;
    private Long lastTrainingId;
    private Map<String, Long> entityIds = new HashMap<>();

    private List<Object> receivedMessages = new ArrayList<>();

    private Map<String, Object> testData = new HashMap<>();

    public void reset() {
        lastResponse = null;
        namedResponses.clear();
        currentUsername = null;
        currentPassword = null;
        currentToken = null;
        userCredentials.clear();
        userTokens.clear();
        usernameAliases.clear();
        lastTraineeId = null;
        lastTrainerId = null;
        lastTrainingId = null;
        entityIds.clear();
        receivedMessages.clear();
        testData.clear();
    }

    public void storeCredentials(String username, String password) {
        userCredentials.put(username, password);
    }

    public void storeToken(String username, String token) {
        userTokens.put(username, token);
    }

    public String getToken(String username) {
        String token = userTokens.get(username);
        if (token != null) {
            return token;
        }
        String actualUsername = usernameAliases.get(username);
        if (actualUsername != null) {
            return userTokens.get(actualUsername);
        }
        return null;
    }

    public void mapUsernameAlias(String expectedUsername, String actualUsername) {
        usernameAliases.put(expectedUsername, actualUsername);
    }

    public String getPassword(String username) {
        String password = userCredentials.get(username);
        if (password != null) {
            return password;
        }
        String actualUsername = usernameAliases.get(username);
        if (actualUsername != null) {
            return userCredentials.get(actualUsername);
        }
        return null;
    }

    public String getActualUsername(String aliasOrActual) {
        String actual = usernameAliases.get(aliasOrActual);
        return actual != null ? actual : aliasOrActual;
    }
}
