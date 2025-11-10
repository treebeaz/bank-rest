package com.example.bankrest.controller;

import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.auth.AuthRequestDto;
import com.example.bankrest.dto.auth.AuthResponseDto;
import com.example.bankrest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Авторизация и Регистрация", description = "API для регистрации и логина пользователей")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    @Operation(summary = "Регистрация пользователей", description = "Регистрация новых пользователей в системе")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неправильный формат введенных данных"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже зарегистрирован")
    })
    public ResponseEntity<AuthResponseDto> registration(@RequestBody @Valid RegisterRequestDto registerRequest) {
        AuthResponseDto response = authService.register(registerRequest, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователей", description = "Авторизация пользователей в системе")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Авторизация успешно пройдена"),
            @ApiResponse(responseCode = "400", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль")
    })
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthRequestDto authRequest) {
        AuthResponseDto response = authService.login(authRequest);
        return ResponseEntity.ok().body(response);
    }
}
