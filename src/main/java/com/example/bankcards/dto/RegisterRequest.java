package com.example.bankcards.dto;

import com.example.bankcards.constants.BusinessConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = BusinessConstants.MIN_USERNAME_LENGTH, max = BusinessConstants.MAX_USERNAME_LENGTH,
              message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = BusinessConstants.MIN_PASSWORD_LENGTH, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Full name is required")
        @Size(max = BusinessConstants.MAX_FULL_NAME_LENGTH, message = "Full name must not exceed 100 characters")
        String fullName
) {}
