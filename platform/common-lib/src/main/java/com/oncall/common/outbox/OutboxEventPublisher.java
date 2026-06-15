package com.oncall.common.outbox;

import java.util.UUID;

/**
 * Contract for persisting transactional outbox events.
 *
 * <p>SOLID notes:</p>
 * <ul>
 *   <li>DIP — service-layer classes depend on this abstraction, never on the
 *       concrete {@link OutboxPublisher} implementation. This allows the
 *       persistence strategy (polling vs. CDC, sync vs. async) to vary
 *       independently of business logic.</li>
 *   <li>ISP — the interface is intentionally single-method; callers are not
 *       forced to implement or depend on publication-lifecycle methods they do
 *       not need.</li>
 * </ul>
 */
public interface OutboxEventPublisher {

    /**
     * Persists an outbox event in the current transaction.
     *
     * @param aggregateType domain entity type (e.g. {@code "team"})
     * @param aggregateId   primary key of the affected aggregate
     * @param eventType     dot-separated event name (e.g. {@code "team.created"})
     * @param payload       serialisable object that becomes the JSON payload
     */
    void publish(String aggregateType, UUID aggregateId, String eventType, Object payload);
}
