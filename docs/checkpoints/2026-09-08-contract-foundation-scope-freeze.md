# Contract Foundation Scope Freeze - First Classroom Vertical Slice

> **Document role:** This is a non-authoritative engineering handoff and
> scope snapshot. It is subordinate to the Product Brief, PRD, feature
> specifications, System Architecture, Accepted ADRs, and eventual
> machine-readable contracts. It does not replace any canonical source.

## 1. Purpose

This checkpoint freezes the smallest first vertical slice for Field-Level
Contract Foundation. It records which semantics the next contract batches
must express without selecting endpoint paths, JSON property names, DTOs,
physical persistence, or implementation structure.

This batch is documentation-only. It does not author OpenAPI operations,
standalone JSON Schemas, source scaffolding, database migrations, deployment
configuration, or application code.

## 2. Snapshot Metadata

| Field | Value |
|---|---|
| Date | `2026-09-08` |
| Branch | `feat/contract-foundation` |
| Initial HEAD | `7cd6a2d` - merge of Architecture Foundation into `main` |
| Initial working tree | Clean |
| Project phase | Field-Level Contract Foundation |
| Batch | Contract Foundation - Batch 1 |
| Implementation state | Pre-source / Pre-scaffolding |

## 3. Inputs Reviewed

Canonical sources used for this scope freeze:

- [PRD](../00_product/PRD.md): `CAP-LESSON-001`, `CAP-SESSION-001`,
  `CAP-SESSION-002`, J-02/J-03/J-04, PR-001-PR-003, PR-010-PR-012,
  PR-017-PR-025, roles/permissions, privacy expectations, and release
  blockers;
- [Lesson Preparation](../01_features/lesson-preparation.md):
  `FR-LESSON-007`, `FR-LESSON-008`, state invariants, and authorization;
- [Classroom Session](../01_features/classroom-session.md):
  `FR-SESSION-001`-`FR-SESSION-017`, `FR-SESSION-021`-`FR-SESSION-025`,
  state/authorization/data invariants, edge cases, and tests;
- [Classroom Canvas](../01_features/classroom-canvas.md):
  `FR-CANVAS-002`, `FR-CANVAS-003`, `FR-CANVAS-005`,
  `FR-CANVAS-006`, and `BR-CANVAS-002`-`BR-CANVAS-005`;
- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md): runtime
  components, module ownership, flows 6.2-6.5, trust boundaries, security,
  reliability, contracts, and `INV-001`-`INV-030` as applicable;
- [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md),
  [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md),
  [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md),
  [ADR-0008](../02_architecture/adr/ADR-0008-browser-first-react-client-strategy.md),
  [ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md),
  [ADR-0011](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md),
  [ADR-0012](../02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md),
  and the pre-source boundary in
  [ADR-0013](../02_architecture/adr/ADR-0013-java21-module-first-hexagonal-backend.md);
- [Contract Index](../../contracts/README.md),
  [OpenAPI Contract Boundary](../../contracts/openapi/README.md), and
  [Reusable JSON Schema Boundary](../../contracts/schemas/README.md);
- [API and Integration Standard](../standards/06_API_INTEGRATION_STANDARD.md),
  relevant [Security Standard](../standards/08_SECURITY_STANDARD.md) rules,
  and the Penatika ECC profile in
  [AI-Assisted Development Standard](../standards/14_AI_ASSISTED_DEVELOPMENT.md).

The prior
[Architecture Foundation checkpoint](./2026-09-08-architecture-foundation-complete.md)
was used only as a non-authoritative handoff snapshot.

## 4. Contract Foundation Objective

The first executable contract slice must prove one protected, deterministic,
backend-authoritative classroom mutation from an authenticated teacher action
through a classroom-safe Display update, including safe duplicate, conflict,
and reconnect behavior.

It must also prove that a teacher-private authority model and a classroom-safe
projection are distinct wire boundaries.

## 5. Frozen First Vertical Slice

