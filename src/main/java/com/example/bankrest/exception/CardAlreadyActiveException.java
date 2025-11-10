package com.example.bankrest.exception;

public class CardAlreadyActiveException extends RuntimeException {
    public CardAlreadyActiveException(String message) {
        super(message);
    }
}
