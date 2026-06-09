package com.oncall.domain.entity;

import com.oncall.domain.enums.ApprovalAction;
import com.oncall.domain.enums.EntityType;
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
 * Immutable record of every approval action taken on a requestable entity.
 * Supports the full approval audit trail including manager delegations.
 *
 * {@code entityType} + {@code entityId} form a polymorphic reference to the
 * entity being acted upon (LeaveRequest, SwapRequest, MidweekReassignment, EmergencyOOO).
 *
 * Relationships:
 * - ManyToOne to {@link Member}: approver (the person who acted),
 *   delegatedFrom (non-null when acting under a delegation).
 */
@Entity
@Table(name = "approval_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"approver", "delegatedFrom"})
public class ApprovalRecord extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    /** Primary key of the referenced entity row. */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approver_id", nullable = false)
    private Member approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalAction action;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    @Column(columnDefinition = "TEXT")
    private String reason;

    /**
     * Non-null when this approval was made by a delegate acting on behalf of
     * the original manager. References the manager who created the delegation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegated_from_id")
    private Member delegatedFrom;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;
}
