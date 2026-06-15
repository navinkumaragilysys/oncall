package com.oncall.teampolicy.service;

import com.oncall.domain.entity.RotationPolicy;
import com.oncall.teampolicy.dto.request.RotationPolicyUpsertRequest;
import com.oncall.teampolicy.dto.response.RotationPolicyResponse;
import com.oncall.teampolicy.exception.ResourceNotFoundException;
import com.oncall.teampolicy.outbox.OutboxPublisher;
import com.oncall.teampolicy.repository.RotationPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RotationPolicyService {

    private final RotationPolicyRepository rotationPolicyRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<RotationPolicyResponse> list() {
        return rotationPolicyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RotationPolicyResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public RotationPolicyResponse create(RotationPolicyUpsertRequest req) {
        RotationPolicy policy = new RotationPolicy();
        apply(policy, req);
        RotationPolicy saved = rotationPolicyRepository.save(policy);
        outboxPublisher.publish("rotation_policy", saved.getId(), "rotation_policy.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public RotationPolicyResponse update(UUID id, RotationPolicyUpsertRequest req) {
        RotationPolicy policy = getEntity(id);
        apply(policy, req);
        RotationPolicy saved = rotationPolicyRepository.save(policy);
        outboxPublisher.publish("rotation_policy", saved.getId(), "rotation_policy.updated", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        RotationPolicy policy = getEntity(id);
        rotationPolicyRepository.delete(policy);
        outboxPublisher.publish("rotation_policy", id, "rotation_policy.deleted", "{}");
    }

    public RotationPolicy getEntity(UUID id) {
        return rotationPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rotation policy not found: " + id));
    }

    private void apply(RotationPolicy policy, RotationPolicyUpsertRequest req) {
        policy.setName(req.name());
        policy.setStrategy(req.strategy());
        policy.setRotationLengthDays(req.rotationLengthDays());
        policy.setMinGapDays(req.minGapDays());
        policy.setWeekendPolicySeparate(req.weekendPolicySeparate());
        policy.setSecondaryEnabled(req.secondaryEnabled());
        policy.setSecondaryFairnessWeight(req.secondaryFairnessWeight());
        policy.setSameTeamAllowed(req.sameTeamAllowed());
        policy.setHorizonMonths(req.horizonMonths());
        policy.setAllowMultiSubsessionPerWeek(req.allowMultiSubsessionPerWeek());
    }

    private RotationPolicyResponse toResponse(RotationPolicy p) {
        return new RotationPolicyResponse(
                p.getId(),
                p.getName(),
                p.getStrategy(),
                p.getRotationLengthDays(),
                p.getMinGapDays(),
                p.isWeekendPolicySeparate(),
                p.isSecondaryEnabled(),
                p.getSecondaryFairnessWeight(),
                p.isSameTeamAllowed(),
                p.getHorizonMonths(),
                p.isAllowMultiSubsessionPerWeek(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
