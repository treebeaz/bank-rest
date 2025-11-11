package com.example.bankrest.controller;

import com.example.bankrest.config.SecurityConfig;
import com.example.bankrest.dto.card.CardRequestDto;
import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.entity.Status;
import com.example.bankrest.exception.CardAlreadyActiveException;
import com.example.bankrest.exception.CardAlreadyBlockedException;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@WithMockUser(username = "admin", roles = "ADMIN")
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @Test
    void createCard_WithAdminRole_Return201Created() throws Exception {
        CardRequestDto requestDto = CardRequestDto.builder()
                .userId(1L)
                .build();

        CardResponseDto responseDto = CardResponseDto.builder()
                .id(1L)
                .masked("**** **** **** 1234")
                .cardHolderName("John Doe")
                .balance(BigDecimal.ZERO)
                .status(Status.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .build();

        when(cardService.createCard(1L)).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.masked").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        verify(cardService).createCard(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCard_WithUserRole_Return403Status() throws Exception {
        CardRequestDto requestDto = CardRequestDto.builder().userId(1L).build();

        mockMvc.perform(post("/api/admin/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(cardService, never()).createCard(any());
    }

    @Test
    void createCard_WhenUserNotFound_Return404Status() throws Exception {
        CardRequestDto requestDto = CardRequestDto.builder().userId(999L).build();

        when(cardService.createCard(999L))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/admin/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(cardService).createCard(999L);
    }

    @Test
    void getAllCards_WithAdminRole_Return200Status() throws Exception {
        List<CardResponseDto> cards = Arrays.asList(
                CardResponseDto.builder().id(1L).masked("**** **** **** 1111").cardHolderName("User1").build(),
                CardResponseDto.builder().id(2L).masked("**** **** **** 2222").cardHolderName("User2").build()
        );

        Page<CardResponseDto> page = new PageImpl<>(cards, PageRequest.of(0, 10), 2);

        when(cardService.getAllCards(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].masked").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].masked").value("**** **** **** 2222"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(cardService).getAllCards(0, 10);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_WithUserRole_Return403Status() throws Exception {
        List<CardResponseDto> cards = Arrays.asList(
                CardResponseDto.builder().id(1L).masked("**** **** **** 1111").cardHolderName("User1").build(),
                CardResponseDto.builder().id(2L).masked("**** **** **** 2222").cardHolderName("User2").build()
        );

        Page<CardResponseDto> page = new PageImpl<>(cards, PageRequest.of(0, 10), 2);

        when(cardService.getAllCards(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());

        verify(cardService, never()).getAllCards(0, 10);
    }

    @Test
    void cardBlock_WithAdminRole_Return200Status() throws Exception {
        Long cardId = 1L;
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .status(Status.BLOCKED)
                .masked("**** **** **** 1234")
                .build();

        when(cardService.cardBlock(cardId)).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(cardService).cardBlock(cardId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void cardBlock_WithUserRole_Return403Status() throws Exception {
        Long cardId = 1L;
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .status(Status.BLOCKED)
                .masked("**** **** **** 1234")
                .build();

        when(cardService.cardBlock(cardId)).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(cardService, never()).cardBlock(cardId);
    }

    @Test
    void cardBlock_WhenCardNotFound_Return404Status() throws Exception {
        Long cardId = 999L;

        when(cardService.cardBlock(cardId)).thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(post("/api/admin/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));

        verify(cardService).cardBlock(cardId);
    }

    @Test
    void cardBlock_WhenCardAlreadyBlocked_Return409Status() throws Exception {
        Long cardId = 1L;

        when(cardService.cardBlock(cardId))
                .thenThrow(new CardAlreadyBlockedException("Card already blocked"));

        mockMvc.perform(post("/api/admin/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card already blocked"));

        verify(cardService).cardBlock(cardId);
    }

    @Test
    void cardBlock_WhenCardAlreadyActive_Return409Status() throws Exception {
        Long cardId = 1L;

        when(cardService.cardBlock(cardId))
                .thenThrow(new CardAlreadyActiveException("Card already active"));

        mockMvc.perform(post("/api/admin/cards/{cardId}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card already active"));

        verify(cardService).cardBlock(cardId);
    }

    @Test
    void deleteCard_WithAdminRole_Return200Status() throws Exception {
        Long cardId = 1L;

        mockMvc.perform(delete("/api/admin/cards/{cardId}/delete", cardId))
                .andExpect(status().isOk());

        verify(cardService).deleteCard(cardId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCard_WithUserRole_Return403Status() throws Exception {
        Long cardId = 1L;

        mockMvc.perform(delete("/api/admin/cards/{cardId}/delete", cardId))
                .andExpect(status().isForbidden());

        verify(cardService, never()).deleteCard(cardId);
    }

    @Test
    void deleteCard_WhenCardNotFound_Return404Status() throws Exception {
        Long cardId = 999L;

        doThrow(new CardNotFoundException("Card not found"))
                .when(cardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/admin/cards/{cardId}/delete", cardId))
                .andExpect(status().isNotFound());

        verify(cardService).deleteCard(cardId);
    }

    @Test
    void activateCard_WithAdminRole_Return200Status() throws Exception {
        Long cardId = 1L;
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .status(Status.ACTIVE)
                .masked("**** **** **** 1234")
                .build();

        when(cardService.activateCard(cardId)).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards/{cardId}/activate", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService).activateCard(cardId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void activateCard_WithUserRole_Return403Status() throws Exception {
        Long cardId = 1L;
        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .status(Status.ACTIVE)
                .masked("**** **** **** 1234")
                .build();

        when(cardService.activateCard(cardId)).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards/{cardId}/activate", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(cardService, never()).activateCard(cardId);
    }

    @Test
    void activateCard_WhenCardNotFound_Return404Status() throws Exception {
        Long cardId = 999L;

        when(cardService.activateCard(cardId))
                .thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(post("/api/admin/cards/{cardId}/activate", cardId))
                .andExpect(status().isNotFound());

        verify(cardService).activateCard(cardId);
    }

    @Test
    void activateCard_WhenCardAlreadyActive_Return409Status() throws Exception {
        Long cardId = 1L;

        when(cardService.activateCard(cardId))
                .thenThrow(new CardAlreadyActiveException("Card already active"));

        mockMvc.perform(post("/api/admin/cards/{cardId}/activate", cardId))
                .andExpect(status().isConflict());

        verify(cardService).activateCard(cardId);
    }
}
