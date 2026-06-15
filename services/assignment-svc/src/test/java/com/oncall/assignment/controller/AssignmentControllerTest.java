package com.oncall.assignment.controller;

import com.oncall.domain.enums.AssignmentRole;
import com.oncall.domain.enums.AssignmentSource;
import com.oncall.domain.enums.AssignmentStatus;
import com.oncall.assignment.dto.request.AssignmentCreateRequest;
import com.oncall.assignment.dto.response.AssignmentResponse;
import com.oncall.assignment.service.AssignmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    private final AssignmentService svc = Mockito.mock(AssignmentService.class);
    private final AssignmentController ctrl = new AssignmentController(svc);

    @Test
    void shouldCreateAssignment() {
        UUID id = UUID.randomUUID();
        var req = new AssignmentCreateRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                AssignmentRole.PRIMARY,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                AssignmentSource.GENERATED, AssignmentStatus.SCHEDULED, 7.0);

        var resp = new AssignmentResponse(id, req.sessionId(), req.memberId(), req.teamId(),
                req.role(), req.shiftStart(), req.shiftEnd(), req.source(), req.status(),
                req.fairnessCreditDays(), Instant.now(), Instant.now());

        Mockito.when(svc.create(Mockito.any())).thenReturn(resp);
        var result = ctrl.create(req);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
