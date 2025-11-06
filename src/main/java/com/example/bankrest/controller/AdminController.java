package com.example.bankrest.controller;

import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
