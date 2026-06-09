package com.oncall.domain.entity;

import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.ShiftType;
import com.oncall.domain.enums.WeekDay;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Canonical definition of a shift window for a region.
 * Stores both Standard Time and DST variants.
 *
 * For WEEKDAY shifts: startDay / endDay are null (shift applies Mon–Fri).
 * For WEEKEND shifts: startDay / endDay carry the boundary day-of-week
 *   (e.g., standardStartDay=FRIDAY, standardEndDay=SATURDAY).
 *
 * This entity is standalone; it is not directly linked to sessions.
 * The scheduler resolves the correct window at generation time by
 * inspecting the session date against DST calendar boundaries.
 */
@Entity
@Table(name = "shift_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftDefinition extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    /**
     * When true, the DST variant fields are applied during US Daylight Saving Time.
     * IST (IDC) is not affected by DST; its dstStart/End fields are equal to standard.
     */
    @Column(name = "dst_aware", nullable = false)
    private boolean dstAware;

    // ── Standard Time ─────────────────────────────────────────────────────────

    /** Null for WEEKDAY shifts; set for WEEKEND shifts (e.g. FRIDAY). */
    @Enumerated(EnumType.STRING)
    @Column(name = "standard_start_day")
    private WeekDay standardStartDay;

    @Column(name = "standard_start_time", nullable = false)
    private LocalTime standardStartTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_end_day")
    private WeekDay standardEndDay;

    @Column(name = "standard_end_time", nullable = false)
    private LocalTime standardEndTime;

    // ── Daylight Saving Time ──────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "dst_start_day")
    private WeekDay dstStartDay;

    @Column(name = "dst_start_time")
    private LocalTime dstStartTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "dst_end_day")
    private WeekDay dstEndDay;

    @Column(name = "dst_end_time")
    private LocalTime dstEndTime;
}
