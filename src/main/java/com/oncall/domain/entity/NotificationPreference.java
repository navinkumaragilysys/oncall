package com.oncall.domain.entity;

import com.oncall.domain.enums.NotificationChannel;
import com.oncall.domain.enums.NotificationEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Per-member, per-channel, per-event-type notification opt-in/opt-out setting.
 *
 * Rules from PRD:
 * - EMERGENCY_OOO_DECLARED and EMERGENCY_REPLACEMENT_SELECTED cannot be disabled.
 * - REMINDER_T24H cannot be disabled.
 * These are enforced at the service layer.
 *
 * Relationships:
 * - ManyToOne to {@link Member}.
 */
@Entity
@Table(
        name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "channel", "event_type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"member"})
public class NotificationPreference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private NotificationEventType eventType;

    @Column(nullable = false)
    private boolean enabled;
}
