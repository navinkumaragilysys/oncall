package com.oncall.domain.entity;

import com.oncall.domain.enums.EntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit trail entry. Never updated after creation.
 * Does NOT extend {@link BaseEntity} to avoid the updatedAt column.
 *
 * Records every sensitive operation: assignment overrides, approvals, member changes,
 * constraint relaxations, schedule publications, and delegation events.
 *
 * Relationships:
 * - ManyToOne to {@link Member} (actor who performed the action).
 */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"actor"})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private Member actor;

    /** Human-readable action label, e.g. "ASSIGNMENT_OVERRIDE", "LEAVE_APPROVED". */
    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false, columnDefinition = "uuid")
    private UUID entityId;

    /** JSON snapshot of the entity state before the action. Null for creation events. */
    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    /** JSON snapshot of the entity state after the action. */
    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
}
