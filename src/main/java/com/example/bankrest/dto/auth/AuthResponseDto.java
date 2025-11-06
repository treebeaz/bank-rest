package com.example.bankrest.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponseDto {
    String token;
    String username;
    String role;
}
