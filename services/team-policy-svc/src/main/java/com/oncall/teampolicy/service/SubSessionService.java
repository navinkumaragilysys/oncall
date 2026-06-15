package com.oncall.teampolicy.service;

import com.oncall.domain.entity.SubSessionDefinition;
import com.oncall.teampolicy.dto.request.SubSessionUpsertRequest;
import com.oncall.teampolicy.dto.response.SubSessionResponse;
import com.oncall.teampolicy.exception.ResourceNotFoundException;
import com.oncall.teampolicy.outbox.OutboxPublisher;
import com.oncall.teampolicy.repository.SubSessionDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubSessionService {

    private final SubSessionDefinitionRepository subSessionDefinitionRepository;
    private final RotationPolicyService rotationPolicyService;
    private final OutboxPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<SubSessionResponse> list(UUID policyId) {
        List<SubSessionDefinition> rows = policyId == null
                ? subSessionDefinitionRepository.findAll()
                : subSessionDefinitionRepository.findByPolicyId(policyId);
        return rows.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubSessionResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public SubSessionResponse create(SubSessionUpsertRequest req) {
        SubSessionDefinition row = new SubSessionDefinition();
        apply(row, req);
        SubSessionDefinition saved = subSessionDefinitionRepository.save(row);
        outboxPublisher.publish("sub_session", saved.getId(), "sub_session.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public SubSessionResponse update(UUID id, SubSessionUpsertRequest req) {
        SubSessionDefinition row = getEntity(id);
        apply(row, req);
        SubSessionDefinition saved = subSessionDefinitionRepository.save(row);
        outboxPublisher.publish("sub_session", saved.getId(), "sub_session.updated", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        SubSessionDefinition row = getEntity(id);
        subSessionDefinitionRepository.delete(row);
        outboxPublisher.publish("sub_session", id, "sub_session.deleted", "{}");
    }

    private SubSessionDefinition getEntity(UUID id) {
        return subSessionDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-session not found: " + id));
    }

    private void apply(SubSessionDefinition row, SubSessionUpsertRequest req) {
        row.setPolicy(rotationPolicyService.getEntity(req.policyId()));
        row.setName(req.name());
        row.setOffsetStartDays(req.offsetStartDays());
        row.setOffsetStartTime(req.offsetStartTime());
        row.setOffsetEndDays(req.offsetEndDays());
        row.setOffsetEndTime(req.offsetEndTime());
        row.setOrdinal(req.ordinal());
    }

    private SubSessionResponse toResponse(SubSessionDefinition row) {
        return new SubSessionResponse(
                row.getId(),
                row.getPolicy().getId(),
                row.getName(),
                row.getOffsetStartDays(),
                row.getOffsetStartTime(),
                row.getOffsetEndDays(),
                row.getOffsetEndTime(),
                row.getOrdinal(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
