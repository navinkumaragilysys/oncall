# PRD: Multi-Team On-Call Rotation Platform

## 1. Document Control
- Product: On-Call Rotation and Handover Platform
- Version: 1.2 (Primary/Secondary model, mid-week reassignment, shared sessions)
- Date: 2026-06-09
- Change Log v1.1: Added team member input, on-call history, allocation strategies, holiday/leave/OOO handling, manager approvals, and detailed notification/reminder requirements.
- Change Log v1.2: Added Primary/Secondary on-call roles per session, mid-week reassignment with manager approval, and shared on-call sessions within a single week.
- Owner: Engineering/Product Team
- Primary Tech Stack: Java Spring Boot (Backend), Angular (Frontend)

## 2. Problem Statement
Multiple teams contribute one engineer each to a shared on-call roster. On-call ownership rotates weekly, with formal handover to the next engineer. Rotation should ensure each engineer is assigned approximately once every 4 months, while also supporting weekend coverage and timezone-aware shift windows (including US Daylight Saving Time changes).

Current challenges to solve:
- Manual scheduling is error-prone and hard to maintain.
- Handover quality and accountability are inconsistent.
- DST and multi-region timings are hard to manage manually.
- Fairness (once in ~4 months) is difficult to enforce at scale.

## 3. Product Vision
Provide a reliable, auditable, and fair on-call scheduling platform that automates rotations, supports handovers, and gives teams clear visibility into current and upcoming coverage.

## 4. Goals and Non-Goals

### 4.1 Goals
- Automatically generate weekly on-call schedules across multiple teams.
- Ensure each team contributes one member per rotation cycle.
- Enforce fairness so each member is allocated roughly once every 4 months.
- Support weekday and weekend shifts with region/timezone and DST-aware timing.
- Provide a structured handover workflow between outgoing and incoming on-call members.
- Offer admin controls for overrides, leaves, swaps, and emergency reassignment.
- Provide reporting, auditability, and notifications.

### 4.2 Non-Goals (MVP)
- Incident management replacement (no full ticketing system).
- Pager/alert engine replacement (integrate with existing systems later).
- Payroll/compensation calculations.

## 5. Users and Personas

| Role | Identity | Primary Responsibility |
|---|---|---|
| **Admin** | Platform administrator | Full system configuration: teams, policies, holidays, rotation engine, system settings, all overrides |
| **Manager** | Team manager / Team Lead | Approve/reject leave, swaps, reassignments; add and remove team members from their teams |
| **OncallHost** | Designated on-call scheduler (one or more per team) | Assign members to on-call sessions, manage session roster, initiate mid-week reassignment, override session assignments |
| **Member** | Individual contributor / engineer | View own shifts and history, apply for leave with alternative suggestion, view team calendar (read-only) |

## 6. Core Business Rules
1. Rotation cadence:
- One on-call assignment duration is one week for primary ownership.
- At handover boundary, outgoing member transfers context to incoming member.

2. Team participation:
- Each participating team contributes one member during its assigned turn.
- Teams rotate continuously.

3. Fairness:
- Each member should be assigned roughly once every 4 months (configurable target interval in days).
- Assignment engine should avoid reassigning members before target interval unless required (insufficient staffing).

4. Weekend support:
- Weekend shifts must be explicitly covered and visible.
- Weekend timing must follow configured regional calendar rules.

5. Timezone and DST handling:
- Shift windows must support Standard Time and US Daylight Saving Time schedules.
- System must auto-switch effective timing rules based on calendar date.

6. Primary and Secondary on-call roles:
- Every on-call session has a designated Primary and an optional Secondary member.
- Primary is the first responder and main accountable person for the session.
- Secondary is the backup who takes over if Primary is unavailable, and may also handle shared session windows.
- Both Primary and Secondary receive shift notifications and have access to handover notes.

7. Shared on-call sessions:
- Within a single week-long rotation, the shift can be split into multiple sub-sessions.
- Each sub-session is independently assigned to a member (who may be Primary or Secondary for that window).
- Sub-sessions allow different people to handle different days or time windows within the same rotation week.
- Total coverage must be contiguous with no gaps across the full rotation week.

8. Mid-week reassignment:
- After a schedule is published, a booked assignment (Primary or Secondary) can be reassigned to another person due to unavailability during the active week.
- Mid-week reassignment always requires manager approval.
- The reassignment is tracked separately from emergency OOO to preserve the distinction between planned unavailability discovered late vs true emergencies.
- Partial credit is recorded for the original assignee up to the reassignment point.

9. Overrides and exceptions:
- Approved leave, swap, mid-week reassignment, and manual override must be supported.
- All overrides must be auditable.

## 7. On-Call Timing Calendar Requirements
The platform must support these exact operating windows.

### 7.1 Weekday Shift

#### During Standard Time
| Region | Hours | Time Zone |
|---|---|---|
| US | 6:00am-6:00pm | PST |
| US | 9:00am-9:00pm | EST |
| IDC | 7:30am-7:30pm | IST |

#### During US Daylight Saving Time
| Region | Hours | Time Zone |
|---|---|---|
| US | 7:00am-7:00pm | PDT |
| US | 10:00am-10:00pm | EDT |
| IDC | 7:30am-7:30pm | IST |

### 7.2 Weekend Shift

#### During Standard Time
| Region | Hours | Time Zone |
|---|---|---|
| US | Fri 6:00pm-Sat 6:00pm; Sat 6:00pm-Sun 6:00pm | PST |
| US | Fri 9:00pm-Sat 9:00pm; Sat 9:00pm-Sun 9:00pm | EST |
| IDC | Sat 7:30am-Sun 7:30am; Sun 7:30am-Mon 7:30am | IST |

#### During US Daylight Saving Time
| Region | Hours | Time Zone |
|---|---|---|
| US | Fri 7:00pm-Sat 7:00pm; Sat 7:00pm-Sun 7:00pm | PDT |
| US | Fri 10:00pm-Sat 10:00pm; Sat 10:00pm-Sun 10:00pm | EDT |
| IDC | Sat 7:30am-Sun 7:30am; Sun 7:30am-Mon 7:30am | IST |

### 7.3 Timing Logic Requirements
- Store canonical shift definitions with effective date ranges.
- Automatically pick Standard vs DST window by date.
- Display local times for user-selected timezone while preserving source schedule timezone.
- Prevent overlaps and gaps when converting across timezones.

## 8. Functional Requirements

### 8.1 Team Member Input and Onboarding (Prerequisites)
These inputs must be configured before any schedule can be generated.

#### 8.1.1 Member Profile
Each team member record must capture:
- Full name and display name.
- Email address (used for notifications and login).
- Region: US (PST/EST) or IDC (IST).
- Primary timezone.
- Team affiliation.
- Role: **Admin**, **Manager**, **OncallHost**, **Member**.
- Employment status: Active, On Leave, Deactivated.
- Oncall eligibility flag (can be toggled by manager/admin).
- Manager reference (who approves their leave/swap requests).
- Notification preferences: email, Slack handle (optional), phone (optional).
- Start date in the team (affects eligibility window calculation).

