package com.oncall.identity.repository;

import com.oncall.domain.entity.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMembershipRepository extends JpaRepository<TeamMembership, UUID> {

    List<TeamMembership> findByMemberId(UUID memberId);

    List<TeamMembership> findByTeamId(UUID teamId);

    Optional<TeamMembership> findByMemberIdAndTeamId(UUID memberId, UUID teamId);

    boolean existsByMemberIdAndTeamId(UUID memberId, UUID teamId);

    void deleteByMemberIdAndTeamId(UUID memberId, UUID teamId);
}
