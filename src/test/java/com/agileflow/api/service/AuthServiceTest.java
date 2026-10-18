package com.agileflow.api.service;

import com.agileflow.api.dto.request.AuthRequest;
import com.agileflow.api.dto.request.RegisterRequest;
import com.agileflow.api.dto.response.AuthResponse;
import com.agileflow.api.exception.BadRequestException;
import com.agileflow.api.model.Role;
import com.agileflow.api.model.User;
import com.agileflow.api.repository.UserRepository;
import com.agileflow.api.security.JwtService;
import com.agileflow.api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
            .id(1L)
            .username("johndoe")
            .email("john@example.com")
            .password("encoded_secret")
            .fullName("John Doe")
            .role(Role.ROLE_DEVELOPER)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("register: should register new user and return JWT token")
    void register_ShouldRegisterUser_WhenDataIsValid() {
        RegisterRequest request = new RegisterRequest(
            "johndoe",
            "john@example.com",
            "password123",
            "John Doe",
            Role.ROLE_DEVELOPER
        );

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateToken(any(UserPrincipal.class), eq(1L), eq("ROLE_DEVELOPER")))
            .thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().username()).isEqualTo("johndoe");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register: should throw BadRequestException when username already taken")
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest(
            "johndoe",
            "john@example.com",
            "password123",
            "John Doe",
            Role.ROLE_DEVELOPER
        );

        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Username 'johndoe' is already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("authenticate: should return JWT token when credentials are valid")
    void authenticate_ShouldReturnToken_WhenCredentialsValid() {
        AuthRequest request = new AuthRequest("johndoe", "password123");
        UserPrincipal principal = new UserPrincipal(sampleUser);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateToken(principal, 1L, "ROLE_DEVELOPER"))
            .thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.user().email()).isEqualTo("john@example.com");
    }
}
