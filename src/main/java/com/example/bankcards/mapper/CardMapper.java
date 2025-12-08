package com.example.bankcards.mapper;

import com.example.bankcards.constants.BusinessConstants;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.CardMaskingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper for converting between Card entity and DTOs.
 *
 * <p>This component handles the conversion between:
 * <ul>
 *   <li>Card entity to CardResponse DTO (with card number masking)</li>
 *   <li>CardRequest DTO to Card entity (with card number encryption)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CardMapper {

    private final CardEncryptionUtil cardEncryptionUtil;

    /**
     * Converts a Card entity to a CardResponse DTO.
     *
     * <p>The card number is decrypted and then masked for security purposes.
     *
     * @param card the card entity to convert
     * @return the card response DTO with masked card number
     */
    public CardResponse toResponse(Card card) {
        if (card == null) {
            return null;
        }

        String decryptedNumber = cardEncryptionUtil.decrypt(card.getCardNumber());

        return new CardResponse(
                card.getId(),
                CardMaskingUtil.maskCardNumber(decryptedNumber),
                card.getUser().getFullName(),
                card.getExpiryDate(),
                card.getStatus().name(),
                card.getBalance()
        );
    }

    /**
     * Converts a CardRequest DTO to a Card entity.
     *
     * <p>The card number is encrypted before storing in the entity.
     * The card is created with ACTIVE status and zero initial balance.
     *
     * @param request the card request DTO
     * @param user the user who owns this card
     * @return the card entity with encrypted card number
     */
    public Card toEntity(CardRequest request, User user) {
        if (request == null) {
            return null;
        }

        return Card.builder()
                .cardNumber(cardEncryptionUtil.encrypt(request.cardNumber()))
                .expiryDate(request.expiryDate())
                .status(Card.CardStatus.ACTIVE)
                .balance(BusinessConstants.INITIAL_CARD_BALANCE)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
