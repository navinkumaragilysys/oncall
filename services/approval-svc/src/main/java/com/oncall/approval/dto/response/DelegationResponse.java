package com.oncall.approval.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DelegationResponse(
        UUID id,
        UUID delegatorId,
        UUID delegateeId,
        Instant validFrom,
        Instant validUntil,
        boolean active,
        Instant createdAt
) {}
