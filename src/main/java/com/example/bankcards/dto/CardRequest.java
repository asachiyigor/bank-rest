package com.example.bankcards.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardRequest(
        @NotNull(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,

        @NotNull(message = "Expiry date is required")
        @Future(message = "Expiry date must be in the future")
        LocalDate expiryDate,

        @NotNull(message = "User ID is required")
        Long userId
) {}
