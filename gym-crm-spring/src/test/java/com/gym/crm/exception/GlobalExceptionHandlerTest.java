package com.gym.crm.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationExceptions_ShouldReturn400() {
        ValidationException exception = new ValidationException("Validation failed");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Validation failed");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleInvalidCredentials_ShouldReturn401() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid username or password");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidCredentials(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("Unauthorized");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid username or password");
    }

    @Test
    void handleForbidden_InactiveAccount_ShouldReturn403() {
        InactiveAccountException exception = new InactiveAccountException("Account is inactive");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleForbidden(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(403);
        assertThat(response.getBody().get("error")).isEqualTo("Forbidden");
        assertThat(response.getBody().get("message")).isEqualTo("Account is inactive");
    }

    @Test
    void handleForbidden_UnauthorizedAccess_ShouldReturn403() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Access denied");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleForbidden(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(403);
        assertThat(response.getBody().get("error")).isEqualTo("Forbidden");
        assertThat(response.getBody().get("message")).isEqualTo("Access denied");
    }

    @Test
    void handleMissingResources_ShouldReturn404() {
        NotFoundException exception = new NotFoundException("Resource not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMissingResources(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Resource not found");
    }

    @Test
    void handleMissingResources_UserNotFound_ShouldReturn404() {
        UserNotFoundException exception = new UserNotFoundException("User not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMissingResources(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("message")).isEqualTo("User not found");
    }

    @Test
    void handleValidationExceptions_MethodArgumentNotValid_ShouldReturn400() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Field is required");
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Field is required");
    }

    @Test
    void handleGeneric_ShouldReturn500() {
        Exception exception = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("Internal server error");
    }

    @Test
    void handleGeneric_NullPointerException_ShouldReturn500() {
        NullPointerException exception = new NullPointerException();

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("message")).isEqualTo("Internal server error");
    }

    @Test
    void allHandlers_ShouldIncludeTimestamp() {
        ValidationException validationEx = new ValidationException("Test");
        InvalidCredentialsException credentialsEx = new InvalidCredentialsException("Test");
        InactiveAccountException inactiveEx = new InactiveAccountException("Test");
        NotFoundException notFoundEx = new NotFoundException("Test");
        Exception genericEx = new Exception("Test");

        ResponseEntity<Map<String, Object>> response1 = exceptionHandler.handleValidationExceptions(validationEx);
        ResponseEntity<Map<String, Object>> response2 = exceptionHandler.handleInvalidCredentials(credentialsEx);
        ResponseEntity<Map<String, Object>> response3 = exceptionHandler.handleForbidden(inactiveEx);
        ResponseEntity<Map<String, Object>> response4 = exceptionHandler.handleMissingResources(notFoundEx);
        ResponseEntity<Map<String, Object>> response5 = exceptionHandler.handleGeneric(genericEx);

        assertThat(Objects.requireNonNull(response1.getBody()).get("timestamp")).isNotNull();
        assertThat(Objects.requireNonNull(response2.getBody()).get("timestamp")).isNotNull();
        assertThat(Objects.requireNonNull(response3.getBody()).get("timestamp")).isNotNull();
        assertThat(Objects.requireNonNull(response4.getBody()).get("timestamp")).isNotNull();
        assertThat(Objects.requireNonNull(response5.getBody()).get("timestamp")).isNotNull();
    }

    @Test
    void handleValidationException_WithNullMessage() {
        ValidationException exception = new ValidationException(null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isNull();
    }

    @Test
    void handleInvalidCredentialsException_WithCause() {
        Throwable cause = new RuntimeException("Root cause");
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials", cause);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidCredentials(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("Invalid credentials");
    }

    @Test
    void handleInactiveAccountException_WithCause() {
        Throwable cause = new RuntimeException("Root cause");
        InactiveAccountException exception = new InactiveAccountException("Account inactive", cause);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleForbidden(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("Account inactive");
    }

    @Test
    void handleUnauthorizedAccessException_WithCause() {
        Throwable cause = new RuntimeException("Root cause");
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Unauthorized", cause);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleForbidden(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("Unauthorized");
    }

    @Test
    void handleNotFoundException_WithCause() {
        Throwable cause = new RuntimeException("Root cause");
        NotFoundException exception = new NotFoundException("Not found", cause);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMissingResources(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("Not found");
    }

    @Test
    void handleUserNotFoundException_WithCause() {
        Throwable cause = new RuntimeException("Root cause");
        UserNotFoundException exception = new UserNotFoundException("User not found", cause);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMissingResources(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("User not found");
    }

    @Test
    void responseBody_ShouldHaveConsistentStructure() {
        ValidationException exception = new ValidationException("Test message");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);
        Map<String, Object> body = response.getBody();

        assertThat(body).containsKeys("timestamp", "status", "error", "message");
        assertThat(body).hasSize(4);
    }

    @Test
    void handleMultipleFieldErrors_ShouldUseFirstError() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError firstError = new FieldError("object", "field1", "First error");
        when(bindingResult.getFieldError()).thenReturn(firstError);

        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("First error");
    }

    @Test
    void handleGeneric_ShouldNotExposeInternalDetails() {
        Exception exception = new RuntimeException("Sensitive internal error: database connection failed at xyz");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(exception);

        assertThat(Objects.requireNonNull(response.getBody()).get("message"))
                .asString()
                .isEqualTo("Internal server error")
                .doesNotContain("database")
                .doesNotContain("xyz");
    }


    @Test
    void handleIllegalArgumentException_ShouldReturn500() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("Internal server error");
    }

    @Test
    void handleIllegalStateException_ShouldReturn500() {
        IllegalStateException exception = new IllegalStateException("Invalid state");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("Internal server error");
    }
}