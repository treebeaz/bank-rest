package com.example.bankrest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class RegisterRequestDto {
    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "Password is required")
    @Size(min = 7, message = "Password must contain at least 10 characters")
    String password;

    @NotBlank(message = "Firstname is required")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    String firstname;

    @NotBlank(message = "Lastname is required")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    String lastname;
}
