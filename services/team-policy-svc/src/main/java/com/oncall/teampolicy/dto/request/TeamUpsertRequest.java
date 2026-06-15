package com.oncall.teampolicy.dto.request;

import com.oncall.domain.enums.Region;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TeamUpsertRequest(
        @NotBlank String name,
        @NotNull Region region,
        UUID policyId,
        @Min(1) int minEligibleThreshold,
        boolean active
) {
}
