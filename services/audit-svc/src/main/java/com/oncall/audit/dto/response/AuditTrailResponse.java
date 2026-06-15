package com.oncall.audit.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.oncall.audit.entity.ActorType;

import java.time.Instant;
import java.util.UUID;

public record AuditTrailResponse(
        UUID id,
        UUID actorId,
        ActorType actorType,
        String action,
        String resourceType,
        UUID resourceId,
        JsonNode oldValue,
        JsonNode newValue,
        String ipAddress,
        String serviceName,
        Instant occurredAt
) {}
