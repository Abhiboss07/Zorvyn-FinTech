package com.zorvyn.fintech.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Get the currently authenticated user's ID from SecurityContext.
     */
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return UUID.fromString(auth.getName());
    }

    /**
     * Check if the current user has a specific authority/permission.
     */
    public static boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(authority));
    }

    /**
     * Check if the current user is an admin.
     */
    public static boolean isAdmin() {
        return hasAuthority("ROLE_ADMIN");
    }
}