```text
Authenticated Teacher with an active backend-managed browser session
        |
        v
Select an existing owned/authorized, classroom-ready LessonVersion
        |
        v
Start a Classroom Session and establish its authoritative state/revision
        |
        v
Establish one authorized TEACHER_CONTROLLER and one authorized
CLASSROOM_DISPLAY participant, in either order
        |
        v
Controller submits deterministic DIRECT_ACTION: NEXT over HTTPS/JSON
with command identity and expected authoritative revision
        |
        v
Backend validates TeacherAccount/session ownership, active controller
authority, command support, command identity, and revision
        |
        v
Classroom Session advances the minimum authoritative lesson/scene position
exactly once and advances the authoritative revision
        |
        v
Synchronous HTTP command completes with an authoritative outcome
        |
        v
Backend emits the updated classroom-safe Display projection over SSE
        |
        v
Classroom Display accepts/renders the synchronized projection
```

The flow freezes semantics, not endpoint ordering, field names, DTO shapes,
status-code mappings, or physical storage.

## 6. Contract Slice Evidence Audit

| Concern | Canonical requirement / invariant | Owning source | Required by first slice? | Deferred? | Notes / conflict |
|---|---|---|:---:|:---:|---|
| Authenticated Teacher boundary | Protected teacher actions use an active backend-managed browser session after OIDC authentication; upstream tokens remain server-side | ADR-0011; System Architecture `INV-023` | Yes | Provider details | No conflict |
| `TeacherAccount` ownership/authorization | Authentication alone is insufficient; backend validates active account, object/session ownership, state, and policy | ADR-0011 Authorization Model; `FR-SESSION-001`, `FR-SESSION-021`; Security Standard | Yes | Field expression | No conflict |
| Classroom-ready `LessonVersion` prerequisite | A session starts only from a teacher-reviewed classroom-ready immutable version | `FR-LESSON-007`, `FR-LESSON-008`; `FR-SESSION-001` | Yes | Creation workflow implementation | Existing fixture/state is used; no manual authoring path is invented |
| Classroom Session start | Authorized teacher starts from the selected version; backend creates session identity and state | `CAP-SESSION-001`; `FR-SESSION-001`, `FR-SESSION-002`; System Architecture 6.2 | Yes | Endpoint/DTO | No conflict |
| Initial authoritative state/revision | Backend owns lifecycle, current lesson position, classroom state, and one authoritative revision | ADR-0002; `FR-SESSION-002`; `INV-001` | Yes | Initial numeric value and wire shape | No conflict |
| Controller participant | Mutation requires authenticated teacher authority plus active `TEACHER_CONTROLLER` participant binding | `FR-SESSION-021`, `FR-SESSION-023`; ADR-0011 | Yes | Replacement/handoff UX | Pairing alone is insufficient |
| Classroom Display participant | Display uses a distinct revocable `CLASSROOM_DISPLAY` participant session and has no teacher authority | `FR-SESSION-024`; ADR-0011; `INV-026` | Yes | Replacement UX | No conflict |
| `PairingGrant` semantics | Grant is session/role-purpose bound, five-minute, single-use, revocable, replay-resistant, and invalidated after redemption/revocation/expiry/session end | `FR-SESSION-022`; ADR-0011; `INV-028` | Yes | Exact credential/UX shape | Session-end workflow remains outside this slice, but its invalidation invariant remains binding |
| Participant-session semantics | Successful redemption creates revocable participant authority rather than reusing the grant; controller authority also remains tied to the authenticated teacher browser session | ADR-0011 Pairing Grant Model; `FR-SESSION-025` | Yes | Store/lifetime fields | No conflict |
| Pairing establishment order | Display may arrive before Controller or Controller before Display | Classroom Session edge cases | Yes | No | The primary-flow ordering is illustrative, not a semantic invariant |
| `DIRECT_ACTION` | Explicitly supported deterministic, non-generative actions may bypass AI proposal generation after authorization and command validation | PR-017; ADR-0006 `INV-016`; System Architecture 6.3 | Yes | Broader taxonomy | No AI path is involved |
| `NEXT` action | Navigation is a supported deterministic direct-action class and changes authoritative scene position/revision | ADR-0006; `FR-CANVAS-003`; Live Adaptation test scenario for "halaman berikutnya" | Yes | `PREVIOUS` and all other actions | `NEXT` is the only action frozen for this slice |
| Command identity | Retry/reconciliation requires a stable command identity distinct from transport request correlation | `FR-SESSION-014`; ADR-0007; API Standard section 14 | Yes | Exact placement/name | No field name is selected |
| Expected/current revision | Command presents its expected authoritative revision; backend compares it with current authority | `FR-SESSION-006`, `FR-SESSION-013`; `INV-009` | Yes | Exact representation | No initial value or identifier encoding is selected |
| Stale command rejection | Stale/conflicting assumptions must not overwrite authoritative state | J-04; `FR-SESSION-013`; ADR-0007 | Yes | Exact error mapping | No conflict |
| Duplicate/uncertain acknowledgement | A duplicate accepted command must not mutate twice; a pre-disconnect command with uncertain acknowledgement is reconciled by identity/idempotency/revision | J-04; `FR-SESSION-014`; ADR-0007 | Yes | Equivalence rules and retention window | Newly created offline commands are excluded |
| Role-specific projections | Backend derives projection according to participant authority | PR-002; `FR-SESSION-005`; ADR-0002 | Yes | Controller-private stream contract | Both projection classes remain architecturally required |
| Classroom-safe Display projection | Display receives only data required for student-facing rendering and no teacher-private state, secrets, credentials, provider data, or diagnostics | PR-002; `FR-CANVAS-002`; `INV-003`; security/privacy baseline | Yes | Full future scene model | Explicit confidentiality boundary |
| Synchronous HTTP command boundary | State-changing commands use authorized synchronous HTTPS/JSON, never the push channel | ADR-0012; `INV-029` | Yes | Paths/method/status details | No WebSocket/STOMP/message command path |
| SSE push boundary | Backend pushes role-specific authoritative projection updates over per-participant SSE | ADR-0012; System Architecture 6.4 | Display stream: Yes | Controller-private stream in this slice | Controller SSE remains required by the broader architecture, but is not necessary to prove `NEXT` |
| Event revision / `Last-Event-ID` | Event ID carries authoritative revision or a monotonic sequence bound to it and is used as a reconnect resync entry point | ADR-0012; `INV-030` | Yes | Exact event names/encoding | Event ID is advisory; it does not authorize mutation |
| Reconnect/resynchronization | Client reconciles with backend authority and retrieves current state/delta before mutation resumes | J-04; `FR-SESSION-007`, `FR-SESSION-013`; ADR-0007/0012 | Yes | Delta catch-up | First slice selects full authoritative snapshot as the minimum recovery baseline |
| Mutation freeze without backend authority | Preserve last-known safe projection, freeze new authoritative mutations, and do not promote cached state | PR-021-PR-023; `FR-SESSION-011`-`FR-SESSION-016`; `INV-017`-`INV-020` | Yes | No | No offline authority |
| RFC 9457 error boundary | HTTP failures use `application/problem+json`; stable machine-readable extensions may be added later | ADR-0010; System Architecture section 12 | Yes | Exact codes/type URIs/status mapping | Scope categories are frozen, fields are not |
| Authorization/privacy boundaries | Backend enforces authentication, ownership, participant role, lifecycle, and projection confidentiality; public errors/logs exclude sensitive details | PR-011, PR-025; ADR-0011; Security Standard | Yes | Concrete security scheme names | No conflict |

