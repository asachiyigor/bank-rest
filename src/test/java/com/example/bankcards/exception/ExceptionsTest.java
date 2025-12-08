package com.example.bankcards.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void resourceNotFoundException_WithMessage_CreatesExceptionWithMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void resourceNotFoundException_WithMessageAndCause_CreatesExceptionWithBoth() {
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Root cause");
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void badRequestException_WithMessage_CreatesExceptionWithMessage() {
        String message = "Bad request";
        BadRequestException exception = new BadRequestException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void badRequestException_WithMessageAndCause_CreatesExceptionWithBoth() {
        String message = "Bad request";
        Throwable cause = new IllegalArgumentException("Invalid argument");
        BadRequestException exception = new BadRequestException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void unauthorizedException_WithMessage_CreatesExceptionWithMessage() {
        String message = "Unauthorized access";
        UnauthorizedException exception = new UnauthorizedException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void unauthorizedException_WithMessageAndCause_CreatesExceptionWithBoth() {
        String message = "Unauthorized access";
        Throwable cause = new SecurityException("Security violation");
        UnauthorizedException exception = new UnauthorizedException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void insufficientBalanceException_WithMessage_CreatesExceptionWithMessage() {
        String message = "Insufficient balance";
        InsufficientBalanceException exception = new InsufficientBalanceException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void insufficientBalanceException_WithMessageAndCause_CreatesExceptionWithBoth() {
        String message = "Insufficient balance";
        Throwable cause = new ArithmeticException("Balance calculation error");
        InsufficientBalanceException exception = new InsufficientBalanceException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void resourceNotFoundException_IsRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void badRequestException_IsRuntimeException() {
        BadRequestException exception = new BadRequestException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void unauthorizedException_IsRuntimeException() {
        UnauthorizedException exception = new UnauthorizedException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void insufficientBalanceException_IsRuntimeException() {
        InsufficientBalanceException exception = new InsufficientBalanceException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}
