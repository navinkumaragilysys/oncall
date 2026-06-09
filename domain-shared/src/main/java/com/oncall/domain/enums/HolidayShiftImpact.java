package com.oncall.domain.enums;

public enum HolidayShiftImpact {
    /** Scheduler flags this shift for admin review; no automatic action. */
    FLAG_FOR_REVIEW,
    /** Original assignment is kept; holiday is acknowledged on the record. */
    KEEP_ORIGINAL,
    /** Shift is automatically reassigned to the next eligible member. */
    AUTO_REASSIGN
}
