package com.oncall.teampolicy.repository;

import com.oncall.domain.entity.RotationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RotationPolicyRepository extends JpaRepository<RotationPolicy, UUID> {
}