#### 8.1.2 Member Input Methods
- Manual form entry via Admin UI.
- Bulk CSV import with field validation and error report.
- Bulk update/deactivate via CSV.
- Exported member list for audit.

#### 8.1.3 Team Configuration
- Create/update/deactivate teams.
- Assign members to one or more teams.
- Set team on-call policy link (which rotation policy governs the team).
- Set minimum active eligible members threshold (warn if team falls below).

---

### 8.2 On-Call History per Member

#### 8.2.1 History Tracking
- Every completed or current on-call assignment is permanently recorded per member.
- Each history record captures:
  - Member ID and name.
  - Assignment start and end date/time (with timezone).
  - Shift type: Weekday, Weekend.
  - Region.
  - Assignment source: Generated, Manual Override, Emergency, Swap.
  - Handover status: Submitted/Acknowledged/Missed.
  - Any exceptions (leave approved mid-shift, emergency coverage, etc.).

#### 8.2.2 History Visibility
- Member can view their own full on-call history.
- Manager/Team Lead can view history of all team members.
- Admin can view cross-team history.
- History is used by the allocation engine to enforce fairness (last assignment date, count in period).
- History is immutable: corrections require a logged admin override entry.

#### 8.2.3 History Seeding
- Support bulk import of historical assignments (for teams migrating from manual scheduling).
- Imported history participates in fairness calculations.

---

### 8.3 Allocation Strategies

#### 8.3.1 Configurable Strategy Selection
Admin must be able to choose one allocation strategy per rotation policy from the following options:

| Strategy | Description |
|---|---|
| Round Robin | Teams/members take turns in a fixed cyclic order. |
| Least Recently Assigned | Member who has gone the longest without on-call is assigned next. |
| Weighted Fairness | Assignment probability weighted by last assignment date and opt-out count. |
| Manual Override Only | Schedule is entirely admin-managed; engine only validates, not assigns. |

#### 8.3.2 Fairness Constraints (All Strategies)
- Minimum gap enforcement: No member should be re-assigned before their configured minimum gap (default 4 months / ~120 days).
- If no eligible member exists within constraints, system issues a warning and enters constraint relaxation:
  - Step 1: Warn admin and notify manager.
  - Step 2: On admin approval, allow assignment of nearest-eligible member with gap < minimum.
  - Step 3: Document override reason in audit log.
- Track cumulative assignment count per member per calendar year.
- Track consecutive assignment prevention (no two consecutive weeks for the same person unless explicitly approved).

#### 8.3.3 Weekend Allocation
- Weekend shifts can be governed by a separate strategy and separate eligibility pool.
- Option to reuse same engineer as weekday (same week) or always assign a different engineer.
- Weekend assignments count toward the fairness gap independently (configurable: combine with weekday count or track separately).

#### 8.3.4 Eligibility Rules
An engineer is eligible for assignment if ALL of the following are true:
- On-call eligibility flag is active.
- No approved leave covering that week.
- No approved PTO/OOO covering that week.
- Minimum gap since last assignment is met (or admin override given).
- Not already assigned in same rotation period.

---

### 8.3.5 Primary and Secondary Allocation
- For each on-call session (weekday block or weekend block), the allocation engine assigns:
  - **Primary**: selected by the configured allocation strategy from the eligible pool.
  - **Secondary**: selected from the same or a different eligible pool (configurable per policy).
- Primary and Secondary must not be the same person.
- Primary and Secondary must not both be from the same team (configurable constraint).
- Both count independently toward fairness gap tracking.
- Secondary role counts as a lighter assignment: configurable fairness weight (for example 0.5x gap credit vs 1.0x for Primary).
- Admin can designate a standing backup list for Secondary assignments per team.

---

### 8.3.6 Shared Sessions Within a Rotation Week
- A rotation week can optionally be subdivided into multiple named sub-sessions (for example Monday-Wednesday, Thursday-Sunday).
- Each sub-session is an independent scheduling unit with its own Primary and Secondary.
- Sub-sessions are defined in the rotation policy by the admin.
- Constraints:
  - Sub-sessions must together cover the full rotation week without gaps or overlaps.
  - A member can be assigned to at most one sub-session within the same week (configurable: allow multiple with explicit approval).
  - Each sub-session has its own handover at the boundary.
- Fairness credit for a sub-session is proportional to the number of days in it relative to the full week.

---

### 8.4 Holiday Management

#### 8.4.1 Holiday Calendar
- Admin can maintain regional holiday calendars:
  - US Holidays (federal and company observed).
  - IDC Holidays (national and company observed).
- Holidays are stored with: date, name, region, type (public/company).
- Holidays do not automatically cancel shifts; they are input constraints to the scheduler.

#### 8.4.2 Holiday Impact on Scheduling
- When a shift falls on a regional holiday, scheduler flags it for admin review.
- Admin options per flagged holiday shift:
  - Assign backup/relief engineer.
  - Keep original assignment (engineer still on call, holiday acknowledged).
  - Mark as company holiday: shift auto-reassigned to next eligible member.
- Holiday coverage decisions are recorded in assignment record.

#### 8.4.3 Holiday Visibility
- Holidays are displayed on calendar with region badge.
- Engineers can see holidays in their region during schedule preview.
- Upcoming holiday notifications sent to on-call engineer and manager (T-3 days).

---

### 8.5 Ad-hoc Leave Management

#### 8.5.1 Leave Request
- Any engineer can submit an ad-hoc leave request specifying:
  - Start and end date.
  - Leave type: Sick, Personal, Emergency, Other.
  - Short description (optional).
  - Whether they have a preferred replacement (optional).
- Leave can be submitted at any time, including after schedule publication.

#### 8.5.2 Leave Approval
- Leave request is routed to the engineer's direct manager for approval.
- Manager can: Approve, Reject, or Request Modification.
- SLA: Manager should act within 24 hours (configurable); reminder sent if no action taken.
- On approval:
  - If shift not yet generated: member marked unavailable and excluded from allocation.
  - If shift already assigned: reassignment workflow triggers automatically (see Section 8.6.3).

#### 8.5.3 Leave Status Tracking
- Leave status states: Pending, Approved, Rejected, Withdrawn, Expired.
- Engineer and manager both receive status change notifications.
- Approved leaves are visible on the schedule calendar as unavailability blocks.
- Leave history is stored per member.

---

### 8.6 PTO and OOO Emergency Handling

#### 8.6.1 Scenario Definition
An emergency OOO event is when an engineer who is already on an active or upcoming on-call shift becomes unexpectedly unavailable (for example sudden illness, family emergency, unplanned travel) within less than 48 hours of the shift start or mid-shift.

#### 8.6.2 Emergency OOO Submission
- Engineer or their manager can mark a member as emergency OOO.
- Requires: start time, estimated return, reason (brief, free text).
- No waiting period: this triggers immediate action.

#### 8.6.3 Emergency Reassignment Workflow
1. System identifies the impacted shift.
2. System searches for next eligible replacement using allocation strategy with temporarily relaxed gap constraints.
3. System presents ranked replacement candidates to the manager.
4. Manager selects replacement or approves system recommendation.
5. Replacement engineer is notified immediately with full handover context.
6. Outgoing engineer's on-call history records emergency coverage event.
7. Replacement assignment is logged as source: Emergency.
8. Fairness impact is noted: replacement's gap resets; original engineer's gap is not reset (they were not available).

