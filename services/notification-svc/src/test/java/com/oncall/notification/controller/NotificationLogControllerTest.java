package com.oncall.notification.controller;

import com.oncall.notification.dto.request.NotificationLogRequest;
import com.oncall.notification.dto.response.NotificationLogResponse;
import com.oncall.notification.entity.NotificationChannel;
import com.oncall.notification.entity.NotificationStatus;
import com.oncall.notification.service.NotificationLogService;
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
class NotificationLogControllerTest {

    private final NotificationLogService service = mock(NotificationLogService.class);
    private final NotificationLogController controller = new NotificationLogController(service);

    private NotificationLogResponse sample() {
        return new NotificationLogResponse(
                UUID.randomUUID(), UUID.randomUUID(), NotificationChannel.EMAIL,
                "oncall.assigned", "You are on call", NotificationStatus.SENT,
                Instant.now(), null, Instant.now()
        );
    }

    @Test
    void list_returnsAll() {
        when(service.list()).thenReturn(List.of(sample()));
        var res = controller.list(null);
        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(res.getBody()).hasSize(1);
    }

    @Test
    void listByRecipient_filters() {
        UUID recipientId = UUID.randomUUID();
        when(service.listByRecipient(recipientId)).thenReturn(List.of(sample()));
        assertThat(controller.list(recipientId).getBody()).hasSize(1);
    }

    @Test
    void getById_returnsOk() {
        NotificationLogResponse s = sample();
        when(service.getById(s.id())).thenReturn(s);
        assertThat(controller.getById(s.id()).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void record_returns201() {
        NotificationLogRequest req = new NotificationLogRequest(
                UUID.randomUUID(), NotificationChannel.SLACK, "oncall.swap", "Swap request", null);
        when(service.record(req)).thenReturn(sample());
        assertThat(controller.record(req).getStatusCode().value()).isEqualTo(201);
    }
}
