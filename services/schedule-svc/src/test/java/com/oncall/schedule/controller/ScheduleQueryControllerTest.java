package com.oncall.schedule.controller;

import com.oncall.schedule.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ScheduleQueryControllerTest {

    private final SessionService sessionService = Mockito.mock(SessionService.class);

    private final ScheduleQueryController scheduleQueryController = new ScheduleQueryController(sessionService);

    @Test
    void shouldReturnScheduleRows() {
        Mockito.when(sessionService.listByDateRange(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());

        var result = scheduleQueryController.listByDateRange(
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-30T00:00:00Z"),
                null
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().size());
    }
}
