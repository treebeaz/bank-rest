package com.example.bankrest.constants;

public final class ErrorMessages {

    public static final String USER_NOT_FOUND_ERROR_MESSAGE = "User not found";
    public static final String INVALID_CREDENTIALS_ERROR_MESSAGE = "Invalid credentials";
    public static final String USER_ALREADY_EXISTS_ERROR_MESSAGE = "User already exists";
    public static final String CARD_NOT_FOUND_ERROR_MESSAGE = "Card not found";
    public static final String CARD_ALREADY_ACTIVE_ERROR_MESSAGE = "Card already activated";
    public static final String CARD_PENDING_BLOCK_ERROR_MESSAGE = "Card waiting be pending block";
    public static final String CARD_INVALID_STATUS_ERROR_MESSAGE = "Card status conflict";
    public static final String INSUFFICIENT_FUNDS_ERROR_MESSAGE = "There are not enough funds on the sender card";
    public static final String CARD_NOT_ACTIVE_ERROR_MESSAGE = "One of the cards is not active";
    public static final String DIFFERENT_CARDHOLDERS_ERROR_MESSAGE = "One of the cards is not cardholder";
    public static final String SAME_CARD_TRANSFER_ERROR_MESSAGE = "Can't transfer to the same card";

    private ErrorMessages() {}
}
