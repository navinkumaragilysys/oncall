package com.oncall.approval.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record DelegationCreateRequest(
        @NotNull UUID delegatorId,
        @NotNull UUID delegateeId,
        @NotNull Instant validFrom,
        @NotNull Instant validUntil
) {}
