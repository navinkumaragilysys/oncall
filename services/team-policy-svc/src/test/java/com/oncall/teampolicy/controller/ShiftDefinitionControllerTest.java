package com.oncall.teampolicy.controller;

import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.ShiftType;
import com.oncall.domain.enums.WeekDay;
import com.oncall.teampolicy.dto.request.ShiftDefinitionUpsertRequest;
import com.oncall.teampolicy.dto.response.ShiftDefinitionResponse;
import com.oncall.teampolicy.service.ShiftDefinitionService;
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
class ShiftDefinitionControllerTest {

        private final ShiftDefinitionService shiftDefinitionService = Mockito.mock(ShiftDefinitionService.class);

        private final ShiftDefinitionController shiftDefinitionController = new ShiftDefinitionController(shiftDefinitionService);

    @Test
    void shouldCreateShiftDefinition() throws Exception {
        UUID id = UUID.randomUUID();
        ShiftDefinitionUpsertRequest request = new ShiftDefinitionUpsertRequest(
                ShiftType.WEEKEND,
                Region.US_EST,
                true,
                WeekDay.FRIDAY,
                LocalTime.of(21, 0),
                WeekDay.SATURDAY,
                LocalTime.of(9, 0),
                WeekDay.FRIDAY,
                LocalTime.of(22, 0),
                WeekDay.SATURDAY,
                LocalTime.of(10, 0)
        );

        ShiftDefinitionResponse response = new ShiftDefinitionResponse(
                id,
                request.shiftType(),
                request.region(),
                request.dstAware(),
                request.standardStartDay(),
                request.standardStartTime(),
                request.standardEndDay(),
                request.standardEndTime(),
                request.dstStartDay(),
                request.dstStartTime(),
                request.dstEndDay(),
                request.dstEndTime(),
                Instant.now(),
                Instant.now()
        );

        Mockito.when(shiftDefinitionService.create(Mockito.any())).thenReturn(response);

        ResponseEntity<ShiftDefinitionResponse> result = shiftDefinitionController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
