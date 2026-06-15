package com.oncall.availability.entity;

import com.oncall.domain.enums.LeaveType;
import com.oncall.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "leave_requests") @Getter @Setter
public class LeaveRequestEntity {
    @Id private UUID id;
    @Column(name = "member_id",                nullable = false) private UUID memberId;
    @Column(name = "manager_id",               nullable = false) private UUID managerId;
    @Column(name = "suggested_replacement_id")                   private UUID suggestedReplacementId;
    @Enumerated(EnumType.STRING) @Column(name = "leave_type",   nullable = false) private LeaveType leaveType;
    @Column(name = "start_date",               nullable = false) private LocalDate startDate;
    @Column(name = "end_date",                 nullable = false) private LocalDate endDate;
    @Column(columnDefinition = "TEXT")                           private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false)       private RequestStatus status;
    @Column(name = "rejection_reason")                           private String rejectionReason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)               private Instant updatedAt;

    @PrePersist void prePersist() { if (id==null) id=UUID.randomUUID(); Instant n=Instant.now(); createdAt=n; updatedAt=n; }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
