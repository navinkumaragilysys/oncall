package com.oncall.domain.enums;

public enum ReassignmentReasonCategory {
    TRAVEL,
    PROJECT_CONFLICT,
    PERSONAL,
    /** Leave was approved but not reflected in schedule before generation. */
    LEAVE_APPROVED_LATE,
    OTHER
}
