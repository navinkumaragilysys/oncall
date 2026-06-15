package com.oncall.teampolicy.service;

import com.oncall.domain.entity.Team;
import com.oncall.teampolicy.dto.request.TeamUpsertRequest;
import com.oncall.teampolicy.dto.response.TeamResponse;
import com.oncall.teampolicy.exception.ResourceNotFoundException;
import com.oncall.teampolicy.outbox.OutboxPublisher;
import com.oncall.teampolicy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final RotationPolicyService rotationPolicyService;
    private final OutboxPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<TeamResponse> list() {
        return teamRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public TeamResponse create(TeamUpsertRequest req) {
        Team team = new Team();
        apply(team, req);
        Team saved = teamRepository.save(team);
        outboxPublisher.publish("team", saved.getId(), "team.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public TeamResponse update(UUID id, TeamUpsertRequest req) {
        Team team = getEntity(id);
        apply(team, req);
        Team saved = teamRepository.save(team);
        outboxPublisher.publish("team", saved.getId(), "team.updated", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void deactivate(UUID id) {
        Team team = getEntity(id);
        team.setActive(false);
        teamRepository.save(team);
        outboxPublisher.publish("team", id, "team.deactivated", "{}");
    }

    private Team getEntity(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    private void apply(Team team, TeamUpsertRequest req) {
        team.setName(req.name());
        team.setRegion(req.region());
        team.setPolicy(req.policyId() == null ? null : rotationPolicyService.getEntity(req.policyId()));
        team.setMinEligibleThreshold(req.minEligibleThreshold());
        team.setActive(req.active());
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getRegion(),
                team.getPolicy() == null ? null : team.getPolicy().getId(),
                team.getMinEligibleThreshold(),
                team.isActive(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }
}
