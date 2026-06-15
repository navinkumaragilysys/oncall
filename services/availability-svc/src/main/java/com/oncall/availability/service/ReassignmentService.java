package com.oncall.availability.service;

import com.oncall.availability.dto.request.ReassignmentCreate;
import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.response.ReassignmentResponse;
import com.oncall.availability.entity.MidweekReassignmentEntity;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.availability.repository.MidweekReassignmentRepository;
import com.oncall.domain.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class ReassignmentService {
    private final MidweekReassignmentRepository repo;
    private final OutboxEventPublisher outbox;

    @Transactional(readOnly = true)
    public List<ReassignmentResponse> list(UUID assignmentId) {
        return (assignmentId != null ? repo.findByAssignmentId(assignmentId) : repo.findAll()).stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public ReassignmentResponse getById(UUID id) { return toResp(get(id)); }

    @Transactional
    public ReassignmentResponse create(ReassignmentCreate req) {
        MidweekReassignmentEntity e = new MidweekReassignmentEntity();
        e.setAssignmentId(req.assignmentId()); e.setRequestedById(req.requestedById());
        e.setOriginalMemberId(req.originalMemberId()); e.setManagerId(req.managerId());
        e.setReasonCategory(req.reasonCategory()); e.setDescription(req.description());
        e.setRequestedAt(req.requestedAt()); e.setStatus(RequestStatus.PENDING);
        MidweekReassignmentEntity saved = repo.save(e);
        outbox.publish("reassignment", saved.getId(), "reassignment.created", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public ReassignmentResponse updateStatus(UUID id, StatusUpdateRequest req) {
        MidweekReassignmentEntity e = get(id); e.setStatus(req.status()); e.setRejectionReason(req.rejectionReason());
        MidweekReassignmentEntity saved = repo.save(e);
        outbox.publish("reassignment", saved.getId(), "reassignment.status_changed", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public void delete(UUID id) { repo.delete(get(id)); outbox.publish("reassignment", id, "reassignment.deleted", "{}"); }

    private MidweekReassignmentEntity get(UUID id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reassignment not found: " + id)); }

    private ReassignmentResponse toResp(MidweekReassignmentEntity e) {
        return new ReassignmentResponse(e.getId(), e.getAssignmentId(), e.getRequestedById(),
                e.getOriginalMemberId(), e.getReplacementMemberId(), e.getManagerId(),
                e.getReasonCategory(), e.getDescription(), e.getRequestedAt(), e.getEffectiveAt(),
                e.getStatus(), e.getRejectionReason(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
