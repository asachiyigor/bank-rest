package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое когда на карте недостаточно средств для операции
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
