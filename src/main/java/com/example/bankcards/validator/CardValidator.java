package com.example.bankcards.validator;

import com.example.bankcards.constants.BusinessConstants;
import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardValidator {

    private final CardRepository cardRepository;
    private final CardEncryptionUtil cardEncryptionUtil;

    public void validateCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches(BusinessConstants.CARD_NUMBER_PATTERN)) {
            throw new BadRequestException("Invalid card number format");
        }
    }

    public void validateCardNumberUnique(String cardNumber) {
        String encryptedNumber = cardEncryptionUtil.encrypt(cardNumber);
        if (cardRepository.existsByCardNumber(encryptedNumber)) {
            throw new BadRequestException(ErrorMessages.CARD_NUMBER_EXISTS);
        }
    }
}
