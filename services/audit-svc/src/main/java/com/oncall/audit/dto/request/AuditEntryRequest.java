package com.oncall.audit.dto.request;

import com.oncall.audit.entity.ActorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuditEntryRequest(
        UUID actorId,
        @NotNull ActorType actorType,
        @NotBlank String action,
        @NotBlank String resourceType,
        @NotNull UUID resourceId,
        String oldValue,
        String newValue,
        String ipAddress,
        String serviceName
) {}
