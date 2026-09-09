# Contract Foundation Checkpoint — Complete

> **Document role:** This is a historical handoff snapshot and merge-readiness record. It is **non-authoritative**. If this checkpoint conflicts with a current canonical document, the canonical document wins.

## 1. Purpose

This checkpoint records the completed Field-Level Contract Foundation on
`feat/contract-foundation` and establishes the deliberate merge handoff
boundary. It summarizes the branch state without replacing the authoritative
machine-readable contracts, product documents, architecture documents, or
`PROJECT_STATUS.md`.

## 2. Snapshot Metadata

| Field | Value |
|---|---|
| Date | `2026-09-09` |
| Branch | `feat/contract-foundation` |
| Pre-checkpoint Contract Foundation HEAD | `f9d7bc840a8cec04272d77f142c9143c58156e64` |
| origin/main baseline SHA | `7cd6a2db4652742080952d5ed6b151805057615b` |
| Merge-base SHA | `7cd6a2db4652742080952d5ed6b151805057615b` (`origin/main` is an ancestor of the pre-checkpoint HEAD) |
| OpenAPI | `3.1.2` |
| Penatika HTTP contract version | `0.8.0` |
| JSON Schema | Draft 2020-12 |
| Implementation state | Pre-source / Pre-scaffolding |

The final checkpoint commit SHA will exist only after human review and commit;
it is intentionally not invented in this snapshot.

## 3. Contract Foundation Scope Completed

- Froze the first architecture-proving classroom vertical slice.
- Established shared `ClassroomSessionId` and `Revision` wire primitives plus
  an RFC 9457 `application/problem+json` foundation.
- Defined the authenticated Teacher bootstrap and minimum Classroom Session
  start from an existing authorized, classroom-ready `LessonVersion`.
- Defined role-bound PairingGrant issue, revocation, Controller redemption,
  and Display redemption boundaries.
- Defined bounded Controller revision reconciliation.
- Defined the deterministic revision-aware `DIRECT_ACTION: NEXT` command with
  `CommandId`, expected revision, stale-revision, and accepted-replay semantics.
- Defined the closed classroom-safe Display projection and authoritative
  snapshot.
- Defined authorized Display SSE full-projection delivery and advisory
  `Last-Event-ID` reconciliation.
- Defined explicit Display synchronization acknowledgement as the final
  first-slice mutation-eligibility gate.

## 4. Final First Vertical Slice

```text
Authenticated Teacher
-> select an existing authorized classroom-ready LessonVersion
-> start a backend-authoritative Classroom Session
-> establish Controller and Display participant sessions
-> Display receives the authoritative full projection
-> Display validates, applies, and acknowledges the current revision
-> Controller reconciles the current authoritative revision
-> submit DIRECT_ACTION: NEXT with CommandId + expectedRevision
-> backend applies at most one authoritative mutation effect
-> return resultingRevision
-> push the resulting full authoritative Display projection over SSE
-> Display receives, validates, and applies it as replacement
-> Display acknowledges the new current revision
-> a subsequent mutation may become eligible
-> reconnect requires authoritative reconciliation and acknowledgement again
```

This is a handoff summary only. Detailed field, status, security, and error
behavior remains owned by the canonical contracts.

## 5. Active Contract Surface

