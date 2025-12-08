package com.example.bankcards.dto;

import com.example.bankcards.constants.BusinessConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 2, max = BusinessConstants.MAX_FULL_NAME_LENGTH,
              message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Size(min = BusinessConstants.MIN_PASSWORD_LENGTH, message = "Password must be at least 6 characters")
        String password
) {}
