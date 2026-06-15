package com.oncall.ticket.controller;

import com.oncall.ticket.dto.request.DevOpsTicketRequest;
import com.oncall.ticket.dto.request.TicketSyncRequest;
import com.oncall.ticket.dto.response.DevOpsTicketResponse;
import com.oncall.ticket.entity.TicketStatus;
import com.oncall.ticket.service.DevOpsTicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevOpsTicketControllerTest {

    private final DevOpsTicketService service = mock(DevOpsTicketService.class);
    private final DevOpsTicketController controller = new DevOpsTicketController(service);

    private DevOpsTicketResponse sample() {
        return new DevOpsTicketResponse(
                UUID.randomUUID(), UUID.randomUUID(), null, null,
                12345L, "https://dev.azure.com/org/project/_workitems/edit/12345",
                "Task", "Fix on-call incident", TicketStatus.CREATED,
                null, Instant.now(), Instant.now()
        );
    }

    @Test
    void list_returnsOk() {
        when(service.list()).thenReturn(List.of(sample()));
        var res = controller.list(null);
        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(res.getBody()).hasSize(1);
    }

    @Test
    void listByTeam_filtersCorrectly() {
        UUID teamId = UUID.randomUUID();
        when(service.listByTeam(teamId)).thenReturn(List.of(sample()));
        assertThat(controller.list(teamId).getBody()).hasSize(1);
    }

    @Test
    void getById_returnsOk() {
        DevOpsTicketResponse s = sample();
        when(service.getById(s.id())).thenReturn(s);
        assertThat(controller.getById(s.id()).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void create_returns201() {
        DevOpsTicketRequest req = new DevOpsTicketRequest(
                UUID.randomUUID(), null, null, 12345L,
                "https://dev.azure.com/org/_workitems/12345", "Task", "summary");
        when(service.create(req)).thenReturn(sample());
        assertThat(controller.create(req).getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void sync_returnsOk() {
        UUID id = UUID.randomUUID();
        TicketSyncRequest req = new TicketSyncRequest(TicketStatus.ACTIVE);
        when(service.sync(id, req)).thenReturn(sample());
        assertThat(controller.sync(id, req).getStatusCode().value()).isEqualTo(200);
    }
}
