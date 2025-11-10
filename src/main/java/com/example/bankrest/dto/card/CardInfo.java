package com.example.bankrest.dto.card;

import lombok.*;

@Value
@Builder
public class CardInfo {
    String cardNumber;
    String hashCardNumber;
    String lastDigits;
}
