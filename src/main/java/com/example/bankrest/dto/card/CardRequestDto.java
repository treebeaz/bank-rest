package com.example.bankrest.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CardRequestDto {
    @NotNull
    Long userId;

    @NotBlank
    String firstname;

    @NotBlank
    String lastname;
}
