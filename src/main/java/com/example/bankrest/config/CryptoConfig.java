package com.example.bankrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

@Configuration
public class CryptoConfig {
    @Bean
    public BytesEncryptor bytesEncryptor() {
        return Encryptors.stronger("card-encryptor", "12345678");
    }
}
