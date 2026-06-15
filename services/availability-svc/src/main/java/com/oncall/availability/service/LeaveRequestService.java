package com.oncall.availability.service;

import com.oncall.availability.dto.request.LeaveRequestCreate;
import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.response.LeaveRequestResponse;
import com.oncall.availability.entity.LeaveRequestEntity;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.availability.repository.LeaveRequestRepository;
import com.oncall.domain.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class LeaveRequestService {
    private final LeaveRequestRepository repo;
    private final OutboxEventPublisher outbox;

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> list(UUID memberId, RequestStatus status) {
        if (memberId != null) return repo.findByMemberId(memberId).stream().map(this::toResp).toList();
        if (status   != null) return repo.findByStatus(status).stream().map(this::toResp).toList();
        return repo.findAll().stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse getById(UUID id) { return toResp(get(id)); }

    @Transactional
    public LeaveRequestResponse create(LeaveRequestCreate req) {
        LeaveRequestEntity e = new LeaveRequestEntity();
        e.setMemberId(req.memberId()); e.setManagerId(req.managerId());
        e.setSuggestedReplacementId(req.suggestedReplacementId());
        e.setLeaveType(req.leaveType()); e.setStartDate(req.startDate());
        e.setEndDate(req.endDate()); e.setDescription(req.description());
        e.setStatus(RequestStatus.PENDING);
        LeaveRequestEntity saved = repo.save(e);
        outbox.publish("leave_request", saved.getId(), "leave_request.created", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public LeaveRequestResponse updateStatus(UUID id, StatusUpdateRequest req) {
        LeaveRequestEntity e = get(id); e.setStatus(req.status());
        e.setRejectionReason(req.rejectionReason());
        LeaveRequestEntity saved = repo.save(e);
        outbox.publish("leave_request", saved.getId(), "leave_request.status_changed", toResp(saved));
        return toResp(saved);
    }

    @Transactional
    public void delete(UUID id) { repo.delete(get(id)); outbox.publish("leave_request", id, "leave_request.deleted", "{}"); }

    private LeaveRequestEntity get(UUID id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id)); }

    private LeaveRequestResponse toResp(LeaveRequestEntity e) {
        return new LeaveRequestResponse(e.getId(), e.getMemberId(), e.getManagerId(),
                e.getSuggestedReplacementId(), e.getLeaveType(), e.getStartDate(), e.getEndDate(),
                e.getDescription(), e.getStatus(), e.getRejectionReason(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
