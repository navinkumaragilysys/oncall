package com.oncall.approval.dto.request;

import com.oncall.approval.entity.ApprovalReferenceType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApprovalRequestCreateRequest(
        @NotNull ApprovalReferenceType referenceType,
        @NotNull UUID referenceId,
        @NotNull UUID requestorId,
        @NotNull UUID approverId
) {}
