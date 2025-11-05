package com.example.bankrest.controller;

import com.example.bankrest.dto.*;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.InvalidCredentialsException;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.exception.UsernameAlreadyExistsException;
import com.example.bankrest.service.UserService;
import com.example.bankrest.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
@Slf4j
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

//    @GetMapping("/login")
//    public

    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> createUser(@RequestBody @Valid RegisterRequest registerRequest) {
        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username with that username already exists");
        }

        try {
            UserResponseDto createdUser = userService.create(registerRequest);
            log.info("User created successfully: {}", createdUser);

            User user = userService.findByUsername(createdUser.getUsername());

            String token = jwtUtil.generateToken(user);

            log.info("Registration success. Token: {}", token);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.builder()
                            .token(token)
                            .username(user.getUsername())
                            .role(user.getRole().name())
                            .build());
        } catch (Exception e) {
            log.error("Error creating user {}: {}", registerRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            String token = jwtUtil.generateToken(user);

            log.info("login success. Token: {}", token);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .build());
        } catch (BadCredentialsException e) {
            log.error("Invalid login for user: {}", authRequest);
            throw new InvalidCredentialsException("Invalid username or password");
        } catch (Exception e) {
            log.error("Login error for user: {}", authRequest);
            throw new RuntimeException("Login error");
        }

    }
}
