package com.oncall.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;

/**
 * A time-bounded delegation of approval authority from one manager to another.
 * The delegate may approve/reject any requests that the original manager could act on,
 * for the duration of the active delegation window.
 *
 * Delegation is not recursive: a delegate cannot create further delegations.
 *
 * Relationships:
 * - ManyToOne to {@link Member}: fromManager (delegator), toManager (delegate).
 */
@Entity
@Table(name = "manager_delegations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"fromManager", "toManager"})
public class ManagerDelegation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_manager_id", nullable = false)
    private Member fromManager;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_manager_id", nullable = false)
    private Member toManager;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
