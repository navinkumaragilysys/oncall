package com.oncall.availability.service;

import com.oncall.availability.dto.request.EmergencyOOOCreate;
import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.response.EmergencyOOOResponse;
import com.oncall.availability.entity.EmergencyOOOEntity;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.availability.repository.EmergencyOOORepository;
import com.oncall.domain.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class EmergencyOOOService {
    private final EmergencyOOORepository repo;
    private final OutboxEventPublisher outbox;

    @Transactional(readOnly = true)
    public List<EmergencyOOOResponse> list(UUID memberId) {
        return (memberId != null ? repo.findByMemberId(memberId) : repo.findAll()).stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public EmergencyOOOResponse getById(UUID id) { return toResp(get(id)); }

    @Transactional
    public EmergencyOOOResponse create(EmergencyOOOCreate req) {
        EmergencyOOOEntity e = new EmergencyOOOEntity();
        e.setMemberId(req.memberId()); e.setReportedById(req.reportedById());
        e.setManagerId(req.managerId()); e.setStartTime(req.startTime());
        e.setEffectiveStartTime(req.effectiveStartTime()); e.setReason(req.reason());
        e.setStatus(RequestStatus.PENDING);
        EmergencyOOOEntity saved = repo.save(e);
        outbox.publish("emergency_ooo", saved.getId(), "emergency_ooo.created", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public EmergencyOOOResponse updateStatus(UUID id, StatusUpdateRequest req) {
        EmergencyOOOEntity e = get(id); e.setStatus(req.status());
        EmergencyOOOEntity saved = repo.save(e);
        outbox.publish("emergency_ooo", saved.getId(), "emergency_ooo.status_changed", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public void delete(UUID id) { repo.delete(get(id)); outbox.publish("emergency_ooo", id, "emergency_ooo.deleted", "{}"); }

    private EmergencyOOOEntity get(UUID id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Emergency OOO not found: " + id)); }

    private EmergencyOOOResponse toResp(EmergencyOOOEntity e) {
        return new EmergencyOOOResponse(e.getId(), e.getMemberId(), e.getReportedById(),
                e.getReplacementMemberId(), e.getManagerId(), e.getStartTime(),
                e.getEffectiveStartTime(), e.getReason(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
