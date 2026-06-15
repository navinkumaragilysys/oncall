package com.oncall.teampolicy.dto.response;

import com.oncall.domain.enums.Region;

import java.time.Instant;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        Region region,
        UUID policyId,
        int minEligibleThreshold,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
