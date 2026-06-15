package com.oncall.availability.entity;

import com.oncall.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "emergency_ooo") @Getter @Setter
public class EmergencyOOOEntity {
    @Id private UUID id;
    @Column(name = "member_id",             nullable = false) private UUID memberId;
    @Column(name = "reported_by_id",        nullable = false) private UUID reportedById;
    @Column(name = "replacement_member_id")                   private UUID replacementMemberId;
    @Column(name = "manager_id",            nullable = false) private UUID managerId;
    @Column(name = "start_time",            nullable = false) private Instant startTime;
    @Column(name = "effective_start_time",  nullable = false) private Instant effectiveStartTime;
    @Column(columnDefinition = "TEXT")                        private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false)    private RequestStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)            private Instant updatedAt;

    @PrePersist void prePersist() { if (id==null) id=UUID.randomUUID(); Instant n=Instant.now(); createdAt=n; updatedAt=n; }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
