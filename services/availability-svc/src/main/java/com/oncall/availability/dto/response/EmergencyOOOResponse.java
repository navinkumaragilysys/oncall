package com.oncall.availability.dto.response;

import com.oncall.domain.enums.RequestStatus;

import java.time.Instant;
import java.util.UUID;

public record EmergencyOOOResponse(
        UUID id, UUID memberId, UUID reportedById, UUID replacementMemberId,
        UUID managerId, Instant startTime, Instant effectiveStartTime,
        String reason, RequestStatus status, Instant createdAt, Instant updatedAt) {}
