package com.oncall.identity.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Polling-based outbox publisher.
 *
 * Runs every 500ms, reads up to 100 unpublished rows (SKIP LOCKED so
 * multiple instances don't double-publish), sends each to Kafka, then
 * marks the row as published — all in the same transaction.
 *
 * In production this is complemented / replaced by Debezium CDC which
 * offers lower latency, but the polling publisher works standalone and
 * acts as a fallback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findUnpublishedWithLock();
        if (pending.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pending) {
            try {
                // Key = aggregateId so events for the same entity go to the same partition
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to publish outbox event id={} type={}: {}",
                                        event.getId(), event.getEventType(), ex.getMessage());
                            }
                        });

                event.setPublished(true);
                event.setPublishedAt(Instant.now());
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                log.warn("Outbox publish attempt failed id={} retries={}: {}",
                        event.getId(), event.getRetryCount(), e.getMessage());
            }
        }

        outboxRepository.saveAll(pending);
    }

    /**
     * Convenience factory used by service layer to build and save an outbox row
     * within the same business transaction.
     */
    @Transactional
    public OutboxEvent save(String aggregateType, String aggregateId,
                            String eventType, String topic, String payloadJson) {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .topic(topic)
                .payload(payloadJson)
                .published(false)
                .createdAt(Instant.now())
                .build();
        return outboxRepository.save(event);
    }
}
