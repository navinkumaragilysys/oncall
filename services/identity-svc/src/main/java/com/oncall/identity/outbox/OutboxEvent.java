package com.oncall.identity.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbox table for the identity service.
 *
 * Every CUD operation on Member / TeamMembership / NotificationPreference writes
 * one row here in the SAME database transaction as the domain entity change.
 * The {@link OutboxPublisher} polls this table and publishes to Kafka, ensuring
 * at-least-once delivery without any risk of "committed but not published" events.
 *
 * SELECT FOR UPDATE SKIP LOCKED is used so multiple identity-svc instances
 * don't double-publish the same row.
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    /** Domain aggregate type: "Member", "TeamMembership", "NotificationPreference". */
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    /** String form of the aggregate root's primary key (UUID). */
    @Column(name = "aggregate_id", nullable = false, length = 255)
    private String aggregateId;

    /** e.g. "MemberCreated", "MemberUpdated", "MemberDeactivated". */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /** Kafka topic: "oncall.identity.events". */
    @Column(nullable = false, length = 255)
    private String topic;

    /** JSON-serialised event payload. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;
}
