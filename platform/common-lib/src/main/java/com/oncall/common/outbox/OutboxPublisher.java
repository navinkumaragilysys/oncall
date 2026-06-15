package com.oncall.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Default implementation of {@link OutboxEventPublisher}.
 *
 * <p>Persists one {@link OutboxEvent} row within the caller's transaction so
 * that the business write and the event record succeed or fail atomically.
 * A separate relay (Debezium CDC or the identity-svc polling publisher) is
 * responsible for forwarding rows to Kafka.</p>
 *
 * <p>SRP: this class is solely responsible for building and saving outbox rows.
 * Kafka interaction is handled elsewhere.</p>
 */
@Component
@RequiredArgsConstructor
public class OutboxPublisher implements OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(String aggregateType, UUID aggregateId, String eventType, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(objectMapper.valueToTree(payload));
        event.setOccurredAt(Instant.now());
        event.setRetryCount(0);
        outboxEventRepository.save(event);
    }
}