No contradiction was found among the current canonical sources. Historical
statements inside ADR-0010 and ADR-0011 that left OAD-005 open are resolved by
the later Accepted ADR-0012 and do not change those ADRs' original decision
records.

## 7. In-Scope Matrix

| Boundary | Frozen semantic scope |
|---|---|
| Authentication/session | Current authenticated active Teacher context; backend-managed browser session; protected teacher operations; object/session ownership checks; no concrete OIDC provider |
| Lesson | Select/reference an existing owned or authorized immutable classroom-ready `LessonVersion`; reject a non-ready version |
| Classroom Session | Start from that version; establish session identity, command-permitting lifecycle state, minimum current lesson/scene position, authoritative revision, and classroom-safe snapshot |
| Pairing/participants | Issue and redeem role/session-bound grants; five-minute expiry; single-use; revocable; separate participant sessions; one active Controller and one active Display; either establishment order |
| Direct command | Only deterministic `DIRECT_ACTION: NEXT`; authorize teacher and active Controller; validate identity and expected revision; reject stale/unsupported use; reconcile duplicates/uncertain acknowledgement; mutate exactly once |
| Display projection | Session/revision and only the minimum classroom-safe current position/content needed to render the result of `NEXT` |
| SSE/recovery | Authorized Display stream; event revision/sequence; `Last-Event-ID`; full authoritative snapshot resync when required; no mutation until synchronized |
| Error foundation | Scope categories for unauthenticated, forbidden/ownership, not found, lesson-not-ready, invalid session state, pairing-grant failures, incompatible role, revoked participant, stale/conflict, invalid/unsupported command, synchronization required, and authority unavailable |

