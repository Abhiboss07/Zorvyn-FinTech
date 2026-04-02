package com.zorvyn.fintech.service;

import com.zorvyn.fintech.dto.request.LoginRequest;
import com.zorvyn.fintech.dto.request.RegisterRequest;
import com.zorvyn.fintech.dto.response.AuthResponse;
import com.zorvyn.fintech.entity.Role;
import com.zorvyn.fintech.entity.User;
import com.zorvyn.fintech.exception.AuthenticationException;
import com.zorvyn.fintech.repository.RoleRepository;
import com.zorvyn.fintech.repository.UserRepository;
import com.zorvyn.fintech.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private SessionService sessionService;
    @Mock private AuditService auditService;
    @Mock private EncryptionService encryptionService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, passwordEncoder,
                jwtTokenProvider, sessionService, auditService, encryptionService);
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@zorvyn.com");
        request.setPassword("Test@12345");
        request.setFirstName("Test");
        request.setLastName("User");

        Role userRole = new Role();
        userRole.setId(UUID.randomUUID());
        userRole.setName("user");
        userRole.setPermissions(List.of("read", "write"));

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@zorvyn.com");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");
        savedUser.setRole(userRole);
        savedUser.setPasswordHash("hashed");

        when(userRepository.existsByEmail("test@zorvyn.com")).thenReturn(false);
        when(roleRepository.findByName("user")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString(), anyList())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.register(request, "127.0.0.1", "TestAgent");

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(sessionService).createSession(anyString(), eq("test@zorvyn.com"), eq("user"), eq("127.0.0.1"));
        verify(auditService).log(anyString(), eq("REGISTER"), anyString(), anyString(), anyMap(), anyString(), anyString());
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@zorvyn.com");
        request.setPassword("Test@12345");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userRepository.existsByEmail("existing@zorvyn.com")).thenReturn(true);

        assertThrows(com.zorvyn.fintech.exception.ApiException.class,
                () -> authService.register(request, "127.0.0.1", "TestAgent"));
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@zorvyn.com");
        request.setPassword("Test@12345");

        Role userRole = new Role();
        userRole.setName("user");
        userRole.setPermissions(List.of("read", "write"));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@zorvyn.com");
        user.setPasswordHash("hashed");
        user.setRole(userRole);
        user.setActive(true);

        when(userRepository.findByEmail("test@zorvyn.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Test@12345", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString(), anyList())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.login(request, "127.0.0.1", "TestAgent");

        assertNotNull(response);
        assertFalse(response.isTwoFaRequired());
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void login_invalidPassword_throws() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@zorvyn.com");
        request.setPassword("wrong");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@zorvyn.com");
        user.setPasswordHash("hashed");
        user.setActive(true);
        user.setRole(new Role());

        when(userRepository.findByEmail("test@zorvyn.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> authService.login(request, "127.0.0.1", "TestAgent"));
    }

    @Test
    void login_2faRequired_returnsFlag() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@zorvyn.com");
        request.setPassword("Test@12345");

        Role userRole = new Role();
        userRole.setName("user");
        userRole.setPermissions(List.of("read", "write"));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@zorvyn.com");
        user.setPasswordHash("hashed");
        user.setRole(userRole);
        user.setActive(true);
        user.setTwoFaEnabled(true);
        user.setTwoFaSecret("encrypted-secret");

        when(userRepository.findByEmail("test@zorvyn.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Test@12345", "hashed")).thenReturn(true);

        AuthResponse response = authService.login(request, "127.0.0.1", "TestAgent");

        assertTrue(response.isTwoFaRequired());
        assertNull(response.getAccessToken());
    }

    @Test
    void logout_destroysSession() {
        UUID userId = UUID.randomUUID();
        authService.logout(userId, "127.0.0.1", "TestAgent");

        verify(sessionService).destroySession(userId.toString());
        verify(auditService).log(eq(userId.toString()), eq("LOGOUT"), anyString(), anyString(), isNull(), anyString(), anyString());
    }
}
