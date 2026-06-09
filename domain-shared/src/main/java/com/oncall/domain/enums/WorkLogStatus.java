package com.oncall.domain.enums;

public enum WorkLogStatus {
    /** Member has clocked in and is actively on-call. */
    ACTIVE,

    /** Member has paused the session (e.g. outside shift window, meal break). */
    PAUSED,

    /** Session has been stopped for the day; total time is finalised. */
    COMPLETED
}
