package com.example.bankcards.exception;

import com.example.bankcards.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ReflectionTestUtils.setField(globalExceptionHandler, "activeProfile", "local");
    }

    @Test
    void handleResourceNotFoundException_ReturnsNotFoundResponse() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleResourceNotFoundException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Resource not found", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleBadRequestException_ReturnsBadRequestResponse() {
        BadRequestException exception = new BadRequestException("Bad request");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleBadRequestException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Bad request", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void handleUnauthorizedException_ReturnsUnauthorizedResponse() {
        UnauthorizedException exception = new UnauthorizedException("Unauthorized");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleUnauthorizedException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().status());
        assertEquals("Unauthorized", response.getBody().error());
        assertEquals("Unauthorized", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void handleInsufficientBalanceException_ReturnsBadRequestResponse() {
        InsufficientBalanceException exception = new InsufficientBalanceException("Insufficient balance");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleInsufficientBalanceException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Insufficient balance", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void handleValidationExceptions_ReturnsBadRequestWithValidationErrors() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "error message");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(
                this.getClass().getDeclaredMethod("setUp"), -1
        );
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleValidationExceptions(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Validation failed", response.getBody().message());
        assertNotNull(response.getBody().validationErrors());
        assertEquals(1, response.getBody().validationErrors().size());
        assertTrue(response.getBody().validationErrors().containsKey("fieldName"));
        assertEquals("error message", response.getBody().validationErrors().get("fieldName"));
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequestResponse() {
        IllegalArgumentException exception = new IllegalArgumentException("Illegal argument");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleIllegalArgumentException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Illegal argument", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolation_WithUniqueConstraint_ReturnsConflictResponse() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "unique constraint violation"
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDataIntegrityViolation(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Record already exists", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolation_WithoutUniqueConstraint_ReturnsGenericMessage() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "some other violation"
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDataIntegrityViolation(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Data integrity violation occurred", response.getBody().message());
    }

    @Test
    void handleAccessDeniedException_ReturnsForbiddenResponse() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleAccessDeniedException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().status());
        assertEquals("Forbidden", response.getBody().error());
        assertEquals("You don't have permission to access this resource", response.getBody().message());
    }

    @Test
    void handleAuthenticationException_ReturnsUnauthorizedResponse() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleAuthenticationException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().status());
        assertEquals("Unauthorized", response.getBody().error());
        assertEquals("Invalid username or password", response.getBody().message());
    }

    @Test
    void handleHttpMessageNotReadable_ReturnsBadRequestResponse() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("Malformed JSON");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleHttpMessageNotReadable(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Malformed JSON request", response.getBody().message());
    }

    @Test
    void handleGlobalException_InDevelopmentMode_ReturnsDetailedError() {
        ReflectionTestUtils.setField(globalExceptionHandler, "activeProfile", "local");
        Exception exception = new RuntimeException("Test error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGlobalException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertTrue(response.getBody().message().contains("RuntimeException"));
        assertTrue(response.getBody().message().contains("Test error"));
    }

    @Test
    void handleGlobalException_InProductionMode_ReturnsGenericError() {
        ReflectionTestUtils.setField(globalExceptionHandler, "activeProfile", "prod");
        Exception exception = new RuntimeException("Test error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGlobalException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    void handleGlobalException_InDevMode_ReturnsDetailedError() {
        ReflectionTestUtils.setField(globalExceptionHandler, "activeProfile", "dev");
        Exception exception = new NullPointerException("NPE error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGlobalException(exception, request);

        assertNotNull(response);
        assertTrue(response.getBody().message().contains("NullPointerException"));
    }

    @Test
    void allHandlers_SetCorrectTimestamp() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleResourceNotFoundException(exception, request);

        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void allHandlers_SetCorrectRequestPath() {
        when(request.getRequestURI()).thenReturn("/api/cards/123");
        BadRequestException exception = new BadRequestException("Test");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleBadRequestException(exception, request);

        assertEquals("/api/cards/123", response.getBody().path());
    }
}
