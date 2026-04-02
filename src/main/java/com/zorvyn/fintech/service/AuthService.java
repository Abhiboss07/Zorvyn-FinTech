package com.zorvyn.fintech.service;

import com.zorvyn.fintech.dto.request.LoginRequest;
import com.zorvyn.fintech.dto.request.RegisterRequest;
import com.zorvyn.fintech.dto.response.AuthResponse;
import com.zorvyn.fintech.dto.response.TwoFaSetupResponse;
import com.zorvyn.fintech.dto.response.UserResponse;
import com.zorvyn.fintech.entity.Role;
import com.zorvyn.fintech.entity.User;
import com.zorvyn.fintech.exception.AuthenticationException;
import com.zorvyn.fintech.repository.RoleRepository;
import com.zorvyn.fintech.repository.UserRepository;
import com.zorvyn.fintech.security.JwtTokenProvider;
import com.zorvyn.fintech.util.Constants;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;
    private final AuditService auditService;
    private final EncryptionService encryptionService;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator(32);
    private final CodeVerifier codeVerifier;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                        SessionService sessionService, AuditService auditService,
                        EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionService = sessionService;
        this.auditService = auditService;
        this.encryptionService = encryptionService;

        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        // Check duplicate
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new com.zorvyn.fintech.exception.ApiException("Email already registered", HttpStatus.CONFLICT);
        }

        // Get default "user" role
        Role userRole = roleRepository.findByName(Constants.ROLE_USER)
                .orElseThrow(() -> new com.zorvyn.fintech.exception.ApiException("Default role not found", HttpStatus.INTERNAL_SERVER_ERROR));

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(userRole);
        user = userRepository.save(user);

        // Generate tokens
        AuthResponse response = generateAuthResponse(user);

        // Create Redis session
        sessionService.createSession(user.getId().toString(), user.getEmail(),
                user.getRole().getName(), ipAddress);

        // Audit log
        auditService.log(user.getId().toString(), Constants.AUDIT_REGISTER, "user",
                user.getId().toString(), Map.of("email", user.getEmail()), ipAddress, userAgent);

        return response;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Check 2FA
        if (user.isTwoFaEnabled()) {
            if (request.getTwoFaCode() == null || request.getTwoFaCode().isBlank()) {
                AuthResponse resp = new AuthResponse();
                resp.setTwoFaRequired(true);
                return resp;
            }
            // Decrypt the stored 2FA secret and verify
            String secret = encryptionService.decrypt(user.getTwoFaSecret());
            if (!codeVerifier.isValidCode(secret, request.getTwoFaCode())) {
                throw new AuthenticationException("Invalid 2FA code");
            }
        }

        // Update last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Generate tokens
        AuthResponse response = generateAuthResponse(user);

        // Create Redis session
        sessionService.createSession(user.getId().toString(), user.getEmail(),
                user.getRole().getName(), ipAddress);

        // Audit log
        auditService.log(user.getId().toString(), Constants.AUDIT_LOGIN, "user",
                user.getId().toString(), Map.of("email", user.getEmail()), ipAddress, userAgent);

        return response;
    }

    public TwoFaSetupResponse setup2FA(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.zorvyn.fintech.exception.ResourceNotFoundException("User", "id", userId));

        String secret = secretGenerator.generate();

        // Encrypt and store the secret
        user.setTwoFaSecret(encryptionService.encrypt(secret));
        userRepository.save(user);

        // Generate otpauth URI for QR code
        String otpAuthUri = String.format("otpauth://totp/ZorvynFinTech:%s?secret=%s&issuer=ZorvynFinTech",
                user.getEmail(), secret);

        return new TwoFaSetupResponse(secret, otpAuthUri);
    }

    @Transactional
    public void verify2FA(UUID userId, String code, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.zorvyn.fintech.exception.ResourceNotFoundException("User", "id", userId));

        if (user.getTwoFaSecret() == null) {
            throw new com.zorvyn.fintech.exception.ApiException("2FA not set up. Call setup first.", HttpStatus.BAD_REQUEST);
        }

        String secret = encryptionService.decrypt(user.getTwoFaSecret());
        if (!codeVerifier.isValidCode(secret, code)) {
            throw new AuthenticationException("Invalid 2FA code");
        }

        user.setTwoFaEnabled(true);
        userRepository.save(user);

        auditService.log(userId.toString(), Constants.AUDIT_2FA_VERIFY, "user",
                userId.toString(), null, ipAddress, userAgent);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return generateAuthResponse(user);
    }

    public void logout(UUID userId, String ipAddress, String userAgent) {
        sessionService.destroySession(userId.toString());
        auditService.log(userId.toString(), Constants.AUDIT_LOGOUT, "user",
                userId.toString(), null, ipAddress, userAgent);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().getName(),
                user.getRole().getPermissions());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtTokenProvider.getAccessExpirationMs());
        response.setUser(UserResponse.fromEntity(user));
        return response;
    }
}
