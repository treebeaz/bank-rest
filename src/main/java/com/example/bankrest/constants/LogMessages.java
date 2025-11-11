package com.example.bankrest.constants;

public final class LogMessages {
    public static final String USER_NOT_FOUND_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleUserNotFound.fail.userNotFound";
    public static final String INVALID_CREDENTIALS_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleInvalidCredentials.fail.invalidCredentials";
    public static final String USER_ALREADY_EXISTS_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleUserAlreadyExists.fail.userAlreadyExists";
    public static final String CARD_NOT_FOUND_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleCardNotFound.fail.cardNotFound";
    public static final String CARD_ALREADY_ACTIVE_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleCardAlreadyActive.fail.cardAlreadyActive";
    public static final String CARD_PENDING_BLOCK_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleCardPendingBlock.fail.cardPendingBlock";
    public static final String CARD_INVALID_STATUS_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleCardInvalidStatus.fail.cardInvalidStatus";
    public static final String INSUFFICIENT_FUNDS_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleTransferOperations.fail.InsufficientFunds";
    public static final String CARD_NOT_ACTIVE_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleCardNotActive.fail.cardNotActive";
    public static final String DIFFERENT_CARDHOLDERS_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleDifferentCardHolders.fail.differentCardHolders";
    public static final String SAME_CARD_TRANSFER_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleSameCardTransfer.fail.sameCardTransfer";
    public static final String CARD_ALREADY_BLOCKED_LOG_MESSAGE_IN_GLOBAL_HANDLER = "GlobalRestControllerExceptionHandler.handleCardAlreadyBlocked.fail.cardAlreadyBlocked";

    private LogMessages() {}
}
