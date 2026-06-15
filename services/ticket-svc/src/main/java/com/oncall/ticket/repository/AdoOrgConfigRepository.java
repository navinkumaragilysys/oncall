package com.oncall.ticket.repository;

import com.oncall.ticket.entity.AdoOrgConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdoOrgConfigRepository extends JpaRepository<AdoOrgConfigEntity, UUID> {
    Optional<AdoOrgConfigEntity> findByTeamId(UUID teamId);
}
