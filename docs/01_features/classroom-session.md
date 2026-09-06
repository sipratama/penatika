# Feature Specification — Classroom Session

> Defines starting, pairing, synchronizing, recovering, ending, and saving a cloud teaching session.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.1` |
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
7. Clients reconnect or resynchronize when necessary.
8. Teacher ends and saves the session.

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

## 5. State Model

```text
CREATED → READY → ACTIVE → ENDING → SAVED
             │       ├→ DEGRADED → ACTIVE
             │       └→ FAILED
             └→ EXPIRED
```

### State Invariants

- One backend revision is authoritative at a time.
- A classroom display cannot issue teacher commands.
- An expired pairing credential cannot be reused.
- A saved session references a stable lesson version.

## 6. Permissions and Authorization

- Teacher: start, pair controller, navigate, adapt, annotate, end, and save.
- Classroom display: read classroom-safe projection only.
- Student: no device role in MVP.

The identity provider and authentication mechanism remain open, but server-side authorization is mandatory.

## 7. Data Requirements

Session data may include session identity, teacher identity reference, lesson version, participant roles, pairing status, authoritative revision, accepted scene state, annotations, dependency status, timestamps, and save outcome.

Pairing secrets, access tokens, raw audio, and private AI payloads must not appear in classroom projections or logs.

## 8. Failure and Edge Cases

- Display joins before controller or vice versa.
- Pairing code expires or is replayed.
- Multiple controllers attempt to become active.
- Client sends stale or duplicated commands.
- Teacher connection drops while display remains connected.
- Backend or persistence is unavailable.
- Save partially fails after session end.

Safe behavior must favor existing authoritative state, visible teacher status, idempotent recovery, and no privilege expansion.

## 9. Minimum Test Scenarios

- Start a session from a reviewed lesson.
- Reject an unreviewed lesson.
- Pair devices across different networks.
- Reject expired and replayed pairing credentials.
- Prove private teacher state is absent from classroom projection.
- Reconcile a stale client without overwriting authoritative state.
- Handle duplicate state-changing commands idempotently.
- Recover from a temporary disconnect and save once.

## 10. Open Questions

- What identity and authentication model is required for MVP?
- Can more than one teacher controller be active?
- What is the pairing credential lifetime and replacement flow?
- Which annotations and transient events are retained in a saved session?
- What exact functionality remains available when the backend cannot be reached?

## 11. Definition of Done

- Lifecycle, authorization, projection, ordering, recovery, and save behavior are implemented and tested.
- Pairing threat scenarios are covered.
- Session contracts and revision rules are versioned.
- Degraded states satisfy the selected product policy.

