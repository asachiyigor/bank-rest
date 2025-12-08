package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое при попытке неавторизованного доступа
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
