package com.oncall.reporting.dto.request;

import com.oncall.reporting.entity.ReportType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ReportSnapshotRequest(
        @NotNull ReportType reportType,
        UUID teamId,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        @NotNull String data
) {}
