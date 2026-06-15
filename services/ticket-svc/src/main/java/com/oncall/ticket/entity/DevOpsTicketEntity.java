package com.oncall.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devops_tickets")
@Getter
@Setter
@NoArgsConstructor
public class DevOpsTicketEntity {

    @Id
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "worklog_id")
    private UUID worklogId;

    @Column(name = "ado_ticket_id", nullable = false)
    private Long adoTicketId;

    @Column(name = "ado_url", nullable = false, length = 1000)
    private String adoUrl;

    @Column(name = "ticket_type", nullable = false, length = 100)
    private String ticketType;

    @Column(nullable = false, length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false, length = 20)
    private TicketStatus ticketStatus;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }
}
