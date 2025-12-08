package com.example.bankcards.util;

public class CardMaskingUtil {

    private static final String MASK_CHAR = "*";
    private static final int VISIBLE_DIGITS = 4;

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < VISIBLE_DIGITS) {
            return cardNumber;
        }

        String lastFourDigits = cardNumber.substring(cardNumber.length() - VISIBLE_DIGITS);
        StringBuilder masked = new StringBuilder();

        for (int i = 0; i < cardNumber.length() - VISIBLE_DIGITS; i++) {
            if (i > 0 && i % 4 == 0) {
                masked.append(" ");
            }
            masked.append(MASK_CHAR);
        }

        if (!masked.isEmpty() && (cardNumber.length() - VISIBLE_DIGITS) % 4 == 0) {
            masked.append(" ");
        }

        for (int i = 0; i < VISIBLE_DIGITS; i++) {
            if (i > 0 && (cardNumber.length() - VISIBLE_DIGITS + i) % 4 == 0) {
                masked.append(" ");
            }
            masked.append(lastFourDigits.charAt(i));
        }

        return masked.toString();
    }

    public static String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < cardNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(cardNumber.charAt(i));
        }
        return formatted.toString();
    }
}
