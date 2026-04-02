package com.zorvyn.fintech.controller;

import com.zorvyn.fintech.dto.request.AssignRoleRequest;
import com.zorvyn.fintech.dto.request.UpdateUserRequest;
import com.zorvyn.fintech.dto.response.ApiResponse;
import com.zorvyn.fintech.dto.response.PagedResponse;
import com.zorvyn.fintech.dto.response.UserResponse;
import com.zorvyn.fintech.security.SecurityUtils;
import com.zorvyn.fintech.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (own profile or admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!id.equals(currentUserId) && !SecurityUtils.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user (own profile or admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!id.equals(currentUserId) && !SecurityUtils.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        UserResponse response = userService.updateUser(id, request, getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("User updated", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> result = userService.listUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        PagedResponse<UserResponse> paged = new PagedResponse<>(
                result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user (admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request,
            HttpServletRequest httpRequest) {
        UserResponse response = userService.assignRole(id, request, getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Role assigned", response));
    }

    private String getIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest req) {
        return req.getHeader("User-Agent");
    }
}
