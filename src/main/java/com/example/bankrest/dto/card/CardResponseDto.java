package com.example.bankrest.dto.card;

import com.example.bankrest.entity.Status;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CardResponseDto {
    Long id;
    String lastDigits;
    String firstname;
    String lastname;
    BigDecimal balance;
    LocalDate expiryDate;
    Status status;
    Long userId;
}
