package com.oncall.ticket.dto.response;

import com.oncall.ticket.entity.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record DevOpsTicketResponse(
        UUID id,
        UUID teamId,
        UUID assignmentId,
        UUID worklogId,
        Long adoTicketId,
        String adoUrl,
        String ticketType,
        String summary,
        TicketStatus ticketStatus,
        Instant syncedAt,
        Instant createdAt,
        Instant updatedAt
) {}
