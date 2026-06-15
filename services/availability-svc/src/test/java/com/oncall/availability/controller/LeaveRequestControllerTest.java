package com.oncall.availability.controller;

import com.oncall.availability.dto.request.LeaveRequestCreate;
import com.oncall.availability.dto.response.LeaveRequestResponse;
import com.oncall.availability.service.LeaveRequestService;
import com.oncall.domain.enums.LeaveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LeaveRequestControllerTest {
    private final LeaveRequestService svc = Mockito.mock(LeaveRequestService.class);
    private final LeaveRequestController ctrl = new LeaveRequestController(svc);

    @Test void shouldCreateLeaveRequest() {
        UUID id = UUID.randomUUID();
        var req = new LeaveRequestCreate(UUID.randomUUID(), UUID.randomUUID(), null,
                LeaveType.PERSONAL, LocalDate.of(2026,7,1), LocalDate.of(2026,7,5), "holiday");
        var resp = new LeaveRequestResponse(id, req.memberId(), req.managerId(), null,
                req.leaveType(), req.startDate(), req.endDate(), req.description(),
                com.oncall.domain.enums.RequestStatus.PENDING, null, Instant.now(), Instant.now());
        Mockito.when(svc.create(Mockito.any())).thenReturn(resp);
        var result = ctrl.create(req);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
    }
}
