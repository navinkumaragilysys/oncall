package com.oncall.availability.dto.response;

import com.oncall.domain.enums.RequestStatus;

import java.time.Instant;
import java.util.UUID;

public record SwapRequestResponse(
        UUID id, UUID requestorId, UUID targetMemberId, UUID assignmentId,
        UUID managerId, String reason, RequestStatus status,
        String rejectionReason, Instant createdAt, Instant updatedAt) {}