## 8. Explicit Out-of-Scope Matrix

| Area | Deferred from this first slice |
|---|---|
| Lesson generation | AI lesson generation, OpenRouter calls, curriculum retrieval implementation, Mathematics validator implementation, regeneration, lesson editing, and a newly invented manual/non-AI creation path |
| Live adaptation | Semantic AI adaptation, proposal preview/approval/publication, speech/Deepgram, and AI allowance behavior |
| Classroom actions | `PREVIOUS`, arbitrary navigation, annotation, digital ink, show/hide, zoom, undo/redo, clear, and other direct-action taxonomy members |
| Scene model | Complete Classroom Scene/content model beyond the minimum already-reviewed content/position needed to prove `NEXT` |
| Session lifecycle | End, save, retained history, deletion, export, and account deletion unless a later slice explicitly activates them |
| Participant UX | Controller/display replacement or handoff UX, human-entered short-code design, and account recovery |
| Clients/transport | Student devices, WebSocket, STOMP, polling, broker/queue, AsyncAPI, and controller-private SSE contract for this slice |
| Identity/provider | Concrete OIDC provider, provider-specific payloads, password auth, and account-linking/recovery UX |
| Persistence/runtime | Physical database schema/migrations, Java/Spring source, React/TypeScript source, Docker/deployment, and CI/CD |
| Contract breadth | Complete Penatika API surface and any field-level OpenAPI/JSON Schema artifact in Batch 1 |

## 9. Architecture and Product Invariants Constraining Contracts

- The backend is the only classroom/session state authority (`INV-001`,
  `INV-002`).
- Every accepted state-changing command binds an authorized actor, classroom
  session, command identity, and revision context (`INV-009`).
- `NEXT` is deterministic and non-generative; it does not enter the AI
  proposal/publication path (`INV-016`).
- Pairing grants never become teacher authentication or account authority
  (`INV-025`).
- Controller and Display authorities and projections remain separate
  (`INV-026`, `INV-027`).
- The Display receives only a classroom-safe projection (`INV-003`).
- Commands remain synchronous HTTP; SSE carries projections only (`INV-029`).
- SSE event identity assists resync but does not decide mutation safety
  (`INV-030`).
- Cached state never becomes authority; reconnect must reconcile before
  mutation resumes; newly created offline commands are not replayed
  (`INV-017`-`INV-020`).
- Degradation cannot bypass authorization, privacy, structured-content, or
  stale/revision controls (`INV-022`).

## 10. Lesson Prerequisite and No Invented Manual Authoring

The executable slice assumes an existing classroom-ready `LessonVersion` as a
fixture or pre-existing state. The version remains selected/referenced by
session start and must be owned by or authorized to the Teacher.

This is an engineering sequence decision only. It does not add a manual
lesson-authoring product path and does not change `CAP-LESSON-001` or the
AI-assisted review flow.

## 11. Pairing Order Independence

The required end state is exactly one authorized `TEACHER_CONTROLLER` and one
authorized `CLASSROOM_DISPLAY` for the Classroom Session. Either participant
may arrive first. Contract design must not encode Controller-before-Display or
Display-before-Controller as an invariant.

Role-bound grants cannot be reused for the other role. Successful redemption
establishes a separate revocable participant session; the controller also
requires the active authenticated and session-authorized Teacher browser
session.

## 12. Command Identity and Revision Semantics

For the first slice, `NEXT` must carry enough semantics for the backend to:

1. identify the command independently of request correlation;
2. identify the target Classroom Session;
3. validate the active Teacher and Controller authority;
4. compare the command's expected revision with the backend's current
   authoritative revision;
5. reject stale, conflicting, unsupported, or unauthorized commands;
6. ensure the same accepted command cannot advance the position/revision more
   than once; and
7. retrieve or return the authoritative outcome when acknowledgement of a
   pre-disconnect submission is uncertain.

Exact idempotency placement, request-equivalence rules, conflicting key reuse,
and deduplication retention duration remain field-level decisions.

