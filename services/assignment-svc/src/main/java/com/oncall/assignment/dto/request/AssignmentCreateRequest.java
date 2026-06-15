package com.oncall.assignment.dto.request;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.AssignmentSource;
import com.oncall.domain.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record AssignmentCreateRequest(
        @NotNull UUID sessionId,
        @NotNull UUID memberId,
        @NotNull UUID teamId,
        @NotNull AssignmentRole role,
        @NotNull Instant shiftStart,
        @NotNull Instant shiftEnd,
        @NotNull AssignmentSource source,
        @NotNull AssignmentStatus status,
        double fairnessCreditDays
) {}
