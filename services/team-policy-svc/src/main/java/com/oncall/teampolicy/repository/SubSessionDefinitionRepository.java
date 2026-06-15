package com.oncall.teampolicy.repository;

import com.oncall.domain.entity.SubSessionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubSessionDefinitionRepository extends JpaRepository<SubSessionDefinition, UUID> {
    List<SubSessionDefinition> findByPolicyId(UUID policyId);
}
