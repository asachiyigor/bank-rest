package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое когда запрашиваемый ресурс не найден
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
