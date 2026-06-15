package com.oncall.schedule.controller;

import com.oncall.domain.enums.SessionStatus;
import com.oncall.schedule.dto.request.SessionUpsertRequest;
import com.oncall.schedule.dto.response.SessionResponse;
import com.oncall.schedule.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    private final SessionService sessionService = Mockito.mock(SessionService.class);

    private final SessionController sessionController = new SessionController(sessionService);

    @Test
    void shouldCreateSession() {
        UUID id = UUID.randomUUID();
        SessionUpsertRequest request = new SessionUpsertRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-08T00:00:00Z"),
                SessionStatus.DRAFT
        );

        SessionResponse response = new SessionResponse(
                id,
                request.teamId(),
                request.policyId(),
                request.subSessionDefinitionId(),
                request.rotationWeekStart(),
                request.rotationWeekEnd(),
                request.status(),
                Instant.now(),
                Instant.now()
        );

        Mockito.when(sessionService.create(Mockito.any())).thenReturn(response);

        ResponseEntity<SessionResponse> result = sessionController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
