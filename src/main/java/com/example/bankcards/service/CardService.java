package com.example.bankcards.service;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.constants.LogConstants;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.helper.LogHelper;
import com.example.bankcards.helper.SecurityHelper;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.validator.CardValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardEncryptionUtil cardEncryptionUtil;
    private final CardValidator cardValidator;
    private final SecurityHelper securityHelper;
    private final CardMapper cardMapper;

    @Caching(evict = {
            @CacheEvict(value = "userCards", key = "#request.userId()"),
            @CacheEvict(value = "cards", allEntries = true)
    })
    public CardResponse createCard(CardRequest request, Authentication authentication) {
        LogHelper.logOperationStart(log, LogConstants.CARD_CREATE,
                "userId", request.userId(),
                "expiryDate", request.expiryDate());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        securityHelper.validateUserAccess(authentication, user.getId());

        cardValidator.validateCardNumber(request.cardNumber());
        cardValidator.validateCardNumberUnique(request.cardNumber());

        Card card = cardMapper.toEntity(request, user);
        cardRepository.save(card);

        LogHelper.logOperationSuccess(log, LogConstants.CARD_CREATE,
                "cardId", card.getId(),
                "userId", user.getId());

        return cardMapper.toResponse(card);
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(Long userId, String status, Pageable pageable, Authentication authentication) {
        if (pageable.isPaged()) {
            LogHelper.logOperationStart(log, LogConstants.CARD_GET_ALL,
                    "userId", userId,
                    "status", status,
                    "page", pageable.getPageNumber(),
                    "size", pageable.getPageSize());
        } else {
            LogHelper.logOperationStart(log, LogConstants.CARD_GET_ALL,
                    "userId", userId,
                    "status", status);
        }

        securityHelper.validateUserAccess(authentication, userId);

        Page<Card> cards;
        if (status != null && !status.isEmpty()) {
            try {
                Card.CardStatus cardStatus = Card.CardStatus.valueOf(status.toUpperCase());
                cards = cardRepository.findByUserIdAndStatus(userId, cardStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid card status: " + status + ". Valid values: ACTIVE, BLOCKED, EXPIRED");
            }
        } else {
            cards = cardRepository.findByUserId(userId, pageable);
        }

        LogHelper.logOperationSuccess(log, LogConstants.CARD_GET_ALL,
                "userId", userId,
                "status", status,
                "cardsFound", cards.getTotalElements());

        return cards.map(cardMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CardResponse getCardById(Long cardId, Authentication authentication) {
        LogHelper.logOperationStart(log, LogConstants.CARD_GET,
                "cardId", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CARD_NOT_FOUND));

        securityHelper.validateUserAccess(authentication, card.getUser().getId());

        LogHelper.logOperationSuccess(log, LogConstants.CARD_GET,
                "cardId", cardId,
                "userId", card.getUser().getId());

        return cardMapper.toResponse(card);
    }

    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#cardId"),
            @CacheEvict(value = "userCards", allEntries = true)
    })
    public void blockCard(Long cardId, Authentication authentication) {
        LogHelper.logOperationStart(log, LogConstants.CARD_BLOCK,
                "cardId", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CARD_NOT_FOUND));

        securityHelper.validateUserAccess(authentication, card.getUser().getId());

        card.setStatus(Card.CardStatus.BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());
        cardRepository.save(card);

        LogHelper.logOperationSuccess(log, LogConstants.CARD_BLOCK,
                "cardId", cardId,
                "userId", card.getUser().getId());
    }

    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#cardId"),
            @CacheEvict(value = "userCards", allEntries = true)
    })
    public void activateCard(Long cardId, Authentication authentication) {
        LogHelper.logOperationStart(log, LogConstants.CARD_ACTIVATE,
                "cardId", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CARD_NOT_FOUND));

        if (!securityHelper.isAdmin(authentication)) {
            throw new UnauthorizedException("Only admins can activate cards");
        }

        card.setStatus(Card.CardStatus.ACTIVE);
        card.setUpdatedAt(LocalDateTime.now());
        cardRepository.save(card);

        LogHelper.logOperationSuccess(log, LogConstants.CARD_ACTIVATE,
                "cardId", cardId,
                "userId", card.getUser().getId());
    }

    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#cardId"),
            @CacheEvict(value = "userCards", allEntries = true)
    })
    public void deleteCard(Long cardId, Authentication authentication) {
        LogHelper.logOperationStart(log, LogConstants.CARD_DELETE,
                "cardId", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CARD_NOT_FOUND));

        if (!securityHelper.isAdmin(authentication)) {
            throw new UnauthorizedException("Only admins can delete cards");
        }

        cardRepository.delete(card);

        LogHelper.logOperationSuccess(log, LogConstants.CARD_DELETE,
                "cardId", cardId,
                "userId", card.getUser().getId());
    }
}
