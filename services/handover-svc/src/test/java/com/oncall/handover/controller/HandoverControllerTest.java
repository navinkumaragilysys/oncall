package com.oncall.handover.controller;

import com.oncall.handover.dto.request.HandoverRejectRequest;
import com.oncall.handover.dto.request.HandoverReportRequest;
import com.oncall.handover.dto.request.HandoverSubmitRequest;
import com.oncall.handover.dto.response.HandoverReportResponse;
import com.oncall.handover.entity.HandoverStatus;
import com.oncall.handover.service.HandoverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandoverControllerTest {

    private final HandoverService service = mock(HandoverService.class);
    private final HandoverController controller = new HandoverController(service);

    private HandoverReportResponse sample() {
        return new HandoverReportResponse(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                HandoverStatus.PENDING, "All clear", null, null,
                Instant.now(), null, null, Instant.now(), Instant.now()
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
    void listByAssignment_filtersCorrectly() {
        UUID assignmentId = UUID.randomUUID();
        when(service.listByAssignment(assignmentId)).thenReturn(List.of(sample()));
        var res = controller.list(assignmentId);
        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(res.getBody()).hasSize(1);
    }

    @Test
    void getById_returnsOk() {
        HandoverReportResponse s = sample();
        when(service.getById(s.id())).thenReturn(s);
        var res = controller.getById(s.id());
        assertThat(res.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void create_returns201() {
        HandoverReportRequest req = new HandoverReportRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                Instant.now().plusSeconds(3600), null, null);
        when(service.create(req)).thenReturn(sample());
        var res = controller.create(req);
        assertThat(res.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void submit_returnsOk() {
        UUID id = UUID.randomUUID();
        HandoverSubmitRequest req = new HandoverSubmitRequest("summary", "notes");
        when(service.submit(id, req)).thenReturn(sample());
        assertThat(controller.submit(id, req).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void acknowledge_returnsOk() {
        UUID id = UUID.randomUUID();
        when(service.acknowledge(id)).thenReturn(sample());
        assertThat(controller.acknowledge(id).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void reject_returnsOk() {
        UUID id = UUID.randomUUID();
        HandoverRejectRequest req = new HandoverRejectRequest("incomplete");
        when(service.reject(id, req)).thenReturn(sample());
        assertThat(controller.reject(id, req).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void delete_returns204() {
        assertThat(controller.delete(UUID.randomUUID()).getStatusCode().value()).isEqualTo(204);
    }
}
