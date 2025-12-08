package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.service.CardService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cards", description = "Card management API")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Create card", description = "Create a new bank card")
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CardRequest request,
            Authentication authentication) {
        log.info("Creating card for user: {}", authentication.getName());
        CardResponse response = cardService.createCard(request, authentication);
        log.info("Card created successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user cards", description = "Get all cards for a specific user with pagination and optional status filter")
    public ResponseEntity<Page<CardResponse>> getUserCards(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        log.info("Fetching cards for user ID: {}, status filter: {}", userId, status);
        Page<CardResponse> cards = cardService.getUserCards(userId, status, pageable, authentication);
        log.info("Found {} cards for user ID: {}", cards.getTotalElements(), userId);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Get card by ID", description = "Get card details by ID")
    public ResponseEntity<CardResponse> getCardById(
            @PathVariable Long cardId,
            Authentication authentication) {
        log.info("Fetching card with ID: {}", cardId);
        return ResponseEntity.ok(cardService.getCardById(cardId, authentication));
    }

    @PutMapping("/{cardId}/block")
    @Operation(summary = "Block card", description = "Block a card (user can request to block their own card)")
    public ResponseEntity<Void> blockCard(
            @PathVariable Long cardId,
            Authentication authentication) {
        log.info("Blocking card with ID: {} by user: {}", cardId, authentication.getName());
        cardService.blockCard(cardId, authentication);
        log.info("Card {} blocked successfully", cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate card", description = "Activate a blocked card (admin only)")
    public ResponseEntity<Void> activateCard(
            @PathVariable Long cardId,
            Authentication authentication) {
        log.info("Activating card with ID: {} by admin: {}", cardId, authentication.getName());
        cardService.activateCard(cardId, authentication);
        log.info("Card {} activated successfully", cardId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete card", description = "Delete a card (admin only)")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long cardId,
            Authentication authentication) {
        log.info("Deleting card with ID: {} by admin: {}", cardId, authentication.getName());
        cardService.deleteCard(cardId, authentication);
        log.info("Card {} deleted successfully", cardId);
        return ResponseEntity.noContent().build();
    }
}
