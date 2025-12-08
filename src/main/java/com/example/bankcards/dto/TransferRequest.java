package com.example.bankcards.dto;

import com.example.bankcards.constants.BusinessConstants;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "From card ID is required")
        Long fromCardId,

        @NotNull(message = "To card ID is required")
        Long toCardId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = BusinessConstants.MIN_TRANSFER_AMOUNT_STRING, message = "Amount must be at least 0.01")
        @DecimalMax(value = BusinessConstants.MAX_TRANSFER_AMOUNT_STRING, message = "Amount cannot exceed 1,000,000.00")
        BigDecimal amount,

        @Size(max = BusinessConstants.MAX_DESCRIPTION_LENGTH, message = "Description must not exceed 500 characters")
        String description
) {}
