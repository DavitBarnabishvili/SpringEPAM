package com.gym.crm.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);

        when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("Basic Commence Functionality Tests")
    class BasicCommenceTests {

        @Test
        @DisplayName("Should set correct response status and content type")
        void commence_ShouldSetCorrectResponseHeaders() throws Exception {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(authException.getMessage()).thenReturn("Test auth exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should write JSON error response")
        void commence_ShouldWriteJsonErrorResponse() throws Exception {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(authException.getMessage()).thenReturn("Test auth exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            printWriter.flush();
            String jsonResponse = responseWriter.toString();

            assertThat(jsonResponse).isNotEmpty();
            assertThat(jsonResponse).contains("\"status\":401");
            assertThat(jsonResponse).contains("\"error\":\"Unauthorized\"");
            assertThat(jsonResponse).contains("\"message\":\"Authentication required\"");
            assertThat(jsonResponse).contains("\"path\":\"/api/test\"");
            assertThat(jsonResponse).contains("\"timestamp\":");
        }

        @Test
        @DisplayName("Should handle different request URIs")
        void commence_ShouldHandleDifferentRequestURIs() throws Exception {
            String[] testPaths = {
                    "/api/trainee/profile",
                    "/api/trainer/update",
                    "/api/trainings",
                    "/protected/resource",
                    "/",
                    ""
            };

            for (String path : testPaths) {
                responseWriter.getBuffer().setLength(0);
                when(request.getRequestURI()).thenReturn(path);
                when(request.getMethod()).thenReturn("GET");
                when(authException.getMessage()).thenReturn("Test exception");

                jwtAuthenticationEntryPoint.commence(request, response, authException);

                printWriter.flush();
                String jsonResponse = responseWriter.toString();
                assertThat(jsonResponse).contains("\"path\":\"" + path + "\"");
            }
        }

        @Test
        @DisplayName("Should handle different HTTP methods")
        void commence_ShouldHandleDifferentHttpMethods() throws Exception {
            String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};

            for (String method : httpMethods) {
                when(request.getRequestURI()).thenReturn("/api/test");
                when(request.getMethod()).thenReturn(method);
                when(authException.getMessage()).thenReturn("Test exception");

                jwtAuthenticationEntryPoint.commence(request, response, authException);
                verify(request, atLeastOnce()).getMethod();
            }
        }
    }

    @Nested
    @DisplayName("Error Response Content Tests")
    class ErrorResponseContentTests {

        @Test
        @DisplayName("Should include all required fields in error response")
        void commence_ShouldIncludeAllRequiredFields() throws Exception {
            when(request.getRequestURI()).thenReturn("/api/secure");
            when(request.getMethod()).thenReturn("POST");
            when(authException.getMessage()).thenReturn("Token expired");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            printWriter.flush();
            String jsonResponse = responseWriter.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> errorResponse = objectMapper.readValue(jsonResponse, Map.class);

            assertThat(errorResponse).containsKeys("timestamp", "status", "error", "message", "path");
            assertThat(errorResponse.get("status")).isEqualTo(401);
            assertThat(errorResponse.get("error")).isEqualTo("Unauthorized");
            assertThat(errorResponse.get("message")).isEqualTo("Authentication required");
            assertThat(errorResponse.get("path")).isEqualTo("/api/secure");
            assertThat(errorResponse.get("timestamp")).isNotNull();
        }

        @Test
        @DisplayName("Should generate valid JSON format")
        void commence_ShouldGenerateValidJsonFormat() throws Exception {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(authException.getMessage()).thenReturn("Test exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            printWriter.flush();
            String jsonResponse = responseWriter.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            assertThatCode(() -> objectMapper.readTree(jsonResponse))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle timestamp formatting")
        void commence_ShouldHandleTimestampFormatting() throws Exception {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(authException.getMessage()).thenReturn("Test exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            printWriter.flush();
            String jsonResponse = responseWriter.toString();

            assertThat(jsonResponse).containsPattern("\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("Should use consistent error message")
        void commence_ShouldUseConsistentErrorMessage() throws Exception {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");

            String[] exceptionMessages = {
                    "Invalid token",
                    "Token expired",
                    "Malformed JWT",
                    "Access denied",
                    null
            };

            for (String exceptionMessage : exceptionMessages) {
                responseWriter.getBuffer().setLength(0);
                when(authException.getMessage()).thenReturn(exceptionMessage);

                jwtAuthenticationEntryPoint.commence(request, response, authException);

                printWriter.flush();
                String jsonResponse = responseWriter.toString();
                assertThat(jsonResponse).contains("\"message\":\"Authentication required\"");
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null request URI")
        void commence_ShouldHandleNullRequestUri() throws Exception {
            when(request.getRequestURI()).thenReturn(null);
            when(request.getMethod()).thenReturn("GET");
            when(authException.getMessage()).thenReturn("Test exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            printWriter.flush();
            String jsonResponse = responseWriter.toString();

            assertThat(jsonResponse).contains("\"path\":null");
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should handle null HTTP method")
        void commence_ShouldHandleNullHttpMethod() throws Exception {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn(null);
            when(authException.getMessage()).thenReturn("Test exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should handle very long request URIs")
        void commence_ShouldHandleVeryLongRequestUris() throws Exception {
            String longUri = "/api/very/long/path/that/might/cause/issues/" + "segment/".repeat(100);
            when(request.getRequestURI()).thenReturn(longUri);
            when(request.getMethod()).thenReturn("GET");
            when(authException.getMessage()).thenReturn("Test exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            printWriter.flush();
            String jsonResponse = responseWriter.toString();

            assertThat(jsonResponse).contains("\"path\":\"" + longUri + "\"");
        }

        @Test
        @DisplayName("Should handle special characters in request URI")
        void commence_ShouldHandleSpecialCharactersInUri() throws Exception {
            String specialUri = "/api/test with spaces/symbols!@#$%^&*()";
            when(request.getRequestURI()).thenReturn(specialUri);
            when(request.getMethod()).thenReturn("GET");
            when(authException.getMessage()).thenReturn("Test exception");

            jwtAuthenticationEntryPoint.commence(request, response, authException);

            printWriter.flush();
            String jsonResponse = responseWriter.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            assertThatCode(() -> objectMapper.readTree(jsonResponse))
                    .doesNotThrowAnyException();

            JsonNode node = objectMapper.readTree(jsonResponse);
            assertThat(node.isObject()).isTrue();
        }

        @Nested
        @DisplayName("Integration and Real-world Scenario Tests")
        class IntegrationTests {

            @Test
            @DisplayName("Should handle common JWT authentication scenarios")
            void commence_ShouldHandleCommonJwtScenarios() throws Exception {
                // Test common scenarios that would trigger this entry point
                String[][] scenarios = {
                        {"/api/trainee/profile/john.doe", "GET", "JWT token expired"},
                        {"/api/trainer/update", "PUT", "Invalid JWT signature"},
                        {"/api/trainings", "POST", "JWT token malformed"},
                        {"/api/secure/admin", "DELETE", "No JWT token provided"},
                        {"/api/protected/resource", "PATCH", "JWT token blacklisted"}
                };

                for (String[] scenario : scenarios) {
                    responseWriter.getBuffer().setLength(0); // Clear buffer

                    when(request.getRequestURI()).thenReturn(scenario[0]);
                    when(request.getMethod()).thenReturn(scenario[1]);
                    when(authException.getMessage()).thenReturn(scenario[2]);

                    jwtAuthenticationEntryPoint.commence(request, response, authException);

                    printWriter.flush();
                    String jsonResponse = responseWriter.toString();

                    assertThat(jsonResponse).contains("\"status\":401");
                    assertThat(jsonResponse).contains("\"error\":\"Unauthorized\"");
                    assertThat(jsonResponse).contains("\"path\":\"" + scenario[0] + "\"");
                }
            }

            @Test
            @DisplayName("Should work consistently across multiple calls")
            void commence_ShouldWorkConsistentlyAcrossMultipleCalls() throws Exception {
                for (int i = 0; i < 10; i++) {
                    responseWriter.getBuffer().setLength(0);

                    when(request.getRequestURI()).thenReturn("/api/test" + i);
                    when(request.getMethod()).thenReturn("GET");
                    when(authException.getMessage()).thenReturn("Test exception " + i);

                    jwtAuthenticationEntryPoint.commence(request, response, authException);

                    printWriter.flush();
                    String jsonResponse = responseWriter.toString();

                    assertThat(jsonResponse).contains("\"status\":401");
                    assertThat(jsonResponse).contains("\"path\":\"/api/test" + i + "\"");
                }

                verify(response, times(10)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }

            @Test
            @DisplayName("Should produce response compatible with typical error handlers")
            void commence_ShouldProduceCompatibleErrorResponse() throws Exception {
                when(request.getRequestURI()).thenReturn("/api/protected");
                when(request.getMethod()).thenReturn("POST");
                when(authException.getMessage()).thenReturn("Authentication failed");

                jwtAuthenticationEntryPoint.commence(request, response, authException);

                printWriter.flush();
                String jsonResponse = responseWriter.toString();

                ObjectMapper objectMapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> errorResponse = objectMapper.readValue(jsonResponse, Map.class);

                assertThat(errorResponse.get("status")).isInstanceOf(Integer.class);
                assertThat(errorResponse.get("error")).isInstanceOf(String.class);
                assertThat(errorResponse.get("message")).isInstanceOf(String.class);
                assertThat(errorResponse.get("path")).isInstanceOf(String.class);
                assertThat(errorResponse.get("timestamp")).isInstanceOf(String.class);

                assertThat((Integer) errorResponse.get("status")).isEqualTo(401);
                assertThat((String) errorResponse.get("error")).isNotBlank();
                assertThat((String) errorResponse.get("message")).isNotBlank();
            }
        }

        @Nested
        @DisplayName("Component and Annotation Tests")
        class ComponentTests {

            @Test
            @DisplayName("Should be properly annotated as Component")
            void shouldBeProperlyAnnotatedAsComponent() {
                assertThat(JwtAuthenticationEntryPoint.class.isAnnotationPresent(org.springframework.stereotype.Component.class))
                        .isTrue();
            }

            @Test
            @DisplayName("Should implement AuthenticationEntryPoint interface")
            void shouldImplementAuthenticationEntryPointInterface() {
                assertThat(org.springframework.security.web.AuthenticationEntryPoint.class)
                        .isAssignableFrom(JwtAuthenticationEntryPoint.class);
            }

            @Test
            @DisplayName("Should have correct commence method signature")
            void shouldHaveCorrectCommenceMethodSignature() throws NoSuchMethodException {
                java.lang.reflect.Method commenceMethod = JwtAuthenticationEntryPoint.class.getMethod(
                        "commence",
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        AuthenticationException.class
                );

                assertThat(commenceMethod).isNotNull();
                assertThat(commenceMethod.getReturnType()).isEqualTo(void.class);
            }
        }
    }
}