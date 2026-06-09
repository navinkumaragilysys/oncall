package com.oncall.domain.entity;

import com.oncall.domain.enums.RequestStatus;
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
 * A request by an engineer to exchange their on-call assignment with a target member.
 * Requires manager approval before the swap takes effect.
 *
 * Relationships:
 * - ManyToOne to {@link Member}: requestor, targetMember, manager.
 * - ManyToOne to {@link OnCallAssignment}: the assignment being swapped.
 */
@Entity
@Table(name = "swap_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"requestor", "targetMember", "assignment", "manager"})
public class SwapRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requestor_id", nullable = false)
    private Member requestor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_member_id", nullable = false)
    private Member targetMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private OnCallAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;
}
