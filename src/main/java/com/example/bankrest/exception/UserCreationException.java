package com.example.bankrest.exception;

public class UserCreationException extends RuntimeException {
    public UserCreationException(String message) {
        super(message);
    }
}
