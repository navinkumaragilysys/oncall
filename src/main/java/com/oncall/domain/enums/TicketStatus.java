package com.oncall.domain.enums;

public enum TicketStatus {
    /** Ticket received; not yet being worked on. */
    OPEN,

    /** Member is actively investigating or resolving the ticket. */
    IN_PROGRESS,

    /** Ticket has been resolved by the on-call member. */
    RESOLVED,

    /** Ticket requires involvement beyond on-call scope; escalated to specialist. */
    ESCALATED,

    /** Ticket is fully closed and confirmed resolved. */
    CLOSED
}
