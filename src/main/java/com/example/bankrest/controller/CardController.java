package com.example.bankrest.controller;

import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.dto.card.TransferCardRequestDto;
import com.example.bankrest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/cards")
@RestController
@Slf4j
@Tag(name = "Управление картами", description = "API для управления картами от пользователей")
public class CardController {

    private final CardService cardService;

    @PostMapping("/create")
    @Operation(summary = "Запрос на создание карты", description = "Запрос пользователя на создание карты")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Запрос на создание успешно создан"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Карта уже находится в ожидании создания")
    })
    public ResponseEntity<CardResponseDto> requestCreateCard() {
        CardResponseDto response = cardService.requestCreateCard();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    @Operation(summary = "Получение карт", description = "Получение всех карт")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карты найдены"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<List<CardResponseDto>> findUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CardResponseDto> cards = cardService.getUserCards(page, size);
        return ResponseEntity.ok().body(cards.getContent());
    }

    @GetMapping("/{cardId}/balance")
    @Operation(summary = "Получение баланса", description = "Получение баланса карты")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс найден"),
            @ApiResponse(responseCode = "404", description = "Карта или пользователь не найдены")
    })
    public ResponseEntity<BigDecimal> findBalance(@PathVariable Long cardId) {
        return ResponseEntity.ok().body(cardService.getBalance(cardId));
    }

    @PostMapping("/{cardId}/block")
    @Operation(summary = "Запрос на блокировку карты", description = "Запрос пользователя на блокировку карты")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Запрос на блокировку успешно создан"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "409", description = "Карта уже заблокирована")
    })
    public ResponseEntity<CardResponseDto> requestCardBlock(@PathVariable @NonNull Long cardId) {
        CardResponseDto response = cardService.requestCardBlock(cardId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/{cardId}/transfer")
    @Operation(summary = "Переводы между картами", description = "Перевод денег с одной карты на другую")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Транзакция прошла успешно"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "409", description = "Ошибка операции перевода")
    })
    public ResponseEntity<Void> transferCard(@PathVariable Long cardId, @RequestBody TransferCardRequestDto transferCardRequestDto) {
        cardService.moneyTransfer(cardId, transferCardRequestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
