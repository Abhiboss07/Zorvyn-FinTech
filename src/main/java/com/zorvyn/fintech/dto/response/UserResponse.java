package com.zorvyn.fintech.dto.response;

import com.zorvyn.fintech.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String roleName;
    private List<String> permissions;
    private boolean twoFaEnabled;
    private boolean active;
    private Instant lastLoginAt;
    private Instant createdAt;

    public static UserResponse fromEntity(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.email = user.getEmail();
        r.firstName = user.getFirstName();
        r.lastName = user.getLastName();
        r.roleName = user.getRole().getName();
        r.permissions = user.getRole().getPermissions();
        r.twoFaEnabled = user.isTwoFaEnabled();
        r.active = user.isActive();
        r.lastLoginAt = user.getLastLoginAt();
        r.createdAt = user.getCreatedAt();
        return r;
    }

    // ── Getters ──

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRoleName() { return roleName; }
    public List<String> getPermissions() { return permissions; }
    public boolean isTwoFaEnabled() { return twoFaEnabled; }
    public boolean isActive() { return active; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public Instant getCreatedAt() { return createdAt; }
}
