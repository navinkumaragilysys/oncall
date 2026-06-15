package com.oncall.teampolicy.repository;

import com.oncall.domain.entity.ShiftDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShiftDefinitionRepository extends JpaRepository<ShiftDefinition, UUID> {
}
