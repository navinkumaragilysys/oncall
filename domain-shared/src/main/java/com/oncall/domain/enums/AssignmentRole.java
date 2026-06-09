package com.oncall.domain.enums;

public enum AssignmentRole {
    /** First responder; primary accountable person for the session. */
    PRIMARY,

    /** Backup; handles coverage if Primary is unavailable or during shared sub-sessions. */
    SECONDARY
}
