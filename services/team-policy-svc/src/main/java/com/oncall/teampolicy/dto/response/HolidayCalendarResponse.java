package com.oncall.teampolicy.dto.response;

import com.oncall.domain.enums.HolidayShiftImpact;
import com.oncall.domain.enums.HolidayType;
import com.oncall.domain.enums.Region;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HolidayCalendarResponse(
        UUID id,
        LocalDate date,
        String name,
        Region region,
        HolidayType type,
        HolidayShiftImpact shiftImpact,
        Instant createdAt,
        Instant updatedAt
) {
}
