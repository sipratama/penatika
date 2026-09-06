# Feature Specification — Classroom Session

> Defines starting, pairing, synchronizing, recovering, ending, and saving a cloud teaching session.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.3` |
| Last Updated | `2026-09-06` |
| PRD Capabilities | `CAP-SESSION-001`, `CAP-SESSION-002` |

## 1. Feature Intent

### Problem Addressed

Teachers need a private controller and a student-facing display that share classroom state without screen mirroring, shared Wi-Fi assumptions, or client-owned authority.

### Desired Outcome

An authorized teacher starts a lesson session, pairs supported devices, teaches against a consistent backend-authoritative state, recovers from connection interruptions, and saves the session.

## 2. Scope

### In Scope

- Start a session from a reviewed lesson version.
- Join a classroom display.
- Pair a teacher smartphone controller.
- Authorize participant roles and project role-specific state.
- Synchronize authoritative session revisions.
- Reconnect, end, and save a session.

### Out of Scope

- Screen mirroring.
- Shared-LAN discovery as a requirement.
- Student device participation.
- Multi-class or multi-room orchestration.
- Attendance tracking.

## 3. Primary Flow

1. Teacher selects a classroom-ready lesson version and starts a session.
2. Backend creates an authoritative session identity and initial state.
3. Classroom display joins using an authorized display flow.
4. Teacher pairs the smartphone controller using an expiring, single-purpose pairing mechanism.
5. Backend assigns participant roles and returns only permitted state.
6. Teacher actions produce ordered session commands and authoritative revisions.
7. Dependency failures degrade only affected capabilities while clients preserve backend authority and a safe current projection.
8. Clients reconnect and reconcile against the authoritative revision before mutations resume.
9. Teacher ends the session and receives a successful save state only after durable authoritative persistence acknowledges it.

## 4. Functional Requirements

### FR-SESSION-001 — Start from a Reviewed Lesson

Only an authorized teacher may start a session, and the selected lesson version must be classroom-ready.

### FR-SESSION-002 — Create Authoritative Session State

The backend shall create and own the authoritative session lifecycle, current lesson position, accepted classroom content, annotations included in session state, and revision identity.

### FR-SESSION-003 — Pair without Mirroring or Shared Wi-Fi

The product shall support pairing through the cloud session. Pairing shall not require screen mirroring or devices to share a local network.

### FR-SESSION-004 — Use Bounded Pairing Credentials

Pairing credentials shall be purpose-bound, time-bounded, resistant to replay, and insufficient by themselves to grant broader teacher account access.

### FR-SESSION-005 — Project Role-Specific State

The backend shall provide teacher-private state only to an authorized teacher surface and shall provide classroom-safe state to the display surface.

### FR-SESSION-006 — Order and Reconcile Commands

State-changing commands shall include enough session and revision context to detect stale, duplicated, or out-of-order operations.

### FR-SESSION-007 — Recover after Interruption

On reconnect, a client shall reconcile against backend-authoritative state rather than silently overwriting it with cached client state.

### FR-SESSION-008 — Expose Degraded State

Teacher surfaces shall show connection, synchronization, and dependency degradation without exposing private diagnostic detail on the classroom display.

### FR-SESSION-009 — End and Save

Only an authorized teacher may end a session. Saving shall preserve the selected lesson version and allowed final session state according to the retention policy.

### FR-SESSION-010 — Reject Unauthorized Participation

Invalid, expired, replayed, or role-incompatible join attempts shall fail safely and shall not reveal sensitive session state.

### FR-SESSION-011 — Preserve Last-Known Safe Projection

During temporary backend/session connectivity loss, clients may preserve the last-known safe classroom projection but shall not treat cached state as authoritative.

### FR-SESSION-012 — Freeze Mutations without Backend Authority

New authoritative state-changing commands shall not be accepted locally when backend authority cannot be reached.

### FR-SESSION-013 — Reconcile before Resuming

After reconnect, clients shall retrieve and reconcile against backend-authoritative state before classroom mutations resume. Stale or conflicting local assumptions shall be rejected.

### FR-SESSION-014 — Do Not Replay New Offline Mutations

The MVP shall not queue newly created offline state-changing commands for automatic replay. A command sent before connection loss whose acknowledgement is uncertain may be reconciled using command identity, idempotency, and revision checks.

### FR-SESSION-015 — Degrade Controller Loss Safely

If the teacher controller disconnects, the classroom display shall preserve the current authoritative projection and no automatic mutation shall occur. Another teacher control surface may act only when separately authorized by the backend.

### FR-SESSION-016 — Pause Student-Facing Mutation on Display Loss

If the classroom display disconnects, publication and direct student-facing classroom mutation shall pause until the display has reconnected and synchronized against the authoritative revision. Safe teacher-private work may continue.

### FR-SESSION-017 — Report Save Truthfully

The product shall not report a successful save until durable authoritative persistence acknowledges the save. A failed or unavailable save path shall remain `SAVE_PENDING`, `SAVE_FAILED`, or `RETRY_REQUIRED` as applicable.

### FR-SESSION-018 — Retain Saved Session History for the Approved Window

A successfully saved session shall become eligible for teacher-visible history for `90 days` after session end/save. The retained history shall include only allowed accepted classroom state, lesson-version or allowed historical snapshot/reference, retained annotations, save outcome, relevant assurance/provenance references, and privacy-minimized action or decision metadata.

### FR-SESSION-019 — Support Early Session Deletion and Authorized Export

An authorized teacher shall be able to delete a retained saved session before its default expiry and obtain an authorized export of retained teacher-owned session data. Deletion shall remove ordinary product access when committed and follow the canonical primary-purge and backup-expiry process.

### FR-SESSION-020 — Bound Session-History Content

Retained annotations shall follow the owning saved-session lifecycle. Session history shall not contain raw audio, full prompts, raw provider payloads, full AI conversation history, permanent rejected-proposal bodies, or persistent student identity/profiling.

## 5. State Model

```text
CREATED → READY → ACTIVE → ENDING → SAVE_PENDING → SAVED
             │       │                  └→ SAVE_FAILED → RETRY_REQUIRED
             │       ├→ RECONNECTING → ACTIVE
             │       └→ FAILED
             └→ EXPIRED
