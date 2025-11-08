package com.example.bankrest.component;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CardCrypto {
    private final BytesEncryptor bytesEncryptor;

    public String encrypt(String cardNumber) {
        byte[] bytes = bytesEncryptor.encrypt(cardNumber.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String decrypt(String cardNumber) {
        byte[] decode = Base64.getDecoder().decode(cardNumber);
        byte[] decrypted = bytesEncryptor.decrypt(decode);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
