package com.example.bankcards.validator;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardValidatorTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @InjectMocks
    private CardValidator cardValidator;

    @Test
    void validateCardNumber_ValidFormat_Success() {
        String validCardNumber = "1234567890123456";

        assertDoesNotThrow(() -> cardValidator.validateCardNumber(validCardNumber));
    }

    @Test
    void validateCardNumber_NullCardNumber_ThrowsException() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                cardValidator.validateCardNumber(null)
        );

        assertEquals("Invalid card number format", exception.getMessage());
    }

    @Test
    void validateCardNumber_EmptyCardNumber_ThrowsException() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                cardValidator.validateCardNumber("")
        );

        assertEquals("Invalid card number format", exception.getMessage());
    }

    @Test
    void validateCardNumber_WrongLength_ThrowsException() {
        String shortCardNumber = "12345";

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                cardValidator.validateCardNumber(shortCardNumber)
        );

        assertEquals("Invalid card number format", exception.getMessage());
    }

    @Test
    void validateCardNumber_NonNumeric_ThrowsException() {
        String nonNumericCardNumber = "abcd567890123456";

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                cardValidator.validateCardNumber(nonNumericCardNumber)
        );

        assertEquals("Invalid card number format", exception.getMessage());
    }

    @Test
    void validateCardNumberUnique_UniqueCardNumber_Success() {
        String cardNumber = "1234567890123456";
        String encryptedCardNumber = "encrypted_" + cardNumber;

        when(cardEncryptionUtil.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.existsByCardNumber(encryptedCardNumber)).thenReturn(false);

        assertDoesNotThrow(() -> cardValidator.validateCardNumberUnique(cardNumber));
    }

    @Test
    void validateCardNumberUnique_DuplicateCardNumber_ThrowsException() {
        String cardNumber = "1234567890123456";
        String encryptedCardNumber = "encrypted_" + cardNumber;

        when(cardEncryptionUtil.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.existsByCardNumber(encryptedCardNumber)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                cardValidator.validateCardNumberUnique(cardNumber)
        );

        assertEquals(ErrorMessages.CARD_NUMBER_EXISTS, exception.getMessage());
    }

    @Test
    void validateCardNumber_WithSpaces_ThrowsException() {
        String cardNumberWithSpaces = "1234 5678 9012 3456";

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                cardValidator.validateCardNumber(cardNumberWithSpaces)
        );

        assertEquals("Invalid card number format", exception.getMessage());
    }

    @Test
    void validateCardNumber_TooLong_ThrowsException() {
        String longCardNumber = "12345678901234567";

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                cardValidator.validateCardNumber(longCardNumber)
        );

        assertEquals("Invalid card number format", exception.getMessage());
    }
}
