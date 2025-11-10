package com.example.bankrest.exception;

public class TransferOperationException extends RuntimeException {
    public TransferOperationException(String message) {
        super(message);
    }
}
