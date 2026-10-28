package com.tradeway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 32, message = "Username must be between 3 and 32 characters")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    String password,

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 80, message = "First name must be between 1 and 80 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 80, message = "Last name must be between 1 and 80 characters")
    String lastName
) {
}
