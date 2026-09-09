# Contract Foundation Working Plan

> **Document role:** This is a working engineering plan, not an authoritative
> product, architecture, or wire-contract source.

This plan preserves the durable scope and sequencing for Penatika's first
architecture-proving classroom contract slice. It guides contract work without
replacing canonical requirements, architecture decisions, or machine-readable
contracts.

## Authoritative Sources

- [Product Brief](../00_product/PRODUCT_BRIEF.md) and
  [PRD](../00_product/PRD.md) own product purpose, capabilities, and rules.
- [Lesson Preparation](../01_features/lesson-preparation.md),
  [Classroom Session](../01_features/classroom-session.md), and
  [Classroom Canvas](../01_features/classroom-canvas.md) own detailed behavior.
- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) and Accepted
  [ADRs](../02_architecture/adr/) own system boundaries and rationale.
- Machine-readable contracts under [`contracts/`](../../contracts/README.md)
  own wire-level definitions.
- [Project Status](../PROJECT_STATUS.md) is the primary phase and progress
  router.

If this plan conflicts with an authoritative source, the authoritative source
wins and this plan must be corrected.

## Objective

The first executable contract slice proves one protected, deterministic,
backend-authoritative classroom mutation from an authenticated Teacher action
through a classroom-safe Display update. It must include safe duplicate,
conflict, and reconnect behavior while keeping teacher-private authority and
the student-facing Display projection as distinct wire boundaries.

## First Vertical Slice

```text
Authenticated Teacher with an active backend-managed browser session
-> select an existing authorized, classroom-ready LessonVersion
-> start a Classroom Session with authoritative state and revision
-> establish TEACHER_CONTROLLER and CLASSROOM_DISPLAY in either order
-> submit deterministic DIRECT_ACTION: NEXT over HTTPS/JSON
   with command identity and expected revision
-> validate account/session ownership, participant authority, command,
   identity, and revision at the backend
-> advance the authoritative lesson/scene position exactly once
-> return the authoritative HTTP command outcome
-> push the classroom-safe Display projection over SSE
-> receive, validate, then apply the full projection as replacement
-> acknowledge the current revision on the active Display stream
-> reconcile with backend authority after reconnect before mutation resumes
```

This flow fixes semantic boundaries, not endpoint paths, JSON field names,
DTOs, status mappings, physical persistence, or implementation structure.

## Scope Boundaries

| Area | Required scope |
|---|---|
| Teacher boundary | Active backend-managed browser session; active account and ownership checks; upstream OIDC tokens remain server-side |
| Lesson prerequisite | Existing immutable, teacher-reviewed, classroom-ready `LessonVersion`; no invented manual lesson-authoring path |
| Session start | Authorized start from the selected version; establish session identity, lifecycle state, minimum current position, authoritative revision, and safe snapshot |
| Pairing | Session/role-bound, five-minute, single-use, revocable grants; separate participant sessions; exactly one active Controller and one active Display; either may pair first |
| Direct command | Only deterministic `DIRECT_ACTION: NEXT`; active Teacher and Controller authority; command identity; expected revision; stale/unsupported rejection; exactly-once mutation outcome |
| Display projection | Positive allow-list containing only student-facing render data and required session/revision synchronization context |
| Transport | State-changing commands use synchronous HTTPS/JSON; authorized projection push uses SSE |
| Recovery | `Last-Event-ID` or last-observed revision initiates authoritative reconciliation; full snapshot resync is the minimum baseline; current active-stream acknowledgement is required before new student-facing mutation; cached state never becomes authority |
| Errors | RFC 9457 `application/problem+json`; operation-specific codes, problem types, and status mappings are defined with their operations |

## Binding Invariants

- The backend is the only Classroom Session state authority (`INV-001`,
  `INV-002`).
- Every accepted mutation binds an authorized actor, Classroom Session,
  command identity, and revision context (`INV-009`).
- `NEXT` is deterministic and non-generative (`INV-016`).
- Pairing grants never become Teacher authentication or account authority
  (`INV-025`).
- Controller and Display authorities and projections remain separate
  (`INV-026`, `INV-027`).
- The Display receives only a classroom-safe projection (`INV-003`).
- Commands remain synchronous HTTP; SSE carries projections only (`INV-029`).
- SSE event identity supports resynchronization but never authorizes mutation
  (`INV-030`).
- Reconnect reconciles with backend authority before mutation resumes; newly
  created offline commands are never queued or automatically replayed
  (`INV-017`-`INV-020`).
- Degradation cannot bypass authorization, privacy, structured-content, or
  stale/revision controls (`INV-022`).

## Explicitly Deferred

- AI lesson generation, semantic live adaptation, speech, curriculum retrieval
  implementation, Mathematics validation implementation, and proposal flows;
- classroom actions other than `NEXT`, including navigation variants,
  annotation, digital ink, visibility, zoom, undo/redo, and clear;
- the complete Classroom Scene/content model beyond the minimum safe content
  required to prove the slice;
- session end/save/history/export/deletion and participant replacement or
  handoff UX;
- concrete OIDC provider details, password authentication, account recovery,
  and provider payloads;
- student devices, WebSocket, STOMP, polling, brokers, queues, AsyncAPI, and
  the Controller-private SSE operation for this slice;
- physical persistence, migrations, application source, deployment, and CI/CD.

Field-level details remain deferred until their owning contract slice,
including later operation paths and methods, pairing credential
representation beyond the current machine-level grant, participant replacement
or handoff, post-session command-retention cleanup, richer scene/content types,
SSE event names and retry details, and later operation-level problem codes,
types, and status mappings.

## Contract Foundation Sequence

| Capability | Status |
|---|---|
| First vertical slice scope | COMPLETE |
| Shared wire / RFC 9457 problem foundation | COMPLETE |
| Authenticated Teacher / minimum LessonVersion and session start | COMPLETE |
| Pairing | COMPLETE |
| `NEXT` command / revision | COMPLETE |
| Display projection / authoritative snapshot | COMPLETE |
| SSE / reconnect plus Display synchronization acknowledgement | COMPLETE |
| Final consistency and readiness audit | COMPLETE |
| Final branch checkpoint | NEXT |
| Merge to `main` | PENDING |

A Contract Foundation checkpoint is created only when the phase or branch is
ready for deliberate handoff/merge, or when the human explicitly requests
one. Intermediate work units use commits and `PROJECT_STATUS.md` for progress.
