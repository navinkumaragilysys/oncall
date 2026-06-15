package com.oncall.handover.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record HandoverReportRequest(
        @NotNull UUID assignmentId,
        @NotNull UUID fromMemberId,
        @NotNull UUID toMemberId,
        @NotNull Instant scheduledAt,
        String summary,
        String notes
) {}
