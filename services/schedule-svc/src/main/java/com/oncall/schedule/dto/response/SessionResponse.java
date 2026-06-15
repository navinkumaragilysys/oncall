package com.oncall.schedule.dto.response;

import com.oncall.domain.enums.SessionStatus;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID teamId,
        UUID policyId,
        UUID subSessionDefinitionId,
        Instant rotationWeekStart,
        Instant rotationWeekEnd,
        SessionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
