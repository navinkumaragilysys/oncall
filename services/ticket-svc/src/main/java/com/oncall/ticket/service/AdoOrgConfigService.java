package com.oncall.ticket.service;

import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.ticket.dto.request.AdoOrgConfigRequest;
import com.oncall.ticket.dto.response.AdoOrgConfigResponse;
import com.oncall.ticket.entity.AdoOrgConfigEntity;
import com.oncall.ticket.repository.AdoOrgConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * SRP: manages ADO org configuration only.
 */
@Service
@RequiredArgsConstructor
public class AdoOrgConfigService {

    private final AdoOrgConfigRepository repo;

    @Transactional(readOnly = true)
    public List<AdoOrgConfigResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdoOrgConfigResponse getById(UUID id) {
        return toResponse(fetch(id));
    }

    @Transactional
    public AdoOrgConfigResponse create(AdoOrgConfigRequest req) {
        AdoOrgConfigEntity entity = new AdoOrgConfigEntity();
        entity.setId(UUID.randomUUID());
        entity.setTeamId(req.teamId());
        entity.setOrgUrl(req.orgUrl());
        entity.setProject(req.project());
        entity.setAreaPath(req.areaPath());
        entity.setIterationPath(req.iterationPath());
        entity.setDefaultWorkItemType(req.defaultWorkItemType() != null ? req.defaultWorkItemType() : "Task");
        entity.setActive(true);
        return toResponse(repo.save(entity));
    }

    @Transactional
    public AdoOrgConfigResponse deactivate(UUID id) {
        AdoOrgConfigEntity entity = fetch(id);
        entity.setActive(false);
        return toResponse(repo.save(entity));
    }

    private AdoOrgConfigEntity fetch(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ADO config not found: " + id));
    }

    private AdoOrgConfigResponse toResponse(AdoOrgConfigEntity e) {
        return new AdoOrgConfigResponse(
                e.getId(), e.getTeamId(), e.getOrgUrl(), e.getProject(),
                e.getAreaPath(), e.getIterationPath(), e.getDefaultWorkItemType(),
                e.isActive(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
