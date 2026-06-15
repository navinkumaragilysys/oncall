package com.oncall.notification.service;

import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.notification.dto.request.NotificationLogRequest;
import com.oncall.notification.dto.response.NotificationLogResponse;
import com.oncall.notification.entity.NotificationLogEntity;
import com.oncall.notification.entity.NotificationStatus;
import com.oncall.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SRP: manages notification log records only.
 * DIP: depends on OutboxEventPublisher abstraction.
 */
@Service
@RequiredArgsConstructor
public class NotificationLogService {

    private final NotificationLogRepository repo;
    private final OutboxEventPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<NotificationLogResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationLogResponse> listByRecipient(UUID recipientId) {
        return repo.findByRecipientId(recipientId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NotificationLogResponse getById(UUID id) {
        return toResponse(fetch(id));
    }

    /** Records a notification dispatch attempt as PENDING, then marks it SENT. */
    @Transactional
    public NotificationLogResponse record(NotificationLogRequest req) {
        NotificationLogEntity entity = new NotificationLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setRecipientId(req.recipientId());
        entity.setChannel(req.channel());
        entity.setEventType(req.eventType());
        entity.setSubject(req.subject());
        entity.setBody(req.body());
        entity.setStatus(NotificationStatus.SENT);
        entity.setSentAt(Instant.now());

        NotificationLogEntity saved = repo.save(entity);
        outboxPublisher.publish("notification_log", saved.getId(), "notification.sent", toResponse(saved));
        return toResponse(saved);
    }

    private NotificationLogEntity fetch(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification log not found: " + id));
    }

    private NotificationLogResponse toResponse(NotificationLogEntity e) {
        return new NotificationLogResponse(
                e.getId(), e.getRecipientId(), e.getChannel(), e.getEventType(),
                e.getSubject(), e.getStatus(), e.getSentAt(), e.getFailureReason(), e.getCreatedAt()
        );
    }
}
