package com.oncall.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdoOrgConfigRequest(
        @NotNull UUID teamId,
        @NotBlank String orgUrl,
        @NotBlank String project,
        String areaPath,
        String iterationPath,
        String defaultWorkItemType
) {}
