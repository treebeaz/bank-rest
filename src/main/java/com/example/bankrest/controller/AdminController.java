package com.example.bankrest.controller;

import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/cards")
// Авторизация админов
@PreAuthorize("hasRole('ADMIN')")  // вынести в константы (enum)
public class AdminController {

    private final CardService cardService;

    @PostMapping("/create")
    public ResponseEntity<CardResponseDto> createCard(@RequestBody CardRequestDto cardRequest) {
        CardResponseDto response = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CardResponseDto>> getAllCards() {
        return ResponseEntity.ok().body(cardService.getAllCards());
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<CardResponseDto> cardBlock(@PathVariable Long cardId) {
        CardResponseDto response = cardService.cardBlock(cardId);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{cardId}/delete")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/activate")
    public ResponseEntity<CardResponseDto> activateCard(@PathVariable Long cardId) {
        CardResponseDto response = cardService.activateCard(cardId);
        return ResponseEntity.ok().body(response);
    }
}
