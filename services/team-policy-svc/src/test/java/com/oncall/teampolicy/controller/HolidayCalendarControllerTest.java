package com.oncall.teampolicy.controller;

import com.oncall.domain.enums.HolidayShiftImpact;
import com.oncall.domain.enums.HolidayType;
import com.oncall.domain.enums.Region;
import com.oncall.teampolicy.dto.request.HolidayCalendarUpsertRequest;
import com.oncall.teampolicy.dto.response.HolidayCalendarResponse;
import com.oncall.teampolicy.service.HolidayCalendarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HolidayCalendarControllerTest {

        private final HolidayCalendarService holidayCalendarService = Mockito.mock(HolidayCalendarService.class);

        private final HolidayCalendarController holidayCalendarController = new HolidayCalendarController(holidayCalendarService);

    @Test
    void shouldCreateHoliday() throws Exception {
        UUID id = UUID.randomUUID();
        HolidayCalendarUpsertRequest request = new HolidayCalendarUpsertRequest(
                LocalDate.of(2026, 1, 1),
                "New Year",
                Region.US_EST,
                HolidayType.PUBLIC,
                HolidayShiftImpact.FLAG_FOR_REVIEW
        );

        HolidayCalendarResponse response = new HolidayCalendarResponse(
                id,
                request.date(),
                request.name(),
                request.region(),
                request.type(),
                request.shiftImpact(),
                Instant.now(),
                Instant.now()
        );

        Mockito.when(holidayCalendarService.create(Mockito.any())).thenReturn(response);

        ResponseEntity<HolidayCalendarResponse> result = holidayCalendarController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
