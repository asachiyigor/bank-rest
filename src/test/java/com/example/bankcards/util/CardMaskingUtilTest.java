package com.example.bankcards.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardMaskingUtilTest {

    @Test
    void maskCardNumber_SixteenDigitCard_ReturnsMaskedNumber() {
        String cardNumber = "1234567890123456";

        String masked = CardMaskingUtil.maskCardNumber(cardNumber);

        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void maskCardNumber_FifteenDigitCard_ReturnsMaskedNumber() {
        String cardNumber = "123456789012345";

        String masked = CardMaskingUtil.maskCardNumber(cardNumber);

        assertEquals("**** **** ***2 345", masked);
    }

    @Test
    void maskCardNumber_ShortCardNumber_ReturnsOriginal() {
        String cardNumber = "123";

        String masked = CardMaskingUtil.maskCardNumber(cardNumber);

        assertEquals("123", masked);
    }

    @Test
    void maskCardNumber_ExactlyFourDigits_ReturnsOriginal() {
        String cardNumber = "1234";

        String masked = CardMaskingUtil.maskCardNumber(cardNumber);

        assertEquals("1234", masked);
    }

    @Test
    void maskCardNumber_NullValue_ReturnsNull() {
        String masked = CardMaskingUtil.maskCardNumber(null);

        assertNull(masked);
    }

    @Test
    void maskCardNumber_EmptyString_ReturnsEmptyString() {
        String masked = CardMaskingUtil.maskCardNumber("");

        assertEquals("", masked);
    }

    @Test
    void maskCardNumber_TwentyDigitCard_ReturnsMaskedNumber() {
        String cardNumber = "12345678901234567890";

        String masked = CardMaskingUtil.maskCardNumber(cardNumber);

        assertEquals("**** **** **** **** 7890", masked);
    }

    @Test
    void maskCardNumber_EightDigitCard_ReturnsMaskedNumber() {
        String cardNumber = "12345678";

        String masked = CardMaskingUtil.maskCardNumber(cardNumber);

        assertEquals("**** 5678", masked);
    }

    @Test
    void formatCardNumber_SixteenDigits_ReturnsFormatted() {
        String cardNumber = "1234567890123456";

        String formatted = CardMaskingUtil.formatCardNumber(cardNumber);

        assertEquals("1234 5678 9012 3456", formatted);
    }

    @Test
    void formatCardNumber_FifteenDigits_ReturnsFormatted() {
        String cardNumber = "123456789012345";

        String formatted = CardMaskingUtil.formatCardNumber(cardNumber);

        assertEquals("1234 5678 9012 345", formatted);
    }

    @Test
    void formatCardNumber_NullValue_ReturnsNull() {
        String formatted = CardMaskingUtil.formatCardNumber(null);

        assertNull(formatted);
    }

    @Test
    void formatCardNumber_EmptyString_ReturnsEmptyString() {
        String formatted = CardMaskingUtil.formatCardNumber("");

        assertEquals("", formatted);
    }

    @Test
    void formatCardNumber_FourDigits_ReturnsFormatted() {
        String cardNumber = "1234";

        String formatted = CardMaskingUtil.formatCardNumber(cardNumber);

        assertEquals("1234", formatted);
    }

    @Test
    void formatCardNumber_TwentyDigits_ReturnsFormatted() {
        String cardNumber = "12345678901234567890";

        String formatted = CardMaskingUtil.formatCardNumber(cardNumber);

        assertEquals("1234 5678 9012 3456 7890", formatted);
    }
}
