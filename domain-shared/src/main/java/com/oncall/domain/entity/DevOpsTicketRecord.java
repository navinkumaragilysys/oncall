package com.oncall.domain.entity;

import com.oncall.domain.enums.TicketLinkSource;
import com.oncall.domain.enums.TicketStatus;
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
 * Records an Azure DevOps work item handled by a member during an on-call work
 * log session.
 *
 * <p><strong>ADO integration (Agilysys-Inc/tracker pattern):</strong>
 * Work items are fetched using the ADO REST API via
 * {@code POST /{project}/_apis/wit/workitemsbatch} with up to 200 IDs per batch.
 * The following fields are stored from {@code DEFAULT_WORK_ITEM_FIELDS}:
 * <pre>
 *   System.Id, System.Title, System.State, System.AssignedTo,
 *   System.WorkItemType, System.CreatedDate, System.ChangedDate,
 *   System.AreaPath, System.IterationPath, System.Tags,
 *   System.Description, Microsoft.VSTS.Common.Priority,
 *   Microsoft.VSTS.Common.Severity, Microsoft.VSTS.Scheduling.TargetDate,
 *   Microsoft.VSTS.Scheduling.DueDate
 * </pre>
 *
 * <p><strong>Link / Unlink semantics:</strong>
 * <ul>
 *   <li><em>Link</em>: set {@code unlinkedAt = null}, persist the record.
 *       The member is the one who performed the link action.</li>
 *   <li><em>Unlink</em>: set {@code unlinkedAt = now} and
 *       {@code unlinkedBy} to the acting member. The record is retained for
 *       audit; soft-deleted rows are excluded from active ticket queries by
 *       the service layer ({@code WHERE unlinked_at IS NULL}).</li>
 * </ul>
 *
 * <p>A ticket that spans multiple days produces one record per
 * {@link OnCallWorkLog} (i.e., per day), allowing per-day time tracking.
 * The same {@code adoTicketId} may therefore appear in multiple records
 * across different work logs within the same or different assignments.
 *
 * Relationships:
 * - ManyToOne to {@link OnCallWorkLog} (the day's session this ticket was worked in).
 * - ManyToOne to {@link OnCallAssignment} (the broader assignment week).
 * - ManyToOne to {@link Member} (the member who linked/worked the ticket).
 * - ManyToOne (nullable) to {@link Member} (who unlinked the ticket).
 */
@Entity
@Table(name = "devops_ticket_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"workLog", "assignment", "member", "unlinkedBy"})
public class DevOpsTicketRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_log_id", nullable = false)
    private OnCallWorkLog workLog;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private OnCallAssignment assignment;

    /** The member who linked this ticket to the work log session. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // ── ADO Identity ─────────────────────────────────────────────────────────

    /**
     * Azure DevOps work item ID (System.Id).
     * Opaque integer reference; not a foreign key in this system.
     */
    @Column(name = "ado_ticket_id", nullable = false)
    private String adoTicketId;

    /**
     * Deep-link URL to the ADO work item.
     * Pattern: {@code https://dev.azure.com/{org}/{project}/_workitems/edit/{id}}
     */
    @Column(name = "ado_ticket_url")
    private String adoTicketUrl;

    // ── Core ADO fields (from DEFAULT_WORK_ITEM_FIELDS in tracker) ─────────────

    /** System.Title */
    @Column(name = "ticket_title")
    private String ticketTitle;

    /** System.State — raw ADO state string, e.g. "Active", "Resolved", "Closed". */
    @Column(name = "ado_state")
    private String adoState;

    /** System.WorkItemType — e.g. "Bug", "Task", "User Story". */
    @Column(name = "work_item_type")
    private String workItemType;

    /**
     * System.AssignedTo — display name of the ADO-assigned person.
     * May differ from {@link #member} (the on-call engineer who linked it).
     */
    @Column(name = "ado_assigned_to")
    private String adoAssignedTo;

    /** System.AreaPath */
    @Column(name = "area_path")
    private String areaPath;

    /** System.IterationPath */
    @Column(name = "iteration_path")
    private String iterationPath;

    /** System.Tags — comma-separated tag string from ADO. */
    @Column(name = "ado_tags")
    private String adoTags;

    /** System.Description — stored as plain text (HTML stripped by the service layer). */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Microsoft.VSTS.Common.Priority — integer priority (1 = highest). */
    @Column(name = "priority")
    private Integer priority;

    /** Microsoft.VSTS.Common.Severity — severity label, e.g. "1 - Critical". */
    @Column(name = "severity")
    private String severity;

    /** System.CreatedDate — when the ADO work item was originally created. */
    @Column(name = "ado_created_date")
    private LocalDateTime adoCreatedDate;

    /** System.ChangedDate — last update timestamp in ADO. */
    @Column(name = "ado_changed_date")
    private LocalDateTime adoChangedDate;

    /** Microsoft.VSTS.Scheduling.TargetDate */
    @Column(name = "target_date")
    private LocalDateTime targetDate;

    /** Microsoft.VSTS.Scheduling.DueDate */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // ── On-call tracking fields ───────────────────────────────────────────────

    /** Status of this ticket as managed within the on-call platform. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    /**
     * Time spent on this ticket during this specific work session (minutes).
     * 0 = recorded but no explicit time logged.
     */
    @Column(name = "time_spent_minutes", nullable = false)
    private int timeSpentMinutes;

    /** How this ticket was associated with the work log session. */
    @Enumerated(EnumType.STRING)
    @Column(name = "link_source", nullable = false)
    private TicketLinkSource linkSource;

    /** Free-text notes from the on-call engineer about this ticket. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** When the ticket status moved to RESOLVED or CLOSED within this platform. */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // ── Unlink support ────────────────────────────────────────────────────────

    /**
     * Set to {@code now} when a member or manager unlinks this ticket from the
     * work log session. Null = still linked (active).
     * Service layer filters: {@code WHERE unlinked_at IS NULL} for active records.
     */
    @Column(name = "unlinked_at")
    private LocalDateTime unlinkedAt;

    /**
     * The member who performed the unlink action.
     * Null while the ticket is still linked.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unlinked_by_id")
    private Member unlinkedBy;
}
