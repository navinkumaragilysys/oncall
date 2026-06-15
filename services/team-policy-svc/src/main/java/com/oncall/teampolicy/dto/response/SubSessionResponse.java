package com.oncall.teampolicy.dto.response;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record SubSessionResponse(
        UUID id,
        UUID policyId,
        String name,
        int offsetStartDays,
        LocalTime offsetStartTime,
        int offsetEndDays,
        LocalTime offsetEndTime,
        int ordinal,
        Instant createdAt,
        Instant updatedAt
) {
}
