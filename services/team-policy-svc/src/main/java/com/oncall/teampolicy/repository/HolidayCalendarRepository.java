package com.oncall.teampolicy.repository;

import com.oncall.domain.entity.HolidayCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, UUID> {
}
