package com.oncall.identity.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Fetches up to {@code limit} unpublished events ordered by creation time.
     * SKIP LOCKED ensures concurrent identity-svc instances don't race on the same rows.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // SKIP LOCKED
    @Query("SELECT e FROM OutboxEvent e WHERE e.published = false ORDER BY e.createdAt ASC LIMIT 100")
    List<OutboxEvent> findUnpublishedWithLock();
}
