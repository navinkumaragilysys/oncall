package com.oncall.assignment.dto.request;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.CompletionStatus;
import com.oncall.domain.enums.HandoverStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record HistoryCreateRequest(
        @NotNull UUID memberId,
        @NotNull UUID assignmentId,
        @NotNull Instant periodStart,
        @NotNull Instant periodEnd,
        @NotNull AssignmentRole role,
        @NotNull CompletionStatus completionStatus,
        HandoverStatus handoverStatus,
        double fairnessCreditDays,
        String notes,
        boolean imported
) {}
