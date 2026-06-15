package com.oncall.availability.dto.response;

import com.oncall.domain.enums.LeaveType;
import com.oncall.domain.enums.RequestStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestResponse(
        UUID id, UUID memberId, UUID managerId, UUID suggestedReplacementId,
        LeaveType leaveType, LocalDate startDate, LocalDate endDate,
        String description, RequestStatus status, String rejectionReason,
        Instant createdAt, Instant updatedAt) {}
