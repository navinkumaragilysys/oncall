package com.oncall.domain.entity;

import com.oncall.domain.enums.HandoverStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the handover lifecycle between the outgoing group and the incoming group
 * of on-call engineers. Each {@link OnCallSession} has at most one Handover record.
 *
 * Participant model (replaces fixed outgoingPrimary / incomingPrimary columns):
 * - Any number of OUTGOING participants contribute notes via {@link HandoverParticipant}.
 * - Any number of INCOMING participants must acknowledge via {@link HandoverParticipant}.
 * - Status advances to SUBMITTED when at least one OUTGOING participant submits.
 * - Status advances to ACKNOWLEDGED when ALL INCOMING participants acknowledge.
 *
 * Relationships:
 * - OneToOne to {@link OnCallSession}.
 * - OneToMany to {@link HandoverParticipant} (the full outgoing + incoming groups).
 */
@Entity
@Table(name = "handovers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"session", "participants"})
public class Handover extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private OnCallSession session;

    /**
     * All participants in this handover session.
     * Filter by {@link HandoverParticipant#role} to get OUTGOING or INCOMING groups.
     */
    @OneToMany(mappedBy = "handover", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HandoverParticipant> participants = new ArrayList<>();

    /** Deadline by which the handover must be submitted and acknowledged. */
    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    /**
     * Timestamp of the first OUTGOING participant submission.
     * Subsequent submissions update individual {@link HandoverParticipant#actedAt} only.
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * Timestamp when the last required INCOMING participant acknowledged.
     * At this point the handover transitions to ACKNOWLEDGED.
     */
    @Column(name = "fully_acknowledged_at")
    private LocalDateTime fullyAcknowledgedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandoverStatus status;

    /**
     * Shared JSON blob for the structured handover template:
     * open incidents, risks/watch items, pending actions, runbooks, region-specific notes.
     * Individual participant notes live on {@link HandoverParticipant#participantNotes}.
     */
    @Column(name = "template_data", columnDefinition = "TEXT")
    private String templateData;
}