| Method | Path | Purpose | Authority |
|---|---|---|---|
| `GET` | `/api/teacher-session` | Bootstrap the authenticated Teacher browser session and CSRF material | `TeacherBrowserSession` |
| `POST` | `/api/classroom-sessions` | Start a Classroom Session from an authorized classroom-ready `LessonVersion` | `TeacherBrowserSession` + Teacher CSRF + current Teacher authorization |
| `POST` | `/api/classroom-sessions/{classroomSessionId}/pairing-grants` | Issue a session-bound and role-bound PairingGrant | `TeacherBrowserSession` + Teacher CSRF + current Teacher authorization |
| `DELETE` | `/api/classroom-sessions/{classroomSessionId}/pairing-grants/{pairingGrantId}` | Revoke a PairingGrant by its non-secret identifier | `TeacherBrowserSession` + Teacher CSRF + current Teacher authorization |
| `POST` | `/api/teacher-controller-participants` | Redeem a Controller grant and establish the Controller participant session | `TeacherBrowserSession` + Teacher CSRF + valid `TEACHER_CONTROLLER` grant + current Teacher authorization |
| `POST` | `/api/classroom-display-participants` | Redeem a Display grant and establish the Display participant session | Valid `CLASSROOM_DISPLAY` grant only; no Teacher authority is conferred |
| `GET` | `/api/classroom-sessions/{classroomSessionId}/controller-state` | Retrieve minimal authoritative Controller reconciliation state | `TeacherBrowserSession` + active same-session `TEACHER_CONTROLLER` `ParticipantBrowserSession` + current Teacher authorization |
| `POST` | `/api/classroom-sessions/{classroomSessionId}/commands` | Submit deterministic revision-aware `DIRECT_ACTION: NEXT` | Dual Teacher/Controller sessions + current Teacher authorization + Teacher CSRF + expected authoritative revision + current Display synchronization gate |
| `GET` | `/api/classroom-sessions/{classroomSessionId}/display-snapshot` | Retrieve the current full classroom-safe Display projection | Active same-session `CLASSROOM_DISPLAY` `ParticipantBrowserSession` |
| `GET` | `/api/classroom-sessions/{classroomSessionId}/display-events` | Stream full authoritative Display projections over SSE | Active same-session `CLASSROOM_DISPLAY` `ParticipantBrowserSession` |
| `PUT` | `/api/classroom-sessions/{classroomSessionId}/display-synchronization` | Acknowledge the current projection on the current active Display stream | Active same-session `CLASSROOM_DISPLAY` `ParticipantBrowserSession` + `X-Penatika-Display-Intent: synchronize` + trusted-origin/CORS validation |

## 6. Canonical Schema Ownership

- [`wire-primitives.schema.json`](../../contracts/schemas/wire-primitives.schema.json)
  canonically owns `ClassroomSessionId` and `Revision`.
- [`classroom-display-projection.schema.json`](../../contracts/schemas/classroom-display-projection.schema.json)
  canonically owns `ClassroomDisplayProjection`.
- [`openapi.yaml`](../../contracts/openapi/openapi.yaml) canonically owns HTTP
  operations, HTTP-only schemas, security requirements, RFC 9457 error
  boundaries, and SSE framing.

Each reusable wire structure has one canonical owner. Referencing consumers do
not independently redefine the same structure.

## 7. Security and Authority Summary

- The backend is the sole Classroom Session authority; cached browser state is
  never authority.
- Teacher authentication uses an opaque backend-managed Teacher cookie;
  participant authority uses a separate opaque role-bound participant cookie.
- The backend performs current authorization on every protected boundary.
- Teacher state changes require Teacher CSRF protection. Display
  synchronization uses the fixed custom-header CSRF guard
  `X-Penatika-Display-Intent: synchronize` with trusted-origin/CORS validation.
- Controller and Display authority remain separate. Neither role may use the
  other role's projection or mutation boundary.
- Foreign and nonexistent identifiers use bounded non-disclosing responses to
  preserve BOLA resistance.
- `PairingGrantToken` is a one-time secret distinct from the revocable,
  non-secret PairingGrant identifier. A pairing token never becomes Teacher
  authentication.
- OAuth/OIDC access, ID, and refresh tokens remain server-side and are not
  exposed through browser application contracts.

## 8. Revision / Command / Recovery Summary

- `Revision` is monotonic only within one Classroom Session. The contract makes
  no initial `0` or `1` assumption; revision is not an event count, scene
  position, database row version, or timestamp.
- Command identity is scoped by `(ClassroomSessionId, CommandId)`. A newly
  accepted logical command produces at most one authoritative mutation effect.
- An equivalent accepted retry returns the original result. A new `CommandId`
  with a stale expected revision is rejected. The contract makes no generic
  exactly-once transport claim.
