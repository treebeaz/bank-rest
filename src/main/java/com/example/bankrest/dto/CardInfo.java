package com.example.bankrest.dto;

import lombok.*;

@Value
@Builder
public class CardInfo {
    String cardNumber;
    String hashCardNumber;
    String lastDigits;
}
