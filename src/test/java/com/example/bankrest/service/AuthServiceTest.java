package com.example.bankrest.service;

import com.example.bankrest.dto.auth.AuthRequestDto;
import com.example.bankrest.dto.auth.AuthResponseDto;
import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.InvalidCredentialsException;
import com.example.bankrest.exception.UserAlreadyExistsException;
import com.example.bankrest.mapper.UserMapper;
import com.example.bankrest.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_NewUser_Success() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("user")
                .password("password")
                .firstname("Name")
                .lastname("Last")
                .build();

        UserResponseDto mockUserResponse = UserResponseDto.builder()
                .id(3L)
                .username("user")
                .firstname("Name")
                .lastname("Last")
                .role(Role.USER)
                .build();

        User mockUserEntity = User.builder()
                .id(3L)
                .username("user")
                .firstname("Name")
                .lastname("Last")
                .role(Role.USER)
                .enabled(true)
                .build();

        when(userService.existsByUsername("user")).thenReturn(false);
        when(userService.createUser(request)).thenReturn(mockUserResponse);
        when(userMapper.userResponseDtoToUserEntity(mockUserResponse)).thenReturn(mockUserEntity);
        when(jwtUtil.generateToken(mockUserEntity)).thenReturn("test.jwt.token");

        AuthResponseDto result = authService.register(request, true);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user");
        assertThat(result.getRole()).isEqualTo(Role.USER.name());
        assertThat(result.getToken()).isEqualTo("test.jwt.token");

    }

    @Test
    void register_WhenUserExists_ThrowUserAlreadyExistsException() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("qwe")
                .password("password")
                .firstname("test")
                .lastname("test")
                .build();

        when(userService.existsByUsername("qwe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, true))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User already exists");
    }

    @Test
    void register_NewUser_ReturnToken() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("user")
                .password("password")
                .firstname("Name")
                .lastname("Last")
                .build();

        UserResponseDto mockUserResponse = UserResponseDto.builder()
                .id(3L)
                .username("user")
                .firstname("Name")
                .lastname("Last")
                .role(Role.USER)
                .build();

        User mockUserEntity = User.builder()
                .id(3L)
                .username("user")
                .firstname("Name")
                .lastname("Last")
                .role(Role.USER)
                .enabled(true)
                .build();

        when(userService.existsByUsername("user")).thenReturn(false);
        when(userService.createUser(request)).thenReturn(mockUserResponse);
        when(userMapper.userResponseDtoToUserEntity(mockUserResponse)).thenReturn(mockUserEntity);
        when(jwtUtil.generateToken(mockUserEntity)).thenReturn("test.jwt.token");

        AuthResponseDto result = authService.register(request, true);

        verify(jwtUtil).generateToken(mockUserEntity);
        assertThat(result.getToken()).isEqualTo("test.jwt.token");
    }

    @Test
    void login_WithValidCredentials_Success() {

        Authentication authentication = mock(Authentication.class);

        AuthRequestDto request = AuthRequestDto.builder()
                .username("qwe")
                .password("password")
                .build();

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("qwe")
                .password("password")
                .authorities("USER")
                .build();

        User mockUserEntity = User.builder()
                .id(1L)
                .username("qwe")
                .role(Role.USER)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.findByUsername("qwe")).thenReturn(mockUserEntity);
        when(jwtUtil.generateToken(mockUserEntity)).thenReturn("test.jwt.token");

        AuthResponseDto result = authService.login(request);

        verify(authenticationManager).authenticate(any());
        verify(userService).findByUsername("qwe");
        verify(jwtUtil).generateToken(mockUserEntity);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("qwe");
        assertThat(result.getRole()).isEqualTo(Role.USER.name());
        assertThat(result.getToken()).isEqualTo("test.jwt.token");

    }

    @Test
    void login_WithInvalidCredentials_ThrowInvalidCredentialsException() {
        AuthRequestDto request = AuthRequestDto.builder()
                .username("newUsername")
                .password("password123")
                .build();

        when(authenticationManager.authenticate(any())).thenThrow(new InvalidCredentialsException("Invalid username or password"));
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userService, never()).findByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_AdminWithValidCredentials_Success() {

        AuthRequestDto request = AuthRequestDto.builder()
                .username("admin")
                .password("admin123")
                .build();

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("admin123")
                .authorities("ADMIN")
                .build();

        Authentication authentication = mock(Authentication.class);

        User mockUserEntity = User.builder()
                .id(2L)
                .username("admin")
                .role(Role.ADMIN)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.findByUsername("admin")).thenReturn(mockUserEntity);
        when(jwtUtil.generateToken(mockUserEntity)).thenReturn("test.jwt.token");

        AuthResponseDto result = authService.login(request);

        verify(authenticationManager).authenticate(any());
        verify(userService).findByUsername("admin");
        verify(jwtUtil).generateToken(mockUserEntity);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN.name());
        assertThat(result.getToken()).isEqualTo("test.jwt.token");
    }

    @Test
    void login_WithNullRequest_ThrowNullPointerException() {
        assertThatThrownBy(() -> authService.login(null))
                .isInstanceOf(NullPointerException.class);
    }

}

