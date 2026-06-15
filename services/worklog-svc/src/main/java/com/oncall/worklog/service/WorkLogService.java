package com.oncall.worklog.service;

import com.oncall.domain.enums.WorkLogEventType;
import com.oncall.domain.enums.WorkLogStatus;
import com.oncall.worklog.dto.request.WorkLogCreateRequest;
import com.oncall.worklog.dto.request.WorkLogEventRequest;
import com.oncall.worklog.dto.response.WorkLogEntryResponse;
import com.oncall.worklog.dto.response.WorkLogResponse;
import com.oncall.worklog.entity.WorkLogEntity;
import com.oncall.worklog.entity.WorkLogEntryEntity;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.worklog.repository.WorkLogEntryRepository;
import com.oncall.worklog.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final WorkLogEntryRepository workLogEntryRepository;
    private final OutboxEventPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<WorkLogResponse> list(UUID assignmentId, UUID memberId) {
        List<WorkLogEntity> rows;
        if (assignmentId != null) {
            rows = workLogRepository.findByAssignmentId(assignmentId);
        } else if (memberId != null) {
            rows = workLogRepository.findByMemberId(memberId);
        } else {
            rows = workLogRepository.findAll();
        }
        return rows.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkLogResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public WorkLogResponse create(WorkLogCreateRequest req) {
        workLogRepository.findByAssignmentIdAndMemberIdAndLogDate(req.assignmentId(), req.memberId(), req.logDate())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Work log already exists for assignment/member/date");
                });

        WorkLogEntity row = new WorkLogEntity();
        row.setAssignmentId(req.assignmentId());
        row.setMemberId(req.memberId());
        row.setLogDate(req.logDate());
        row.setStatus(WorkLogStatus.PAUSED);
        row.setTotalActiveMinutes(0);
        WorkLogEntity saved = workLogRepository.save(row);
        outboxPublisher.publish("work_log", saved.getId(), "work_log.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public WorkLogResponse addEvent(UUID workLogId, WorkLogEventRequest req) {
        WorkLogEntity workLog = getEntity(workLogId);
        List<WorkLogEntryEntity> entries = workLogEntryRepository.findByWorkLogIdOrderByOccurredAtAsc(workLogId);
        validateTransition(entries, req.eventType());

        WorkLogEntryEntity entry = new WorkLogEntryEntity();
        entry.setWorkLogId(workLogId);
        entry.setEventType(req.eventType());
        entry.setOccurredAt(req.occurredAt());
        entry.setNote(req.note());
        workLogEntryRepository.save(entry);

        if (req.eventType() == WorkLogEventType.CLOCK_OUT && req.dailySummary() != null) {
            workLog.setDailySummary(req.dailySummary());
        }
        workLog.setStatus(resolveStatus(req.eventType()));
        workLog.setTotalActiveMinutes(computeActiveMinutes(workLogId));
        workLogRepository.save(workLog);

        WorkLogResponse response = toResponse(workLog);
        outboxPublisher.publish("work_log", workLog.getId(), "work_log.event_added", response);
        return response;
    }

    private void validateTransition(List<WorkLogEntryEntity> entries, WorkLogEventType nextEvent) {
        if (entries.isEmpty() && nextEvent != WorkLogEventType.CLOCK_IN) {
            throw new IllegalStateException("First event must be CLOCK_IN");
        }
        if (entries.isEmpty()) {
            return;
        }
        WorkLogEventType prev = entries.get(entries.size() - 1).getEventType();
        boolean valid = switch (nextEvent) {
            case CLOCK_IN -> false;
            case PAUSE -> prev == WorkLogEventType.CLOCK_IN || prev == WorkLogEventType.RESUME;
            case RESUME -> prev == WorkLogEventType.PAUSE;
            case CLOCK_OUT -> prev == WorkLogEventType.CLOCK_IN || prev == WorkLogEventType.RESUME;
        };
        if (!valid) {
            throw new IllegalStateException("Invalid work log transition: " + prev + " -> " + nextEvent);
        }
    }

    private WorkLogStatus resolveStatus(WorkLogEventType eventType) {
        return switch (eventType) {
            case CLOCK_IN, RESUME -> WorkLogStatus.ACTIVE;
            case PAUSE -> WorkLogStatus.PAUSED;
            case CLOCK_OUT -> WorkLogStatus.COMPLETED;
        };
    }

    private int computeActiveMinutes(UUID workLogId) {
        List<WorkLogEntryEntity> entries = workLogEntryRepository.findByWorkLogIdOrderByOccurredAtAsc(workLogId);
        if (entries.isEmpty()) {
            return 0;
        }

        int totalMinutes = 0;
        WorkLogEntryEntity activeStart = null;
        for (WorkLogEntryEntity entry : entries) {
            switch (entry.getEventType()) {
                case CLOCK_IN, RESUME -> activeStart = entry;
                case PAUSE, CLOCK_OUT -> {
                    if (activeStart != null) {
                        totalMinutes += (int) Duration.between(activeStart.getOccurredAt(), entry.getOccurredAt()).toMinutes();
                        activeStart = null;
                    }
                }
            }
        }
        return Math.max(totalMinutes, 0);
    }

    private WorkLogEntity getEntity(UUID id) {
        return workLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work log not found: " + id));
    }

    private WorkLogResponse toResponse(WorkLogEntity row) {
        List<WorkLogEntryResponse> entries = new ArrayList<>(workLogEntryRepository.findByWorkLogIdOrderByOccurredAtAsc(row.getId())
                .stream()
                .map(entry -> new WorkLogEntryResponse(
                        entry.getId(),
                        entry.getWorkLogId(),
                        entry.getEventType(),
                        entry.getOccurredAt(),
                        entry.getNote(),
                        entry.getCreatedAt(),
                        entry.getUpdatedAt()
                ))
                .toList());

        return new WorkLogResponse(
                row.getId(),
                row.getAssignmentId(),
                row.getMemberId(),
                row.getLogDate(),
                row.getStatus(),
                row.getTotalActiveMinutes(),
                row.getDailySummary(),
                entries,
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
