package com.oncall.worklog.service;

import com.oncall.domain.enums.WorkLogEventType;
import com.oncall.worklog.dto.request.WorkLogEventRequest;
import com.oncall.worklog.entity.WorkLogEntity;
import com.oncall.worklog.entity.WorkLogEntryEntity;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.worklog.repository.WorkLogEntryRepository;
import com.oncall.worklog.repository.WorkLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkLogServiceTest {

    @Mock private WorkLogRepository workLogRepository;
    @Mock private WorkLogEntryRepository workLogEntryRepository;
    @Mock private OutboxEventPublisher outboxPublisher;
    @InjectMocks private WorkLogService workLogService;

    @Test
    void shouldComputeActiveMinutesOnClockOut() {
        UUID workLogId = UUID.randomUUID();
        WorkLogEntity entity = new WorkLogEntity();
        entity.setId(workLogId);
        entity.setAssignmentId(UUID.randomUUID());
        entity.setMemberId(UUID.randomUUID());
        entity.setLogDate(LocalDate.of(2026, 6, 11));

        WorkLogEntryEntity in = new WorkLogEntryEntity();
        in.setId(UUID.randomUUID());
        in.setWorkLogId(workLogId);
        in.setEventType(WorkLogEventType.CLOCK_IN);
        in.setOccurredAt(Instant.parse("2026-06-11T10:00:00Z"));

        WorkLogEntryEntity out = new WorkLogEntryEntity();
        out.setId(UUID.randomUUID());
        out.setWorkLogId(workLogId);
        out.setEventType(WorkLogEventType.CLOCK_OUT);
        out.setOccurredAt(Instant.parse("2026-06-11T11:30:00Z"));

        when(workLogRepository.findById(workLogId)).thenReturn(Optional.of(entity));
        when(workLogEntryRepository.findByWorkLogIdOrderByOccurredAtAsc(workLogId))
            .thenReturn(List.of(in), List.of(in, out));
        when(workLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(workLogEntryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        workLogService.addEvent(workLogId, new WorkLogEventRequest(
                WorkLogEventType.CLOCK_OUT,
                Instant.parse("2026-06-11T11:30:00Z"),
                null,
                "done"
        ));

        ArgumentCaptor<WorkLogEntity> captor = ArgumentCaptor.forClass(WorkLogEntity.class);
        verify(workLogRepository, atLeastOnce()).save(captor.capture());
        WorkLogEntity saved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(90, saved.getTotalActiveMinutes());
    }
}
