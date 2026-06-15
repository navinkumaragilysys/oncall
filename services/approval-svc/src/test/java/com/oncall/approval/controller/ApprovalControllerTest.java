package com.oncall.approval.controller;

import com.oncall.approval.dto.request.ApprovalDecisionRequest;
import com.oncall.approval.dto.request.ApprovalRequestCreateRequest;
import com.oncall.approval.dto.response.ApprovalRequestResponse;
import com.oncall.approval.entity.ApprovalReferenceType;
import com.oncall.approval.entity.ApprovalStatus;
import com.oncall.approval.service.ApprovalRequestService;
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
class ApprovalControllerTest {

    private final ApprovalRequestService service = mock(ApprovalRequestService.class);
    private final ApprovalController controller = new ApprovalController(service);

    private ApprovalRequestResponse sample() {
        return new ApprovalRequestResponse(
                UUID.randomUUID(), ApprovalReferenceType.LEAVE_REQUEST, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), ApprovalStatus.PENDING,
                null, null, Instant.now(), Instant.now()
        );
    }

    @Test
    void list_returnsAll() {
        when(service.list()).thenReturn(List.of(sample()));
        var res = controller.list(null, null);
        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(res.getBody()).hasSize(1);
    }

    @Test
    void list_filtersByApprover() {
        UUID approverId = UUID.randomUUID();
        when(service.listByApprover(approverId)).thenReturn(List.of(sample()));
        assertThat(controller.list(approverId, null).getBody()).hasSize(1);
    }

    @Test
    void list_pendingOnly() {
        when(service.listPending()).thenReturn(List.of(sample()));
        assertThat(controller.list(null, true).getBody()).hasSize(1);
    }

    @Test
    void getById_returnsOk() {
        ApprovalRequestResponse s = sample();
        when(service.getById(s.id())).thenReturn(s);
        assertThat(controller.getById(s.id()).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void create_returns201() {
        ApprovalRequestCreateRequest req = new ApprovalRequestCreateRequest(
                ApprovalReferenceType.SWAP_REQUEST, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID());
        when(service.create(req)).thenReturn(sample());
        assertThat(controller.create(req).getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void approve_returnsOk() {
        UUID id = UUID.randomUUID();
        ApprovalDecisionRequest req = new ApprovalDecisionRequest("looks good");
        when(service.approve(id, req)).thenReturn(sample());
        assertThat(controller.approve(id, req).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void reject_returnsOk() {
        UUID id = UUID.randomUUID();
        ApprovalDecisionRequest req = new ApprovalDecisionRequest("incomplete");
        when(service.reject(id, req)).thenReturn(sample());
        assertThat(controller.reject(id, req).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void delegate_returnsOk() {
        UUID id = UUID.randomUUID();
        UUID newApprover = UUID.randomUUID();
        when(service.delegate(id, newApprover)).thenReturn(sample());
        assertThat(controller.delegate(id, newApprover).getStatusCode().value()).isEqualTo(200);
    }
}
