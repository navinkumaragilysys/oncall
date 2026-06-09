package com.oncall.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalTime;

/**
 * Defines a named time block within a rotation week.
 * A {@link RotationPolicy} with sub-sessions splits the week into multiple
 * independently assigned windows (e.g., Mon–Wed, Thu–Sun).
 *
 * Offset fields are relative to {@link OnCallSession#rotationWeekStart}.
 *
 * Relationships:
 * - ManyToOne to {@link RotationPolicy}.
 */
@Entity
@Table(name = "sub_session_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"policy"})
public class SubSessionDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private RotationPolicy policy;

    @Column(nullable = false)
    private String name;

    /** Days from rotationWeekStart when this sub-session begins. */
    @Column(name = "offset_start_days", nullable = false)
    private int offsetStartDays;

    @Column(name = "offset_start_time", nullable = false)
    private LocalTime offsetStartTime;

    /** Days from rotationWeekStart when this sub-session ends. */
    @Column(name = "offset_end_days", nullable = false)
    private int offsetEndDays;

    @Column(name = "offset_end_time", nullable = false)
    private LocalTime offsetEndTime;

    /** Display and sort order within the rotation week. Must be unique per policy. */
    @Column(nullable = false)
    private int ordinal;
}
