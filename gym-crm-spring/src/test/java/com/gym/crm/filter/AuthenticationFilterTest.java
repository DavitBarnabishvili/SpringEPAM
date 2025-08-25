package com.gym.crm.filter;

import com.gym.crm.util.impl.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() throws IOException {
        authenticationFilter = new AuthenticationFilter(jwtUtil);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void doFilter_ShouldPassThrough_WhenPublicEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/trainees/register");
        when(request.getMethod()).thenReturn("POST");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response).setHeader(eq("X-Transaction-Id"), anyString());
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void doFilter_ShouldPassThrough_ForLoginEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("GET");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void doFilter_ShouldPassThrough_ForTrainerRegistration() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/trainers/register");
        when(request.getMethod()).thenReturn("POST");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void doFilter_ShouldPassThrough_ForTrainingTypes() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/training-types");
        when(request.getMethod()).thenReturn("GET");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void doFilter_ShouldPassThrough_ForSwaggerUI() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        when(request.getMethod()).thenReturn("GET");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void doFilter_ShouldPassThrough_ForApiDocs() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/v3/api-docs");
        when(request.getMethod()).thenReturn("GET");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void doFilter_ShouldPassThrough_ForH2Console() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/h2-console/login.jsp");
        when(request.getMethod()).thenReturn("GET");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
    }

    @Test
    void doFilter_ShouldAuthenticate_WhenProtectedEndpointWithValidToken() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request).setAttribute("authenticatedUsername", "john.doe");
        verify(request).setAttribute("authenticatedRole", "TRAINEE");
        verify(request).setAttribute("authenticatedUserId", 123L);
    }

    @Test
    void doFilter_ShouldRejectRequest_WhenNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(printWriter).write(contains("Authentication required"));
    }

    @Test
    void doFilter_ShouldRejectRequest_WhenAuthorizationHeaderInvalid() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(printWriter).write(contains("Authentication required"));
    }

    @Test
    void doFilter_ShouldRejectRequest_WhenTokenInvalid() throws ServletException, IOException {
        String invalidToken = "invalid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(printWriter).write(contains("Invalid or expired token"));
    }

    @Test
    void doFilter_ShouldSetTransactionId() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/training-types");
        when(request.getMethod()).thenReturn("GET");

        authenticationFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        assertThat(headerNameCaptor.getValue()).isEqualTo("X-Transaction-Id");
        assertThat(headerValueCaptor.getValue()).isNotEmpty();
    }

    @Test
    void doFilter_ShouldHandleTrainerAuthentication() throws ServletException, IOException {
        String validToken = "valid.trainer.token";
        when(request.getRequestURI()).thenReturn("/api/trainers/profile/jane.smith");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("jane.smith");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINER");
        when(jwtUtil.extractUserId(validToken)).thenReturn(456L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request).setAttribute("authenticatedUsername", "jane.smith");
        verify(request).setAttribute("authenticatedRole", "TRAINER");
        verify(request).setAttribute("authenticatedUserId", 456L);
    }

    @Test
    void doFilter_ShouldHandleUpdateTraineeProfile() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request).setAttribute("authenticatedUsername", "john.doe");
    }

    @Test
    void doFilter_ShouldHandleDeleteTraineeProfile() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldHandleActivateDeactivateEndpoint() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe/activate");
        when(request.getMethod()).thenReturn("PATCH");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldHandleTrainingsEndpoint() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainings");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldHandleGetTraineeTrainings() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/john.doe/trainings");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldHandleGetNotAssignedTrainers() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/john.doe/not-assigned-trainers");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldHandleUpdateTrainersList() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        when(request.getRequestURI()).thenReturn("/api/trainees/john.doe/trainers");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUsername(validToken)).thenReturn("john.doe");
        when(jwtUtil.extractRole(validToken)).thenReturn("TRAINEE");
        when(jwtUtil.extractUserId(validToken)).thenReturn(123L);

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldRejectRequest_WhenBearerPrefixMissing() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("just.a.token");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doFilter_ShouldHandleEmptyBearerToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/trainees/profile/john.doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        authenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}