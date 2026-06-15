package com.oncall.availability.dto.response;

import com.oncall.domain.enums.ReassignmentReasonCategory;
import com.oncall.domain.enums.RequestStatus;

import java.time.Instant;
import java.util.UUID;

public record ReassignmentResponse(
        UUID id, UUID assignmentId, UUID requestedById, UUID originalMemberId,
        UUID replacementMemberId, UUID managerId,
        ReassignmentReasonCategory reasonCategory, String description,
        Instant requestedAt, Instant effectiveAt,
        RequestStatus status, String rejectionReason,
        Instant createdAt, Instant updatedAt) {}
