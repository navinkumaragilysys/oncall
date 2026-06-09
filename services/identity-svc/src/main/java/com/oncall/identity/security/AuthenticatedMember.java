package com.oncall.identity.security;

import java.util.List;
import java.util.UUID;

/**
 * Immutable principal placed into the Spring Security context after JWT validation.
 * Passed to controller methods via {@code @AuthenticationPrincipal AuthenticatedMember}.
 * Records are ideal here — a principal should never be mutated after construction.
 */
public record AuthenticatedMember(UUID memberId, String email, List<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }
}
