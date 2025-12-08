package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое при некорректном запросе
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
