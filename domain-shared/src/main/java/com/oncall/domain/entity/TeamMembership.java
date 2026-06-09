package com.oncall.domain.entity;

import com.oncall.domain.enums.SystemRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Join entity expressing the many-to-many relationship between {@link Member} and {@link Team}.
 * Carries the team-scoped role the member holds within that specific team.
 *
 * Unique constraint: a member can appear in a team only once (one active membership row).
 *
 * Relationships:
 * - ManyToOne to {@link Member}.
 * - ManyToOne to {@link Team}.
 */
@Entity
@Table(
        name = "team_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "team_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"member", "team"})
public class TeamMembership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Role this member holds within this team.
     * Typically ROLE_MANAGER or ROLE_ONCALL_HOST.
     * Null if the member is a plain ROLE_MEMBER in this team.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "team_role")
    private SystemRole teamRole;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "joined_at")
    private LocalDate joinedAt;
}
