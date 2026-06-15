package com.oncall.approval.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A manager's temporary delegation of approval authority.
 *
 * <p>SRP: represents the delegation record only.</p>
 */
@Entity
@Table(name = "approval_delegations")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalDelegationEntity {

    @Id
    private UUID id;

    @Column(name = "delegator_id", nullable = false)
    private UUID delegatorId;

    @Column(name = "delegatee_id", nullable = false)
    private UUID delegateeId;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }
}
