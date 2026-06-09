package com.oncall.domain.entity;

import com.oncall.domain.enums.LeaveType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An engineer's request to be marked unavailable for on-call during a date range.
 *
 * The requesting member may optionally suggest an alternative eligible team member
 * to cover any on-call shift that overlaps the leave period.
 *
 * Pre-schedule approval: member is excluded from allocation for the period.
 * Post-schedule approval: triggers the reassignment workflow automatically.
 *
 * Relationships:
 * - ManyToOne to {@link Member}: member (requestor), manager (approver),
 *   suggestedReplacement (optional hint).
 */
@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"member", "manager", "suggestedReplacement"})
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    /**
     * Optional: member-suggested alternative on-call replacement.
     * The scheduler uses this as a preference hint, not a binding assignment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_replacement_id")
    private Member suggestedReplacement;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    /** Manager must act by this time; expiry triggers reminder and then admin escalation. */
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;
}
