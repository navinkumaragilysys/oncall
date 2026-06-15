package com.oncall.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DevOpsTicketRequest(
        @NotNull UUID teamId,
        UUID assignmentId,
        UUID worklogId,
        @NotNull Long adoTicketId,
        @NotBlank String adoUrl,
        @NotBlank String ticketType,
        @NotBlank String summary
) {}
