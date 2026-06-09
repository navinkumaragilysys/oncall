package com.oncall.domain.enums;

/**
 * Platform-wide roles. A {@link com.oncall.domain.entity.Member} may hold multiple roles.
 * Team-scoped roles (ROLE_MANAGER, ROLE_ONCALL_HOST) are also stored on
 * {@link com.oncall.domain.entity.TeamMembership#teamRole} to express the scope.
 */
public enum SystemRole {
    ROLE_ADMIN,
    ROLE_MANAGER,
    ROLE_ONCALL_HOST,
    ROLE_MEMBER
}
