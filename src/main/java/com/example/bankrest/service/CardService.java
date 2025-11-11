package com.example.bankrest.service;

import com.example.bankrest.component.CardNumberGenerator;
import com.example.bankrest.dto.card.CardInfo;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.dto.card.TransferCardRequestDto;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Status;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.*;
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

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;

    private static final BigDecimal DEFAULT_BALANCE_WHEN_CREATING_CARD = BigDecimal.ZERO;
    private static final Status DEFAULT_STATUS_WHEN_CREATING_CARD = Status.PENDING_ACTIVE;
    private static final LocalDate DEFAULT_EXPIRY_DATE_WHEN_CREATING_CARD = LocalDate.now().plusYears(5);

    @Transactional
    public CardResponseDto requestCreateCard() {
        User user = getAuthenticatedUser();
//        if (cardRepository.existsByUserAndStatus(user, DEFAULT_STATUS_WHEN_CREATING_CARD)) {
//            log.error("CardService.requestCreateCard.fail.CardAlreadyHasPendingCard");
//            throw new Card("Card already has pending card");
//        }
        CardInfo cardInfo = cardNumberGenerator.generateUniqueCardNumber();
        Card card = buildCard(cardInfo, user);
        cardRepository.save(card);

        log.info("CardService.requestCreateCard.success.forUser: {}", user.getId());
        return createResponse(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto createCard(Long userId) {
        Card card = cardRepository.findByUser_IdAndStatus(userId, DEFAULT_STATUS_WHEN_CREATING_CARD)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        card.setStatus(Status.ACTIVE);
        log.info("CardService.createCard.success.forUser: {}", userId);

        return createResponse(card);
    }

    private Card buildCard(CardInfo cardInfo, User user) {
        return Card.builder()
                .cardNumber(cardInfo.getCardNumber())
                .lastDigits(cardInfo.getLastDigits())
                .hashCardNumber(cardInfo.getHashCardNumber())
                .user(user)
                .cardholderName(buildCardHolderName(user.getFirstname(), user.getLastname()))
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
        Card card = cardRepository.findByIdAndUser_Id(cardId,
                user.getId()).orElseThrow(() -> new CardNotFoundException("Card not found"));
        log.info("CardService.getBalance.success.forUser: {}", card.getUser().getId());

        return card.getBalance();
    }

    private User getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponseDto> getAllCards(int page, int size) {
        Page<Card> cards = cardRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        log.info("CardService.getAllCards.success: {}", cards.getTotalElements());

        return cards.map(this::createResponse);
    }

    @Transactional
    public CardResponseDto requestCardBlock(Long cardId) {
        User user = getAuthenticatedUser();
        Card card = cardRepository.findByIdAndUser_Id(cardId,
                user.getId()).orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (card.getStatus().equals(Status.BLOCKED)) {
            throw new CardAlreadyBlockedException("Card already is blocked");
        } else {
            card.setStatus(Status.PENDING_BLOCK);
            log.info("CardService.requestCardBlock.success.forUser: {}", card.getUser().getId());
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
                throw new CardAlreadyActiveException("Card already activated");
            case PENDING_BLOCK:
                card.setStatus(Status.BLOCKED);
                log.info("CardService.cardBlock.success.forUser: {}", card.getUser().getId());
                return createResponse(card);
            case BLOCKED:
                throw new CardAlreadyBlockedException("Card already is blocked");
            default:
                throw new CardInvalidStatusException("Can't blocked card in currentStatus" + card.getStatus());
        }
    }

    /// Еще один метод нужен на запрос разблокировки карты от пользователя.
    ///  Карта в нем переходит в новое состояние, допустим PENDING_UNBLOCK и обрабатывается в методе ниже

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDto activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        switch (card.getStatus()) {
            case ACTIVE:
                throw new CardAlreadyActiveException("Card already activated");
            case PENDING_BLOCK:
                throw new CardPendingBlockException("Card waiting be block");
            case BLOCKED:
                card.setStatus(Status.ACTIVE);
                log.info("CardService.activateCard.success.forUser {}", card.getUser().getId());
                return createResponse(card);
            default:
                throw new CardInvalidStatusException("Card status conflict");
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(Long cardId) {
        cardRepository.deleteCardById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    @Transactional(timeout = 5)
    public void moneyTransfer(Long cardId, TransferCardRequestDto transferCardRequestDto) {

        Long firstLock = Math.min(cardId, transferCardRequestDto.getId());
        Long secondLock = Math.max(cardId, transferCardRequestDto.getId());

        Card firstCard = cardRepository.findByIdWithLock(firstLock).orElseThrow(() -> new CardNotFoundException("Sender card not found"));
        Card secondCard = cardRepository.findByIdWithLock(secondLock).orElseThrow(() -> new CardNotFoundException("Recipient card not found"));

        Card from = cardId.equals(firstCard.getId()) ? firstCard : secondCard;
        Card to = transferCardRequestDto.getId().equals(secondCard.getId()) ? secondCard : firstCard;

        if (checkTransferCard(from, to, transferCardRequestDto.getAmount())) {
            from.setBalance(from.getBalance().subtract(transferCardRequestDto.getAmount()));
            to.setBalance(to.getBalance().add(transferCardRequestDto.getAmount()));
            log.info("CardService.moneyTransfer.success");
        }
    }

    private boolean checkTransferCard(Card cardFrom, Card cardTo, BigDecimal amount) {
        if (cardFrom.getBalance().compareTo(amount) < 0) {
            log.info("CardService.moneyTransfer.fail.notEnoughFunds");
            throw new InsufficientFundsException("There are not enough funds on the sender card");
        }

        if (cardFrom.getStatus() != Status.ACTIVE || cardTo.getStatus() != Status.ACTIVE) {
            log.info("CardService.moneyTransfer.fail.oneCardIsNotActive");
            throw new CardNotActiveException("One of the cards is not active");
        }

        if (!cardFrom.getCardholderName().equals(cardTo.getCardholderName())) {
            log.info("CardService.moneyTransfer.fail.oneCardIsNotCardholder");
            throw new DifferentCardholdersException("One of the cards is not cardholder");
        }

        if (cardFrom.getId().equals(cardTo.getId())) {
            log.info("CardService.moneyTransfer.fail.oneCardIsSame");
            throw new SameCardTransferException("Cannot transfer to the same card");
        }

        return true;
    }

}
