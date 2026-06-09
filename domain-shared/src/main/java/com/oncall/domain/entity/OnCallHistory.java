package com.oncall.domain.entity;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.CompletionStatus;
import com.oncall.domain.enums.HandoverStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Immutable audit record of a completed or partially completed on-call assignment.
 * Created when an {@link OnCallAssignment} transitions to COMPLETED or PARTIALLY_COMPLETED.
 * Historical records seeded from bulk import set {@code imported = true}.
 *
 * Used by the allocation engine to enforce the fairness gap rule.
 *
 * Relationships:
 * - ManyToOne to {@link Member}.
 * - OneToOne to {@link OnCallAssignment}.
 */
@Entity
@Table(name = "oncall_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"member", "assignment"})
public class OnCallHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private OnCallAssignment assignment;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_status", nullable = false)
    private CompletionStatus completionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "handover_status")
    private HandoverStatus handoverStatus;

    /**
     * Actual fairness credit awarded for this assignment period.
     * Matches {@link OnCallAssignment#fairnessCreditDays} at the time of completion.
     */
    @Column(name = "fairness_credit_days", nullable = false)
    private double fairnessCreditDays;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** True when seeded from a bulk historical data import. */
    @Column(nullable = false)
    private boolean imported;
}
