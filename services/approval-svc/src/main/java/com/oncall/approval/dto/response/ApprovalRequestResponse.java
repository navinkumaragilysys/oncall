package com.oncall.approval.dto.response;

import com.oncall.approval.entity.ApprovalReferenceType;
import com.oncall.approval.entity.ApprovalStatus;

import java.time.Instant;
import java.util.UUID;

public record ApprovalRequestResponse(
        UUID id,
        ApprovalReferenceType referenceType,
        UUID referenceId,
        UUID requestorId,
        UUID approverId,
        ApprovalStatus status,
        String decisionReason,
        Instant decidedAt,
        Instant createdAt,
        Instant updatedAt
) {}