- Controller recovery uses
  `GET /api/classroom-sessions/{classroomSessionId}/controller-state`, which
  returns only `classroomSessionId` and the current authoritative `revision`
  under dual Teacher and Controller authority.
- Display recovery uses the current full authoritative projection from the
  snapshot or the first state-bearing event on every successful SSE connection.
  `Last-Event-ID` is advisory reconciliation context only; no historical replay
  or delta guarantee exists.
- After projection delivery, the Display must receive, schema-validate, apply
  the full replacement, and acknowledge the current revision. Server dispatch
  alone is insufficient. Acknowledgement is retry-safe, does not advance the
  Classroom Session revision, grants no command authority, and is not proof of
  pixel rendering.
- Revision advance, stream termination, participant revocation/replacement,
  session end, or loss of authority invalidates the current Display
  synchronization gate.

## 9. Final Readiness Remediations

The final audit closed four material consistency gaps:

- added the minimal Controller-authorized authoritative revision read;
- aligned `Last-Event-ID` wording and machine-contract behavior around
  advisory full-state reconciliation;
- clarified accepted `CommandId` reuse under the currently closed
  `DIRECT_ACTION: NEXT` schema;
- required explicit Display synchronization acknowledgement before a new
  student-facing mutation becomes eligible.

These remediations required no product or architecture decision change.

## 10. Validation Evidence

The completed final audit reported:

- `git diff --check` — PASS, with non-blocking Windows LF/CRLF working-tree
  warnings where applicable;
- project-mode template validator — PASS via Podman `python:3.13-slim`;
- Redocly CLI `2.51.2` lint — PASS;
- Redocly dereferenced bundle — PASS;
- Ajv `8.17.1` Draft 2020-12 schema compilation — PASS;
- final structural/security audit — PASS after remediation.

Checkpoint-task verification was rerun after this checkpoint and the routing
status updates were authored. Current results: `git diff --check` passed with
non-blocking Windows LF/CRLF warnings; native Python was unavailable, so the
project-mode template validator passed through the Podman
`python:3.13-slim` fallback; Redocly CLI `2.51.2` lint passed; `origin/main`
remained an ancestor of the branch; and the `git merge-tree` conflict preview
was clean. Exact commands are recorded in the handoff report accompanying the
uncommitted working tree.

## 11. Explicitly Deferred

- lesson creation or generation APIs beyond the existing classroom-ready
  `LessonVersion` prerequisite;
- `PREVIOUS`, arbitrary navigation, digital ink, AI adaptation/proposals, and
  speech;
- Controller-private SSE and participant replacement/handoff;
- session end, save, history, export, deletion, and command-retention cleanup;
- richer Display content models beyond projection schema `1.0` `PLAIN_TEXT`;
- historical SSE replay or delta catch-up;
- physical persistence, Flyway migrations, Java/Spring source, and
  React/TypeScript source;
- Docker, deployment, CI/CD, and pilot execution.

## 12. Merge Readiness Statement

`feat/contract-foundation` is ready for deliberate merge into `main` only
after this checkpoint and the status updates are reviewed, validation passes,
the human commits and pushes the checkpoint, and the remote branch state is
reverified. This checkpoint task does not perform the merge.

## 13. Next Phase

After deliberate merge to `main`:

```text
create feat/source-scaffolding
-> Source Scaffolding
```

Do not begin source scaffolding before the merge. Implementation of the first
vertical slice follows source scaffolding.

## 14. Historical Checkpoint Note

The previous phase handoff is the immutable
[Architecture Foundation Checkpoint — Complete](./2026-09-08-architecture-foundation-complete.md).
It remains historically correct. This Contract Foundation checkpoint becomes
the latest overall handoff snapshot only; it does not supersede canonical
architecture or any other canonical source.

The already-published Batch 7 commit subject
`eat(contracts): define display sse reconciliation` contains a non-blocking
Git-history typo. Published history is not rewritten by this task.
