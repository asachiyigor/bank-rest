package com.example.bankcards.service;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.constants.LogConstants;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.helper.LogHelper;
import com.example.bankcards.helper.SecurityHelper;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.validator.TransferValidator;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransferValidator transferValidator;
    private final SecurityHelper securityHelper;
    private final TransferMapper transferMapper;

    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#request.fromCardId()"),
            @CacheEvict(value = "cards", key = "#request.toCardId()"),
            @CacheEvict(value = "userCards", allEntries = true)
    })
    public TransferResponse transfer(TransferRequest request, Authentication authentication) {
        LogHelper.logOperationStart(log, LogConstants.TRANSFER_CREATE,
                "fromCardId", request.fromCardId(),
                "toCardId", request.toCardId(),
                "amount", request.amount());

        Card fromCard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SOURCE_CARD_NOT_FOUND));

        Card toCard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DESTINATION_CARD_NOT_FOUND));

        User currentUser = securityHelper.getCurrentUser(authentication);

        transferValidator.validateTransfer(fromCard, toCard, request.amount(), currentUser);

        log.debug("{} Updating balances - fromCardBalance={}, toCardBalance={}",
                LogConstants.TRANSFER_CREATE,
                fromCard.getBalance(),
                toCard.getBalance());

        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        fromCard.setUpdatedAt(LocalDateTime.now());

        toCard.setBalance(toCard.getBalance().add(request.amount()));
        toCard.setUpdatedAt(LocalDateTime.now());

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = transferMapper.toEntity(request);
        transferRepository.save(transfer);

        LogHelper.logOperationSuccess(log, LogConstants.TRANSFER_CREATE,
                "transferId", transfer.getId(),
                "userId", currentUser.getId());

        return transferMapper.toResponse(transfer);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransferHistory(Long cardId, Pageable pageable, Authentication authentication) {
        if (pageable.isPaged()) {
            LogHelper.logOperationStart(log, LogConstants.TRANSFER_HISTORY,
                    "cardId", cardId,
                    "page", pageable.getPageNumber(),
                    "size", pageable.getPageSize());
        } else {
            LogHelper.logOperationStart(log, LogConstants.TRANSFER_HISTORY,
                    "cardId", cardId);
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CARD_NOT_FOUND));

        User currentUser = securityHelper.getCurrentUser(authentication);

        securityHelper.validateCardOwnership(card, currentUser);

        Page<Transfer> transfers = transferRepository.findByCardId(cardId, pageable);

        LogHelper.logOperationSuccess(log, LogConstants.TRANSFER_HISTORY,
                "cardId", cardId,
                "transfersFound", transfers.getTotalElements());

        return transfers.map(transferMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getUserTransferHistory(Long userId, Pageable pageable, Authentication authentication) {
        if (pageable.isPaged()) {
            LogHelper.logOperationStart(log, LogConstants.TRANSFER_USER_HISTORY,
                    "userId", userId,
                    "page", pageable.getPageNumber(),
                    "size", pageable.getPageSize());
        } else {
            LogHelper.logOperationStart(log, LogConstants.TRANSFER_USER_HISTORY,
                    "userId", userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        User currentUser = securityHelper.getCurrentUser(authentication);

        if (!user.getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(ErrorMessages.UNAUTHORIZED_VIEW_USER_HISTORY);
        }

        List<Long> cardIds = cardRepository.findByUserId(userId).stream()
                .map(Card::getId)
                .collect(Collectors.toList());

        log.debug("{} User has {} cards", LogConstants.TRANSFER_USER_HISTORY, cardIds.size());

        if (cardIds.isEmpty()) {
            LogHelper.logOperationSuccess(log, LogConstants.TRANSFER_USER_HISTORY,
                    "userId", userId,
                    "transfersFound", "0 (no cards)");
            return Page.empty(pageable);
        }

        Page<Transfer> transfers = transferRepository.findByCardIds(cardIds, pageable);

        LogHelper.logOperationSuccess(log, LogConstants.TRANSFER_USER_HISTORY,
                "userId", userId,
                "transfersFound", transfers.getTotalElements());

        return transfers.map(transferMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long transferId, Authentication authentication) {
        LogHelper.logOperationStart(log, LogConstants.TRANSFER_GET, "transferId", transferId);

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.TRANSFER_NOT_FOUND));

        Card fromCard = cardRepository.findById(transfer.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SOURCE_CARD_NOT_FOUND));

        User currentUser = securityHelper.getCurrentUser(authentication);

        // Проверка доступа: пользователь может просматривать только свои переводы
        if (!fromCard.getUser().getId().equals(currentUser.getId())) {
            Card toCard = cardRepository.findById(transfer.getToCardId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.DESTINATION_CARD_NOT_FOUND));

            if (!toCard.getUser().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException(ErrorMessages.UNAUTHORIZED_VIEW_TRANSFER);
            }
        }

        LogHelper.logOperationSuccess(log, LogConstants.TRANSFER_GET,
                "transferId", transferId,
                "userId", currentUser.getId());

        return transferMapper.toResponse(transfer);
    }
}
