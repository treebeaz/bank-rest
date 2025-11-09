package com.example.bankrest.controller;

import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.service.CardService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/cards")
@RestController
@Slf4j
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<CardResponseDto>> findUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CardResponseDto> cards = cardService.getUserCards(page, size);
        return ResponseEntity.ok().body(cards.getContent());
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> findBalance(@PathVariable Long cardId) {
        return ResponseEntity.ok().body(cardService.getBalance(cardId));
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<CardResponseDto> requestCardBlock(@PathVariable @NonNull Long cardId) {
        CardResponseDto response = cardService.requestCardBlock(cardId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<CardResponseDto> requestCreateCard(@RequestBody CardRequestDto cardRequestDto) {
        CardResponseDto response = cardService.requestCreateCard(cardRequestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
