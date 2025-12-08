package com.example.bankcards.constants;

import java.math.BigDecimal;

public final class BusinessConstants {

    private BusinessConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final BigDecimal INITIAL_CARD_BALANCE = BigDecimal.ZERO;

    public static final String MIN_TRANSFER_AMOUNT_STRING = "0.01";
    public static final String MAX_TRANSFER_AMOUNT_STRING = "1000000.00";

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_FULL_NAME_LENGTH = 100;

    public static final String CARD_NUMBER_PATTERN = "^[0-9]{16}$";
}
