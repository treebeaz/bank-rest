package com.example.bankrest.controller;

import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.UserService;
import jakarta.transaction.Transactional;
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
@RequestMapping("api/users")
@RestController
@Slf4j
public class UserController {

//    private final UserService userService;
//    private final CardService cardService;

//    @Transactional
//    public void requestBlockedCard(Long cardId) {
//        User user = cardService.getAu
//    }

//
//    @GetMapping
//    public ResponseEntity<?> findUserById(@PathVariable Long userId) {
//        try {
//            UserResponseDto userResponse = userService.findById(userId);
//            log.info("User found successfully: {}", userResponse);
//            return ResponseEntity.ok(userResponse);
//        } catch (UserNotFoundException e) {
//            log.error("Error finding user {}: {}", userId, e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }



}
