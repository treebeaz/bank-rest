package com.example.bankrest.controller;

import com.example.bankrest.config.SecurityConfig;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.dto.card.TransferCardRequestDto;
import com.example.bankrest.entity.Status;
import com.example.bankrest.exception.*;
import com.example.bankrest.integration.IntegrationTestBase;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.UserService;
import com.example.bankrest.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@WithMockUser(username = "testUser", roles = "USER")
public class CardControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @Test
    void requestCreateCard_Return202Status() throws Exception {
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(1L)
                .masked("**** **** **** 1234")
                .cardHolderName("John Doe")
                .balance(BigDecimal.ZERO)
                .status(Status.PENDING_ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .build();

        when(cardService.requestCreateCard()).thenReturn(responseDto);

        mockMvc.perform(post("/api/cards/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.masked").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.status").value("PENDING_ACTIVE"));

        verify(cardService).requestCreateCard();
    }

    @Test
    void requestCreateCard_WhenUesrNotFound_Return404Status() throws Exception {
        when(cardService.requestCreateCard())
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/cards/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(cardService).requestCreateCard();
    }

    @Test
    void findUserCards_Return200Status() throws Exception {
        List<CardResponseDto> cards = Arrays.asList(
                CardResponseDto.builder()
                        .id(1L)
                        .masked("**** **** **** 1111")
                        .cardHolderName("John Doe")
                        .balance(new BigDecimal("100.00"))
                        .status(Status.ACTIVE)
                        .expiryDate(LocalDate.now().plusYears(3))
                        .build(),
                CardResponseDto.builder()
                        .id(2L)
                        .masked("**** **** **** 2222")
                        .cardHolderName("John Doe")
                        .balance(new BigDecimal("50.00"))
                        .status(Status.ACTIVE)
                        .expiryDate(LocalDate.now().plusYears(2))
                        .build()
        );

        Page<CardResponseDto> page = new PageImpl<>(cards, PageRequest.of(0, 10), 2);

        when(cardService.getUserCards(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].masked").value("**** **** **** 1111"))
                .andExpect(jsonPath("$[0].cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$[0].balance").value(100.00))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].masked").value("**** **** **** 2222"))
                .andExpect(jsonPath("$[1].balance").value(50.00))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"));

        verify(cardService).getUserCards(0, 10);
    }

    @Test
    void findUserCards_WhenUserNotFound_Return404Status() throws Exception {
        when(cardService.getUserCards(0, 10))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(cardService).getUserCards(0, 10);
    }

    @Test
    void findBalance_Return200Status() throws Exception {
        Long cardId = 1L;
        BigDecimal balance = new BigDecimal("150.75");

        when(cardService.getBalance(cardId)).thenReturn(balance);

        mockMvc.perform(get("/api/cards/{cardId}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("150.75"));

        verify(cardService).getBalance(cardId);
    }

    @Test
    void findBalance_WhenCardNotFound_Return404Status() throws Exception {
        Long cardId = 999L;

        when(cardService.getBalance(cardId))
                .thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/api/cards/{cardId}/balance", cardId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));

        verify(cardService).getBalance(cardId);
    }

    @Test
    void requestCardBlock_Return202Status() throws Exception {
        Long cardId = 1L;
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .masked("**** **** **** 1234")
                .cardHolderName("John Doe")
                .balance(new BigDecimal("100.00"))
                .status(Status.PENDING_BLOCK)
                .expiryDate(LocalDate.now().plusYears(3))
                .build();

        when(cardService.requestCardBlock(cardId)).thenReturn(responseDto);

        mockMvc.perform(post("/api/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.masked").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.status").value("PENDING_BLOCK"));

        verify(cardService).requestCardBlock(cardId);
    }

    @Test
    void requestCardBlock_WhenCardNotFound_Return404Status() throws Exception {
        Long cardId = 999L;

        when(cardService.requestCardBlock(cardId))
                .thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(post("/api/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));

        verify(cardService).requestCardBlock(cardId);
    }

    @Test
    void requestCardBlock_WhenUserNotFound_Return404Status() throws Exception {
        Long cardId = 1L;

        when(cardService.requestCardBlock(cardId))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(cardService).requestCardBlock(cardId);
    }

    @Test
    void requestCardBlock_WhenCardAlreadyBlocked_Return409Status() throws Exception {
        Long cardId = 1L;

        when(cardService.requestCardBlock(cardId))
                .thenThrow(new CardAlreadyBlockedException("Card already blocked"));

        mockMvc.perform(post("/api/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card already blocked"));

        verify(cardService).requestCardBlock(cardId);
    }

    @Test
    void transferCard_Return202Status() throws Exception {
        Long fromCardId = 1L;
        TransferCardRequestDto requestDto = TransferCardRequestDto.builder()
                .id(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/cards/{cardId}/transfer", fromCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isAccepted());

        verify(cardService).moneyTransfer(fromCardId, requestDto);
    }

    @Test
    void transferCard_WhenSenderCardNotFound_Return404Status() throws Exception {
        Long fromCardId = 999L;
        TransferCardRequestDto requestDto = TransferCardRequestDto.builder()
                .id(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        doThrow(new CardNotFoundException("Sender card not found"))
                .when(cardService).moneyTransfer(fromCardId, requestDto);

        mockMvc.perform(post("/api/cards/{cardId}/transfer", fromCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sender card not found"));

        verify(cardService).moneyTransfer(fromCardId, requestDto);
    }

    @Test
    void transferCard_WhenRecipientCardNotFound_Return404Status() throws Exception {
        Long fromCardId = 1L;
        TransferCardRequestDto requestDto = TransferCardRequestDto.builder()
                .id(999L)
                .amount(new BigDecimal("100.00"))
                .build();

        doThrow(new CardNotFoundException("Recipient card not found"))
                .when(cardService).moneyTransfer(fromCardId, requestDto);

        mockMvc.perform(post("/api/cards/{cardId}/transfer", fromCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recipient card not found"));

        verify(cardService).moneyTransfer(fromCardId, requestDto);
    }

    @Test
    void transferCard_WhenInsufficientFunds_Return409Status() throws Exception {
        Long fromCardId = 1L;
        TransferCardRequestDto requestDto = TransferCardRequestDto.builder()
                .id(2L)
                .amount(new BigDecimal("1000.00"))
                .build();

        doThrow(new InsufficientFundsException("Insufficient funds"))
                .when(cardService).moneyTransfer(fromCardId, requestDto);

        mockMvc.perform(post("/api/cards/{cardId}/transfer", fromCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));

        verify(cardService).moneyTransfer(fromCardId, requestDto);
    }

    @Test
    void transferCard_WhenCardNotActive_Return409Status() throws Exception {
        Long fromCardId = 1L;
        TransferCardRequestDto requestDto = TransferCardRequestDto.builder()
                .id(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        doThrow(new CardNotActiveException("Card not active"))
                .when(cardService).moneyTransfer(fromCardId, requestDto);

        mockMvc.perform(post("/api/cards/{cardId}/transfer", fromCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card not active"));

        verify(cardService).moneyTransfer(fromCardId, requestDto);
    }

    @Test
    void transferCard_WhenDifferentCardholders_Return409Status() throws Exception {
        Long fromCardId = 1L;
        TransferCardRequestDto requestDto = TransferCardRequestDto.builder()
                .id(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        doThrow(new DifferentCardholdersException("Different cardholders"))
                .when(cardService).moneyTransfer(fromCardId, requestDto);

        mockMvc.perform(post("/api/cards/{cardId}/transfer", fromCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Different cardholders"));

        verify(cardService).moneyTransfer(fromCardId, requestDto);
    }

    @Test
    void transferCard_WhenSameCardTransfer_Return409Status() throws Exception {
        Long fromCardId = 1L;
        TransferCardRequestDto requestDto = TransferCardRequestDto.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        doThrow(new SameCardTransferException("Same card transfer"))
                .when(cardService).moneyTransfer(fromCardId, requestDto);

        mockMvc.perform(post("/api/cards/{cardId}/transfer", fromCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Same card transfer"));

        verify(cardService).moneyTransfer(fromCardId, requestDto);
    }

}
