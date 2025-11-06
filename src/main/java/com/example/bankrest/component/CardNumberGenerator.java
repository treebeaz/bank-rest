package com.example.bankrest.component;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CardNumberGenerator {
    private static final String BIN = "411111";

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

//    private boolean isValidCardNumber(String cardNumber) {
//        return lunaAlgorithm(cardNumber) % 10 == 0;
//    }

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

}
