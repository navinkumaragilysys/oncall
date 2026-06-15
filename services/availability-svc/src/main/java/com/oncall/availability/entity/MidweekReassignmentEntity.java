package com.oncall.availability.entity;

import com.oncall.domain.enums.ReassignmentReasonCategory;
import com.oncall.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "midweek_reassignments") @Getter @Setter
public class MidweekReassignmentEntity {
    @Id private UUID id;
    @Column(name = "assignment_id",         nullable = false) private UUID assignmentId;
    @Column(name = "requested_by_id",       nullable = false) private UUID requestedById;
    @Column(name = "original_member_id",    nullable = false) private UUID originalMemberId;
    @Column(name = "replacement_member_id")                   private UUID replacementMemberId;
    @Column(name = "manager_id",            nullable = false) private UUID managerId;
    @Enumerated(EnumType.STRING) @Column(name = "reason_category", nullable = false) private ReassignmentReasonCategory reasonCategory;
    @Column(columnDefinition = "TEXT")                        private String description;
    @Column(name = "requested_at",          nullable = false) private Instant requestedAt;
    @Column(name = "effective_at")                            private Instant effectiveAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false)    private RequestStatus status;
    @Column(name = "rejection_reason")                        private String rejectionReason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)            private Instant updatedAt;

    @PrePersist void prePersist() { if (id==null) id=UUID.randomUUID(); Instant n=Instant.now(); createdAt=n; updatedAt=n; }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
