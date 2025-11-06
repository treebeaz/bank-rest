package com.example.bankrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Вынес в отдельный конфигурационный файл, потому что получалось так, что при создании токена:
 * JwtFilter зависит от UserService
 * UserService зависит от PasswordEncoder (был в SecurityConfig)
 * SecurityConfig создает PasswordEncoder и зависит от JwtFilter
 * и получалось зацикливание
 */
@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
