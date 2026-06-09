package com.oncall.domain.entity;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.AssignmentSource;
import com.oncall.domain.enums.AssignmentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An individual member's assignment within an {@link OnCallSession}.
 * Each session has at most one PRIMARY and one SECONDARY assignment.
 *
 * Relationships:
 * - ManyToOne to {@link OnCallSession}.
 * - ManyToOne to {@link Member} (the assigned engineer).
 * - ManyToOne to {@link Team} (the team this assignment belongs to).
 * - OneToOne to {@link OnCallHistory} (created on completion).
 * - OneToMany to {@link MidweekReassignment} (reassignment requests against this assignment).
 * - OneToMany to {@link SwapRequest} (swap requests against this assignment).
 * - OneToMany to {@link OnCallWorkLog} (daily clock-in/out sessions within this assignment).
 * - OneToMany to {@link DevOpsTicketRecord} (all ADO tickets linked to this assignment week).
 */
@Entity
@Table(name = "oncall_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"session", "member", "team", "history", "reassignments", "swapRequests"})
public class OnCallAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private OnCallSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentRole role;

    @Column(name = "shift_start", nullable = false)
    private LocalDateTime shiftStart;

    @Column(name = "shift_end", nullable = false)
    private LocalDateTime shiftEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    /**
     * Proportional fairness credit in days.
     * Full credit = policy.rotationLengthDays for PRIMARY, or
     * policy.rotationLengthDays * policy.secondaryFairnessWeight for SECONDARY.
     * Reduced proportionally for partial coverage due to reassignment or emergency OOO.
     */
    @Column(name = "fairness_credit_days", nullable = false)
    private double fairnessCreditDays;

    @OneToOne(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private OnCallHistory history;

    @OneToMany(mappedBy = "assignment")
    @Builder.Default
    private List<MidweekReassignment> reassignments = new ArrayList<>();

    @OneToMany(mappedBy = "assignment")
    @Builder.Default
    private List<SwapRequest> swapRequests = new ArrayList<>();

    /**
     * Daily work log sessions for this assignment.
     * One row per (member, date) within the assignment window.
     */
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OnCallWorkLog> workLogs = new ArrayList<>();

    /**
     * All Azure DevOps ticket records linked to this assignment week.
     * Cross-day aggregate; individual per-day time is on each {@link DevOpsTicketRecord}.
     */
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DevOpsTicketRecord> ticketRecords = new ArrayList<>();
}
