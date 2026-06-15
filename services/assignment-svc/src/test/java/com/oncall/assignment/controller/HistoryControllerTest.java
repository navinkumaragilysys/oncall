package com.oncall.assignment.controller;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.CompletionStatus;
import com.oncall.assignment.dto.request.HistoryCreateRequest;
import com.oncall.assignment.dto.response.HistoryResponse;
import com.oncall.assignment.service.HistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HistoryControllerTest {

    private final HistoryService svc = Mockito.mock(HistoryService.class);
    private final HistoryController ctrl = new HistoryController(svc);

    @Test
    void shouldCreateHistoryRecord() {
        UUID id = UUID.randomUUID();
        var req = new HistoryCreateRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                AssignmentRole.PRIMARY, CompletionStatus.COMPLETED,
                null, 7.0, null, false);

        var resp = new HistoryResponse(id, req.memberId(), req.assignmentId(),
                req.periodStart(), req.periodEnd(), req.role(), req.completionStatus(),
                null, 7.0, null, false, Instant.now(), Instant.now());

        Mockito.when(svc.create(Mockito.any())).thenReturn(resp);
        var result = ctrl.create(req);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
