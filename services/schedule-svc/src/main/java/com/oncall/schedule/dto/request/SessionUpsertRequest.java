package com.oncall.schedule.dto.request;

import com.oncall.domain.enums.SessionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record SessionUpsertRequest(
        @NotNull UUID teamId,
        @NotNull UUID policyId,
        UUID subSessionDefinitionId,
        @NotNull Instant rotationWeekStart,
        @NotNull Instant rotationWeekEnd,
        @NotNull SessionStatus status
) {
}
