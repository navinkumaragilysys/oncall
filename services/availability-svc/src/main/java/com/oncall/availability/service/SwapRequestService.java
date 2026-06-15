package com.oncall.availability.service;

import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.request.SwapRequestCreate;
import com.oncall.availability.dto.response.SwapRequestResponse;
import com.oncall.availability.entity.SwapRequestEntity;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.availability.repository.SwapRequestRepository;
import com.oncall.domain.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class SwapRequestService {
    private final SwapRequestRepository repo;
    private final OutboxEventPublisher outbox;

    @Transactional(readOnly = true)
    public List<SwapRequestResponse> list(UUID requestorId) {
        return (requestorId != null ? repo.findByRequestorId(requestorId) : repo.findAll()).stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public SwapRequestResponse getById(UUID id) { return toResp(get(id)); }

    @Transactional
    public SwapRequestResponse create(SwapRequestCreate req) {
        SwapRequestEntity e = new SwapRequestEntity();
        e.setRequestorId(req.requestorId()); e.setTargetMemberId(req.targetMemberId());
        e.setAssignmentId(req.assignmentId()); e.setManagerId(req.managerId());
        e.setReason(req.reason()); e.setStatus(RequestStatus.PENDING);
        SwapRequestEntity saved = repo.save(e);
        outbox.publish("swap_request", saved.getId(), "swap_request.created", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public SwapRequestResponse updateStatus(UUID id, StatusUpdateRequest req) {
        SwapRequestEntity e = get(id); e.setStatus(req.status()); e.setRejectionReason(req.rejectionReason());
        SwapRequestEntity saved = repo.save(e);
        outbox.publish("swap_request", saved.getId(), "swap_request.status_changed", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public void delete(UUID id) { repo.delete(get(id)); outbox.publish("swap_request", id, "swap_request.deleted", "{}"); }

    private SwapRequestEntity get(UUID id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Swap request not found: " + id)); }

    private SwapRequestResponse toResp(SwapRequestEntity e) {
        return new SwapRequestResponse(e.getId(), e.getRequestorId(), e.getTargetMemberId(),
                e.getAssignmentId(), e.getManagerId(), e.getReason(), e.getStatus(),
                e.getRejectionReason(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
