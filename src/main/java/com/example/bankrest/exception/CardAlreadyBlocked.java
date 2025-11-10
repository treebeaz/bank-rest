package com.example.bankrest.exception;

public class CardAlreadyBlocked extends RuntimeException {
    public CardAlreadyBlocked(String message) {
        super(message);
    }
}