#### 8.6.4 Partial Shift Coverage
- Emergency OOO can happen mid-shift.
- System must support splitting a shift record: completed portion credited to original engineer, remaining portion to replacement.
- Handover notes from original engineer are carried over to replacement.

#### 8.6.5 Manager Visibility
- Manager receives instant notification when an emergency OOO is declared for any of their reports.
- Manager must confirm replacement selection within a configurable SLA (default 2 hours).
- If manager does not respond, system escalates to admin.

---

### 8.6.6 Mid-Week Reassignment (Non-Emergency)

#### Scenario
A mid-week reassignment occurs when an engineer is already booked for a published on-call week but becomes unavailable for a known reason (not a sudden emergency) — for example pre-approved travel that was not registered in time, or a project deadline discovered after scheduling.

#### Request Submission
- The engineer or their manager initiates a mid-week reassignment request specifying:
  - Which assignment (Primary or Secondary) is being reassigned.
  - From date/time: when the reassignment takes effect.
  - To date/time: end of original shift or a specific date if partial.
  - Reason: free text with category (Travel, Project Conflict, Personal, Other).
  - Preferred replacement member (optional).

#### Approval Requirement
- All mid-week reassignment requests require **manager approval** before taking effect.
- SLA: Manager must act within 4 hours (configurable).
- If no action within SLA: auto-escalate to admin.
- Approved reassignment immediately triggers replacement notification.

#### Reassignment Processing
1. Original assignment record is split at the reassignment effective time.
2. Original engineer is credited for the portion already completed.
3. Replacement engineer is allocated for the remaining period using the strategy pool with relaxed gap constraints (marked as source: MIDWEEK_REASSIGNMENT).
4. Replacement is notified with handover context from original engineer.
5. If original assignment was Primary: replacement becomes Primary for the remaining period; Secondary of the original session is notified.
6. If original assignment was Secondary: replacement takes the Secondary role.

#### Fairness Impact
- Original engineer's gap clock is reset proportionally based on completed coverage (for example if they covered 3 of 7 days, their gap resets by 3/7 of the full gap credit).
- Replacement engineer's gap resets for the portion they covered.

---

### 8.7 Manager Approval Workflows

#### 8.7.1 Approval Types
The following actions require manager approval:

| Action | Who Requests | Approver | SLA |
|---|---|---|---|
| Ad-hoc leave request | Engineer | Direct Manager | 24 hours |
| Emergency OOO declaration | Engineer or Manager | Direct Manager (or Admin if self) | 2 hours |
| Mid-week reassignment request | Engineer or Manager | Direct Manager | 4 hours |
| Shift swap request | Engineer | Direct Manager of requestor | 24 hours |
| Assignment override (manual) | Admin | Manager notification (not blocking) | N/A |
| Emergency replacement selection | System | Direct Manager | 2 hours |
| Constraint relaxation approval | System | Admin | 8 hours |
| Secondary role assignment change | Admin | Manager notification (not blocking) | N/A |

#### 8.7.2 Approval Workflow States
All approval items follow this state machine:
```
Pending -> Approved
Pending -> Rejected
Pending -> Expired (no action within SLA)
Approved -> Withdrawn (before effective date)
```

#### 8.7.3 Manager Approval Interface
- Dedicated Pending Approvals inbox in the UI.
- Email notification with inline Approve/Reject links (quick action without login, Phase 2).
- Approval deadline shown with countdown.
- Mandatory rejection reason field when rejecting.
- Bulk approval for non-emergency items.

#### 8.7.4 Approval Delegation
- Manager can delegate approval authority to another manager for a defined period (for example when manager is OOO).
- Delegation is time-bound and logged.
- Delegation is not recursive (delegated manager cannot re-delegate).

#### 8.7.5 Escalation
- If approval SLA expires for a non-emergency item: reminder to manager + notify admin.
- If approval SLA expires for an emergency item (2-hour SLA): auto-escalate to admin immediately.
- All escalation events are logged.

---

### 8.8 Team and Rotation Management
- Create/update/deactivate teams.
- Add/update/deactivate members.
- Map members to teams.
- Maintain member metadata: timezone, region, role, active status.
- Import/export members via CSV.

### 8.9 Rotation Policy Management
- Configure:
  - Rotation length (default 1 week).
  - Target member gap (default 4 months / 120 days).
  - Team sequence and participation rules.
  - Weekend policy on/off and regional windows.
  - Exception policy (leave, holidays, blackout dates).
  - Allocation strategy selection (see 8.3.1).
  - Minimum eligible members threshold per team.
  - Primary/Secondary assignment: enabled/disabled.
  - Secondary fairness weight (default 0.5).
  - Allow same team for Primary and Secondary: yes/no.
  - Sub-session definitions: list of named time blocks within the rotation week.
  - Allow member in multiple sub-sessions within one week: yes/no (default no).

### 8.10 Schedule Generation
- Generate future schedules for configurable horizon (for example 3-6 months).
- Apply selected allocation strategy and fairness constraints.
- Assign Primary and Secondary for each session (if policy enables it).
- Assign Primary and Secondary independently for each sub-session (if sub-sessions are configured).
- Detect and report conflicts:
  - Member leave/unavailable dates.
  - Holiday overlap (flag for review).
  - Duplicate assignment within minimum gap period.
  - Primary and Secondary are the same person.
  - Primary and Secondary are from the same team (if policy disallows it).
  - Sub-session gap or overlap within a rotation week.
  - Team falls below minimum eligible threshold (separate check for Primary pool and Secondary pool).
  - Incomplete handover dependency.
- Allow draft schedule review and admin approval before publish.
- Re-generation support: regenerate future portion without affecting published past weeks.

### 8.11 Handover Workflow
- Outgoing engineer submits handover notes using structured template:
  - Open incidents.
  - Risks and watch items.
  - Pending actions.
  - Important links and runbooks.
  - Notes for specific timezone region if applicable.
  - Notes for Secondary on-call member if applicable.
- Handover is delivered to:
  - Incoming Primary of the next session.
  - Incoming Secondary of the next session (read-only copy).
  - In a mid-week reassignment: handover is delivered immediately to the replacement.
- At sub-session boundaries, outgoing Primary of the sub-session must hand over to incoming Primary of the next sub-session.
- Incoming Primary acknowledges handover (required before session start).
- Incoming Secondary is notified and may optionally acknowledge.
- Track handover status per session: Pending, Submitted, Acknowledged, Overdue.
- Reminder to outgoing engineer 48 hours before shift end if handover not submitted.
- Escalation to manager if handover not acknowledged 2 hours before session start.

### 8.12 Shift View and Calendar
- Weekly and monthly calendar views.
- Current on-call banner: who is on call right now (per region), showing Primary and Secondary.
- Upcoming rotation view.
- Weekday and weekend shifts visually distinct.
- Sub-sessions displayed as distinct time blocks within the same week cell.
- Primary and Secondary labeled with visual role badges on the calendar.
- Mid-week reassignment events shown with a change indicator on the affected day.
- Holiday markers with region badge.
- Unavailability blocks (approved leave/OOO).
- Filter by team, member, region, role (Primary/Secondary), and date range.

