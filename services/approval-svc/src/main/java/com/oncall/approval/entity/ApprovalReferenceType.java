package com.oncall.approval.entity;

/** Resource types that flow through the approval queue. */
public enum ApprovalReferenceType {
    LEAVE_REQUEST,
    SWAP_REQUEST,
    MIDWEEK_REASSIGNMENT,
    EMERGENCY_OOO
}
