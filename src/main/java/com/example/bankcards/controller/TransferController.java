package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transfers", description = "Transfer operations API")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Transfer money", description = "Transfer money between user's own cards")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        log.info("Transfer request from card {} to card {} for amount: {}",
                request.fromCardId(), request.toCardId(), request.amount());
        TransferResponse response = transferService.transfer(request, authentication);
        log.info("Transfer completed successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/card/{cardId}")
    @Operation(summary = "Get card transfer history", description = "Get transfer history for a specific card")
    public ResponseEntity<Page<TransferResponse>> getCardTransferHistory(
            @PathVariable Long cardId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        log.info("Fetching transfer history for card ID: {}", cardId);
        Page<TransferResponse> transfers = transferService.getTransferHistory(cardId, pageable, authentication);
        log.info("Found {} transfers for card ID: {}", transfers.getTotalElements(), cardId);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user transfer history", description = "Get all transfer history for a user")
    public ResponseEntity<Page<TransferResponse>> getUserTransferHistory(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        log.info("Fetching transfer history for user ID: {}", userId);
        Page<TransferResponse> transfers = transferService.getUserTransferHistory(userId, pageable, authentication);
        log.info("Found {} transfers for user ID: {}", transfers.getTotalElements(), userId);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/{transferId}")
    @Operation(summary = "Get transfer by ID", description = "Get detailed information about a specific transfer")
    public ResponseEntity<TransferResponse> getTransferById(
            @PathVariable Long transferId,
            Authentication authentication) {
        log.info("Fetching transfer with ID: {}", transferId);
        TransferResponse transfer = transferService.getTransferById(transferId, authentication);
        log.info("Transfer {} fetched successfully", transferId);
        return ResponseEntity.ok(transfer);
    }
}
