package com.gym.crm.cucumber.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Lazy
public class RestAssuredClient {

    @LocalServerPort
    private int port;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    public Response post(String endpoint, Object body) {
        return given()
                .body(body)
                .post(baseUrl() + endpoint);
    }

    public Response post(String endpoint, Object body, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .body(body)
                .post(baseUrl() + endpoint);
    }

    public Response get(String endpoint) {
        return given()
                .get(baseUrl() + endpoint);
    }

    public Response get(String endpoint, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .get(baseUrl() + endpoint);
    }

    public Response get(String endpoint, Map<String, String> queryParams, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .queryParams(queryParams)
                .get(baseUrl() + endpoint);
    }

    public Response put(String endpoint, Object body, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .body(body)
                .put(baseUrl() + endpoint);
    }

    public Response patch(String endpoint, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .patch(baseUrl() + endpoint);
    }

    public Response patch(String endpoint, Map<String, String> queryParams, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .queryParams(queryParams)
                .patch(baseUrl() + endpoint);
    }

    public Response delete(String endpoint, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .delete(baseUrl() + endpoint);
    }

    private RequestSpecification given() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().all();
    }
}
