package com.oncall.assignment.dto.response;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.CompletionStatus;
import com.oncall.domain.enums.HandoverStatus;

import java.time.Instant;
import java.util.UUID;

public record HistoryResponse(
        UUID id, UUID memberId, UUID assignmentId,
        Instant periodStart, Instant periodEnd,
        AssignmentRole role, CompletionStatus completionStatus,
        HandoverStatus handoverStatus, double fairnessCreditDays,
        String notes, boolean imported, Instant createdAt, Instant updatedAt
) {}
