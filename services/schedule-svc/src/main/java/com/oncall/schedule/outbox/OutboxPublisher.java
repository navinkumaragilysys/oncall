package com.oncall.schedule.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oncall.schedule.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

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
