package com.oncall.domain.enums;

/**
 * How a DevOps ticket was associated with an on-call work log session.
 */
public enum TicketLinkSource {
    /** Member manually typed or pasted the ADO work item ID. */
    MANUAL,

    /** Ticket was fetched and linked via a WIQL saved query from the ADO integration. */
    ADO_QUERY,

    /** Ticket was looked up directly by ID through the ADO API. */
    ADO_LOOKUP
}
