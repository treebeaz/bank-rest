package com.example.bankrest.controller;

import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.auth.AuthRequestDto;
import com.example.bankrest.dto.auth.AuthResponseDto;
import com.example.bankrest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    public ResponseEntity<AuthResponseDto> registration(@RequestBody @Valid RegisterRequestDto registerRequest) {
        AuthResponseDto response = authService.register(registerRequest, true);
        log.info("Register user successful: {}", response.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthRequestDto authRequest) {
        AuthResponseDto response = authService.login(authRequest);
        log.info("Login user successful: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }
}
