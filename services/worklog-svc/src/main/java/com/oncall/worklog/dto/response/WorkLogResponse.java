package com.oncall.worklog.dto.response;

import com.oncall.domain.enums.WorkLogStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WorkLogResponse(
        UUID id,
        UUID assignmentId,
        UUID memberId,
        LocalDate logDate,
        WorkLogStatus status,
        int totalActiveMinutes,
        String dailySummary,
        List<WorkLogEntryResponse> entries,
        Instant createdAt,
        Instant updatedAt
) {
}
