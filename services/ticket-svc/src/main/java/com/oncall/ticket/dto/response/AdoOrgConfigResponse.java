package com.oncall.ticket.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AdoOrgConfigResponse(
        UUID id,
        UUID teamId,
        String orgUrl,
        String project,
        String areaPath,
        String iterationPath,
        String defaultWorkItemType,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
