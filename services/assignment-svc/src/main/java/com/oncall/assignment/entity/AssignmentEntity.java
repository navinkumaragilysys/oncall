package com.oncall.assignment.entity;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.AssignmentSource;
import com.oncall.domain.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oncall_assignments")
@Getter @Setter
public class AssignmentEntity {

    @Id private UUID id;
    @Column(name = "session_id", nullable = false) private UUID sessionId;
    @Column(name = "member_id",  nullable = false) private UUID memberId;
    @Column(name = "team_id",    nullable = false) private UUID teamId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AssignmentRole role;
    @Column(name = "shift_start", nullable = false) private Instant shiftStart;
    @Column(name = "shift_end",   nullable = false) private Instant shiftEnd;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AssignmentSource source;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AssignmentStatus status;
    @Column(name = "fairness_credit_days", nullable = false) private double fairnessCreditDays;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void prePersist() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now(); createdAt = now; updatedAt = now;
    }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
