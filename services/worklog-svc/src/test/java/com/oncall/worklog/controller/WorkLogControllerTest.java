package com.oncall.worklog.controller;

import com.oncall.domain.enums.WorkLogStatus;
import com.oncall.worklog.dto.request.WorkLogCreateRequest;
import com.oncall.worklog.dto.response.WorkLogResponse;
import com.oncall.worklog.service.WorkLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WorkLogControllerTest {

    private final WorkLogService workLogService = Mockito.mock(WorkLogService.class);
    private final WorkLogController workLogController = new WorkLogController(workLogService);

    @Test
    void shouldCreateWorkLog() {
        UUID id = UUID.randomUUID();
        WorkLogCreateRequest request = new WorkLogCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 6, 11)
        );

        WorkLogResponse response = new WorkLogResponse(
                id,
                request.assignmentId(),
                request.memberId(),
                request.logDate(),
                WorkLogStatus.PAUSED,
                0,
                null,
                List.of(),
                Instant.now(),
                Instant.now()
        );

        Mockito.when(workLogService.create(Mockito.any())).thenReturn(response);

        var result = workLogController.create(request);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
