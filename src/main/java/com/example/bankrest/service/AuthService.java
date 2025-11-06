package com.example.bankrest.service;

import com.example.bankrest.dto.auth.AuthRequestDto;
import com.example.bankrest.dto.auth.AuthResponseDto;
import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.InvalidCredentialsException;
import com.example.bankrest.exception.UserAlreadyExistsException;
import com.example.bankrest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDto register(RegisterRequestDto registerRequest, boolean isUser) {
        log.info("Starting registration user: {}", registerRequest.getUsername());

        if (userService.existsByUsername(registerRequest.getUsername())) {
            log.error("Username already exists : {}", registerRequest.getUsername());
            throw new UserAlreadyExistsException(registerRequest.getUsername());
        }

        UserResponseDto createdUser = userService.create(registerRequest, isUser);
        User user = userService.findByUsername(createdUser.getUsername());
        String token = jwtUtil.generateToken(user);

        return AuthResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponseDto login(AuthRequestDto authRequest) {
        log.info("Starting login user: {}", authRequest.getUsername());

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

            return AuthResponseDto.builder()
                    .token(token)
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", authRequest.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }
}
