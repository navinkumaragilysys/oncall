package com.oncall.domain.enums;

public enum CompletionStatus {
    COMPLETED,
    /** Member covered only part of the shift (mid-week reassignment or emergency). */
    PARTIALLY_COMPLETED,
    MISSED,
    EMERGENCY_COVERED,
    REASSIGNED
}
