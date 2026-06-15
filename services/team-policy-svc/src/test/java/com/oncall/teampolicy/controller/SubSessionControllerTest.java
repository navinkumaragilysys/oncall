package com.oncall.teampolicy.controller;

import com.oncall.teampolicy.dto.request.SubSessionUpsertRequest;
import com.oncall.teampolicy.dto.response.SubSessionResponse;
import com.oncall.teampolicy.service.SubSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SubSessionControllerTest {

        private final SubSessionService subSessionService = Mockito.mock(SubSessionService.class);

        private final SubSessionController subSessionController = new SubSessionController(subSessionService);

    @Test
    void shouldCreateSubSession() throws Exception {
        UUID id = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        SubSessionUpsertRequest request = new SubSessionUpsertRequest(
                policyId,
                "Weekday Window",
                0,
                LocalTime.of(9, 0),
                2,
                LocalTime.of(18, 0),
                1
        );

        SubSessionResponse response = new SubSessionResponse(
                id,
                policyId,
                "Weekday Window",
                0,
                LocalTime.of(9, 0),
                2,
                LocalTime.of(18, 0),
                1,
                Instant.now(),
                Instant.now()
        );

        Mockito.when(subSessionService.create(Mockito.any())).thenReturn(response);

        ResponseEntity<SubSessionResponse> result = subSessionController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
