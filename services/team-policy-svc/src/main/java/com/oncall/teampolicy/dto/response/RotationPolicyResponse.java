package com.oncall.teampolicy.dto.response;

import com.oncall.domain.enums.AllocationStrategy;

import java.time.Instant;
import java.util.UUID;

public record RotationPolicyResponse(
        UUID id,
        String name,
        AllocationStrategy strategy,
        int rotationLengthDays,
        int minGapDays,
        boolean weekendPolicySeparate,
        boolean secondaryEnabled,
        double secondaryFairnessWeight,
        boolean sameTeamAllowed,
        int horizonMonths,
        boolean allowMultiSubsessionPerWeek,
        Instant createdAt,
        Instant updatedAt
) {
}
