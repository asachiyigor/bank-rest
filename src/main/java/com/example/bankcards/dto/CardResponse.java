package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String cardNumber,
        String ownerFullName,
        LocalDate expiryDate,
        String status,
        BigDecimal balance
) {}
