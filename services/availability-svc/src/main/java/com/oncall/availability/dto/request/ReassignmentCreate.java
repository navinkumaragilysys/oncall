package com.oncall.availability.dto.request;

import com.oncall.domain.enums.ReassignmentReasonCategory;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ReassignmentCreate(
        @NotNull UUID assignmentId, @NotNull UUID requestedById,
        @NotNull UUID originalMemberId, @NotNull UUID managerId,
        @NotNull ReassignmentReasonCategory reasonCategory,
        String description, @NotNull Instant requestedAt) {}
