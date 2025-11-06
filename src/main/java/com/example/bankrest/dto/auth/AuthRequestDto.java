package com.example.bankrest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class AuthRequestDto {
    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "Password is required")
    @Size(min = 7, message = "Password must contain at least 7 characters")
    String password;
}
