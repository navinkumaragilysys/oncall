package com.oncall.domain.entity;

import com.oncall.domain.enums.WorkLogEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * An individual clock event within an {@link OnCallWorkLog}.
 *
 * Event sequence for a normal day:
 *   CLOCK_IN → (PAUSE → RESUME)* → CLOCK_OUT
 *
 * The service layer validates that:
 *   - CLOCK_IN is the first event if no prior events exist.
 *   - PAUSE may only follow CLOCK_IN or RESUME.
 *   - RESUME may only follow PAUSE.
 *   - CLOCK_OUT may only follow CLOCK_IN or RESUME.
 *
 * Time intervals are computed as:
 *   sum of (PAUSE.timestamp - preceding CLOCK_IN or RESUME.timestamp)
 *   + (CLOCK_OUT.timestamp - preceding CLOCK_IN or RESUME.timestamp)
 *
 * Relationships:
 * - ManyToOne to {@link OnCallWorkLog}.
 */
@Entity
@Table(name = "work_log_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"workLog"})
public class WorkLogEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_log_id", nullable = false)
    private OnCallWorkLog workLog;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private WorkLogEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    /** Optional note the member may attach to a PAUSE or CLOCK_OUT event. */
    @Column(columnDefinition = "TEXT")
    private String note;
}
