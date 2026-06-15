package com.oncall.worklog.dto.response;

import com.oncall.domain.enums.WorkLogEventType;

import java.time.Instant;
import java.util.UUID;

public record WorkLogEntryResponse(
        UUID id,
        UUID workLogId,
        WorkLogEventType eventType,
        Instant occurredAt,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
