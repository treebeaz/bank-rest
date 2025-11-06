package com.example.bankrest.dto.auth;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ErrorResponseDto {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
}
