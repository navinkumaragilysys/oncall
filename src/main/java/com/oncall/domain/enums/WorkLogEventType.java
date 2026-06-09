package com.oncall.domain.enums;

public enum WorkLogEventType {
    /** Member clocked in to start a work session. */
    CLOCK_IN,

    /** Member paused the active session. */
    PAUSE,

    /** Member resumed a paused session. */
    RESUME,

    /** Member clocked out and ended the session for the day. */
    CLOCK_OUT
}
