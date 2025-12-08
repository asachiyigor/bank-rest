package com.example.bankcards.validator;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TransferValidatorTest {

    private TransferValidator transferValidator;
    private User testUser;
    private User otherUser;
    private Card activeCardWithBalance;
    private Card activeCardWithoutBalance;
    private Card blockedCard;

    @BeforeEach
    void setUp() {
        transferValidator = new TransferValidator();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(new HashSet<>())
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .roles(new HashSet<>())
                .build();

        activeCardWithBalance = Card.builder()
                .id(1L)
                .cardNumber("encrypted123")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(testUser)
                .build();

        activeCardWithoutBalance = Card.builder()
                .id(2L)
                .cardNumber("encrypted456")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(testUser)
                .build();

        blockedCard = Card.builder()
                .id(3L)
                .cardNumber("encrypted789")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.BLOCKED)
                .balance(BigDecimal.valueOf(500))
                .user(testUser)
                .build();
    }

    @Test
    void validateTransfer_Success() {
        Card toCard = Card.builder()
                .id(4L)
                .cardNumber("encrypted000")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .user(testUser)
                .build();

        assertDoesNotThrow(() ->
                transferValidator.validateTransfer(
                        activeCardWithBalance,
                        toCard,
                        BigDecimal.valueOf(100),
                        testUser
                )
        );
    }

    @Test
    void validateTransfer_SameCard_ThrowsException() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                transferValidator.validateTransfer(
                        activeCardWithBalance,
                        activeCardWithBalance,
                        BigDecimal.valueOf(100),
                        testUser
                )
        );

        assertEquals(ErrorMessages.TRANSFER_SAME_CARD, exception.getMessage());
    }

    @Test
    void validateTransfer_FromCardNotOwnedByUser_ThrowsException() {
        Card otherUserCard = Card.builder()
                .id(5L)
                .cardNumber("encrypted999")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(otherUser)
                .build();

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                transferValidator.validateTransfer(
                        otherUserCard,
                        activeCardWithBalance,
                        BigDecimal.valueOf(100),
                        testUser
                )
        );

        assertEquals(ErrorMessages.UNAUTHORIZED_TRANSFER_FROM, exception.getMessage());
    }

    @Test
    void validateTransfer_ToCardNotOwnedByUser_ThrowsException() {
        Card otherUserCard = Card.builder()
                .id(5L)
                .cardNumber("encrypted999")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(otherUser)
                .build();

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                transferValidator.validateTransfer(
                        activeCardWithBalance,
                        otherUserCard,
                        BigDecimal.valueOf(100),
                        testUser
                )
        );

        assertEquals(ErrorMessages.UNAUTHORIZED_TRANSFER_TO, exception.getMessage());
    }

    @Test
    void validateTransfer_SourceCardBlocked_ThrowsException() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                transferValidator.validateTransfer(
                        blockedCard,
                        activeCardWithBalance,
                        BigDecimal.valueOf(100),
                        testUser
                )
        );

        assertEquals(ErrorMessages.SOURCE_CARD_NOT_ACTIVE, exception.getMessage());
    }

    @Test
    void validateTransfer_DestinationCardBlocked_ThrowsException() {
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                transferValidator.validateTransfer(
                        activeCardWithBalance,
                        blockedCard,
                        BigDecimal.valueOf(100),
                        testUser
                )
        );

        assertEquals(ErrorMessages.DESTINATION_CARD_NOT_ACTIVE, exception.getMessage());
    }

    @Test
    void validateTransfer_InsufficientBalance_ThrowsException() {
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () ->
                transferValidator.validateTransfer(
                        activeCardWithBalance,
                        activeCardWithoutBalance,
                        BigDecimal.valueOf(2000),
                        testUser
                )
        );

        assertEquals(ErrorMessages.INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    void validateTransfer_ExactBalance_Success() {
        assertDoesNotThrow(() ->
                transferValidator.validateTransfer(
                        activeCardWithBalance,
                        activeCardWithoutBalance,
                        BigDecimal.valueOf(1000),
                        testUser
                )
        );
    }
}
