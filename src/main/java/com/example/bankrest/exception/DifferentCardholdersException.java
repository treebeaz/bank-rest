package com.example.bankrest.exception;

public class DifferentCardholdersException extends RuntimeException {
    public DifferentCardholdersException(String message) {
        super(message);
    }
}
