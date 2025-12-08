package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private com.example.bankcards.validator.CardValidator cardValidator;

    @Mock
    private com.example.bankcards.helper.SecurityHelper securityHelper;

    @Mock
    private com.example.bankcards.mapper.CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CardRequest cardRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>())
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted123")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        cardRequest = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), 1L);
    }

    @Test
    void createCard_Success() {
        CardResponse expectedResponse = new CardResponse(
                1L,
                "1234****3456",
                "Test User",
                LocalDate.now().plusYears(2),
                "ACTIVE",
                BigDecimal.ZERO
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        doNothing().when(cardValidator).validateCardNumber(anyString());
        doNothing().when(cardValidator).validateCardNumberUnique(anyString());
        when(cardMapper.toEntity(cardRequest, testUser)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toResponse(testCard)).thenReturn(expectedResponse);

        CardResponse response = cardService.createCard(cardRequest, authentication);

        assertNotNull(response);
        verify(cardRepository, times(1)).save(testCard);
        verify(cardMapper, times(1)).toResponse(testCard);
    }

    @Test
    void createCard_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.createCard(cardRequest, authentication)
        );
    }

    @Test
    void createCard_DuplicateCardNumber_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        doNothing().when(cardValidator).validateCardNumber(anyString());
        doThrow(new BadRequestException("Card number already exists"))
                .when(cardValidator).validateCardNumberUnique(anyString());

        assertThrows(BadRequestException.class, () ->
                cardService.createCard(cardRequest, authentication)
        );
    }

    @Test
    void blockCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.blockCard(1L, authentication);

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void getCardById_Success() {
        CardResponse expectedResponse = new CardResponse(
                1L,
                "1234****3456",
                "Test User",
                LocalDate.now().plusYears(2),
                "ACTIVE",
                BigDecimal.valueOf(1000)
        );

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        when(cardMapper.toResponse(testCard)).thenReturn(expectedResponse);

        CardResponse response = cardService.getCardById(1L, authentication);

        assertNotNull(response);
        verify(cardMapper, times(1)).toResponse(testCard);
    }

    @Test
    void getCardById_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.getCardById(1L, authentication)
        );
    }

    @Test
    void createCard_UnauthorizedUser_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doThrow(new UnauthorizedException("Unauthorized"))
                .when(securityHelper).validateUserAccess(authentication, 1L);

        assertThrows(UnauthorizedException.class, () ->
                cardService.createCard(cardRequest, authentication)
        );
    }

    @Test
    void blockCard_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.blockCard(1L, authentication)
        );
    }

    @Test
    void blockCard_UnauthorizedUser_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        doThrow(new UnauthorizedException("Unauthorized"))
                .when(securityHelper).validateUserAccess(authentication, 1L);

        assertThrows(UnauthorizedException.class, () ->
                cardService.blockCard(1L, authentication)
        );
    }

    @Test
    void getCardById_UnauthorizedUser_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        doThrow(new UnauthorizedException("Unauthorized"))
                .when(securityHelper).validateUserAccess(authentication, 1L);

        assertThrows(UnauthorizedException.class, () ->
                cardService.getCardById(1L, authentication)
        );
    }

    @Test
    void activateCard_Success_AsAdmin() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(securityHelper.isAdmin(authentication)).thenReturn(true);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        cardService.activateCard(1L, authentication);

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void activateCard_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.activateCard(1L, authentication)
        );
    }

    @Test
    void activateCard_NonAdmin_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(securityHelper.isAdmin(authentication)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                cardService.activateCard(1L, authentication)
        );
    }

    @Test
    void deleteCard_Success_AsAdmin() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(securityHelper.isAdmin(authentication)).thenReturn(true);

        cardService.deleteCard(1L, authentication);

        verify(cardRepository, times(1)).delete(testCard);
    }

    @Test
    void deleteCard_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.deleteCard(1L, authentication)
        );
    }

    @Test
    void deleteCard_NonAdmin_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(securityHelper.isAdmin(authentication)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                cardService.deleteCard(1L, authentication)
        );
    }

    @Test
    void getUserCards_Success() {
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(testCard));
        CardResponse cardResponse = new CardResponse(
                1L,
                "1234****3456",
                "Test User",
                LocalDate.now().plusYears(2),
                "ACTIVE",
                BigDecimal.valueOf(1000)
        );

        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        when(cardRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(cardPage);
        when(cardMapper.toResponse(any(Card.class))).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getUserCards(1L, null, Pageable.unpaged(), authentication);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void getUserCards_UnauthorizedUser_ThrowsException() {
        doThrow(new UnauthorizedException("Unauthorized"))
                .when(securityHelper).validateUserAccess(authentication, 1L);

        assertThrows(UnauthorizedException.class, () ->
                cardService.getUserCards(1L, null, Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getUserCards_AdminCanAccessAnyUser() {
        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        when(cardRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<CardResponse> result = cardService.getUserCards(1L, null, Pageable.unpaged(), authentication);

        assertNotNull(result);
        verify(cardRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void getUserCards_WithValidStatus_FiltersCards() {
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(testCard));
        CardResponse cardResponse = new CardResponse(
                1L,
                "1234****3456",
                "Test User",
                LocalDate.now().plusYears(2),
                "ACTIVE",
                BigDecimal.valueOf(1000)
        );

        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        when(cardRepository.findByUserIdAndStatus(eq(1L), eq(Card.CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(cardPage);
        when(cardMapper.toResponse(any(Card.class))).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getUserCards(1L, "ACTIVE", Pageable.unpaged(), authentication);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository, times(1)).findByUserIdAndStatus(eq(1L), eq(Card.CardStatus.ACTIVE), any(Pageable.class));
        verify(cardRepository, never()).findByUserId(any(), any());
    }

    @Test
    void getUserCards_WithInvalidStatus_ThrowsException() {
        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);

        assertThrows(BadRequestException.class, () ->
                cardService.getUserCards(1L, "INVALID_STATUS", Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getUserCards_WithEmptyStatus_ReturnsAllCards() {
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(testCard));
        CardResponse cardResponse = new CardResponse(
                1L,
                "1234****3456",
                "Test User",
                LocalDate.now().plusYears(2),
                "ACTIVE",
                BigDecimal.valueOf(1000)
        );

        doNothing().when(securityHelper).validateUserAccess(authentication, 1L);
        when(cardRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(cardPage);
        when(cardMapper.toResponse(any(Card.class))).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getUserCards(1L, "", Pageable.unpaged(), authentication);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
        verify(cardRepository, never()).findByUserIdAndStatus(any(), any(), any());
    }
}
