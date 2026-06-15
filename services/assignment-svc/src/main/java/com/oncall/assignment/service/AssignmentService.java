package com.oncall.assignment.service;

import com.oncall.assignment.dto.request.AssignmentCreateRequest;
import com.oncall.assignment.dto.request.AssignmentStatusUpdateRequest;
import com.oncall.assignment.dto.response.AssignmentResponse;
import com.oncall.assignment.entity.AssignmentEntity;
import com.oncall.assignment.exception.ResourceNotFoundException;
import com.oncall.assignment.outbox.OutboxPublisher;
import com.oncall.assignment.repository.AssignmentRepository;
import com.oncall.domain.enums.AssignmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<AssignmentResponse> list(UUID sessionId, UUID memberId, UUID teamId, AssignmentStatus status) {
        if (sessionId != null) return assignmentRepository.findBySessionId(sessionId).stream().map(this::toResponse).toList();
        if (memberId  != null) return assignmentRepository.findByMemberId(memberId).stream().map(this::toResponse).toList();
        if (teamId    != null) return assignmentRepository.findByTeamId(teamId).stream().map(this::toResponse).toList();
        if (status    != null) return assignmentRepository.findByStatus(status).stream().map(this::toResponse).toList();
        return assignmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public AssignmentResponse create(AssignmentCreateRequest req) {
        AssignmentEntity e = new AssignmentEntity();
        apply(e, req);
        AssignmentEntity saved = assignmentRepository.save(e);
        outboxPublisher.publish("assignment", saved.getId(), "assignment.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public AssignmentResponse updateStatus(UUID id, AssignmentStatusUpdateRequest req) {
        AssignmentEntity e = getEntity(id);
        e.setStatus(req.status());
        AssignmentEntity saved = assignmentRepository.save(e);
        outboxPublisher.publish("assignment", saved.getId(), "assignment.status_changed", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        AssignmentEntity e = getEntity(id);
        assignmentRepository.delete(e);
        outboxPublisher.publish("assignment", id, "assignment.deleted", "{}");
    }

    private AssignmentEntity getEntity(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));
    }

    private void apply(AssignmentEntity e, AssignmentCreateRequest req) {
        e.setSessionId(req.sessionId());
        e.setMemberId(req.memberId());
        e.setTeamId(req.teamId());
        e.setRole(req.role());
        e.setShiftStart(req.shiftStart());
        e.setShiftEnd(req.shiftEnd());
        e.setSource(req.source());
        e.setStatus(req.status());
        e.setFairnessCreditDays(req.fairnessCreditDays());
    }

    private AssignmentResponse toResponse(AssignmentEntity e) {
        return new AssignmentResponse(e.getId(), e.getSessionId(), e.getMemberId(), e.getTeamId(),
                e.getRole(), e.getShiftStart(), e.getShiftEnd(), e.getSource(), e.getStatus(),
                e.getFairnessCreditDays(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
