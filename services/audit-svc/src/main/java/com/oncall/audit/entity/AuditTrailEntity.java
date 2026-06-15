package com.oncall.audit.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit trail entry.
 *
 * <p>SRP: represents a single immutable audit event. No update/delete via API.</p>
 */
@Entity
@Table(name = "audit_trail")
@Getter
@Setter
@NoArgsConstructor
public class AuditTrailEntity {

    @Id
    private UUID id;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private ActorType actorType;

    @Column(nullable = false, length = 200)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value")
    private JsonNode oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value")
    private JsonNode newValue;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @PrePersist
    void prePersist() { occurredAt = Instant.now(); }
}
