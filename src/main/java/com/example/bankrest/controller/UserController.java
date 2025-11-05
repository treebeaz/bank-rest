package com.example.bankrest.controller;

import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("api/users")
@RestController
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> findUserById(@PathVariable Long id) {
        try {
            UserResponseDto userResponse = userService.findById(id);
            log.info("User found successfully: {}", userResponse);
            return ResponseEntity.ok(userResponse);
        } catch (UserNotFoundException e) {
            log.error("Error finding user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
