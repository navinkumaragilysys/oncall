package com.oncall.reporting.controller;

import com.oncall.reporting.dto.request.ReportSnapshotRequest;
import com.oncall.reporting.dto.response.ReportSnapshotResponse;
import com.oncall.reporting.entity.ReportType;
import com.oncall.reporting.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private final ReportService service = mock(ReportService.class);
    private final ReportController controller = new ReportController(service);

    private ReportSnapshotResponse sample() {
        return new ReportSnapshotResponse(
                UUID.randomUUID(), ReportType.FAIRNESS_SUMMARY, UUID.randomUUID(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                null, Instant.now()
        );
    }

    @Test
    void list_returnsAll() {
        when(service.list(null, null)).thenReturn(List.of(sample()));
        var res = controller.list(null, null);
        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(res.getBody()).hasSize(1);
    }

    @Test
    void list_filtersByReportType() {
        when(service.list(ReportType.WORKLOAD_SUMMARY, null)).thenReturn(List.of(sample()));
        assertThat(controller.list(ReportType.WORKLOAD_SUMMARY, null).getBody()).hasSize(1);
    }

    @Test
    void getById_returnsOk() {
        ReportSnapshotResponse s = sample();
        when(service.getById(s.id())).thenReturn(s);
        assertThat(controller.getById(s.id()).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void store_returns201() {
        ReportSnapshotRequest req = new ReportSnapshotRequest(
                ReportType.FAIRNESS_SUMMARY, UUID.randomUUID(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                "{\"members\":[]}");
        when(service.store(req)).thenReturn(sample());
        assertThat(controller.store(req).getStatusCode().value()).isEqualTo(201);
    }
}
