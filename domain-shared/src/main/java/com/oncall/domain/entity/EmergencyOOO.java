package com.oncall.domain.entity;

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
 * Records an unplanned unavailability declared within 48 hours of a shift start or mid-shift.
 *
 * On declaration the system immediately searches for a replacement candidate.
 * The manager has a 2-hour SLA to confirm; expiry auto-escalates to admin.
 *
 * Partial shift credit: the original member is credited for any coverage completed
 * before the effective start time; the replacement receives credit for the remainder.
 *
 * Relationships:
 * - ManyToOne to {@link Member}: member (absent), reportedBy, replacementMember, manager.
 */
@Entity
@Table(name = "emergency_ooo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"member", "reportedBy", "replacementMember", "manager"})
public class EmergencyOOO extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private Member reportedBy;

    /** Null until the manager selects and confirms a replacement. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacement_member_id")
    private Member replacementMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "estimated_return")
    private LocalDateTime estimatedReturn;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    /** Manager must confirm replacement by this time (default: 2 hours after declaration). */
    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;

    @Column(name = "replacement_assigned_at")
    private LocalDateTime replacementAssignedAt;
}
