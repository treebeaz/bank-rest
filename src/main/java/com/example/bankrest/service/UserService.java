package com.example.bankrest.service;


import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.mapper.UserMapper;
import com.example.bankrest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    protected UserResponseDto createUser(RegisterRequestDto registerRequest,
                                     boolean isUser) {

        return Optional.of(registerRequest)
                .map(userMapper::registerRequestDtoToUser)
                .map(user -> {
                    user.setEnabled(true);
                    user.setRole((isUser) ? Role.USER : Role.ADMIN);
                    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
                    log.info("UserService.createUser.success.forUser: {}", user.getId());
                    return userRepository.save(user);
                })
                .map(userMapper::userEntityToUserResponseDto)
                .orElseThrow(() -> new RuntimeException("Failed to create user"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getRole(user.getRole().name()))
                .build();
    }

    private String getRole(String role) {
        return "ROLE_" + role;
    }

}
