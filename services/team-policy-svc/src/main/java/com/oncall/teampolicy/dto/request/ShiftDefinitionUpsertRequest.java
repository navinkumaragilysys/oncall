package com.oncall.teampolicy.dto.request;

import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.ShiftType;
import com.oncall.domain.enums.WeekDay;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ShiftDefinitionUpsertRequest(
        @NotNull ShiftType shiftType,
        @NotNull Region region,
        boolean dstAware,
        WeekDay standardStartDay,
        @NotNull LocalTime standardStartTime,
        WeekDay standardEndDay,
        @NotNull LocalTime standardEndTime,
        WeekDay dstStartDay,
        LocalTime dstStartTime,
        WeekDay dstEndDay,
        LocalTime dstEndTime
) {
}