### 8.13 Swap and Override Management
- Engineer can request swap with another eligible engineer.
- Manager approves or rejects swap.
- Admin can force-assign replacement for emergencies.
- All changes logged with reason and actor.

### 8.14 Notifications and Reminders

#### 8.14.1 Notification Recipients
| Event | Engineer | Manager | Admin |
|---|---|---|---|
| Assigned as Primary to on-call shift | Yes | Yes (informational) | No |
| Assigned as Secondary to on-call shift | Yes | Yes (informational) | No |
| Reminder T-48h before shift (Primary) | Yes | No | No |
| Reminder T-24h before shift (Primary) | Yes | No | No |
| Reminder T-24h before shift (Secondary) | Yes | No | No |
| Shift started | Yes (Primary + Secondary) | No | No |
| Sub-session boundary handover due | Yes (outgoing Primary) | No | No |
| Handover due in 48h (not submitted) | Yes (Primary) | No | No |
| Handover not acknowledged 2h before shift | Yes (incoming Primary) | Yes | No |
| Handover copy delivered to Secondary | Yes (Secondary, read-only) | No | No |
| Leave/swap request pending approval | No | Yes | No |
| Leave/swap approved or rejected | Yes | No | No |
| Mid-week reassignment requested | No | Yes | No |
| Mid-week reassignment approved | Yes (original + replacement) | Yes | No |
| Mid-week reassignment SLA expired | No | Yes | Yes |
| Emergency OOO declared | Yes (replacement) | Yes | Yes |
| Emergency replacement selected | Yes (replacement) | Yes | No |
| Secondary stepped up to Primary | Yes (Secondary) | Yes | No |
| Approval SLA expired | No | Yes | Yes |
| Uncovered shift detected | No | Yes | Yes |
| Holiday falling on assigned shift | Yes | Yes | No |

#### 8.14.2 Notification Content
- Each notification includes:
  - What the event is.
  - Shift date, time, and timezone.
  - Direct link to relevant action in the app.
  - Contact of current/incoming on-call engineer.

#### 8.14.3 Notification Channels (MVP)
- Email: all notifications.
- In-app notification bell with unread count.

#### 8.14.4 Notification Channels (Phase 2)
- Slack/Microsoft Teams integration.
- SMS for emergency OOO events.

#### 8.14.5 Notification Preferences
- Engineer can configure per-channel preference (email on/off, Slack on/off).
- Emergency OOO notifications cannot be disabled.
- Reminders: engineer can adjust reminder window (T-48h only, T-24h only, or both); T-24h cannot be disabled.

### 8.15 Reporting and Audit
- Coverage report by team/member/date (broken down by Primary vs Secondary).
- Fairness report: assignment count (Primary and Secondary weighted), last assignment date, gap analysis per member.
- Sub-session coverage report: which members handled which windows within each rotation week.
- Mid-week reassignment report: frequency, reasons, and time-to-approval.
- Handover compliance report (submitted on time / Primary acknowledged / Secondary notified).
- Override/swap/emergency/reassignment audit report.
- Leave and OOO history report.
- Manager approval SLA compliance report.
- Export reports (CSV, PDF optional in later phase).

## 9. Backend Requirements (Java Spring Boot)

### 9.1 Architecture
- Spring Boot REST API with layered architecture:
- Controller
- Service
- Repository
- Domain Model
- Scheduler/Rules Engine module for schedule creation.
- Relational database (PostgreSQL recommended).
- Flyway/Liquibase for schema migrations.

### 9.2 Suggested Domain Entities

| Entity | Key Attributes |
|---|---|
| Team | id, name, region, policyId, minEligibleThreshold, active |
| Member | id, name, email, teamId, managerId, region, timezone, eligibilityFlag, status, joinDate |
| NotificationPreference | memberId, channel, eventType, enabled |
| RotationPolicy | id, teamId, strategy, rotationLengthDays, minGapDays, weekendPolicySeparate, secondaryEnabled, secondaryFairnessWeight, sameTeamAllowed, horizonMonths |
| SubSessionDefinition | id, policyId, name, offsetStartDays, offsetStartTime, offsetEndDays, offsetEndTime, ordinal |
| ShiftDefinition | id, shiftType (WEEKDAY/WEEKEND), region, dstAware, standardStart, standardEnd, dstStart, dstEnd |
| OnCallSession | id, rotationWeekStart, rotationWeekEnd, policyId, teamId, subSessionDefinitionId (nullable), status (DRAFT/PUBLISHED/COMPLETED) |
| OnCallAssignment | id, sessionId, memberId, teamId, role (PRIMARY/SECONDARY), shiftStart, shiftEnd, source (GENERATED/OVERRIDE/EMERGENCY/SWAP/MIDWEEK_REASSIGNMENT), status, handoverId, fairnessCreditDays |
| OnCallHistory | id, memberId, assignmentId, periodStart, periodEnd, role, completionStatus, fairnessCreditDays, notes |
| Handover | id, sessionId, outgoingPrimaryId, incomingPrimaryId, outgoingSecondaryId (nullable), incomingSecondaryId (nullable), dueAt, submittedAt, primaryAcknowledgedAt, secondaryAcknowledgedAt, status, templateData (JSON) |
| MidweekReassignment | id, assignmentId, requestedBy, reason, reasonCategory, effectiveFrom, effectiveTo, originalMemberId, replacementMemberId, status, managerId, approvedAt |
| LeaveRequest | id, memberId, managerId, leaveType, startDate, endDate, status, reason, replacementMemberId |
| EmergencyOOO | id, memberId, reportedBy, startTime, estimatedReturn, reason, status, replacementMemberId |
| SwapRequest | id, requestorId, targetMemberId, assignmentId, managerId, status, reason |
| ApprovalRecord | id, entityType, entityId, approverId, action, actionAt, reason, delegatedFrom |
| ManagerDelegation | id, fromManagerId, toManagerId, startDate, endDate, active |
| HolidayCalendar | id, date, name, region, type (PUBLIC/COMPANY), impactOnShift |
| NotificationEvent | id, recipientId, eventType, channel, status, scheduledAt, sentAt, payload |
| AuditLog | id, actor, action, entityType, entityId, timestamp, before (JSON), after (JSON), reason |

### 9.3 API Requirements (High-Level)

#### Auth and Profiles
- `POST /auth/login`, `POST /auth/logout`, `POST /auth/refresh`
- `GET/PUT /users/me` (profile and notification preferences)

#### Team and Member Management
- `GET/POST /teams`, `GET/PUT/DELETE /teams/{id}`
- `GET/POST /teams/{id}/members`
- `GET/POST /members`, `GET/PUT/DELETE /members/{id}`
- `POST /members/import` (bulk CSV)
- `GET /members/{id}/history` (on-call history)
- `POST /members/history/import` (seed historical data)

