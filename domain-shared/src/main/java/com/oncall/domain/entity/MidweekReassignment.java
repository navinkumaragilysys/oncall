package com.oncall.domain.entity;

import com.oncall.domain.enums.ReassignmentReasonCategory;
import com.oncall.domain.enums.RequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Records a request to reassign a published {@link OnCallAssignment} to another
 * member due to known but late-discovered unavailability (non-emergency).
 *
 * Distinct from {@link EmergencyOOO}: mid-week reassignment has a planned reason,
 * a 4-hour manager approval SLA, and proportional fairness credit splitting.
 *
 * Relationships:
 * - ManyToOne to {@link OnCallAssignment} (the assignment being reassigned).
 * - ManyToOne to {@link Member}: requestedBy, originalMember, replacementMember, manager.
 */
@Entity
@Table(name = "midweek_reassignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"assignment", "requestedBy", "originalMember", "replacementMember", "manager"})
public class MidweekReassignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private OnCallAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private Member requestedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_member_id", nullable = false)
    private Member originalMember;

    /**
     * Null until the manager selects a replacement from the candidate list.
     * Set on approval.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacement_member_id")
    private Member replacementMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_category", nullable = false)
    private ReassignmentReasonCategory reasonCategory;

    @Column(nullable = false)
    private String reason;

    /** Point in time from which the reassignment takes effect. */
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    /** End of the original shift (or earlier if partial reassignment). */
    @Column(name = "effective_to", nullable = false)
    private LocalDateTime effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    /** Approval must be given by this deadline; expiry triggers admin escalation. */
    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
