package com.oncall.domain.entity;

import com.oncall.domain.enums.SessionStatus;
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
 * Represents one schedulable on-call window.
 *
 * A session is either:
 *  - A full rotation week (subSessionDefinition = null), or
 *  - One sub-session block within a week (subSessionDefinition != null).
 *
 * Relationships:
 * - ManyToOne to {@link Team}.
 * - ManyToOne to {@link RotationPolicy}.
 * - ManyToOne (nullable) to {@link SubSessionDefinition}.
 * - OneToMany to {@link OnCallAssignment} (exactly one PRIMARY, optionally one SECONDARY).
 * - OneToOne to {@link Handover}.
 */
@Entity
@Table(name = "oncall_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"team", "policy", "subSessionDefinition", "assignments", "handover"})
public class OnCallSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private RotationPolicy policy;

    /**
     * Null when this session covers the full rotation week.
     * Non-null when this session is one block within a sub-session split.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_session_definition_id")
    private SubSessionDefinition subSessionDefinition;

    @Column(name = "rotation_week_start", nullable = false)
    private LocalDateTime rotationWeekStart;

    @Column(name = "rotation_week_end", nullable = false)
    private LocalDateTime rotationWeekEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OnCallAssignment> assignments = new ArrayList<>();

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Handover handover;
}
