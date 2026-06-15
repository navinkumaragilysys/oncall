package com.oncall.assignment.dto.response;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.AssignmentSource;
import com.oncall.domain.enums.AssignmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AssignmentResponse(
        UUID id, UUID sessionId, UUID memberId, UUID teamId,
        AssignmentRole role, Instant shiftStart, Instant shiftEnd,
        AssignmentSource source, AssignmentStatus status,
        double fairnessCreditDays, Instant createdAt, Instant updatedAt
) {}
