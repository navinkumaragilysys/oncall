package com.oncall.schedule.service;

import com.oncall.domain.enums.SessionStatus;
import com.oncall.schedule.dto.request.SessionStatusUpdateRequest;
import com.oncall.schedule.dto.request.SessionUpsertRequest;
import com.oncall.schedule.dto.response.SessionResponse;
import com.oncall.schedule.entity.OnCallSessionEntity;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.schedule.repository.OnCallSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final OnCallSessionRepository onCallSessionRepository;
    private final OutboxEventPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<SessionResponse> list(UUID teamId, SessionStatus status) {
        List<OnCallSessionEntity> rows;
        if (teamId != null) {
            rows = onCallSessionRepository.findByTeamId(teamId);
        } else if (status != null) {
            rows = onCallSessionRepository.findByStatus(status);
        } else {
            rows = onCallSessionRepository.findAll();
        }
        return rows.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public SessionResponse create(SessionUpsertRequest req) {
        validateWindow(req.rotationWeekStart(), req.rotationWeekEnd());

        OnCallSessionEntity row = new OnCallSessionEntity();
        apply(row, req);
        OnCallSessionEntity saved = onCallSessionRepository.save(row);
        outboxPublisher.publish("oncall_session", saved.getId(), "oncall_session.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public SessionResponse update(UUID id, SessionUpsertRequest req) {
        validateWindow(req.rotationWeekStart(), req.rotationWeekEnd());

        OnCallSessionEntity row = getEntity(id);
        apply(row, req);
        OnCallSessionEntity saved = onCallSessionRepository.save(row);
        outboxPublisher.publish("oncall_session", saved.getId(), "oncall_session.updated", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public SessionResponse updateStatus(UUID id, SessionStatusUpdateRequest req) {
        OnCallSessionEntity row = getEntity(id);
        row.setStatus(req.status());
        OnCallSessionEntity saved = onCallSessionRepository.save(row);
        outboxPublisher.publish("oncall_session", saved.getId(), "oncall_session.status_changed", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        OnCallSessionEntity row = getEntity(id);
        onCallSessionRepository.delete(row);
        outboxPublisher.publish("oncall_session", id, "oncall_session.deleted", "{}");
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listByDateRange(Instant from, Instant to, UUID teamId) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to query params are required");
        }
        validateWindow(from, to);

        List<OnCallSessionEntity> rows = teamId == null
                ? onCallSessionRepository.findByRotationWeekStartGreaterThanEqualAndRotationWeekEndLessThanEqual(from, to)
                : onCallSessionRepository.findByTeamIdAndRotationWeekStartGreaterThanEqualAndRotationWeekEndLessThanEqual(teamId, from, to);
        return rows.stream().map(this::toResponse).toList();
    }

    private OnCallSessionEntity getEntity(UUID id) {
        return onCallSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + id));
    }

    private void apply(OnCallSessionEntity row, SessionUpsertRequest req) {
        row.setTeamId(req.teamId());
        row.setPolicyId(req.policyId());
        row.setSubSessionDefinitionId(req.subSessionDefinitionId());
        row.setRotationWeekStart(req.rotationWeekStart());
        row.setRotationWeekEnd(req.rotationWeekEnd());
        row.setStatus(req.status());
    }

    private void validateWindow(Instant start, Instant end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("rotationWeekEnd must be after rotationWeekStart");
        }
    }

    private SessionResponse toResponse(OnCallSessionEntity row) {
        return new SessionResponse(
                row.getId(),
                row.getTeamId(),
                row.getPolicyId(),
                row.getSubSessionDefinitionId(),
                row.getRotationWeekStart(),
                row.getRotationWeekEnd(),
                row.getStatus(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
