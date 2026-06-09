package com.oncall.domain.entity;

import com.oncall.domain.enums.NotificationChannel;
import com.oncall.domain.enums.NotificationEventType;
import com.oncall.domain.enums.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * A queued or delivered notification to a specific member via a specific channel.
 * Created by background jobs for scheduled reminders and by event handlers for
 * immediate notifications.
 *
 * Relationships:
 * - ManyToOne to {@link Member} (recipient).
 */
@Entity
@Table(name = "notification_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"recipient"})
public class NotificationEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Member recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /**
     * JSON payload with event-specific context:
     * shift date/time, timezone, direct action link, contact info.
     */
    @Column(columnDefinition = "TEXT")
    private String payload;
}