#### Rotation Policy
- `GET/POST /policies`, `GET/PUT/DELETE /policies/{id}`
- `GET /policies/{id}/strategy-options`

#### Schedule, Sessions and Assignments
- `POST /schedules/generate` (draft schedule with policy + horizon)
- `GET /schedules/draft/{id}` (review draft)
- `POST /schedules/publish/{id}` (admin publish)
- `GET /sessions` (query sessions by team, date range, status)
- `GET /sessions/{id}` (session detail with Primary, Secondary, sub-sessions)
- `GET /assignments` (query by team, member, role, date range)
- `GET /assignments/current` (current Primary and Secondary per region)
- `GET /assignments/upcoming`
- `POST /assignments/{id}/override`
- `GET /policies/{id}/subsessions` (sub-session definitions)
- `POST /policies/{id}/subsessions`, `PUT /policies/{id}/subsessions/{subId}`

#### Handover
- `GET/POST /handovers`, `GET/PUT /handovers/{id}`
- `POST /handovers/{id}/submit`
- `POST /handovers/{id}/acknowledge`

#### Leave Management
- `GET/POST /leave-requests`, `GET/PUT /leave-requests/{id}`
- `POST /leave-requests/{id}/approve`, `POST /leave-requests/{id}/reject`
- `POST /leave-requests/{id}/withdraw`

#### Mid-Week Reassignment
- `POST /midweek-reassignments` (request reassignment for a booked assignment)
- `GET /midweek-reassignments/{id}/candidates` (ranked replacement candidates)
- `POST /midweek-reassignments/{id}/approve`
- `POST /midweek-reassignments/{id}/reject`
- `GET /midweek-reassignments` (query by assignment, member, status)

#### Emergency OOO
- `POST /emergency-ooo` (declare emergency)
- `GET /emergency-ooo/{id}/candidates` (replacement candidates)
- `POST /emergency-ooo/{id}/assign-replacement`

#### Swap
- `GET/POST /swaps`, `GET /swaps/{id}`
- `POST /swaps/{id}/approve`, `POST /swaps/{id}/reject`

#### Approvals
- `GET /approvals/pending` (manager inbox)
- `POST /approvals/{id}/approve`, `POST /approvals/{id}/reject`
- `GET/POST /delegations`, `DELETE /delegations/{id}`

#### Holiday Calendar
- `GET/POST /holidays`, `GET/PUT/DELETE /holidays/{id}`
- `POST /holidays/import`

#### Notifications
- `GET /notifications/me` (in-app inbox)
- `POST /notifications/{id}/read`
- `GET/PUT /notifications/preferences`

#### Reports
- `GET /reports/coverage`
- `GET /reports/fairness`
- `GET /reports/handover-compliance`
- `GET /reports/audit`
- `GET /reports/leave-ooo`
- `GET /reports/approval-sla`
- `GET /reports/*/export` (CSV)

### 9.4 Security

#### 9.4.1 Authentication
- JWT (JSON Web Token) with short-lived access tokens and refresh token rotation.
- OAuth2 / SSO integration (Azure AD, Okta, or Google Workspace — configurable).
- All API endpoints require a valid bearer token except the login and health endpoints.
- Token payload carries: `memberId`, `roles[]`, `teamIds[]`, `managerId`.

#### 9.4.2 Roles

| Role | Code | Description |
|---|---|---|
| Admin | `ROLE_ADMIN` | Full platform access; all configuration and override capabilities |
| Manager | `ROLE_MANAGER` | Approve/reject requests for own team members; add/remove members from own teams |
| OncallHost | `ROLE_ONCALL_HOST` | Assign members to sessions, manage session roster, initiate reassignment |
| Member | `ROLE_MEMBER` | View-only access to schedules and history; submit leave with alternative suggestion |

> A user may hold multiple roles simultaneously (e.g., a Manager who is also an OncallHost).

#### 9.4.3 Permission Matrix

| Capability | Admin | Manager | OncallHost | Member |
|---|:---:|:---:|:---:|:---:|
| **System & Policy** | | | | |
| Configure rotation policies | ✅ | ❌ | ❌ | ❌ |
| Configure shift definitions and DST windows | ✅ | ❌ | ❌ | ❌ |
| Manage holiday calendars | ✅ | ❌ | ❌ | ❌ |
| Configure sub-session definitions | ✅ | ❌ | ❌ | ❌ |
| Trigger schedule generation | ✅ | ❌ | ✅ (own team) | ❌ |
| Publish draft schedule | ✅ | ❌ | ❌ | ❌ |
| Force-assign / manual override any assignment | ✅ | ❌ | ❌ | ❌ |
| **Team & Member Management** | | | | |
| Create / deactivate teams | ✅ | ❌ | ❌ | ❌ |
| Add member to a team | ✅ | ✅ (own team) | ❌ | ❌ |
| Remove member from a team | ✅ | ✅ (own team) | ❌ | ❌ |
| Toggle member on-call eligibility flag | ✅ | ✅ (own team) | ❌ | ❌ |
| View all members cross-team | ✅ | ❌ | ✅ (for assignment) | ❌ |
| View own team members | ✅ | ✅ | ✅ | ❌ |
| Bulk CSV import / export members | ✅ | ❌ | ❌ | ❌ |
| Seed historical on-call data | ✅ | ❌ | ❌ | ❌ |
| **Session Assignment** | | | | |
| Assign Primary for a session | ✅ | ❌ | ✅ (own team) | ❌ |
| Assign Secondary for a session | ✅ | ❌ | ✅ (own team) | ❌ |
| Reassign Primary / Secondary (mid-week) | ✅ | ❌ | ✅ (initiates request, needs Manager approval) | ❌ |
| Override emergency replacement | ✅ | ✅ (approve/reject) | ❌ | ❌ |
| **Leave & Availability** | | | | |
| Submit leave request (self) | ✅ | ✅ | ✅ | ✅ |
| Suggest alternative member when requesting leave | ✅ | ✅ | ✅ | ✅ |
| Approve / reject leave request for own team | ✅ | ✅ | ❌ | ❌ |
| Declare emergency OOO for own report | ✅ | ✅ | ❌ | ❌ |
| Declare emergency OOO for self | ✅ | ✅ | ✅ | ✅ |
| Approve emergency replacement for own team | ✅ | ✅ | ❌ | ❌ |
| **Swaps** | | | | |
| Request shift swap (self) | ✅ | ✅ | ✅ | ✅ |
| Approve / reject swap for own team member | ✅ | ✅ | ❌ | ❌ |
| **Handover** | | | | |
| Submit handover notes (own shift) | ✅ | ✅ | ✅ | ✅ |
| Acknowledge handover (incoming Primary) | ✅ | ✅ | ✅ | ✅ |
| Read handover notes of any session | ✅ | ✅ | ✅ (own team sessions) | ❌ |
| Read handover notes of own session | ✅ | ✅ | ✅ | ✅ (Secondary copy) |
| **Approvals & Delegation** | | | | |
| View own pending approvals inbox | ✅ | ✅ | ❌ | ❌ |
| Approve any request cross-team | ✅ | ❌ | ❌ | ❌ |
| Delegate approval authority | ✅ | ✅ | ❌ | ❌ |
| **Reporting & Audit** | | | | |
| View full cross-team reports | ✅ | ❌ | ❌ | ❌ |
| View own team reports | ✅ | ✅ | ✅ (session/coverage) | ❌ |
| View own on-call history and fairness gap | ✅ | ✅ | ✅ | ✅ |
| Export reports | ✅ | ✅ (own team) | ❌ | ❌ |
| View audit log | ✅ | ❌ | ❌ | ❌ |
| **Calendar / Schedule View** | | | | |
| View full platform calendar | ✅ | ✅ | ✅ | ❌ |
| View own team calendar | ✅ | ✅ | ✅ | ✅ |
| View own assignments | ✅ | ✅ | ✅ | ✅ |

