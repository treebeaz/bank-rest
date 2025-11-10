package com.example.bankrest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterRequestDto {
    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must contain at least 5 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "The username must contain only letters and numbers")
    String username;

    @NotBlank(message = "Password is required")
    @Size(min = 7, max = 50, message = "The password must contain at least 7 characters")
    String password;

    @NotBlank(message = "Firstname is required")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z].*", message = "The firstname must begin with a capital letter")
    String firstname;

    @NotBlank(message = "Lastname is required")
    @Size(min = 2, max = 50, message = "Lastname must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z].*", message = "The lastname must begin with a capital letter")
    String lastname;
}
