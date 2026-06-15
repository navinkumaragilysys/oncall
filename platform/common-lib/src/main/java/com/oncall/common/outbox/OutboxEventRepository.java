package com.oncall.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Data-access contract for {@link OutboxEvent} rows.
 *
 * <p>ISP: the interface is intentionally narrow — services only ever need to
 * save outbox events. The inherited {@code JpaRepository} methods cover all
 * required operations without forcing callers to depend on query methods they
 * never use.</p>
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
}
