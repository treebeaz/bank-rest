package com.example.bankrest.exception;

public class CardInvalidStatusException extends RuntimeException {
    public CardInvalidStatusException(String message) {
        super(message);
    }
}
