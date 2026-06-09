package com.oncall.identity.dto.response;

import com.oncall.domain.enums.MemberStatus;
import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.SystemRole;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        String fullName,
        String displayName,
        String email,
        Region region,
        String timezone,
        MemberStatus status,
        boolean oncallEligible,
        LocalDate joinDate,
        String slackHandle,
        String phone,
        UUID managerId,
        String managerName,
        Set<SystemRole> systemRoles,
        Instant createdAt,
        Instant updatedAt
) {}
