package com.oncall.identity.dto.request;

import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.SystemRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable request record for creating a new Member.
 * Bean Validation annotations are placed on record components — Spring validates
 * the canonical constructor parameters when {@code @Valid} is present on the controller param.
 */
public record CreateMemberRequest(

        @NotBlank @Size(max = 150)
        String fullName,

        @Size(max = 100)
        String displayName,

        @NotBlank
        @Email
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+\\-]+@agilysys\\.com$",
                message = "Only @agilysys.com email addresses are permitted"
        )
        String email,

        /** Raw password — hashed immediately in the service layer; never stored as-is. */
        @NotBlank @Size(min = 8, max = 72)
        String password,

        @NotNull
        Region region,

        @NotBlank
        String timezone,

        boolean oncallEligible,

        LocalDate joinDate,

        String slackHandle,

        String phone,

        /** Manager's memberId — null for top-level admins. */
        UUID managerId,

        /** System-wide roles — defaults to ROLE_MEMBER if null/empty (handled in service). */
        Set<SystemRole> systemRoles
) {}
