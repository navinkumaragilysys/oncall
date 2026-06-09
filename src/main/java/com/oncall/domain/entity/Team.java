package com.oncall.domain.entity;

import com.oncall.domain.enums.Region;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a participating team in the on-call rotation platform.
 *
 * Relationships:
 * - Many teams may share one {@link RotationPolicy} (ManyToOne).
 * - One team has many {@link TeamMembership} records linking it to members.
 * - One team hosts many {@link OnCallSession} instances over time.
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"policy", "memberships", "sessions"})
public class Team extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    /**
     * The rotation policy governing this team's on-call schedule.
     * Nullable: a team may exist before a policy is assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private RotationPolicy policy;

    /** Minimum number of eligible active members required; triggers a warning if breached. */
    @Column(name = "min_eligible_threshold", nullable = false)
    private int minEligibleThreshold;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private List<OnCallSession> sessions = new ArrayList<>();
}
