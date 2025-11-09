package com.example.bankrest.component;

import com.example.bankrest.dto.CardInfo;
import com.example.bankrest.repository.CardRepository;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class CardNumberGenerator {

    private static final String BIN = "411111";
    private final CardRepository cardRepository;
    private final CardCrypto cardCrypto;

    public CardInfo generateUniqueCardNumber() {
        String cardNumber;
        String hashNumber;

        do {
            cardNumber = generateCardNumber();
            hashNumber = getHashCardNumber(cardNumber);
        } while (!isUnique(hashNumber));

        return createUniqueCardNumber(cardNumber, hashNumber);
    }

    private CardInfo createUniqueCardNumber(String cardNumber, String hashNumber) {
        return CardInfo.builder()
                .cardNumber(cardCrypto.encrypt(cardNumber))
                .hashCardNumber(hashNumber)
                .lastDigits(cardNumber.substring(cardNumber.length() - 4))
                .build();
    }

    private boolean isUnique(String hash) {
        return !cardRepository.existsByHashCardNumber(hash);
    }

    public String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(BIN);
        for(int i = BIN.length(); i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        String lastDigitFromAlgorithmLuna = calculateLastDigitFromLuna(sb.toString());
        sb.append(lastDigitFromAlgorithmLuna);

        return sb.toString();
    }

    private String calculateLastDigitFromLuna(String cardNumber) {

        int check = 10 - (lunaAlgorithm(cardNumber + "0") % 10);
        if(check == 10) {
            check = 0;
        }

        return Integer.toString(check);
    }

    private int lunaAlgorithm(String cardNumber) {
        int sum = 0;
        boolean rotate = false;
        for(int i = cardNumber.length() - 1; i >= 0; i--) {
            int element = cardNumber.charAt(i) - '0';
            if(rotate) {
                element *= 2;
                if (element > 9) {
                    element = (element % 10) + 1;
                }
            }
            sum += element;
            rotate = !rotate;
        }
        System.out.println(sum);
        return sum;
    }

    public String getHashCardNumber(String cardNumber) {
        return Hashing.sha256()
                .hashString(cardNumber, StandardCharsets.UTF_8)
                .toString();
    }
}
