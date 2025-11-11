package com.example.bankrest.controller.handler;

import com.example.bankrest.constants.ErrorMessages;
import com.example.bankrest.constants.LogMessages;
import com.example.bankrest.dto.auth.ErrorResponseDto;
import com.example.bankrest.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.example.bankrest.controller")
@Slf4j
public class GlobalRestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(HttpServletRequest request, UserNotFoundException ex) {
        return createResponse(request, ex, HttpStatus.NOT_FOUND, ErrorMessages.USER_NOT_FOUND_ERROR_MESSAGE, LogMessages.USER_NOT_FOUND_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(HttpServletRequest request, InvalidCredentialsException ex) {
        return createResponse(request, ex, HttpStatus.UNAUTHORIZED, ErrorMessages.INVALID_CREDENTIALS_ERROR_MESSAGE, LogMessages.INVALID_CREDENTIALS_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserExists(HttpServletRequest request, UserAlreadyExistsException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.USER_ALREADY_EXISTS_ERROR_MESSAGE, LogMessages.USER_ALREADY_EXISTS_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCardNotFound(HttpServletRequest request, CardNotFoundException ex) {
        return createResponse(request,ex, HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND_ERROR_MESSAGE, LogMessages.CARD_NOT_FOUND_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(CardAlreadyActiveException.class)
    public ResponseEntity<ErrorResponseDto> handleCardAlreadyActive(HttpServletRequest request, CardAlreadyActiveException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.CARD_ALREADY_ACTIVE_ERROR_MESSAGE, LogMessages.CARD_ALREADY_ACTIVE_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(CardPendingBlockException.class)
    public ResponseEntity<ErrorResponseDto> handleCardPendingBlock(HttpServletRequest request, CardPendingBlockException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.CARD_PENDING_BLOCK_ERROR_MESSAGE, LogMessages.CARD_PENDING_BLOCK_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(CardInvalidStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleCardInvalidStatus(HttpServletRequest request, CardInvalidStatusException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.CARD_INVALID_STATUS_ERROR_MESSAGE, LogMessages.CARD_INVALID_STATUS_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientFunds(HttpServletRequest request, InsufficientFundsException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.INSUFFICIENT_FUNDS_ERROR_MESSAGE, LogMessages.INSUFFICIENT_FUNDS_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(CardNotActiveException.class)
    public ResponseEntity<ErrorResponseDto> handleCardNotActive(HttpServletRequest request, CardNotActiveException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.CARD_NOT_ACTIVE_ERROR_MESSAGE, LogMessages.CARD_NOT_ACTIVE_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(DifferentCardholdersException.class)
    public ResponseEntity<ErrorResponseDto> handleDifferentCardholders(HttpServletRequest request, DifferentCardholdersException ex) {
        return createResponse(request,ex, HttpStatus.CONFLICT, ErrorMessages.DIFFERENT_CARDHOLDERS_ERROR_MESSAGE, LogMessages.DIFFERENT_CARDHOLDERS_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(SameCardTransferException.class)
    public ResponseEntity<ErrorResponseDto> handleSameCardTransfer(HttpServletRequest request, SameCardTransferException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.SAME_CARD_TRANSFER_ERROR_MESSAGE, LogMessages.SAME_CARD_TRANSFER_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    @ExceptionHandler(CardAlreadyBlockedException.class)
    public ResponseEntity<ErrorResponseDto> handleCardAlreadyBlocked(HttpServletRequest request, CardAlreadyBlockedException ex) {
        return createResponse(request, ex, HttpStatus.CONFLICT, ErrorMessages.CARD_ALREADY_BLOCKED_ERROR_MESSAGE, LogMessages.CARD_ALREADY_BLOCKED_LOG_MESSAGE_IN_GLOBAL_HANDLER);
    }

    private ResponseEntity<ErrorResponseDto> createResponse(HttpServletRequest request,
                                                            RuntimeException ex,
                                                            HttpStatus status,
                                                            String error,
                                                            String logMessage) {
        log.error("{}: {}", logMessage, ex.getMessage());
        return ResponseEntity.status(status).body(ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build()
        );
    }
}
