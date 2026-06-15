package com.oncall.handover.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent record of a shift handover.
 *
 * <p>SRP: solely responsible for representing the state of a handover report.
 * Transition logic lives in the service layer, not here.</p>
 */
@Entity
@Table(name = "handover_reports")
@Getter
@Setter
@NoArgsConstructor
public class HandoverReportEntity {

    @Id
    private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "from_member_id", nullable = false)
    private UUID fromMemberId;

    @Column(name = "to_member_id", nullable = false)
    private UUID toMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HandoverStatus status;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
