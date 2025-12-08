package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class CardEncryptionUtilTest {

    private CardEncryptionUtil cardEncryptionUtil;

    @BeforeEach
    void setUp() {
        cardEncryptionUtil = new CardEncryptionUtil();
        ReflectionTestUtils.setField(cardEncryptionUtil, "secretKey", "TestSecretKey123");
    }

    @Test
    void encrypt_ValidCardNumber_ReturnsEncryptedString() {
        String cardNumber = "1234567890123456";

        String encrypted = cardEncryptionUtil.encrypt(cardNumber);

        assertNotNull(encrypted);
        assertNotEquals(cardNumber, encrypted);
        assertTrue(encrypted.length() > 0);
    }

    @Test
    void decrypt_ValidEncryptedString_ReturnsOriginalCardNumber() {
        String originalCardNumber = "1234567890123456";
        String encrypted = cardEncryptionUtil.encrypt(originalCardNumber);

        String decrypted = cardEncryptionUtil.decrypt(encrypted);

        assertEquals(originalCardNumber, decrypted);
    }

    @Test
    void encryptDecrypt_DifferentCardNumbers_WorksCorrectly() {
        String cardNumber1 = "1111222233334444";
        String cardNumber2 = "5555666677778888";

        String encrypted1 = cardEncryptionUtil.encrypt(cardNumber1);
        String encrypted2 = cardEncryptionUtil.encrypt(cardNumber2);

        assertNotEquals(encrypted1, encrypted2);
        assertEquals(cardNumber1, cardEncryptionUtil.decrypt(encrypted1));
        assertEquals(cardNumber2, cardEncryptionUtil.decrypt(encrypted2));
    }

    @Test
    void encrypt_EmptyString_WorksCorrectly() {
        String emptyString = "";

        String encrypted = cardEncryptionUtil.encrypt(emptyString);

        assertNotNull(encrypted);
        assertEquals(emptyString, cardEncryptionUtil.decrypt(encrypted));
    }

    @Test
    void encrypt_SpecialCharacters_WorksCorrectly() {
        String specialChars = "Test@#$%^&*()123";

        String encrypted = cardEncryptionUtil.encrypt(specialChars);

        assertNotNull(encrypted);
        assertEquals(specialChars, cardEncryptionUtil.decrypt(encrypted));
    }

    @Test
    void decrypt_InvalidBase64String_ThrowsRuntimeException() {
        String invalidEncrypted = "this-is-not-base64!@#$";

        assertThrows(RuntimeException.class, () -> {
            cardEncryptionUtil.decrypt(invalidEncrypted);
        });
    }

    @Test
    void encrypt_LongString_WorksCorrectly() {
        String longString = "1234567890".repeat(10);

        String encrypted = cardEncryptionUtil.encrypt(longString);

        assertNotNull(encrypted);
        assertEquals(longString, cardEncryptionUtil.decrypt(encrypted));
    }
}
