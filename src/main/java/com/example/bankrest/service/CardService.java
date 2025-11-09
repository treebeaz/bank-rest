package com.example.bankrest.service;

import com.example.bankrest.component.CardNumberGenerator;
import com.example.bankrest.dto.CardInfo;
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

    private static final BigDecimal DEFAULT_BALANCE_WHEN_CREATING_CARD = BigDecimal.ZERO;
    private static final Status DEFAULT_STATUS_WHEN_CREATING_CARD = Status.ACTIVE;
    private static final LocalDate DEFAULT_EXPIRY_DATE_WHEN_CREATING_CARD = LocalDate.now().plusYears(5);

    public CardResponseDto requestCreateCard(CardRequestDto cardRequestDto) {

        User user = userRepository.findById(cardRequestDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));


    }

    // Добавить запрос на создание карты от пользователя.
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto createCard(CardRequestDto cardRequest) {
        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        CardInfo cardInfo = cardNumberGenerator.generateUniqueCardNumber();
        Card card = buildCard(cardInfo, cardRequest, user);
        log.info("CardService.createCard.success_for_user: {}", card.getUser().getId());

        return createResponse(cardRepository.save(card));
    }

    private Card buildCard(CardInfo cardInfo,
                           CardRequestDto cardRequest,
                           User user) {
        return Card.builder()
                .cardNumber(cardInfo.getCardNumber())
                .lastDigits(cardInfo.getLastDigits())
                .hashCardNumber(cardInfo.getLastDigits())
                .user(user)
                .cardholderName(buildCardHolderName(cardRequest.getFirstname(), cardRequest.getLastname()))
                .balance(DEFAULT_BALANCE_WHEN_CREATING_CARD)
                .status(DEFAULT_STATUS_WHEN_CREATING_CARD)
                .expiryDate(DEFAULT_EXPIRY_DATE_WHEN_CREATING_CARD)
                .build();
    }

    private String buildCardHolderName(String firstname, String lastname) {
        return firstname + " " + lastname;
    }

    private CardResponseDto createResponse(Card card) {
        return CardResponseDto.builder()
                .id(card.getId())
                .masked(getMask(card.getLastDigits()))
                .cardHolderName(card.getCardholderName())
                .balance(card.getBalance())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .build();
    }

    private String getMask(String lastDigits) {
        return "**** **** **** " + lastDigits;
    }

    public Page<CardResponseDto> getUserCards(int page, int size) {
        User user = getAuthenticatedUser();

        Page<Card> cards = cardRepository.findByUser_Id(user.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        log.info("CardService.getUserCards.success: {}", cards.getTotalElements());

        return cards.map(this::createResponse);
    }

    public BigDecimal getBalance(Long cardId) {
        User user = getAuthenticatedUser();

        Card card = cardRepository.findByIdAndUser_Id(cardId, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        log.info("CardService.getBalance.success_for_user: {}", card.getUser().getId());

        return card.getBalance();
    }

    private User getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("CardService.getAuthenticatedUser.success");
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
    public CardResponseDto requestCardBlock(Long cardId) {
        User user = getAuthenticatedUser();

        Card card = cardRepository.findByIdAndUser_Id(cardId, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (card.getStatus().equals(Status.BLOCKED)) {
            throw new CardOperationException("Card already is blocked");
        } else {
            card.setStatus(Status.PENDING_BLOCK);
            log.info("CardService.requestCardBlock.success_for_user: {}", card.getUser().getId());
            return createResponse(card);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto cardBlock(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        switch (card.getStatus()) {
            case ACTIVE:
                throw new CardOperationException("Card already activated");
            case PENDING_BLOCK:
                card.setStatus(Status.BLOCKED);
                break;
            case BLOCKED:
                throw new CardOperationException("Card already is blocked");
            default:
                throw new CardOperationException("Can't blocked card in currentStatus" + card.getStatus());
        }

        log.info("CardService.cardBlock.success_for_user: {}", card.getUser().getId());

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
                break;
            default:
                throw new CardOperationException("Can't activate card in currentStatus " + card.getStatus());
        }

        log.info("CardService.activateCard.success_for_user {}", card.getUser().getId());

        return createResponse(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(Long cardId) {
        cardRepository.deleteCardById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

}
