package com.oncall.ticket.dto.request;

import com.oncall.ticket.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketSyncRequest(@NotNull TicketStatus ticketStatus) {}
