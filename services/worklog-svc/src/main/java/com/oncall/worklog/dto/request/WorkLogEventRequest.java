package com.oncall.worklog.dto.request;

import com.oncall.domain.enums.WorkLogEventType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record WorkLogEventRequest(
        @NotNull WorkLogEventType eventType,
        @NotNull Instant occurredAt,
        String note,
        String dailySummary
) {
}