```

The active lifecycle may carry concise degradation overlays rather than separate competing session authorities:

```text
DEGRADED_AI
DEGRADED_SPEECH
DISPLAY_DISCONNECTED
CONTROLLER_DISCONNECTED
AUTHORITY_UNAVAILABLE
RECONNECTING
```

`AUTHORITY_UNAVAILABLE` freezes new authoritative mutations. Returning to `ACTIVE` requires completed reconciliation with backend-authoritative state.

Saved-session history has a separate retention lifecycle from the active session lifecycle:

```text
SAVED → RETAINED_HISTORY
          ├→ RETENTION_EXPIRED
          └→ DELETION_REQUESTED
                    → INACCESSIBLE
                    → PRIMARY_PURGED
                    → BACKUP_EXPIRED
```

`INACCESSIBLE` or deletion processing must not be presented as completed deletion before the applicable purge stage is complete.

### State Invariants

- One backend revision is authoritative at a time.
- A classroom display cannot issue teacher commands.
- An expired pairing credential cannot be reused.
- A saved session references a stable lesson version and durable authoritative save acknowledgement.
- Cached client state is never promoted to temporary authority.
- Newly created offline mutations are never queued for automatic replay.

## 6. Permissions and Authorization

- Teacher: start, pair controller, navigate, adapt, annotate, end, and save.
- Classroom display: read classroom-safe projection only.
- Student: no device role in MVP.

The identity provider and authentication mechanism remain open, but server-side authorization is mandatory.

## 7. Data Requirements

Active session data may include session identity, teacher identity reference, lesson version, participant roles, pairing status, authoritative revision, accepted scene state, annotations, dependency status, timestamps, and save outcome.

Retained saved-session history is limited to the data classes allowed by [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md) and expires after `90 days` by default. Retained annotations expire or are deleted with the owning session. Save, retention, deletion, primary-purge, backup-expiry, and export states must remain truthful and authorization-controlled.

Pairing secrets, access tokens, raw audio, full prompts, raw provider payloads, and unnecessary private AI or classroom content must not appear in classroom projections, retained history, or logs.

## 8. Failure and Edge Cases

- Display joins before controller or vice versa.
- Pairing code expires or is replayed.
- Multiple controllers attempt to become active.
- Client sends stale or duplicated commands.
- Teacher connection drops while display remains connected.
- Classroom display disconnects while the teacher remains connected.
- AI or speech dependency becomes unavailable while backend authority remains healthy.
- Backend authority becomes unavailable while clients retain cached state.
- A command was sent before disconnect but its acknowledgement is uncertain.
- Save persistence is unavailable while authoritative runtime state remains healthy.
- Save partially fails after session end or remains pending.

Safe behavior must favor existing authoritative state, visible teacher status, idempotent reconciliation, no new offline mutation replay, truthful save status, and no privilege expansion.

## 9. Minimum Test Scenarios

- Start a session from a reviewed lesson.
- Reject an unreviewed lesson.
- Pair devices across different networks.
- Reject expired and replayed pairing credentials.
- Prove private teacher state is absent from classroom projection.
- Reconcile a stale client without overwriting authoritative state.
- Handle duplicate state-changing commands idempotently.
- Preserve the last-known safe projection and freeze mutations while backend authority is unavailable.
- Reconcile before accepting mutations after reconnect.
- Reconcile uncertain acknowledgement for a pre-disconnect command without replaying newly created offline commands.
- Keep the display stable and prevent automatic mutation after controller disconnect.
- Pause student-facing mutation until a reconnected display is synchronized.
- Isolate AI and speech dependency failures from healthy session capabilities.
- Report save pending/failure/retry states until durable acknowledgement, then save once.
- Retain a durably saved session for the `90-day` history window and expire it according to policy.
- Delete a saved session early, remove ordinary access when deletion commits, and evidence primary-purge and backup-expiry states truthfully.
- Verify retained annotations follow the saved-session lifecycle.
- Authorize export for the owning teacher and reject unauthorized session export.
- Verify raw audio, full prompts, raw provider payloads, and unaccepted proposal bodies are absent from retained session history.

## 10. Open Questions

- What identity and authentication model is required for MVP?
- Can more than one teacher controller be active?
- What is the pairing credential lifetime and replacement flow?
- What exact teacher-visible history and deletion/export interaction design should represent the policy-compliant saved-session data?

## 11. Definition of Done

- Lifecycle, authorization, projection, ordering, recovery, and save behavior are implemented and tested.
- Pairing threat scenarios are covered.
- Session contracts and revision rules are versioned.
- Degraded states satisfy the selected product policy.
