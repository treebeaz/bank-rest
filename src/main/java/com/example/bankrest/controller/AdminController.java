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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CardService cardService;

    @PostMapping("/create")
    public ResponseEntity<CardResponseDto> createCard(@RequestBody CardRequestDto cardRequest) {
        CardResponseDto response = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardResponseDto>> getAllCards() {
        return ResponseEntity.ok().body(cardService.getAllCards());
    }

    @PostMapping("/cards/{cardId}/blocked")
    public ResponseEntity<CardResponseDto> blockCard(@PathVariable Long cardId) {
        CardResponseDto response = cardService.blockedCard(cardId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/cards/{cardId}/delete")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        if (cardService.deleteCard(cardId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/cards/{cardId}/activate")
    public ResponseEntity<CardResponseDto> activateCard(@PathVariable Long cardId) {
        CardResponseDto response = cardService.activateCard(cardId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
