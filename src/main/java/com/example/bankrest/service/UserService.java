package com.example.bankrest.service;

import com.example.bankrest.dto.RegisterRequest;
import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.exception.UsernameAlreadyExistsException;
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

import java.util.Collections;
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
    public UserResponseDto create(RegisterRequest registerRequest) {

        if(userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username with that username already exists");
        }

        return Optional.of(registerRequest)
                .map(userMapper::registerRequestDtoToUser)
                .map(user -> {
                    user.setEnabled(true);
                    user.setRole(Role.USER);
                    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
                    log.info("Saving user: {}", user);
                    return userRepository.save(user);
                })
                .map(userMapper::userEntityToUserResponseDto)
                .orElseThrow(() -> new RuntimeException("Failed to create user"));
    }

    public UserResponseDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::userEntityToUserResponseDto)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
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
        return userRepository.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));
    }

}
