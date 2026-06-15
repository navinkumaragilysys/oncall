package com.oncall.availability.entity;

import com.oncall.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "swap_requests") @Getter @Setter
public class SwapRequestEntity {
    @Id private UUID id;
    @Column(name = "requestor_id",     nullable = false) private UUID requestorId;
    @Column(name = "target_member_id", nullable = false) private UUID targetMemberId;
    @Column(name = "assignment_id",    nullable = false) private UUID assignmentId;
    @Column(name = "manager_id",       nullable = false) private UUID managerId;
    @Column(nullable = false)                            private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RequestStatus status;
    @Column(name = "rejection_reason")                   private String rejectionReason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)       private Instant updatedAt;

    @PrePersist void prePersist() { if (id==null) id=UUID.randomUUID(); Instant n=Instant.now(); createdAt=n; updatedAt=n; }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
