package com.oncall.identity.dto.request;

import com.oncall.domain.enums.MemberStatus;
import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.SystemRole;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * PATCH-semantics update record — all components are nullable.
 * The service applies only non-null values.
 * Email and password changes have dedicated endpoints for clean audit trails.
 */
public record UpdateMemberRequest(

        @Size(max = 150)
        String fullName,

        @Size(max = 100)
        String displayName,

        Region region,

        String timezone,

        /** Use Boolean (boxed) so null means "not specified" vs false = "explicitly false". */
        Boolean oncallEligible,

        LocalDate joinDate,

        String slackHandle,

        String phone,

        UUID managerId,

        /** Admin-only: update system-wide roles. */
        Set<SystemRole> systemRoles,

        /** Admin/Manager-only: change member status. */
        MemberStatus status
) {}
