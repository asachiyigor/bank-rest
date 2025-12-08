package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private com.example.bankcards.validator.TransferValidator transferValidator;

    @Mock
    private com.example.bankcards.helper.SecurityHelper securityHelper;

    @Mock
    private com.example.bankcards.mapper.TransferMapper transferMapper;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>())
                .build();

        fromCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted123")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        toCard = Card.builder()
                .id(2L)
                .cardNumber("encrypted456")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        transferRequest = new TransferRequest(1L, 2L, BigDecimal.valueOf(100), "Test transfer");
    }

    @Test
    void transfer_Success() {
        Transfer savedTransfer = new Transfer();
        TransferResponse expectedResponse = new TransferResponse(
                1L,
                1L,
                2L,
                BigDecimal.valueOf(100),
                "SUCCESS",
                LocalDateTime.now(),
                "Test transfer"
        );

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doNothing().when(transferValidator).validateTransfer(eq(fromCard), eq(toCard), any(BigDecimal.class), eq(testUser));
        when(transferMapper.toEntity(transferRequest)).thenReturn(savedTransfer);
        when(transferRepository.save(savedTransfer)).thenReturn(savedTransfer);
        when(transferMapper.toResponse(savedTransfer)).thenReturn(expectedResponse);

        TransferResponse response = transferService.transfer(transferRequest, authentication);

        assertNotNull(response);
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository, times(1)).save(savedTransfer);
        verify(transferMapper, times(1)).toResponse(savedTransfer);
    }

    @Test
    void transfer_SameCard_ThrowsException() {
        TransferRequest modifiedRequest = new TransferRequest(
            transferRequest.fromCardId(),
            1L,
            transferRequest.amount(),
            transferRequest.description()
        );
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doThrow(new BadRequestException("Cannot transfer to the same card"))
                .when(transferValidator).validateTransfer(eq(fromCard), eq(fromCard), any(BigDecimal.class), eq(testUser));

        assertThrows(BadRequestException.class, () ->
                transferService.transfer(modifiedRequest, authentication)
        );
    }

    @Test
    void transfer_InsufficientBalance_ThrowsException() {
        TransferRequest modifiedRequest = new TransferRequest(
            transferRequest.fromCardId(),
            transferRequest.toCardId(),
            BigDecimal.valueOf(2000),
            transferRequest.description()
        );

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doThrow(new InsufficientBalanceException("Insufficient balance"))
                .when(transferValidator).validateTransfer(eq(fromCard), eq(toCard), any(BigDecimal.class), eq(testUser));

        assertThrows(InsufficientBalanceException.class, () ->
                transferService.transfer(modifiedRequest, authentication)
        );
    }

    @Test
    void transfer_SourceCardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transferService.transfer(transferRequest, authentication)
        );
    }

    @Test
    void transfer_BlockedCard_ThrowsException() {
        fromCard.setStatus(Card.CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doThrow(new BadRequestException("Card is blocked"))
                .when(transferValidator).validateTransfer(eq(fromCard), eq(toCard), any(BigDecimal.class), eq(testUser));

        assertThrows(BadRequestException.class, () ->
                transferService.transfer(transferRequest, authentication)
        );
    }

    @Test
    void transfer_DestinationCardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transferService.transfer(transferRequest, authentication)
        );
    }

    @Test
    void transfer_CurrentUserNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityHelper.getCurrentUser(authentication))
                .thenThrow(new ResourceNotFoundException("User not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                transferService.transfer(transferRequest, authentication)
        );
    }

    @Test
    void transfer_UnauthorizedFromCard_ThrowsException() {
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .fullName("Other User")
                .roles(new HashSet<>())
                .build();

        fromCard.setUser(otherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doThrow(new UnauthorizedException("Unauthorized"))
                .when(transferValidator).validateTransfer(eq(fromCard), eq(toCard), any(BigDecimal.class), eq(testUser));

        assertThrows(UnauthorizedException.class, () ->
                transferService.transfer(transferRequest, authentication)
        );
    }

    @Test
    void transfer_UnauthorizedToCard_ThrowsException() {
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .fullName("Other User")
                .roles(new HashSet<>())
                .build();

        toCard.setUser(otherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doThrow(new UnauthorizedException("Unauthorized"))
                .when(transferValidator).validateTransfer(eq(fromCard), eq(toCard), any(BigDecimal.class), eq(testUser));

        assertThrows(UnauthorizedException.class, () ->
                transferService.transfer(transferRequest, authentication)
        );
    }

    @Test
    void transfer_DestinationCardBlocked_ThrowsException() {
        toCard.setStatus(Card.CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doThrow(new BadRequestException("Destination card is blocked"))
                .when(transferValidator).validateTransfer(eq(fromCard), eq(toCard), any(BigDecimal.class), eq(testUser));

        assertThrows(BadRequestException.class, () ->
                transferService.transfer(transferRequest, authentication)
        );
    }

    @Test
    void getTransferHistory_Success() {
        Transfer transfer = Transfer.builder()
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100))
                .status(Transfer.TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .description("Test transfer")
                .build();

        Page<Transfer> transferPage = new PageImpl<>(Collections.singletonList(transfer));
        TransferResponse transferResponse = new TransferResponse(
                1L,
                1L,
                2L,
                BigDecimal.valueOf(100),
                "SUCCESS",
                LocalDateTime.now(),
                "Test transfer"
        );

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doNothing().when(securityHelper).validateCardOwnership(fromCard, testUser);
        when(transferRepository.findByCardId(eq(1L), any(Pageable.class))).thenReturn(transferPage);
        when(transferMapper.toResponse(any(Transfer.class))).thenReturn(transferResponse);

        Page<TransferResponse> result = transferService.getTransferHistory(1L, Pageable.unpaged(), authentication);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(transferRepository, times(1)).findByCardId(eq(1L), any(Pageable.class));
    }

    @Test
    void getTransferHistory_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transferService.getTransferHistory(1L, Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getTransferHistory_CurrentUserNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(securityHelper.getCurrentUser(authentication))
                .thenThrow(new ResourceNotFoundException("User not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                transferService.getTransferHistory(1L, Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getTransferHistory_UnauthorizedAccess_ThrowsException() {
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .fullName("Other User")
                .roles(new HashSet<>())
                .build();

        fromCard.setUser(otherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        doThrow(new UnauthorizedException("Unauthorized"))
                .when(securityHelper).validateCardOwnership(fromCard, testUser);

        assertThrows(UnauthorizedException.class, () ->
                transferService.getTransferHistory(1L, Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getUserTransferHistory_Success() {
        Transfer transfer = Transfer.builder()
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(100))
                .status(Transfer.TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .description("Test transfer")
                .build();

        Page<Transfer> transferPage = new PageImpl<>(Collections.singletonList(transfer));
        TransferResponse transferResponse = new TransferResponse(
                1L,
                1L,
                2L,
                BigDecimal.valueOf(100),
                "SUCCESS",
                LocalDateTime.now(),
                "Test transfer"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        when(cardRepository.findByUserId(1L)).thenReturn(List.of(fromCard, toCard));
        when(transferRepository.findByCardIds(anyList(), any(Pageable.class))).thenReturn(transferPage);
        when(transferMapper.toResponse(any(Transfer.class))).thenReturn(transferResponse);

        Page<TransferResponse> result = transferService.getUserTransferHistory(1L, Pageable.unpaged(), authentication);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(transferRepository, times(1)).findByCardIds(anyList(), any(Pageable.class));
    }

    @Test
    void getUserTransferHistory_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transferService.getUserTransferHistory(1L, Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getUserTransferHistory_CurrentUserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(securityHelper.getCurrentUser(authentication))
                .thenThrow(new ResourceNotFoundException("User not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                transferService.getUserTransferHistory(1L, Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getUserTransferHistory_UnauthorizedAccess_ThrowsException() {
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .fullName("Other User")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);

        assertThrows(UnauthorizedException.class, () ->
                transferService.getUserTransferHistory(2L, Pageable.unpaged(), authentication)
        );
    }

    @Test
    void getUserTransferHistory_EmptyCardList_ReturnsEmptyPage() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(securityHelper.getCurrentUser(authentication)).thenReturn(testUser);
        when(cardRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        Page<TransferResponse> result = transferService.getUserTransferHistory(1L, Pageable.unpaged(), authentication);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transferRepository, never()).findByCardIds(anyList(), any(Pageable.class));
    }
}
