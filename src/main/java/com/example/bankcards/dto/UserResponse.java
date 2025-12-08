package com.example.bankcards.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        Set<String> roles,
        LocalDateTime createdAt
) {}
