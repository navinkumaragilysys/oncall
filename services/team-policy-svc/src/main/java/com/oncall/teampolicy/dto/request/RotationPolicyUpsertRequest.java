package com.oncall.teampolicy.dto.request;

import com.oncall.domain.enums.AllocationStrategy;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RotationPolicyUpsertRequest(
        @NotBlank String name,
        @NotNull AllocationStrategy strategy,
        @Min(1) int rotationLengthDays,
        @Min(0) int minGapDays,
        boolean weekendPolicySeparate,
        boolean secondaryEnabled,
        @DecimalMin("0.0") double secondaryFairnessWeight,
        boolean sameTeamAllowed,
        @Min(1) int horizonMonths,
        boolean allowMultiSubsessionPerWeek
) {
}
