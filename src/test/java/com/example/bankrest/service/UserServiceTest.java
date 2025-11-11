package com.example.bankrest.service;

import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.UserCreationException;
import com.example.bankrest.mapper.UserMapper;
import com.example.bankrest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("testUser")
                .password("password123")
                .firstname("John")
                .lastname("Doe")
                .build();

        User userEntity = User.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPassword")
                .firstname("John")
                .lastname("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();


        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(1L)
                .username("testUser")
                .firstname("John")
                .lastname("Doe")
                .role(Role.USER)
                .build();

        when(userMapper.registerRequestDtoToUser(request)).thenReturn(userEntity);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.userEntityToUserResponseDto(userEntity)).thenReturn(expectedResponse);

        UserResponseDto result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getRole()).isEqualTo(Role.USER);

        verify(userMapper).registerRequestDtoToUser(request);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(userEntity);
        verify(userMapper).userEntityToUserResponseDto(userEntity);
    }

    @Test
    void createUser_WhenUserMapperToEntityReturnNull_ThrowUserCreationException() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("testUser")
                .password("password123")
                .build();

        when(userMapper.registerRequestDtoToUser(request)).thenReturn(null);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("Failed to create user");
    }

    @Test
    void createUser_WhenUserMapperToDtoReturnNull_ThrowUserCreationException() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("testUser")
                .password("password123")
                .build();

        User userEntity = User.builder().username("testUser").build();
        User savedUser = User.builder().id(1L).username("testUser").build();

        when(userMapper.registerRequestDtoToUser(request)).thenReturn(userEntity);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(userMapper.userEntityToUserResponseDto(savedUser)).thenReturn(null);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("Failed to create user");
    }

    @Test
    void createUser_WhenSaveReturnNull_ThrowUserCreationException() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("testUser")
                .password("password123")
                .build();

        User userEntity = User.builder().username("testUser").build();

        when(userMapper.registerRequestDtoToUser(request)).thenReturn(userEntity);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(null);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("Failed to create user");
    }

    @Test
    void findByUsername_Success() {
        String username = "testUser";
        User expectedUser = User.builder()
                .id(1L)
                .username(username)
                .firstname("John")
                .lastname("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        User result = userService.findByUsername(username);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getFirstname()).isEqualTo("John");
        assertThat(result.getLastname()).isEqualTo("Doe");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void findByUsername_WhenUserNotFound_ThrowUserNotFoundException() {
        String username = "nonExistentUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(userRepository).findByUsername(username);
    }

    @Test
    void existsByUsername_Success() {
        String username = "testUser";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean result = userService.existsByUsername(username);

        assertThat(result).isTrue();
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void existsByUsername_WhenUserNotExists_ReturnFalse() {
        String username = "nonExistentUser";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        boolean result = userService.existsByUsername(username);

        assertThat(result).isFalse();
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void existsByUsername_WhenUserEmpty_ReturnFalse() {
        String username = "";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        boolean result = userService.existsByUsername(username);

        assertThat(result).isFalse();
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void loadByUsername_WhenUserLoad_Success() {
        String username = "testUser";
        String encodedPassword = "encodedPassword123";

        User user = User.builder()
                .id(1L)
                .username(username)
                .password(encodedPassword)
                .firstname("John")
                .lastname("Doe")
                .role(Role.USER)
                .enabled(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        UserDetails userDetails = userService.loadUserByUsername(username);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo(encodedPassword);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadByUsername_WhenAdminLoad_Success() {
        String username = "adminUser";
        User user = User.builder()
                .id(1L)
                .username(username)
                .password("adminPass")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(username);

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadByUsername_WhenUserNotFound_ThrowUserNotFoundException() {
        String username = "nonExistentUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(userRepository).findByUsername(username);
    }
}







