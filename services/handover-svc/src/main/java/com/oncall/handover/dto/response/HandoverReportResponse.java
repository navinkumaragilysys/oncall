package com.oncall.handover.dto.response;

import com.oncall.handover.entity.HandoverStatus;

import java.time.Instant;
import java.util.UUID;

public record HandoverReportResponse(
        UUID id,
        UUID assignmentId,
        UUID fromMemberId,
        UUID toMemberId,
        HandoverStatus status,
        String summary,
        String notes,
        String rejectionReason,
        Instant scheduledAt,
        Instant submittedAt,
        Instant acknowledgedAt,
        Instant createdAt,
        Instant updatedAt
) {}
