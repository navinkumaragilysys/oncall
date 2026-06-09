package com.oncall.domain.entity;

import com.oncall.domain.enums.MemberStatus;
import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.SystemRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A platform user who can be assigned to on-call sessions.
 *
 * <p><strong>Email domain restriction:</strong> only {@code @agilysys.com} addresses
 * are accepted. This is enforced at three layers:
 * <ol>
 *   <li>Bean Validation ({@code @Pattern}) on this field – rejects at the service boundary.</li>
 *   <li>SSO / OAuth2 configuration – the identity provider is configured to allow only
 *       the {@code agilysys.com} tenant/domain.</li>
 *   <li>Guest sign-in – guest accounts must supply an {@code @agilysys.com} email;
 *       any other domain is rejected with HTTP 403 before a session token is issued.</li>
 * </ol>
 *
 * Relationships:
 * - Self-referential ManyToOne to represent the direct manager hierarchy.
 * - ManyToMany with {@link Team} expressed via {@link TeamMembership} join entity.
 * - Global roles stored in element collection table {@code member_system_roles}.
 *   Team-scoped roles (ROLE_MANAGER, ROLE_ONCALL_HOST) are additionally stored
 *   on {@link TeamMembership#teamRole} to capture which team they govern.
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"manager", "directReports", "teamMemberships", "notificationPreferences"})
public class Member extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "display_name")
    private String displayName;

    /**
     * Must be an {@code @agilysys.com} address. No other domain is permitted anywhere
     * in the system, including guest sign-in accounts.
     */
    @Email
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+\\-]+@agilysys\.com$",
            message = "Only @agilysys.com email addresses are permitted in this system"
    )
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    /** IANA timezone identifier, e.g. "America/Los_Angeles". */
    @Column(nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(name = "oncall_eligible", nullable = false)
    private boolean oncallEligible;

    @Column(name = "join_date")
    private LocalDate joinDate;

    /** Encrypted at the column level in production. */
    @Column(name = "slack_handle")
    private String slackHandle;

    /** Encrypted at the column level in production. */
    @Column(name = "phone")
    private String phone;

    // ── Hierarchy ────────────────────────────────────────────────────────────

    /** Direct manager. Null for top-level admins with no manager above them. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member manager;

    @OneToMany(mappedBy = "manager")
    @Builder.Default
    private List<Member> directReports = new ArrayList<>();

    // ── Roles ─────────────────────────────────────────────────────────────────

    /**
     * Platform-wide roles held by this member.
     * Stored in the {@code member_system_roles} table.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "member_system_roles",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<SystemRole> systemRoles = new HashSet<>();

    // ── Team membership ───────────────────────────────────────────────────────

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamMembership> teamMemberships = new ArrayList<>();

    // ── Notification preferences ──────────────────────────────────────────────

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotificationPreference> notificationPreferences = new ArrayList<>();
}
