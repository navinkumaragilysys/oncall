package com.oncall.approval.service;

import com.oncall.approval.dto.request.DelegationCreateRequest;
import com.oncall.approval.dto.response.DelegationResponse;
import com.oncall.approval.entity.ApprovalDelegationEntity;
import com.oncall.approval.repository.ApprovalDelegationRepository;
import com.oncall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Manages approval delegations.
 *
 * <p>SRP: solely manages the delegation lifecycle; does not touch approval requests.</p>
 */
@Service
@RequiredArgsConstructor
public class DelegationService {

    private final ApprovalDelegationRepository repo;

    @Transactional(readOnly = true)
    public List<DelegationResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DelegationResponse> listByDelegator(UUID delegatorId) {
        return repo.findByDelegatorId(delegatorId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DelegationResponse getById(UUID id) {
        return toResponse(fetch(id));
    }

    @Transactional
    public DelegationResponse create(DelegationCreateRequest req) {
        if (!req.validUntil().isAfter(req.validFrom())) {
            throw new IllegalArgumentException("validUntil must be after validFrom");
        }

        ApprovalDelegationEntity entity = new ApprovalDelegationEntity();
        entity.setId(UUID.randomUUID());
        entity.setDelegatorId(req.delegatorId());
        entity.setDelegateeId(req.delegateeId());
        entity.setValidFrom(req.validFrom());
        entity.setValidUntil(req.validUntil());
        entity.setActive(true);

        return toResponse(repo.save(entity));
    }

    /** Revokes a delegation by marking it inactive. */
    @Transactional
    public DelegationResponse revoke(UUID id) {
        ApprovalDelegationEntity entity = fetch(id);
        entity.setActive(false);
        return toResponse(repo.save(entity));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ApprovalDelegationEntity fetch(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delegation not found: " + id));
    }

    private DelegationResponse toResponse(ApprovalDelegationEntity e) {
        return new DelegationResponse(
                e.getId(), e.getDelegatorId(), e.getDelegateeId(),
                e.getValidFrom(), e.getValidUntil(), e.isActive(), e.getCreatedAt()
        );
    }
}
