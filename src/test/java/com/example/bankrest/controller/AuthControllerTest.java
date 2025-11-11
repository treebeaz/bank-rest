package com.example.bankrest.controller;

import com.example.bankrest.config.SecurityConfig;
import com.example.bankrest.dto.auth.AuthRequestDto;
import com.example.bankrest.dto.auth.AuthResponseDto;
import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.exception.InvalidCredentialsException;
import com.example.bankrest.exception.UserAlreadyExistsException;
import com.example.bankrest.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@WithMockUser(username = "testUser", roles = "USER")
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void registration_Return201Status() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .password("password123")
                .firstname("John")
                .lastname("Doe")
                .build();

        AuthResponseDto response = AuthResponseDto.builder()
                .token("jwt.token.user")
                .username("newuser")
                .role("USER")
                .build();

        when(authService.register(request, true)).thenReturn(response);

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt.token.user"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).register(request, true);
    }

    @Test
    void registration_WhenInvalidData_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("")
                .password("short")
                .firstname("")
                .lastname("")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithNullFields_Return400Status() throws Exception {
        String invalidJson = """
                {
                    "username": null,
                    "password": null
                }
                """;

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithInvalidUsername_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("Invalid@User")
                .password("password123")
                .firstname("John")
                .lastname("Doe")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithLowerCaseFirstname_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("ValidUser")
                .password("password123")
                .firstname("john")
                .lastname("Doe")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithShortFirstname_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("ValidUser")
                .password("password123")
                .firstname("J")
                .lastname("Doe")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithLowerCaseLastname_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("ValidUser")
                .password("password123")
                .firstname("John")
                .lastname("doe")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithShortLastname_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("ValidUser")
                .password("password123")
                .firstname("John")
                .lastname("D")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithEmptyFirstname_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("ValidUser")
                .password("password123")
                .firstname("")
                .lastname("Doe")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithEmptyLastname_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("ValidUser")
                .password("password123")
                .firstname("John")
                .lastname("")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }

    @Test
    void registration_WithAllFieldsEmpty_Return400Status() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("")
                .password("")
                .firstname("")
                .lastname("")
                .build();

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/auth/registration"));

        verify(authService, never()).register(any(), anyBoolean());
    }


    @Test
    void registration_WhenUserAlreadyExists_Return409Status() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("existinguser")
                .password("password123")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(authService.register(request, true))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));

        verify(authService).register(request, true);
    }

    @Test
    void login_Return200Status() throws Exception {
        AuthRequestDto request = AuthRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponseDto response = AuthResponseDto.builder()
                .token("jwt.token.user")
                .username("testuser")
                .role("USER")
                .build();

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.user"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).login(request);
    }

    @Test
    void login_WithShortUsername_Return400Status() throws Exception {
        AuthRequestDto invalidRequest = AuthRequestDto.builder()
                .username("user")
                .password("validpassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verify(authService, never()).login(any());
    }

    @Test
    void login_WithShortPassword_Return400Status() throws Exception {
        AuthRequestDto invalidRequest = AuthRequestDto.builder()
                .username("validuser")
                .password("short")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verify(authService, never()).login(any());
    }

    @Test
    void login_WithEmptyUsername_Return400Status() throws Exception {
        AuthRequestDto invalidRequest = AuthRequestDto.builder()
                .username("")
                .password("validpassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verify(authService, never()).login(any());
    }

    @Test
    void login_WithEmptyPassword_Return400Status() throws Exception {
        AuthRequestDto invalidRequest = AuthRequestDto.builder()
                .username("validUser")
                .password("")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verify(authService, never()).login(any());
    }

    @Test
    void login_WithNullFields_Return400Status() throws Exception {
        String invalidJson = """
        {
            "username": null,
            "password": null
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verify(authService, never()).login(any());
    }

    @Test
    void login_WithMaxLengthFields_Return200Status() throws Exception {
        String maxUsername = "u".repeat(50);
        String maxPassword = "p".repeat(50);

        AuthRequestDto validRequest = AuthRequestDto.builder()
                .username(maxUsername)
                .password(maxPassword)
                .build();

        AuthResponseDto response = AuthResponseDto.builder()
                .token("token")
                .username(maxUsername)
                .role("USER")
                .build();

        when(authService.login(validRequest)).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(maxUsername));

        verify(authService).login(validRequest);
    }

    @Test
    void login_WithLengthFieldsGreaterThanMaxLength_Return400Status() throws Exception {
        String maxUsername = "u".repeat(51);
        String maxPassword = "p".repeat(51);

        AuthRequestDto invalidRequest = AuthRequestDto.builder()
                .username(maxUsername)
                .password(maxPassword)
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verify(authService,never()).login(any());
    }

    @Test
    void login_WithInvalidCredentials_Return401Status() throws Exception {
        AuthRequestDto request = AuthRequestDto.builder()
                .username("wronguser")
                .password("wrongpassword")
                .build();

        when(authService.login(request))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        verify(authService).login(request);
    }
}
