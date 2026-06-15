package com.oncall.notification.service;

import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.notification.dto.request.NotificationPreferenceRequest;
import com.oncall.notification.dto.response.NotificationPreferenceResponse;
import com.oncall.notification.entity.NotificationPreferenceEntity;
import com.oncall.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * SRP: manages notification preferences only; does not touch the log.
 */
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository repo;

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> listByMember(UUID memberId) {
        return repo.findByMemberId(memberId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getById(UUID id) {
        return toResponse(fetch(id));
    }

    @Transactional
    public NotificationPreferenceResponse upsert(NotificationPreferenceRequest req) {
        NotificationPreferenceEntity entity = new NotificationPreferenceEntity();
        entity.setId(UUID.randomUUID());
        entity.setMemberId(req.memberId());
        entity.setChannel(req.channel());
        entity.setEventType(req.eventType());
        entity.setEnabled(req.enabled());
        return toResponse(repo.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Notification preference not found: " + id);
        }
        repo.deleteById(id);
    }

    private NotificationPreferenceEntity fetch(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification preference not found: " + id));
    }

    private NotificationPreferenceResponse toResponse(NotificationPreferenceEntity e) {
        return new NotificationPreferenceResponse(
                e.getId(), e.getMemberId(), e.getChannel(), e.getEventType(),
                e.isEnabled(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
