package com.zorvyn.fintech.controller;

import com.zorvyn.fintech.dto.request.LoginRequest;
import com.zorvyn.fintech.dto.request.RegisterRequest;
import com.zorvyn.fintech.dto.request.TwoFaVerifyRequest;
import com.zorvyn.fintech.dto.response.ApiResponse;
import com.zorvyn.fintech.dto.response.AuthResponse;
import com.zorvyn.fintech.dto.response.TwoFaSetupResponse;
import com.zorvyn.fintech.security.SecurityUtils;
import com.zorvyn.fintech.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication & 2FA endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.register(request, getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email/password (+ optional 2FA)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request, getIp(httpRequest), getUserAgent(httpRequest));
        if (response.isTwoFaRequired()) {
            return ResponseEntity.ok(ApiResponse.success("2FA code required", response));
        }
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — destroy session")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        UUID userId = SecurityUtils.getCurrentUserId();
        authService.logout(userId, getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/2fa/setup")
    @Operation(summary = "Setup 2FA for current user")
    public ResponseEntity<ApiResponse<TwoFaSetupResponse>> setup2FA() {
        UUID userId = SecurityUtils.getCurrentUserId();
        TwoFaSetupResponse response = authService.setup2FA(userId);
        return ResponseEntity.ok(ApiResponse.success("2FA setup initiated", response));
    }

    @PostMapping("/2fa/verify")
    @Operation(summary = "Verify and enable 2FA")
    public ResponseEntity<ApiResponse<Void>> verify2FA(
            @Valid @RequestBody TwoFaVerifyRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtils.getCurrentUserId();
        authService.verify2FA(userId, request.getCode(), getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("2FA enabled successfully"));
    }

    private String getIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest req) {
        return req.getHeader("User-Agent");
    }
}
