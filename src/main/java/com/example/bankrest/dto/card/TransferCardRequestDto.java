package com.example.bankrest.dto.card;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferCardRequestDto {
    Long id;
    BigDecimal amount;

}
