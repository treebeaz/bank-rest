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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardService cardService;


    @Test
    void requestCreateCard_Success() {
        String username = "testUser";
        setupSecurityContext(username);
        User user = createTestUser(username);
        CardInfo cardInfo = createTestCardInfo();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardNumberGenerator.generateUniqueCardNumber()).thenReturn(cardInfo);
        when(cardRepository.save(any(Card.class))).thenReturn(createSavedCardWithId(user, cardInfo));

        CardResponseDto result = cardService.requestCreateCard();

        assertThat(result.getStatus()).isEqualTo(Status.PENDING_ACTIVE);
        assertThat(result.getMasked()).isEqualTo("**** **** **** 1234");
        assertThat(result.getCardHolderName()).isEqualTo("John Doe");
        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getExpiryDate()).isEqualTo(LocalDate.now().plusYears(5));
    }

    @Test
    void requestCreateCard_CallAllRequiredDependencies() {
        String username = "testUser";
        setupSecurityContext(username);
        User user = createTestUser(username);
        CardInfo cardInfo = createTestCardInfo();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardNumberGenerator.generateUniqueCardNumber()).thenReturn(cardInfo);
        when(cardRepository.save(any(Card.class))).thenReturn(createSavedCardWithId(user, cardInfo));

        cardService.requestCreateCard();

        verify(userRepository).findByUsername(username);
        verify(cardNumberGenerator).generateUniqueCardNumber();
        verify(cardRepository).save(any(Card.class));
    }

    private Card createSavedCardWithId(User user, CardInfo cardInfo) {
        return Card.builder()
                .id(1L)
                .cardNumber(cardInfo.getCardNumber())
                .lastDigits(cardInfo.getLastDigits())
                .hashCardNumber(cardInfo.getHashCardNumber())
                .user(user)
                .cardholderName("John Doe")
                .balance(BigDecimal.ZERO)
                .status(Status.PENDING_ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .build();
    }

    @Test
    void requestCreateCard_WhenUserNotFound_ThrowUserNotFoundException() {
        String username = "nonExistentUser";
        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.requestCreateCard())
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(cardNumberGenerator);
        verifyNoInteractions(cardRepository);
    }

    @Test
    void createCardForAdmin_Success() {
        Long userId = 1L;
        String username = "testUser";
        User user = createTestUser(username);

        Card pendingCard = createDefaultTestCard(user, Status.PENDING_ACTIVE);

        when(cardRepository.findByUser_IdAndStatus(userId, pendingCard.getStatus())).thenReturn(Optional.of(pendingCard));

        CardResponseDto result = cardService.createCard(userId);

        assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(result.getMasked()).isEqualTo("**** **** **** 1111");
        assertThat(result.getCardHolderName()).isEqualTo("John Doe");
        assertThat(result.getBalance()).isEqualTo("100.00");
        assertThat(result.getExpiryDate()).isEqualTo(LocalDate.now().plusYears(5));

    }

    @Test
    void createCardForAdmin_WhenCardNotFound_ThrowCardNotFoundException() {
        Long userId = 999L;

        when(cardRepository.findByUser_IdAndStatus(userId, Status.PENDING_ACTIVE)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.createCard(userId))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository).findByUser_IdAndStatus(userId, Status.PENDING_ACTIVE);
    }
    @Test
    void getUserCards_ReturnPaginatedCards() {
        String username = "testUser";
        User user = createTestUser(username);

        List<Card> mockCards = Arrays.asList(
                Card.builder()
                        .id(1L)
                        .user(user)
                        .cardNumber("encrypted1")
                        .lastDigits("1111")
                        .hashCardNumber("hash1")
                        .cardholderName("John Doe")
                        .balance(new BigDecimal("100.00"))
                        .status(Status.ACTIVE)
                        .expiryDate(LocalDate.now().plusYears(5))
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build(),
                Card.builder()
                        .id(2L)
                        .user(user)
                        .cardNumber("encrypted2")
                        .lastDigits("2222")
                        .hashCardNumber("hash2")
                        .cardholderName("John Doe")
                        .balance(new BigDecimal("50.00"))
                        .status(Status.ACTIVE)
                        .expiryDate(LocalDate.now().plusYears(5))
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build()
        );

        Page<Card> mockPage = new PageImpl<>(
                mockCards,
                PageRequest.of(0, 10, Sort.by("createdAt").descending()),
                mockCards.size()
        );

        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        when(cardRepository.findByUser_Id(eq(3L), any(PageRequest.class))).thenReturn(mockPage);

        Page<CardResponseDto> result = cardService.getUserCards(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        CardResponseDto firstCard = result.getContent().get(0);
        assertThat(firstCard.getId()).isEqualTo(1L);
        assertThat(firstCard.getMasked()).isEqualTo("**** **** **** 1111");
        assertThat(firstCard.getBalance()).isEqualTo(new BigDecimal("100.00"));

        CardResponseDto secondCard = result.getContent().get(1);
        assertThat(secondCard.getId()).isEqualTo(2L);
        assertThat(secondCard.getMasked()).isEqualTo("**** **** **** 2222");
        assertThat(secondCard.getBalance()).isEqualTo(new BigDecimal("50.00"));

        verify(cardRepository).findByUser_Id(eq(3L),
                argThat(pageRequest ->
                        pageRequest.getPageNumber() == 0 &&
                        pageRequest.getPageSize() == 10 &&
                        pageRequest.getSort().getOrderFor("createdAt").isDescending()
                ));
    }

    @Test
    void getBalance_Success() {
        String username = "testUser";
        User user = createTestUser(username);
        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Card card = createDefaultTestCard(user, Status.ACTIVE);

        when(cardRepository.findByIdAndUser_Id(card.getId(), user.getId())).thenReturn(Optional.of(card));
        BigDecimal expected = cardService.getBalance(card.getId());

        assertThat(expected).isEqualTo(new BigDecimal("100.00"));

        verify(userRepository).findByUsername(username);
        verify(cardRepository).findByIdAndUser_Id(card.getId(), user.getId());
    }

    @Test
    void getBalance_WhenCardNotFound_ThrowCardNotFoundException() {
        Long cardId = 999L;

        String username = "testUser";
        User user = createTestUser(username);

        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        when(cardRepository.findByIdAndUser_Id(cardId, user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getBalance(cardId))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void getBalance_WhenUserNotFound_ThrowUserNotFoundException() {
        Long cardId = 1L;
        String username = "NonExistsUser";

        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getBalance(cardId))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(cardRepository);
    }

    @Test
    void getAllCardsForAdmin_Success() {
        String username = "testUser";
        User user = createTestUser(username);

        List<Card> mockCards = Arrays.asList(
                Card.builder()
                        .id(1L)
                        .user(user)
                        .cardNumber("encrypted1")
                        .lastDigits("1111")
                        .hashCardNumber("hash1")
                        .cardholderName("John Doe")
                        .balance(new BigDecimal("100.00"))
                        .status(Status.ACTIVE)
                        .expiryDate(LocalDate.now().plusYears(5))
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build(),
                Card.builder()
                        .id(2L)
                        .user(user)
                        .cardNumber("encrypted2")
                        .lastDigits("2222")
                        .hashCardNumber("hash2")
                        .cardholderName("John Doe")
                        .balance(new BigDecimal("50.00"))
                        .status(Status.ACTIVE)
                        .expiryDate(LocalDate.now().plusYears(5))
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build()
        );

        Page<Card> mockPage = new PageImpl<>(
                mockCards,
                PageRequest.of(0, 10, Sort.by("createdAt").descending()),
                mockCards.size()
        );

        when(cardRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);
        Page<CardResponseDto> result = cardService.getAllCards(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        CardResponseDto firstCard = result.getContent().get(0);
        assertThat(firstCard.getId()).isEqualTo(1L);
        assertThat(firstCard.getMasked()).isEqualTo("**** **** **** 1111");
        assertThat(firstCard.getBalance()).isEqualTo(new BigDecimal("100.00"));

        CardResponseDto secondCard = result.getContent().get(1);
        assertThat(secondCard.getId()).isEqualTo(2L);
        assertThat(secondCard.getMasked()).isEqualTo("**** **** **** 2222");
        assertThat(secondCard.getBalance()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void requestCardBlock_Success() {
        String username = "testUser";
        User user = createTestUser(username);

        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Card card = createDefaultTestCard(user, Status.ACTIVE);

        when(cardRepository.findByIdAndUser_Id(card.getId(), user.getId())).thenReturn(Optional.of(card));

        CardResponseDto result = cardService.requestCardBlock(card.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.PENDING_BLOCK);
        assertThat(result.getCardHolderName()).isEqualTo("John Doe");
        assertThat(result.getBalance()).isEqualTo(new BigDecimal("100.00"));

        verify(userRepository).findByUsername(username);
        verify(cardRepository).findByIdAndUser_Id(card.getId(), user.getId());

    }

    @Test
    void requestCardBlock_WhenCardNotFound_ThrowCardNotFoundException() {
        Long cardId = 999L;

        String username = "testUser";
        User user = createTestUser(username);

        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        when(cardRepository.findByIdAndUser_Id(cardId, user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.requestCardBlock(cardId))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void requestCardBlock_WhenUserNotFound_ThrowUserNotFoundException() {
        Long cardId = 1L;
        String username = "NonExistsUser";

        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.requestCardBlock(cardId))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(cardRepository);
    }

    @Test
    void requestCardBlockFor_WhenCardAlreadyBlocked_ThrowCardAlreadyBlockedException() {
        String username = "testUser";
        User user = createTestUser(username);

        setupSecurityContext(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Card card = createDefaultTestCard(user, Status.BLOCKED);

        when(cardRepository.findByIdAndUser_Id(card.getId(), user.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.requestCardBlock(card.getId()))
                .isInstanceOf(CardAlreadyBlockedException.class);

        verify(cardRepository).findByIdAndUser_Id(card.getId(), user.getId());
        verify(cardRepository, never()).save(any(Card.class));

    }

    @Test
    void cardBlockForAdmin_Success() {
        Long cardId = 1L;
        String username = "adminUser";


        User user = createTestUser(username);

        Card card = createDefaultTestCard(user, Status.PENDING_BLOCK);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardResponseDto result = cardService.cardBlock(cardId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.BLOCKED);

        verify(cardRepository).findById(cardId);
    }

    @Test
    void cardBlockForAdmin_WhenCardAlreadyBlocked_ThrowCardAlreadyBlockedException() {
        String username = "testUser";
        User user = createTestUser(username);

        Card card = createDefaultTestCard(user, Status.BLOCKED);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.cardBlock(card.getId()))
                .isInstanceOf(CardAlreadyBlockedException.class);

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void cardBlockForAdmin_WhenCardAlreadyActive_ThrowCardAlreadyActiveException() {
        String username = "testUser";
        User user = createTestUser(username);

        Card card = createDefaultTestCard(user, Status.ACTIVE);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.cardBlock(card.getId()))
                .isInstanceOf(CardAlreadyActiveException.class);

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void cardBlockForAdmin_WhenCardInvalidStatus_ThrowCardInvalidStatusException() {
        String username = "testUser";
        User user = createTestUser(username);

        Card card = createDefaultTestCard(user, Status.PENDING_ACTIVE);  // для теста, так как это состояние как раз упадет в default
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.cardBlock(card.getId()))
                .isInstanceOf(CardInvalidStatusException.class);

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void cardBlockForAdmin_WhenCardNotFound_ThrowCardNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.cardBlock(cardId))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateCardForAdmin_Success() {
        String username = "testUser";
        User user = createTestUser(username);

        Card card = createDefaultTestCard(user, Status.BLOCKED);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        CardResponseDto result = cardService.activateCard(card.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);

        verify(cardRepository).findById(card.getId());
    }

    @Test
    void activateCardForAdmin_WhenCardAlreadyActivated_ThrowCardAlreadyActivatedException() {
        String username = "testUser";
        User user = createTestUser(username);

        Card card = createDefaultTestCard(user, Status.ACTIVE);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(card.getId()))
                .isInstanceOf(CardAlreadyActiveException.class);

        verify(cardRepository).findById(card.getId());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateCardForAdmin_WhenCardNotFound_ThrowCardNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.activateCard(cardId))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCardForAdmin_Success() {
        Long cardId = 1L;
        when(cardRepository.deleteCardById(cardId)).thenReturn(Optional.of(true));
        cardService.deleteCard(cardId);
        verify(cardRepository).deleteCardById(cardId);
    }

    @Test
    void deleteCardForAdmin_WhenCardNotFound_ThrowCardNotFoundException() {
        Long cardId = 999L;

        when(cardRepository.deleteCardById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(cardId))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void moneyTransfer_Success() {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        TransferCardRequestDto request = TransferCardRequestDto.builder()
                .id(toCardId)
                .amount(amount)
                .build();

        String username = "testUser";
        User user = createTestUser(username);

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("500.00"))
                .status(Status.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("200.00"))
                .status(Status.ACTIVE)
                .build();

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        cardService.moneyTransfer(fromCardId, request);
        assertThat(fromCard.getBalance()).isEqualTo(new BigDecimal("400.00"));
        assertThat(toCard.getBalance()).isEqualTo(new BigDecimal("300.00"));

        verify(cardRepository).findByIdWithLock(1L);
        verify(cardRepository).findByIdWithLock(2L);
    }

    @Test
    void moneyTransfer_WhenOneCardIsNotActive_ThrowCardNotActiveException() {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        TransferCardRequestDto request = TransferCardRequestDto.builder()
                .id(toCardId)
                .amount(amount)
                .build();

        String username = "testUser";
        User user = createTestUser(username);

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("500.00"))
                .status(Status.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("200.00"))
                .status(Status.BLOCKED)
                .build();

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.moneyTransfer(fromCardId, request))
                .isInstanceOf(CardNotActiveException.class);

        verify(cardRepository).findByIdWithLock(1L);
        verify(cardRepository).findByIdWithLock(2L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void moneyTransfer_WhenInsufficientFundsOnSenderCards_ThrowInsufficientFundsException() {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        TransferCardRequestDto request = TransferCardRequestDto.builder()
                .id(toCardId)
                .amount(amount)
                .build();

        String username = "testUser";
        User user = createTestUser(username);

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("10.00"))
                .status(Status.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("200.00"))
                .status(Status.ACTIVE)
                .build();

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.moneyTransfer(fromCardId, request))
                .isInstanceOf(InsufficientFundsException.class);

        verify(cardRepository).findByIdWithLock(1L);
        verify(cardRepository).findByIdWithLock(2L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void moneyTransfer_WhenDifferentCardholders_ThrowDifferentCardholdersException() {
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        TransferCardRequestDto request = TransferCardRequestDto.builder()
                .id(toCardId)
                .amount(amount)
                .build();

        String username = "testUser";
        User user = createTestUser(username);

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(user)
                .cardholderName("Harry Gob")
                .balance(new BigDecimal("500.00"))
                .status(Status.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("200.00"))
                .status(Status.ACTIVE)
                .build();

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.moneyTransfer(fromCardId, request))
                .isInstanceOf(DifferentCardholdersException.class);

        verify(cardRepository).findByIdWithLock(1L);
        verify(cardRepository).findByIdWithLock(2L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void moneyTransfer_WhenSameCardTransfer_ThrowSameCardTransferException() {
        Long fromCardId = 1L;
        Long toCardId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        TransferCardRequestDto request = TransferCardRequestDto.builder()
                .id(toCardId)
                .amount(amount)
                .build();

        String username = "testUser";
        User user = createTestUser(username);

        Card card = Card.builder()
                .id(fromCardId)
                .user(user)
                .cardholderName("John Doe")
                .balance(new BigDecimal("500.00"))
                .status(Status.ACTIVE)
                .build();

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.moneyTransfer(fromCardId, request))
                .isInstanceOf(SameCardTransferException.class);

        verify(cardRepository, never()).save(any(Card.class));
    }

    private CardInfo createTestCardInfo() {
        return CardInfo.builder()
                .cardNumber("encryptedCardNumber")
                .hashCardNumber("hash123")
                .lastDigits("1234")
                .build();
    }

    private void setupSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
    }

    private User createTestUser(String username) {
        return User.builder()
                .id(3L)
                .username(username)
                .firstname("John")
                .lastname("Doe")
                .build();
    }

    private Card createDefaultTestCard(User user, Status status) {
        return Card.builder()
                .id(1L)
                .cardNumber("encrypted")
                .lastDigits("1111")
                .hashCardNumber("hash_number")
                .user(user)
                .cardholderName("John Doe")
                .status(status)
                .balance(new BigDecimal("100.00"))
                .expiryDate(LocalDate.now().plusYears(5))
                .build();
    }
}
