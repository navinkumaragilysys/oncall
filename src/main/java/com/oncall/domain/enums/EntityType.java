package com.oncall.domain.enums;

/**
 * Identifies the domain entity referenced by an {@link com.oncall.domain.entity.ApprovalRecord}
 * or {@link com.oncall.domain.entity.AuditLog} entry.
 */
public enum EntityType {
    TEAM,
    MEMBER,
    TEAM_MEMBERSHIP,
    ROTATION_POLICY,
    SUB_SESSION_DEFINITION,
    SHIFT_DEFINITION,
    ONCALL_SESSION,
    ONCALL_ASSIGNMENT,
    ONCALL_HISTORY,
    HANDOVER,
    MIDWEEK_REASSIGNMENT,
    LEAVE_REQUEST,
    EMERGENCY_OOO,
    SWAP_REQUEST,
    MANAGER_DELEGATION,
    SCHEDULE_DRAFT,
    CONSTRAINT_RELAXATION,
    ONCALL_WORK_LOG,
    WORK_LOG_ENTRY,
    DEVOPS_TICKET_RECORD,
    HANDOVER_PARTICIPANT,
    AZURE_DEVOPS_CONFIG
}
