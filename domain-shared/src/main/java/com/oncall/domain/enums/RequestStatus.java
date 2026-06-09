package com.oncall.domain.enums;

/** Universal status for leave, swap, and mid-week reassignment requests. */
public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN,
    EXPIRED
}
