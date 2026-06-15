package com.oncall.assignment.entity;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.CompletionStatus;
import com.oncall.domain.enums.HandoverStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oncall_history")
@Getter @Setter
public class OnCallHistoryEntity {

    @Id private UUID id;
    @Column(name = "member_id",     nullable = false) private UUID memberId;
    @Column(name = "assignment_id", nullable = false, unique = true) private UUID assignmentId;
    @Column(name = "period_start",  nullable = false) private Instant periodStart;
    @Column(name = "period_end",    nullable = false) private Instant periodEnd;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AssignmentRole role;
    @Enumerated(EnumType.STRING) @Column(name = "completion_status", nullable = false) private CompletionStatus completionStatus;
    @Enumerated(EnumType.STRING) @Column(name = "handover_status") private HandoverStatus handoverStatus;
    @Column(name = "fairness_credit_days", nullable = false) private double fairnessCreditDays;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(nullable = false) private boolean imported;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void prePersist() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now(); createdAt = now; updatedAt = now;
    }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
