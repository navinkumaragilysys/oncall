package com.oncall.availability.repository;

import com.oncall.availability.entity.EmergencyOOOEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmergencyOOORepository extends JpaRepository<EmergencyOOOEntity, UUID> {
    List<EmergencyOOOEntity> findByMemberId(UUID memberId);
}
