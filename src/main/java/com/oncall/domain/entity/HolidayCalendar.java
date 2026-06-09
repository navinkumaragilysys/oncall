package com.oncall.domain.entity;

import com.oncall.domain.enums.HolidayShiftImpact;
import com.oncall.domain.enums.HolidayType;
import com.oncall.domain.enums.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A named public or company-observed holiday for a specific region.
 * Holidays are input constraints to the scheduler; they do not automatically
 * cancel shifts but trigger the configured {@link HolidayShiftImpact} action.
 *
 * Unique constraint prevents duplicate holiday entries for the same date and region.
 */
@Entity
@Table(
        name = "holiday_calendars",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "region", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayCalendar extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HolidayType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_impact", nullable = false)
    private HolidayShiftImpact shiftImpact;
}
