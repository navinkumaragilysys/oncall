package com.oncall.domain.entity;

import com.oncall.domain.enums.HandoverParticipantRole;
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

import java.time.LocalDateTime;

/**
 * Join entity linking a {@link Handover} to one of its participants.
 *
 * A handover session has two groups of participants:
 * <ul>
 *   <li>OUTGOING – the engineers handing over responsibility.</li>
 *   <li>INCOMING – the engineers taking over responsibility.</li>
 * </ul>
 * Each group may contain multiple members (e.g. outgoing Primary + Secondary +
 * any additional shadow engineers participating in the session).
 *
 * Acknowledgement is tracked per participant:
 * - OUTGOING participants submit the handover (at least one must submit for the
 *   handover to transition to SUBMITTED).
 * - INCOMING participants acknowledge receipt (all must acknowledge for the
 *   handover to transition to ACKNOWLEDGED).
 *
 * Unique constraint: a member cannot appear in the same handover in the same role twice.
 *
 * Relationships:
 * - ManyToOne to {@link Handover}.
 * - ManyToOne to {@link Member}.
 */
@Entity
@Table(
        name = "handover_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"handover_id", "member_id", "role"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"handover", "member"})
public class HandoverParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "handover_id", nullable = false)
    private Handover handover;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandoverParticipantRole role;

    /**
     * True when this OUTGOING participant has submitted their portion of the
     * handover notes. At least one OUTGOING participant must submit for the
     * parent {@link Handover} status to advance to SUBMITTED.
     */
    @Column(name = "has_submitted", nullable = false)
    private boolean hasSubmitted;

    /**
     * Timestamp when this participant submitted their notes (OUTGOING),
     * or acknowledged the handover (INCOMING).
     * Null until the action is taken.
     */
    @Column(name = "acted_at")
    private LocalDateTime actedAt;

    /**
     * Freeform notes contributed by this individual participant.
     * For OUTGOING: their personal additions to the handover.
     * For INCOMING: optional acknowledgement comments.
     */
    @Column(name = "participant_notes", columnDefinition = "TEXT")
    private String participantNotes;
}
