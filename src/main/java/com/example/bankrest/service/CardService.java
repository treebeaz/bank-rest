package com.example.bankrest.service;

import com.example.bankrest.component.CardNumberGenerator;
import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Status;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.mapper.CardMapper;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final CardNumberGenerator cardNumberGenerator;

    @Transactional
    public CardResponseDto createCard(CardRequestDto cardRequest) {
        log.info("Started creating a card: {}", cardRequest.getFirstname());
        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String cardNumber = cardNumberGenerator.generateCardNumber();

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .lastDigits(cardNumber.substring(cardNumber.length() - 4))
                .user(user)
                .firstname(cardRequest.getFirstname())
                .lastname(cardRequest.getLastname())
                .balance(BigDecimal.ZERO)
                .status(Status.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .build();
        log.info("Card created successfully: {}", card.getFirstname());

        return cardMapper.cardEntityToResponseDto(cardRepository.save(card));
    }

}