#### 9.4.4 Row-Level Data Scoping Rules
- **Manager**: All reads, writes, and approvals are scoped to teams they directly manage (`managerId` must match the member's `managerId`).
- **OncallHost**: All session assignment operations are scoped to teams they are designated host for. Cross-team member lookup is read-only for assignment purpose only.
- **Member**: All writes (leave request, emergency OOO self-declaration, swap request, handover) are scoped to their own records. Calendar/schedule reads are scoped to own team and own assignments.
- **Admin**: No scoping restrictions. All operations permitted.

#### 9.4.5 Sensitive Operations Requiring Audit Log Entry
The following operations always produce an `AuditLog` record with actor, before-state, after-state, and reason:
- Any assignment override or force-assign.
- Any mid-week reassignment (approval or rejection).
- Any emergency OOO declaration and replacement selection.
- Adding or removing a member from a team.
- Toggling on-call eligibility flag.
- Approving or rejecting any leave, swap, or reassignment request.
- Publishing a draft schedule.
- Approval delegation (create/revoke).
- Constraint relaxation approval.
- Admin history correction.

#### 9.4.6 Additional Security Controls
- All API responses must not include fields the calling role is not permitted to read (field-level filtering per role).
- Passwords / SSO tokens are never stored in application database; delegate to identity provider.
- Rate limiting on authentication endpoints (configurable threshold per IP).
- HTTPS enforced; HSTS header required in production.
- Sensitive PII fields (email, phone) encrypted at the column level in database.
- CSRF protection on all state-changing endpoints.
- Input validation and output encoding on all API boundaries to prevent injection.

### 9.5 Background Jobs
- Reminder notifications.
- Handover overdue checks.
- Precompute schedule windows.
- DST boundary recalculation checks.

## 10. Frontend Requirements (Angular)

### 10.1 Core Modules
- Authentication module.
- Dashboard module (role-aware: Admin/Manager/OncallHost/Member views differ).
- Schedule/Calendar module.
- Session Assignment module (OncallHost and Admin only).
- Handover module.
- Leave and Availability module.
- Team/member administration module (Admin and Manager).
- Approvals inbox module (Manager and Admin).
- Reports module (Admin and Manager; limited view for OncallHost).

### 10.2 Key Screens
- **Login**: SSO redirect and fallback local login. Post-login redirect based on role.
- **Dashboard** (role-adaptive):
  - *All roles*: Current Primary and Secondary on-call owner (per region), next handover, own upcoming shifts.
  - *Manager/Admin*: Pending approvals count badge, unacknowledged handovers, uncovered shift alerts.
  - *OncallHost*: Session roster gaps, upcoming sessions needing assignment.
- **Calendar screen** (week/month):
  - Primary and Secondary role badges per session.
  - Sub-session blocks within a rotation week.
  - Mid-week reassignment change indicator.
  - *Member*: own team calendar, own assignments highlighted; no cross-team data.
- **Session Assignment screen** *(OncallHost / Admin only)*:
  - Assign or change Primary and Secondary per session.
  - Initiate mid-week reassignment request.
  - View eligibility status and fairness gap for candidate members.
- **Handover detail and submission screen** (Primary submits; Secondary read-only copy).
- **Leave Request screen** *(all roles)*:
  - Date picker, leave type selector.
  - Alternative member suggestion field: searchable dropdown filtered to eligible teammates.
  - Status tracker (Pending / Approved / Rejected).
- **Approvals inbox** *(Manager / Admin only)*:
  - Tabbed view: Leave | Swap | Mid-week Reassignment | Emergency OOO.
  - SLA countdown per item; overdue items highlighted.
  - Approve / Reject with mandatory reason on rejection.
  - Bulk approve for non-emergency items.
- **Mid-week Reassignment workflow screen** *(OncallHost initiates; Manager approves)*.
- **Swap request workflow screen** *(all roles can initiate; Manager approves)*.
- **Team/Member management screen** *(Admin: all teams; Manager: own team only)*:
  - Add, edit, deactivate members.
  - Toggle eligibility flag.
  - Bulk CSV import.
- **Admin policy configuration screen** *(Admin only)*: rotation policy, sub-session builder, DST windows, holidays.
- **Reports screen** *(Admin: full; Manager: own team; OncallHost: session/coverage)*.

### 10.3 UI/UX Requirements
- Role-adaptive navigation: menu items and action buttons are shown or hidden based on the logged-in user's role.
- Clear timezone labels for all shifts.
- Distinct styling for weekday vs weekend shifts.
- Role badges: Primary (solid) and Secondary (outlined) on all assignment cards.
- Sub-session blocks rendered as segmented bars within a week row.
- Mid-week reassignment events shown with a swap icon and before/after member display.
- Status chips for handover, swap, and reassignment states.
- Leave request form shows a searchable alternative-member suggestion field, pre-filtered to eligible teammates in the same region.
- Responsive layout for desktop and laptop first; mobile-friendly read views.
- Accessible components (WCAG-friendly color contrast, keyboard navigation).

## 11. Non-Functional Requirements

### 11.1 Reliability and Availability
- Target availability: 99.9% (business hours).
- No uncovered shift should exist in published schedule.
- Graceful fallback for notification failures with retry.

### 11.2 Performance
- Calendar query response under 2 seconds for 6-month window.
- Schedule generation under 60 seconds for up to 500 members.

### 11.3 Scalability
- Support multiple teams and at least 500 active members in initial design.
- Design for horizontal API scaling.

### 11.4 Observability
- Structured logs and correlation IDs.
- Metrics: assignments generated, handover SLA, uncovered shift count.
- Alerting for generation failures and overdue handovers.

### 11.5 Compliance and Security
- Least-privilege access.
- Encryption in transit (HTTPS) and at rest (DB managed encryption).
- Data retention policy for audit logs (configurable, for example 12-24 months).

## 12. Acceptance Criteria (MVP)

### Prerequisites and Data
1. Admin can onboard team members via form and bulk CSV import.
2. Historical on-call assignments can be seeded to seed fairness calculations.
3. Holiday calendars for US and IDC regions are configurable.

### Scheduling and Allocation
4. Admin can configure rotation policy with strategy choice (Round Robin, Least Recently Assigned).
5. System generates at least 4 months of weekly schedules assigning both Primary and Secondary for each session without conflicts.
6. Primary and Secondary are never the same person and (by default) never from the same team.
7. Sub-sessions defined in policy are scheduled independently with no gaps between them within the week.
8. A member is not assigned to more than one sub-session in the same week unless explicitly overridden.
9. Fairness rule enforces approximately one assignment per member per 4 months; Secondary counts at configurable weight.
10. Constraint relaxation triggers admin warning when no eligible member meets the gap rule.
11. Weekday and weekend schedules follow the provided Standard and DST timing windows.
12. Weekend allocation tracks separately and respects fairness gap configuration.

### Leave and Emergency Handling
13. Engineer can submit ad-hoc leave; manager approval routes correctly.
14. Approved leave before schedule generation excludes member from allocation.
15. Approved leave after publication triggers automatic reassignment workflow.
16. Emergency OOO can be declared mid-shift; partial shift records are created correctly.
17. Emergency replacement candidates are ranked and manager can approve within 2-hour SLA.
18. Admin escalation triggers automatically if manager does not act within SLA.

### Mid-Week Reassignment
19. Engineer or manager can request mid-week reassignment for a published booking.
20. Reassignment request routes to manager for approval with 4-hour SLA.
21. On approval, the original assignment is split: credit assigned proportionally to original engineer.
22. Replacement is selected from eligible pool, notified immediately, and receives handover context.
23. If Primary is reassigned, Secondary of that session is notified of the change.
24. Mid-week reassignment SLA expiry escalates to admin and is logged.

### Approvals
25. All approval types (leave, swap, mid-week reassignment, emergency OOO) route to the correct manager.
26. Manager delegation is time-bound and respected by the approval engine.
27. Expired approvals escalate to admin with log entry.

### Notifications and Reminders
28. Assigned Primary and Secondary each receive T-48h and T-24h reminders.
29. Secondary receives handover copy when outgoing Primary submits it.
30. Manager receives notification when a member under them is assigned as Primary or Secondary.
31. Handover reminder sent to outgoing Primary 48h before session end.
32. Manager escalation fires if Primary handover not acknowledged 2h before next session start.
33. Emergency OOO triggers instant notification to engineer, manager, and admin.
34. Mid-week reassignment approval triggers notification to original engineer and replacement.
35. If Primary is mid-week reassigned, Secondary is notified automatically.
36. Holiday falling on assigned shift notifies Primary, Secondary, and manager T-3 days before.

### Visibility and Reporting
37. Calendar shows Primary and Secondary badges on each session, sub-session blocks, holidays, and unavailability blocks.
38. Mid-week reassignment events are shown as a change indicator on the calendar.
39. On-call history per member shows role (Primary/Secondary), fairness credit, and sub-session details.
40. Fairness report shows last assignment date, role breakdown, and weighted gap for every active member.
41. Sub-session coverage report shows per-day who covered and in which role.
42. Audit log captures all overrides, approvals, reassignments, and escalations with actor and reason.

## 13. MVP Delivery Plan

### Phase 1 (Foundations)
- Auth, user roles, RBAC.
- Team and member management with bulk import.
- On-call history model and seeding.
- Shift definition model with DST-aware windows.
- Holiday calendar management.

### Phase 2 (Scheduling Core)
- Rotation policy with strategy configuration.
- Primary/Secondary allocation engine.
- Sub-session definition and scheduling.
- Fairness and eligibility engine (with weighted Secondary credit).
- Schedule generation with conflict detection (including Primary/Secondary validation).
- Draft review and publish workflow.
- Calendar UI: weekday/weekend/holiday/unavailability display with Primary/Secondary badges and sub-session blocks.

### Phase 3 (Operations)
- Leave request and approval workflow.
- Mid-week reassignment request and manager approval workflow.
- Emergency OOO declaration and replacement workflow.
- Swap request and approval workflow.
- Manager delegation.
- Handover template (Primary + Secondary copy), submission, and acknowledgement.
- Partial shift credit on reassignment and emergency OOO.
- Reminder notifications (T-48h, T-24h, handover due, sub-session boundary).
- In-app notification inbox.

### Phase 4 (Governance and Integrations)
- Full reports suite and export.
- Audit dashboard.
- Approval SLA monitoring.
- Slack/Teams notification integration.
- SMS for emergency OOO.

## 14. Risks and Mitigations
- Risk: DST/timezone misalignment.
- Mitigation: Centralized timezone library and DST integration tests.

- Risk: Fairness not achievable due to low staffing/leave overlap.
- Mitigation: Constraint relaxation with explicit warning and admin approval.

- Risk: Missed handovers.
- Mitigation: Multi-stage reminders and manager escalation.

- Risk: Manual overrides reducing trust in fairness.
- Mitigation: Transparent audit logs and fairness reports.

## 15. Open Decisions
- Final notification channels for MVP (email only vs include chat).
- Exact fairness formula (strict day gap vs weighted scoring).
- Holiday calendar source (manual vs integrated regional calendars).
- SSO provider choice (Azure AD/Okta/Google Workspace).

## 16. Future Enhancements
- Integration with PagerDuty/Opsgenie.
- Auto-import incidents and handover summaries.
- AI-assisted handover summarization.
- What-if simulation for future staffing changes.
- Mobile app or PWA support.

---

## 17. Microservices Architecture (Event-Driven)

### 17.1 Architectural Principles

| Principle | Rule |
|---|---|
| **READ** | Synchronous REST (`GET`) — served directly from each service's own read model / projection. |
| **CREATE / UPDATE / DELETE** | Asynchronous only — caller writes to the service's **outbox table** inside the same DB transaction as the domain entity write. A Debezium CDC connector (or polling publisher) reads the outbox and publishes to Kafka. No direct cross-service DB calls. |
| **Outbox pattern** | Every CUD operation produces one `outbox_events` row (`id`, `aggregateType`, `aggregateId`, `eventType`, `payload JSON`, `createdAt`, `published`). Atomicity is guaranteed because the domain write and the outbox row share the same ACID transaction. |
| **Event consumers** | Each service subscribes to the Kafka topics it cares about and maintains its own local read model (denormalized projection table or cache). |
| **API Gateway** | Single entry point for all Angular UI and external calls. Validates JWT, enforces `@agilysys.com` domain on every request, rate-limits, and routes to the correct downstream service. |

---

### 17.2 Microservice Catalogue

| # | Service | Owns (entities / data) | Publishes events to Kafka | Consumes events from |
|---|---|---|---|---|
| 1 | **api-gateway-service** | Routing rules, rate-limit config, JWT public keys | — | — |
| 2 | **identity-service** | `Member`, `member_system_roles`, `NotificationPreference` | `oncall.identity.events` | — |
| 3 | **team-policy-service** | `Team`, `TeamMembership`, `RotationPolicy`, `SubSessionDefinition`, `ShiftDefinition`, `HolidayCalendar` | `oncall.team.events` | `oncall.identity.events` |
| 4 | **schedule-service** | `OnCallSession` (DRAFT/PUBLISHED), `ScheduleDraft` | `oncall.schedule.events` | `oncall.team.events`, `oncall.availability.events` |
| 5 | **assignment-service** | `OnCallAssignment`, `OnCallHistory` | `oncall.assignment.events` | `oncall.schedule.events`, `oncall.availability.events`, `oncall.approval.events` |
| 6 | **worklog-service** | `OnCallWorkLog`, `WorkLogEntry` | `oncall.worklog.events` | `oncall.assignment.events` |
| 7 | **ticket-service** | `DevOpsTicketRecord`, `AzureDevOpsConfig` | `oncall.ticket.events` | `oncall.worklog.events` |
| 8 | **handover-service** | `Handover`, `HandoverParticipant` | `oncall.handover.events` | `oncall.assignment.events`, `oncall.schedule.events` |
| 9 | **availability-service** | `LeaveRequest`, `EmergencyOOO`, `MidweekReassignment`, `SwapRequest` | `oncall.availability.events` | `oncall.assignment.events`, `oncall.approval.events` |
| 10 | **approval-service** | `ApprovalRecord`, `ManagerDelegation` | `oncall.approval.events` | `oncall.availability.events`, `oncall.assignment.events`, `oncall.identity.events` |
| 11 | **notification-service** | `NotificationEvent` (local projection of preferences) | `oncall.notification.events` | ALL topics — subscribes to every domain event that triggers a notification |
| 12 | **audit-service** | `AuditLog` | — (append-only sink) | ALL topics — writes one `AuditLog` row per sensitive domain event |
| 13 | **reporting-service** | Denormalized read-model projections for all report types | — | ALL topics — maintains up-to-date aggregates for coverage, fairness, SLA, handover compliance |

**Total: 13 services** (1 gateway + 12 domain/infrastructure services)

---

### 17.3 Kafka Topic Map

```
oncall.identity.events
  MemberCreated | MemberUpdated | MemberDeactivated
  TeamMembershipAdded | TeamMembershipRemoved
  EligibilityFlagChanged | NotificationPreferenceUpdated

oncall.team.events
  TeamCreated | TeamUpdated | TeamDeactivated
  PolicyCreated | PolicyUpdated
  SubSessionDefinitionChanged
  HolidayCalendarUpdated | ShiftDefinitionUpdated

oncall.schedule.events
  ScheduleDraftCreated | ScheduleDraftPublished
  SessionCreated | SessionPublished | SessionCancelled
  ConstraintRelaxationWarned | ConstraintRelaxationApproved
  HolidayShiftFlagged

oncall.assignment.events
  AssignmentCreated | AssignmentActivated | AssignmentCompleted
  AssignmentPartiallyCredited | AssignmentCancelled
  OnCallHistoryRecorded

oncall.availability.events
  LeaveRequestSubmitted | LeaveRequestApproved | LeaveRequestRejected
  EmergencyOOODeclared | EmergencyReplacementSelected
  MidweekReassignmentRequested | MidweekReassignmentApproved | MidweekReassignmentRejected
  SwapRequestSubmitted | SwapRequestApproved | SwapRequestRejected

oncall.approval.events
  ApprovalRecordCreated | ApprovalActioned | ApprovalSLAExpired | ApprovalEscalated
  DelegationCreated | DelegationRevoked

oncall.handover.events
  HandoverCreated | HandoverSubmitted | HandoverParticipantAcknowledged
  HandoverFullyAcknowledged | HandoverOverdue | HandoverMissed

oncall.worklog.events
  WorkLogCreated | WorkLogClockIn | WorkLogPaused
  WorkLogResumed | WorkLogClockOut | WorkLogCorrected

oncall.ticket.events
  TicketLinked | TicketUnlinked | TicketStatusUpdated

oncall.notification.events
  NotificationQueued | NotificationSent | NotificationFailed | NotificationRetrying

oncall.audit.events   ← dead-letter / replay only; audit-service writes to DB not Kafka
```

---

### 17.4 Outbox Pattern per Service

Every CUD API call follows this flow — no exceptions:

```
Angular UI  ──POST/PUT/DELETE──►  api-gateway-service
                                        │ JWT + domain check
                                        ▼
                               target microservice
                                        │
                         ┌──────────────▼─────────────────┐
                         │  Single DB Transaction          │
                         │  1. Write domain entity row     │
                         │  2. Write outbox_events row     │
                         │     (aggregateType, eventType,  │
                         │      aggregateId, payload JSON) │
                         └────────────────────────────────┘
                                        │
                         Debezium CDC / Polling Publisher
                         (reads outbox_events WHERE published=false)
                                        │
                                        ▼
                                   Kafka topic
                                        │
                         ┌─────────────┼──────────────┐
                         ▼             ▼               ▼
               assignment-       notification-     audit-
               service           service           service
               (updates          (queues           (writes
               read model)       reminder)         AuditLog)
```

---

### 17.5 Synchronous READ vs Asynchronous CUD

| Operation type | Transport | Latency target | Notes |
|---|---|---|---|
| `GET` any resource | REST sync → service read model | < 200 ms | Service answers from its own denormalized projection; no cross-service calls |
| `GET` reports | REST sync → reporting-service | < 2 s | Reporting service maintains pre-aggregated projections updated by Kafka consumers |
| `POST / PUT / DELETE` | REST → outbox → Kafka → consumers | Eventual (< 1 s typical) | Caller receives `202 Accepted` + `eventId`; UI polls or uses WebSocket for confirmation |
| Emergency OOO replacement | REST → outbox → Kafka → assignment-service | Eventual, high-priority topic partition | Assignment-service consumer has dedicated thread pool for emergency partition |
| Notifications | Kafka consumer → delivery channel | < 5 s end-to-end | Notification-service consumes all topics; queues `NotificationEvent`; background job delivers |

---

### 17.6 Service Boundaries and Data Ownership

```
┌────────────────────────────────────────────────────────────────────────┐
│                         api-gateway-service                            │
│   JWT validation │ @agilysys.com enforcement │ Rate limiting │ Routing │
└───────────────────────────────┬────────────────────────────────────────┘
                                │ routes to
     ┌──────────────────────────┼───────────────────────────────────────┐
     │                          │                                       │
     ▼                          ▼                                       ▼
identity-service        team-policy-service                   schedule-service
Member, Roles           Team, Policy,                         OnCallSession,
NotifPrefs              SubSession,                           ScheduleDraft
                        ShiftDef, Holiday
                                                                       │
                              ┌────────────────────────────────────────┘
                              │ SessionPublished event
                              ▼
                      assignment-service
                      OnCallAssignment, OnCallHistory
                              │
              ┌───────────────┼─────────────────────┐
              │               │                     │
              ▼               ▼                     ▼
        worklog-service  handover-service   availability-service
        WorkLog,         Handover,          LeaveRequest,
        WorkLogEntry     Participant        EmergencyOOO,
              │                             MidweekReassignment,
              ▼                             SwapRequest
        ticket-service                           │
        DevOpsTicket                             ▼
        AzureDevOpsConfig               approval-service
                                        ApprovalRecord,
                                        ManagerDelegation
                                                 │
              ┌──────────────────────────────────┘
              │  ALL domain events
     ┌────────┼────────────────────────────┐
     ▼        ▼                            ▼
notification  audit-service           reporting-service
-service      AuditLog                Denormalized
NotifEvent    (append-only)           read projections
```
