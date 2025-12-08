package com.example.bankcards.validator;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferValidator {

    public void validateTransfer(Card fromCard, Card toCard, BigDecimal amount, User currentUser) {
        validateNotSameCard(fromCard, toCard);
        validateCardOwnership(fromCard, currentUser, ErrorMessages.UNAUTHORIZED_TRANSFER_FROM);
        validateCardOwnership(toCard, currentUser, ErrorMessages.UNAUTHORIZED_TRANSFER_TO);
        validateCardActive(fromCard, true);
        validateCardActive(toCard, false);
        validateSufficientBalance(fromCard, amount);
    }

    private void validateNotSameCard(Card fromCard, Card toCard) {
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BadRequestException(ErrorMessages.TRANSFER_SAME_CARD);
        }
    }

    private void validateCardOwnership(Card card, User user, String errorMessage) {
        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(errorMessage);
        }
    }

    private void validateCardActive(Card card, boolean isSourceCard) {
        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new BadRequestException(
                isSourceCard ? ErrorMessages.SOURCE_CARD_NOT_ACTIVE : ErrorMessages.DESTINATION_CARD_NOT_ACTIVE
            );
        }
    }

    private void validateSufficientBalance(Card card, BigDecimal amount) {
        if (card.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(ErrorMessages.INSUFFICIENT_BALANCE);
        }
    }
}
