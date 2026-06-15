package com.oncall.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oncall.audit.dto.request.AuditEntryRequest;
import com.oncall.audit.dto.response.AuditTrailResponse;
import com.oncall.audit.entity.AuditTrailEntity;
import com.oncall.audit.repository.AuditTrailRepository;
import com.oncall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Manages the append-only audit trail.
 *
 * <p>SOLID notes:</p>
 * <ul>
 *   <li>SRP — records and queries audit events only; no side effects.</li>
 *   <li>OCP — new actor types or resource types are handled by adding enum values,
 *       not modifying this class.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditTrailRepository repo;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<AuditTrailResponse> listPaged(int page, int size) {
        return repo.findAllByOrderByOccurredAtDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditTrailResponse> listByActor(UUID actorId) {
        return repo.findByActorIdOrderByOccurredAtDesc(actorId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditTrailResponse> listByResource(String resourceType, UUID resourceId,
                                                    int page, int size) {
        return repo.findByResourceTypeAndResourceIdOrderByOccurredAtDesc(
                        resourceType, resourceId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AuditTrailResponse getById(UUID id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit entry not found: " + id)));
    }

    /** Appends one immutable audit entry. */
    @Transactional
    public AuditTrailResponse record(AuditEntryRequest req) {
        AuditTrailEntity entity = new AuditTrailEntity();
        entity.setId(UUID.randomUUID());
        entity.setActorId(req.actorId());
        entity.setActorType(req.actorType());
        entity.setAction(req.action());
        entity.setResourceType(req.resourceType());
        entity.setResourceId(req.resourceId());
        entity.setIpAddress(req.ipAddress());
        entity.setServiceName(req.serviceName());

        if (req.oldValue() != null) {
            try { entity.setOldValue(objectMapper.readTree(req.oldValue())); }
            catch (Exception ignored) { /* store as-is null */ }
        }
        if (req.newValue() != null) {
            try { entity.setNewValue(objectMapper.readTree(req.newValue())); }
            catch (Exception ignored) { /* store as-is null */ }
        }

        return toResponse(repo.save(entity));
    }

    private AuditTrailResponse toResponse(AuditTrailEntity e) {
        return new AuditTrailResponse(
                e.getId(), e.getActorId(), e.getActorType(), e.getAction(),
                e.getResourceType(), e.getResourceId(), e.getOldValue(), e.getNewValue(),
                e.getIpAddress(), e.getServiceName(), e.getOccurredAt()
        );
    }
}
