package com.oncall.domain.entity;

import com.oncall.domain.enums.WorkLogStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents one day's on-call work session for a member within an assignment.
 *
 * A member can CLOCK_IN multiple times within a day (if they pause and resume),
 * but only one {@code OnCallWorkLog} row exists per (assignment, member, date).
 *
 * Total active time is the sum of all CLOCK_IN→PAUSE and CLOCK_IN→CLOCK_OUT
 * intervals derived from child {@link WorkLogEntry} records.
 * {@code totalActiveMinutes} is recomputed and persisted on every CLOCK_OUT
 * or manual admin correction.
 *
 * Relationships:
 * - ManyToOne to {@link OnCallAssignment}.
 * - ManyToOne to {@link Member}.
 * - OneToMany to {@link WorkLogEntry}  (ordered clock events for this day).
 * - OneToMany to {@link DevOpsTicketRecord} (tickets handled during this work session).
 */
@Entity
@Table(
        name = "oncall_work_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "member_id", "log_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"assignment", "member", "entries", "tickets"})
public class OnCallWorkLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private OnCallAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** Calendar date this work log covers. */
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkLogStatus status;

    /**
     * Cumulative active (non-paused) minutes for the day.
     * Recomputed by the service layer on every CLOCK_OUT or admin correction.
     * Value is 0 while the session is still ACTIVE or PAUSED.
     */
    @Column(name = "total_active_minutes", nullable = false)
    private int totalActiveMinutes;

    /** Optional free-text summary written by the member at CLOCK_OUT. */
    @Column(name = "daily_summary", columnDefinition = "TEXT")
    private String dailySummary;

    @OneToMany(mappedBy = "workLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkLogEntry> entries = new ArrayList<>();

    @OneToMany(mappedBy = "workLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DevOpsTicketRecord> tickets = new ArrayList<>();
}
