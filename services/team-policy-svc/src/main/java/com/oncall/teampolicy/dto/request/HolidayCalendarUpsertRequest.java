package com.oncall.teampolicy.dto.request;

import com.oncall.domain.enums.HolidayShiftImpact;
import com.oncall.domain.enums.HolidayType;
import com.oncall.domain.enums.Region;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayCalendarUpsertRequest(
        @NotNull LocalDate date,
        @NotBlank String name,
        @NotNull Region region,
        @NotNull HolidayType type,
        @NotNull HolidayShiftImpact shiftImpact
) {
}
