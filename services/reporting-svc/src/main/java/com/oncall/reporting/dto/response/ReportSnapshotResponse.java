package com.oncall.reporting.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.oncall.reporting.entity.ReportType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReportSnapshotResponse(
        UUID id,
        ReportType reportType,
        UUID teamId,
        LocalDate periodStart,
        LocalDate periodEnd,
        JsonNode data,
        Instant generatedAt
) {}
