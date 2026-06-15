package com.oncall.teampolicy.service;

import com.oncall.domain.entity.HolidayCalendar;
import com.oncall.teampolicy.dto.request.HolidayCalendarUpsertRequest;
import com.oncall.teampolicy.dto.response.HolidayCalendarResponse;
import com.oncall.teampolicy.exception.ResourceNotFoundException;
import com.oncall.teampolicy.outbox.OutboxPublisher;
import com.oncall.teampolicy.repository.HolidayCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HolidayCalendarService {

    private final HolidayCalendarRepository holidayCalendarRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<HolidayCalendarResponse> list() {
        return holidayCalendarRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HolidayCalendarResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public HolidayCalendarResponse create(HolidayCalendarUpsertRequest req) {
        HolidayCalendar row = new HolidayCalendar();
        apply(row, req);
        HolidayCalendar saved = holidayCalendarRepository.save(row);
        outboxPublisher.publish("holiday", saved.getId(), "holiday.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public HolidayCalendarResponse update(UUID id, HolidayCalendarUpsertRequest req) {
        HolidayCalendar row = getEntity(id);
        apply(row, req);
        HolidayCalendar saved = holidayCalendarRepository.save(row);
        outboxPublisher.publish("holiday", saved.getId(), "holiday.updated", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        HolidayCalendar row = getEntity(id);
        holidayCalendarRepository.delete(row);
        outboxPublisher.publish("holiday", id, "holiday.deleted", "{}");
    }

    private HolidayCalendar getEntity(UUID id) {
        return holidayCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found: " + id));
    }

    private void apply(HolidayCalendar row, HolidayCalendarUpsertRequest req) {
        row.setDate(req.date());
        row.setName(req.name());
        row.setRegion(req.region());
        row.setType(req.type());
        row.setShiftImpact(req.shiftImpact());
    }

    private HolidayCalendarResponse toResponse(HolidayCalendar row) {
        return new HolidayCalendarResponse(
                row.getId(),
                row.getDate(),
                row.getName(),
                row.getRegion(),
                row.getType(),
                row.getShiftImpact(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
