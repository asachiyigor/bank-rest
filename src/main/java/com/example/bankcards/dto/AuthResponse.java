package com.example.bankcards.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        String type,
        Long id,
        String username,
        String email,
        String fullName,
        Set<String> roles
) {
    public AuthResponse {
        if (type == null) {
            type = "Bearer";
        }
    }

    public AuthResponse(String token, Long id, String username, String email, String fullName, Set<String> roles) {
        this(token, "Bearer", id, username, email, fullName, roles);
    }
}
