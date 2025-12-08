package com.example.bankcards.security;

import com.example.bankcards.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {
        outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override
            public void write(int b) {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            }
        });
    }

    @Test
    void commence_AuthenticationException_Returns401WithErrorResponse() throws Exception {
        AuthenticationException exception = new BadCredentialsException("Invalid credentials");
        when(request.getRequestURI()).thenReturn("/api/cards");

        jwtAuthenticationEntryPoint.commence(request, response, exception);

        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String responseBody = outputStream.toString();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"status\":401"));
        assertTrue(responseBody.contains("\"error\":\"Unauthorized\""));
        assertTrue(responseBody.contains("Unauthorized: Invalid credentials"));
        assertTrue(responseBody.contains("\"/api/cards\""));
    }

    @Test
    void commence_ExceptionWithNullMessage_HandlesGracefully() throws Exception {
        AuthenticationException exception = new AuthenticationException("") {
        };
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        jwtAuthenticationEntryPoint.commence(request, response, exception);

        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String responseBody = outputStream.toString();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"status\":401"));
    }

    @Test
    void commence_DifferentRequestPaths_IncludesCorrectPath() throws Exception {
        AuthenticationException exception = new BadCredentialsException("Access denied");
        when(request.getRequestURI()).thenReturn("/api/admin/users");

        jwtAuthenticationEntryPoint.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"/api/admin/users\""));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void commence_VerifyContentTypeSet_SetsApplicationJson() throws Exception {
        AuthenticationException exception = new BadCredentialsException("Test error");
        when(request.getRequestURI()).thenReturn("/test");

        jwtAuthenticationEntryPoint.commence(request, response, exception);

        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void commence_VerifyStatusCode_Sets401() throws Exception {
        AuthenticationException exception = new BadCredentialsException("Test error");
        when(request.getRequestURI()).thenReturn("/test");

        jwtAuthenticationEntryPoint.commence(request, response, exception);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setStatus(401);
    }

    @Test
    void commence_ErrorResponseStructure_ContainsAllFields() throws Exception {
        AuthenticationException exception = new BadCredentialsException("Authentication failed");
        when(request.getRequestURI()).thenReturn("/api/secure");

        jwtAuthenticationEntryPoint.commence(request, response, exception);

        String responseBody = outputStream.toString();
        ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.timestamp());
        assertEquals(401, errorResponse.status());
        assertEquals("Unauthorized", errorResponse.error());
        assertEquals("Unauthorized: Authentication failed", errorResponse.message());
        assertEquals("/api/secure", errorResponse.path());
    }
}
