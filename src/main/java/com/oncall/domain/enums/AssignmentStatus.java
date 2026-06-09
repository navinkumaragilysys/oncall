package com.oncall.domain.enums;

public enum AssignmentStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    /** Member covered part of the shift before reassignment or emergency OOO. */
    PARTIALLY_COMPLETED,
    CANCELLED
}
