package com.oncall.teampolicy.dto.response;

import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.ShiftType;
import com.oncall.domain.enums.WeekDay;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record ShiftDefinitionResponse(
        UUID id,
        ShiftType shiftType,
        Region region,
        boolean dstAware,
        WeekDay standardStartDay,
        LocalTime standardStartTime,
        WeekDay standardEndDay,
        LocalTime standardEndTime,
        WeekDay dstStartDay,
        LocalTime dstStartTime,
        WeekDay dstEndDay,
        LocalTime dstEndTime,
        Instant createdAt,
        Instant updatedAt
) {
}
