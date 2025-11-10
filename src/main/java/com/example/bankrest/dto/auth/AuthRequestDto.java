package com.example.bankrest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthRequestDto {
    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must contain at least 5 characters")
    String username;

    @NotBlank(message = "Password is required")
    @Size(min = 7, max = 50, message = "Password must contain at least 7 characters")
    String password;
}
