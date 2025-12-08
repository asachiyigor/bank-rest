package com.example.bankcards.mapper;

import com.example.bankcards.constants.BusinessConstants;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardMapperTest {

    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @InjectMocks
    private CardMapper cardMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("John Doe")
                .roles(new HashSet<>())
                .build();
    }

    @Test
    void toResponse_Success() {
        Card card = Card.builder()
                .id(1L)
                .cardNumber("encrypted1234567890123456")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.50))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(cardEncryptionUtil.decrypt("encrypted1234567890123456"))
                .thenReturn("1234567890123456");

        CardResponse response = cardMapper.toResponse(card);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("**** **** **** 3456", response.cardNumber());
        assertEquals("John Doe", response.ownerFullName());
        assertEquals(LocalDate.of(2025, 12, 31), response.expiryDate());
        assertEquals("ACTIVE", response.status());
        assertEquals(BigDecimal.valueOf(1000.50), response.balance());
    }

    @Test
    void toResponse_WithBlockedStatus() {
        Card card = Card.builder()
                .id(2L)
                .cardNumber("encrypted9876543210987654")
                .expiryDate(LocalDate.of(2026, 6, 30))
                .status(Card.CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(cardEncryptionUtil.decrypt("encrypted9876543210987654"))
                .thenReturn("9876543210987654");

        CardResponse response = cardMapper.toResponse(card);

        assertNotNull(response);
        assertEquals("BLOCKED", response.status());
        assertEquals("**** **** **** 7654", response.cardNumber());
    }

    @Test
    void toResponse_WithNullCard() {
        CardResponse response = cardMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    void toEntity_Success() {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.of(2025, 12, 31), 1L);

        when(cardEncryptionUtil.encrypt("1234567890123456"))
                .thenReturn("encrypted1234567890123456");

        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        Card card = cardMapper.toEntity(request, testUser);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertNotNull(card);
        assertEquals("encrypted1234567890123456", card.getCardNumber());
        assertEquals(LocalDate.of(2025, 12, 31), card.getExpiryDate());
        assertEquals(Card.CardStatus.ACTIVE, card.getStatus());
        assertEquals(BusinessConstants.INITIAL_CARD_BALANCE, card.getBalance());
        assertEquals(testUser, card.getUser());
        assertNotNull(card.getCreatedAt());
        assertTrue(card.getCreatedAt().isAfter(beforeCreation));
        assertTrue(card.getCreatedAt().isBefore(afterCreation));
    }

    @Test
    void toEntity_SetsDefaultActiveStatus() {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.of(2025, 12, 31), 1L);

        when(cardEncryptionUtil.encrypt(anyString()))
                .thenReturn("encrypted");

        Card card = cardMapper.toEntity(request, testUser);

        assertNotNull(card);
        assertEquals(Card.CardStatus.ACTIVE, card.getStatus());
    }

    @Test
    void toEntity_SetsInitialBalanceToZero() {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.of(2025, 12, 31), 1L);

        when(cardEncryptionUtil.encrypt(anyString()))
                .thenReturn("encrypted");

        Card card = cardMapper.toEntity(request, testUser);

        assertNotNull(card);
        assertEquals(BigDecimal.ZERO, card.getBalance());
    }

    @Test
    void toEntity_WithNullRequest() {
        Card card = cardMapper.toEntity(null, testUser);

        assertNull(card);
    }

    @Test
    void toEntity_EncryptsCardNumber() {
        CardRequest request = new CardRequest("9876543210123456", LocalDate.of(2025, 12, 31), 1L);

        when(cardEncryptionUtil.encrypt("9876543210123456"))
                .thenReturn("super_encrypted_9876543210123456");

        Card card = cardMapper.toEntity(request, testUser);

        assertNotNull(card);
        assertEquals("super_encrypted_9876543210123456", card.getCardNumber());
    }
}