## 13. Projection Confidentiality Boundary

The Display contract is a positive allow-list: it contains only data required
to render the current student-facing lesson/scene position and the
authoritative revision/synchronization context required by this slice.

Teacher-private AI state, prompts, transcripts, warnings, diagnostics,
pairing secrets, browser/participant credentials, account data, provider
payloads, and internal policy/assurance detail do not cross this boundary.

## 14. SSE and Reconnect Minimum Semantics

Commands remain synchronous HTTPS/JSON. The first slice defines only the
Display's authorized `text/event-stream` projection boundary; the broader
ADR-0012 requirement for a separate Controller-private stream remains valid
and deferred to a later slice.

The minimum Display recovery baseline is:

```text
disconnect
-> preserve last-known safe classroom projection as non-authoritative
-> reconnect with Last-Event-ID / last-observed revision
-> compare against backend-authoritative revision
-> retrieve a full authoritative classroom-safe snapshot when resync is required
-> synchronize
-> resume student-facing mutation
```

Bounded delta catch-up is not required for the first slice. Newly created
offline commands are never queued or automatically replayed. A command sent
before disconnect with uncertain acknowledgement is reconciled through its
existing identity and revision semantics.

## 15. Field-Level Decisions Deliberately Deferred

Batch 1 does not freeze:

- endpoint paths, HTTP methods per operation, or concrete request/response
  DTOs;
- JSON property names or identifier representation;
- exact status-code mapping beyond the established HTTP/RFC 9457 policy;
- RFC 9457 extension names/values, exact error `code` strings, or problem-type
  URI strategy;
- cookie, CSRF token/header, or OpenAPI security-scheme names;
- event names, payload fields, heartbeat/retry intervals, or snapshot path;
- exact initial revision value or revision encoding;
- idempotency header versus command-body placement and deduplication duration;
- exact pairing credential representation, participant-session storage, or
  expiry/index mechanics;
- schema decomposition and `$ref` ownership choices for the eventual wire
  models; or
- physical database and source-code representations.

## 16. Risks and Unresolved Issues

No scope-blocking product or architecture conflict was found.

The field-level representation of the minimum classroom-safe content needed
to render the initial position and the result of `NEXT` remains deliberately
unresolved for the later scene/projection contract batch. Exact command
idempotency and error mappings also remain deliberate Batch 2+ decisions.

Representative school-network evidence for long-lived SSE connections remains
an implementation/pilot evidence requirement, not a blocker for this scope
freeze.

## 17. Exit Criteria for Batch 1

- The first architecture-proving flow is frozen from canonical evidence.
- In-scope, out-of-scope, and field-level deferred decisions are explicit.
- The lesson prerequisite does not invent a new product workflow.
- Pairing establishment order is explicitly independent.
- Command identity/revision, confidentiality, SSE, and recovery semantics are
  preserved.
- Project/contract routing documents reflect the completed Architecture merge
  and active Field-Level Contract Foundation phase.
- No field-level contract, schema, source, migration, or deployment artifact
  is created.
- Repository validation passes.

## 18. Next Batch

Batch 2 should define the shared wire primitives and base RFC 9457 HTTP problem
contract required by this frozen slice, including compatibility rules and
representative examples, without yet broadening into the complete classroom
API surface.

Batch 2 must re-read this checkpoint only as a handoff snapshot and derive
field decisions from the canonical sources listed above.

## 19. Validation Evidence

- `git diff --check` - passed.
- `git diff --no-index --check /dev/null
  docs/checkpoints/2026-09-08-contract-foundation-scope-freeze.md` - passed
  for the new untracked checkpoint file.
- `python3 scripts/validate_template.py --project-mode` -
  `Validation passed (project mode).`
- Stale-phase search - no current routing statement says Architecture
  Foundation still needs merge, OAD-005 remains unresolved, source
  scaffolding is active, or realtime semantics remain undecided. Matches in
  historical changelog/checkpoint or supersession explanations remain
  intentionally historical.
- Source/contract boundary check - only contract README files exist; no
  `openapi.yaml`, standalone production schema, Java/React source scaffold,
  application build file, migration, Docker/Compose/Caddy, or CI artifact was
  created.
- Final `git status`, `git diff --stat`, and full diff review - completed; the
  working tree contains only the four documentation files listed by this
  Batch 1 change.
