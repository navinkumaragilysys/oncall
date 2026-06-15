package com.oncall.teampolicy.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record SubSessionUpsertRequest(
        @NotNull UUID policyId,
        @NotBlank String name,
        @Min(0) int offsetStartDays,
        @NotNull LocalTime offsetStartTime,
        @Min(0) int offsetEndDays,
        @NotNull LocalTime offsetEndTime,
        @Min(1) int ordinal
) {
}
