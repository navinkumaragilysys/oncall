package com.oncall.availability.dto.request;

import com.oncall.domain.enums.LeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestCreate(
        @NotNull UUID memberId, @NotNull UUID managerId,
        UUID suggestedReplacementId, @NotNull LeaveType leaveType,
        @NotNull LocalDate startDate, @NotNull LocalDate endDate,
        String description) {}
