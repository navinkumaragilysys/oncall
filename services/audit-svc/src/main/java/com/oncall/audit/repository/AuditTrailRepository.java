package com.oncall.audit.repository;

import com.oncall.audit.entity.AuditTrailEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * ISP: exposes only the query methods actually needed by the service layer.
 */
public interface AuditTrailRepository extends JpaRepository<AuditTrailEntity, UUID> {

    List<AuditTrailEntity> findByActorIdOrderByOccurredAtDesc(UUID actorId);

    Page<AuditTrailEntity> findByResourceTypeAndResourceIdOrderByOccurredAtDesc(
            String resourceType, UUID resourceId, Pageable pageable);

    Page<AuditTrailEntity> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
