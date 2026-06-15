package com.oncall.availability.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oncall.availability.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component @RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxEventRepository repo;
    private final ObjectMapper mapper;

    public void publish(String aggregateType, UUID aggregateId, String eventType, Object payload) {
        OutboxEvent e = new OutboxEvent();
        e.setId(UUID.randomUUID()); e.setAggregateType(aggregateType); e.setAggregateId(aggregateId);
        e.setEventType(eventType); e.setPayload(mapper.valueToTree(payload));
        e.setOccurredAt(Instant.now()); e.setRetryCount(0);
        repo.save(e);
    }
}
