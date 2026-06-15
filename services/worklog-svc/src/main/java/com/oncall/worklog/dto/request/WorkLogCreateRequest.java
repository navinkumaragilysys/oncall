package com.oncall.worklog.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record WorkLogCreateRequest(
        @NotNull UUID assignmentId,
        @NotNull UUID memberId,
        @NotNull LocalDate logDate
) {
}
