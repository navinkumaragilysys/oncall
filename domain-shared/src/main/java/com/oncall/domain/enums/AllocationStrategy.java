package com.oncall.domain.enums;

public enum AllocationStrategy {
    /** Teams/members take turns in a fixed cyclic order. */
    ROUND_ROBIN,

    /** Member who has gone the longest without an on-call shift is assigned next. */
    LEAST_RECENTLY_ASSIGNED,

    /** Assignment probability weighted by last assignment date and opt-out count. */
    WEIGHTED_FAIRNESS,

    /** Schedule is entirely admin-managed; engine only validates, not assigns. */
    MANUAL_OVERRIDE_ONLY
}
