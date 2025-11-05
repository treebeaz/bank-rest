package com.example.bankrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Вынес в отдельный конфигурационный файл, потому что получилось так, что
 * JwtFilter зависит от UserService
 * UserService зависит от PasswordEncoder
 * SecurityConfig создает PasswordEncoder и зависит от JwtFilter
 */
@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
