package com.example.bankrest.controller;

import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Панель администратора", description = "API для административных функций управления пользователями и картами")
public class AdminController {

    private final CardService cardService;

    @PostMapping("/create")
    @Operation(summary = "Создание карты", description = "Подтверждение создания карты для пользователя. Только для администратора")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль администратора"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<CardResponseDto> createCard(@RequestBody CardRequestDto cardRequest) {  // принимаю просто userId. Firstname lastname пустые
        CardResponseDto response = cardService.createCard(cardRequest.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Получение карт", description = "Получение всех карт пользователей с пагинацией. Только для администратора")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль администратора")
    })
    public ResponseEntity<Page<CardResponseDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CardResponseDto> response = cardService.getAllCards(page, size);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{cardId}/block")
    @Operation(summary = "Блокировка карты", description = "Блокировка карты пользователя после их запроса. Только для администратора")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль администратора"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "409", description = "Карта активирована или уже заблокирована")
    })
    public ResponseEntity<CardResponseDto> cardBlock(@PathVariable Long cardId) {
        CardResponseDto response = cardService.cardBlock(cardId);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{cardId}/delete")
    @Operation(summary = "Удаление карты", description = "Удаление карты пользователя. Только для администратора")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль администратора"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/activate")
    @Operation(summary = "Активация карты", description = "Активация карты пользователя. Только для администратора")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно активирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещеню Требуется роль администратора"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "409", description = "Карта уже активирована или находится в ожидании блокировки")
    })
    public ResponseEntity<CardResponseDto> activateCard(@PathVariable Long cardId) {
        CardResponseDto response = cardService.activateCard(cardId);
        return ResponseEntity.ok().body(response);
    }
}
