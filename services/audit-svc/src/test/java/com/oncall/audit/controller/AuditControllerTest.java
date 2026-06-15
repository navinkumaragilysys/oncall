package com.oncall.audit.controller;

import com.oncall.audit.dto.request.AuditEntryRequest;
import com.oncall.audit.dto.response.AuditTrailResponse;
import com.oncall.audit.entity.ActorType;
import com.oncall.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    private final AuditService service = mock(AuditService.class);
    private final AuditController controller = new AuditController(service);

    private AuditTrailResponse sample() {
        return new AuditTrailResponse(
                UUID.randomUUID(), UUID.randomUUID(), ActorType.MEMBER,
                "team.created", "team", UUID.randomUUID(),
                null, null, "127.0.0.1", "team-policy-svc", Instant.now()
        );
    }

    @Test
    void list_returnsPaged() {
        when(service.listPaged(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(sample())));
        var res = controller.list(0, 50);
        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(res.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void listByActor_returnsOk() {
        UUID actorId = UUID.randomUUID();
        when(service.listByActor(actorId)).thenReturn(List.of(sample()));
        assertThat(controller.listByActor(actorId).getBody()).hasSize(1);
    }

    @Test
    void getById_returnsOk() {
        AuditTrailResponse s = sample();
        when(service.getById(s.id())).thenReturn(s);
        assertThat(controller.getById(s.id()).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void record_returns201() {
        AuditEntryRequest req = new AuditEntryRequest(
                UUID.randomUUID(), ActorType.MEMBER, "team.updated",
                "team", UUID.randomUUID(), null, "{\"name\":\"alpha\"}", null, "team-policy-svc");
        when(service.record(req)).thenReturn(sample());
        assertThat(controller.record(req).getStatusCode().value()).isEqualTo(201);
    }
}
