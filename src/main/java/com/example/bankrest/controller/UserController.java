package com.example.bankrest.controller;

import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("api/users/{userId}")
@RestController
@Slf4j
public class UserController {

    private final UserService userService;


    @GetMapping
    public ResponseEntity<?> findUserById(@PathVariable Long userId) {
        try {
            UserResponseDto userResponse = userService.findById(userId);
            log.info("User found successfully: {}", userResponse);
            return ResponseEntity.ok(userResponse);
        } catch (UserNotFoundException e) {
            log.error("Error finding user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
