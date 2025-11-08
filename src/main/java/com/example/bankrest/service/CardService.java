package com.example.bankrest.service;

import com.example.bankrest.component.CardCrypto;
import com.example.bankrest.component.CardNumberGenerator;
import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Status;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.exception.CardOperationException;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CardCrypto cardCrypto;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto createCard(CardRequestDto cardRequest) {
        log.info("Started creating a card: {}", cardRequest.getFirstname());
        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String[] cardInfo = cardNumberGenerator.generateUniqueCardNumber();

        Card card = Card.builder()
                .cardNumber(cardCrypto.encrypt(cardInfo[0]))
                .lastDigits(cardInfo[0].substring(cardInfo[0].length() - 4))
                .hashCardNumber(cardInfo[1])
                .user(user)
                .cardholderName(cardRequest.getFirstname() + " " + cardRequest.getLastname())
                .balance(BigDecimal.ZERO)
                .status(Status.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .build();

        log.info("Card created successfully: {}", card.getCardholderName());

        return createResponse(cardRepository.save(card));
    }

    private CardResponseDto createResponse(Card card) {
        return CardResponseDto.builder()
                .id(card.getId())
                .masked("**** **** **** " + card.getLastDigits())
                .cardHolderName(card.getCardholderName())
                .balance(card.getBalance())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .build();
    }

    public Page<CardResponseDto> getUserCards(int page, int size) {
        User user = getAuthenticatedUser();

        Page<Card> cards = cardRepository.findByUser_Id(user.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        log.info("Cards found: {}", cards.getTotalElements());

        return cards.map(this::createResponse);
    }

    public BigDecimal getBalance(Long cardId) {
        User user = getAuthenticatedUser();

        Card card = cardRepository.findByIdAndUser_Id(cardId, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        return card.getBalance();
    }

    private User getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<CardResponseDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::createResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardResponseDto requestCardBlocked(Long cardId) {
        User user = getAuthenticatedUser();
        Card card = cardRepository.findByIdAndUser_Id(cardId, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setStatus(Status.PENDING_BLOCK);
//        cardRepository.save(card);

        log.info("Block requested for card {} by user {}", cardId, user.getUsername());

        return createResponse(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto blockedCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        switch (card.getStatus()) {
            case ACTIVE:
                throw new CardOperationException("Card already activated");
            case PENDING_BLOCK:
                card.setStatus(Status.BLOCKED);
                log.info("Card be blocked successfully: {}", card.getCardholderName());
                break;
            case BLOCKED:
                throw new CardOperationException("Card already is blocked");
            default:
                throw new CardOperationException("Can't blocked card in currentStatus" + card.getStatus());
        }

        return createResponse(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        switch (card.getStatus()) {
            case ACTIVE:
                throw new CardOperationException("Card already activated");
            case PENDING_BLOCK:
                throw new CardOperationException("Card waiting be block");
            case BLOCKED:
                card.setStatus(Status.ACTIVE);
                log.info("Card activated: {}", card.getLastDigits());
                break;
            default:
                throw new CardOperationException("Can't activate card in currentStatus" + card.getStatus());
        }

        return createResponse(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteCard(Long cardId) {
        return cardRepository.findById(cardId)
                .map(card -> {
                    cardRepository.delete(card);
                    return true;
                })
                .orElse(false);
    }

}
