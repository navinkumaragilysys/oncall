package com.oncall.reporting.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Materialised report snapshot.
 *
 * <p>SRP: holds one point-in-time report result. Report generation logic
 * lives in the service layer.</p>
 */
@Entity
@Table(name = "report_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class ReportSnapshotEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private JsonNode data;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;

    @PrePersist
    void prePersist() { generatedAt = Instant.now(); }
}
