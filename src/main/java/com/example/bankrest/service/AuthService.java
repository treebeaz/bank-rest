package com.example.bankrest.service;

import com.example.bankrest.dto.auth.AuthRequestDto;
import com.example.bankrest.dto.auth.AuthResponseDto;
import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.InvalidCredentialsException;
import com.example.bankrest.exception.UserAlreadyExistsException;
import com.example.bankrest.mapper.UserMapper;
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
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthResponseDto register(RegisterRequestDto registerRequest, boolean isUser) {
        if (userService.existsByUsername(registerRequest.getUsername())) {
            log.error("AuthService.register.fail");
            throw new UserAlreadyExistsException(registerRequest.getUsername());
        }

        UserResponseDto createdUser = userService.createUser(registerRequest, isUser);
        String token = jwtUtil.generateToken(userMapper.userResponseDtoToUserEntity(createdUser));

        log.info("AuthService.register.success_for_user: {}", createdUser.getId());

        return buildAuthResponse(token, createdUser.getUsername(), createdUser.getRole().name());
    }


    public AuthResponseDto login(AuthRequestDto authRequest) {
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

            return buildAuthResponse(token, user.getUsername(), user.getRole().name());

        } catch (BadCredentialsException e) {
            log.error("AuthService.login.fail");
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    private AuthResponseDto buildAuthResponse(String token, String username, String role) {
        return AuthResponseDto.builder()
                .token(token)
                .username(username)
                .role(role)
                .build();
    }

}
