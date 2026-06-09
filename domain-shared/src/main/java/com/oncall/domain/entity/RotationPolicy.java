package com.oncall.domain.entity;

import com.oncall.domain.enums.AllocationStrategy;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines all scheduling and fairness rules for a set of teams.
 *
 * Relationships:
 * - OneToMany to {@link Team} (a policy can govern multiple teams).
 * - OneToMany to {@link SubSessionDefinition} (optional week splitting).
 * - OneToMany to {@link OnCallSession} (all sessions generated under this policy).
 */
@Entity
@Table(name = "rotation_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"subSessions", "teams", "sessions"})
public class RotationPolicy extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationStrategy strategy;

    /** Length of each rotation window in days. Default: 7. */
    @Column(name = "rotation_length_days", nullable = false)
    private int rotationLengthDays;

    /** Minimum days a member must wait before being assigned again. Default: 120 (~4 months). */
    @Column(name = "min_gap_days", nullable = false)
    private int minGapDays;

    /** When true, weekend shifts use a separate eligibility pool and strategy. */
    @Column(name = "weekend_policy_separate", nullable = false)
    private boolean weekendPolicySeparate;

    /** When true, every session requires both a Primary and a Secondary assignee. */
    @Column(name = "secondary_enabled", nullable = false)
    private boolean secondaryEnabled;

    /**
     * Fairness gap credit weight for Secondary assignments.
     * Default: 0.5 (half the gap credit of a Primary assignment).
     */
    @Column(name = "secondary_fairness_weight", nullable = false)
    private double secondaryFairnessWeight;

    /** When false, Primary and Secondary must not belong to the same team. */
    @Column(name = "same_team_primary_secondary_allowed", nullable = false)
    private boolean sameTeamAllowed;

    /** How many months ahead the schedule generation horizon extends. */
    @Column(name = "horizon_months", nullable = false)
    private int horizonMonths;

    /**
     * When false, a member may not appear in more than one sub-session within
     * the same rotation week without explicit admin override.
     */
    @Column(name = "allow_multi_subsession_per_week", nullable = false)
    private boolean allowMultiSubsessionPerWeek;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubSessionDefinition> subSessions = new ArrayList<>();

    @OneToMany(mappedBy = "policy")
    @Builder.Default
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "policy")
    @Builder.Default
    private List<OnCallSession> sessions = new ArrayList<>();
}
