package com.oncall.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ado_org_configs")
@Getter
@Setter
@NoArgsConstructor
public class AdoOrgConfigEntity {

    @Id
    private UUID id;

    @Column(name = "team_id", nullable = false, unique = true)
    private UUID teamId;

    @Column(name = "org_url", nullable = false, length = 500)
    private String orgUrl;

    @Column(nullable = false, length = 200)
    private String project;

    @Column(name = "area_path", length = 500)
    private String areaPath;

    @Column(name = "iteration_path", length = 500)
    private String iterationPath;

    @Column(name = "default_work_item_type", nullable = false, length = 100)
    private String defaultWorkItemType;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }
}
