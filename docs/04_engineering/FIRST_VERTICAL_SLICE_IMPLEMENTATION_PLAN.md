# First Protected Vertical Slice Implementation Plan

> **Document role:** Working engineering implementation plan. This document
> does not own product behavior, architecture, or wire contracts.
>
> If this plan conflicts with canonical Product/Feature documents, Accepted
> ADRs, or machine-readable contracts, the canonical source wins and this plan
> must be corrected.

## 1. Purpose

This plan translates the accepted Product, Architecture, Contract, and Source
Scaffolding foundations into the implementation sequence for Penatika's first
protected vertical slice. It freezes only implementation-level choices needed
to begin coding safely. It does not reopen settled behavior or modify canonical
contracts. Completing the slice proves an architecture flow, not pilot
readiness.

## 2. Authoritative Inputs

### Product and Feature Behavior

- [Product Brief](../00_product/PRODUCT_BRIEF.md)
- [PRD](../00_product/PRD.md), especially `CAP-LESSON-001`,
  `CAP-SESSION-001`, and `CAP-SESSION-002`
- [Lesson Preparation](../01_features/lesson-preparation.md), especially
  `FR-LESSON-007` and `FR-LESSON-008`
- [Classroom Session](../01_features/classroom-session.md), especially
  `FR-SESSION-001`–`FR-SESSION-016` and `FR-SESSION-021`–`FR-SESSION-025`
- [Classroom Canvas](../01_features/classroom-canvas.md), especially
  `FR-CANVAS-002`, `FR-CANVAS-003`, `FR-CANVAS-005`, and `FR-CANVAS-006`

### Architecture and Data

- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [Conceptual Data Model](../02_architecture/DATA_MODEL.md)
- [ADR-0001](../02_architecture/adr/ADR-0001-modular-monolith-backend.md)
- [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md)
- [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0008](../02_architecture/adr/ADR-0008-browser-first-react-client-strategy.md)
- [ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0011](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0012](../02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md)
- [ADR-0013](../02_architecture/adr/ADR-0013-java21-module-first-hexagonal-backend.md)
- [ADR-0014](../02_architecture/adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)

### Contracts and Engineering Baseline

- [Contract ownership](../../contracts/README.md)
- [OpenAPI](../../contracts/openapi/openapi.yaml)
- [Wire primitives](../../contracts/schemas/wire-primitives.schema.json)
- [Display projection](../../contracts/schemas/classroom-display-projection.schema.json)
- [Source Scaffolding Plan](./SOURCE_SCAFFOLDING_PLAN.md)
- [Test Strategy](./TEST_STRATEGY.md)
- [Developer Setup](../05_operations/DEVELOPER_SETUP.md)
- Relevant standards for architecture, backend, frontend, API, persistence,
  security, testing, and reliability.

The contracted operation inventory is fixed:

1. `GET /api/teacher-session`
2. `POST /api/classroom-sessions`
3. `POST /api/classroom-sessions/{classroomSessionId}/pairing-grants`
4. `DELETE /api/classroom-sessions/{classroomSessionId}/pairing-grants/{pairingGrantId}`
5. `POST /api/teacher-controller-participants`
6. `POST /api/classroom-display-participants`
7. `GET /api/classroom-sessions/{classroomSessionId}/controller-state`
8. `POST /api/classroom-sessions/{classroomSessionId}/commands`
9. `GET /api/classroom-sessions/{classroomSessionId}/display-snapshot`
10. `GET /api/classroom-sessions/{classroomSessionId}/display-events`
11. `PUT /api/classroom-sessions/{classroomSessionId}/display-synchronization`

No operation may be added, removed, or semantically changed by this plan.

## 3. Locked First Vertical Slice

```text
Authenticated Teacher with active backend-managed browser session
→ select an existing authorized, reviewed, classroom-ready LessonVersion
→ start a backend-authoritative Classroom Session with position and Revision
→ establish one TEACHER_CONTROLLER and one CLASSROOM_DISPLAY through
  role-bound PairingGrants, in either order
→ Display receives, validates, and applies the current full safe projection
→ Display acknowledges that Revision on its current active SSE stream
→ Controller reconciles the current authoritative Revision
→ Controller submits DIRECT_ACTION:NEXT with CommandId + expectedRevision
→ backend validates Teacher/account/ownership, active Controller authority,
  Display synchronization, command identity/equivalence, and Revision
→ backend changes position at most once and persists the original outcome
→ backend returns the authoritative HTTP outcome
→ backend pushes the resulting full safe projection over SSE
→ Display validates, replaces, and acknowledges the resulting Revision
→ later mutation becomes eligible
→ after reconnect, both clients reconcile before new mutation resumes
```

Pairing, dual Teacher + Controller authority, Revision, CommandId, accepted
idempotent replay, Display synchronization, and reconnect recovery are not
optional simplifications.

## 4. Scope and Non-Goals

### Complete-Slice Scope

- Provider-neutral OIDC login and backend-managed Teacher sessions.
- Existing authorized classroom-ready LessonVersion resolution.
- Durable Classroom Session authority.
- Pairing grant lifecycle and participant sessions.
- Controller reconciliation and deterministic `NEXT`.
- Durable idempotency and optimistic revision enforcement.
- Safe Display projection, snapshot, SSE, and acknowledgement.
- Minimal Teacher and Display browser integration.
- PostgreSQL and contract-conforming failure evidence.

### Non-Goals

- Lesson authoring/generation, curriculum retrieval, AI, speech, or ink.
- Commands other than `DIRECT_ACTION:NEXT`.
- Scene blocks beyond schema `1.0` `PLAIN_TEXT`.
- Session end/save/history/export/deletion.
- Participant replacement/handoff APIs or UX.
- Concrete production OIDC provider or account self-provisioning.
- Controller-private SSE.
- Offline command queues or new-command automatic replay.
- WebSocket, STOMP, broker, queue, Redis, WebFlux, or AsyncAPI.
- Docker, deployment, CI/CD, or pilot execution.

## 5. Current Physical Baseline

- Java 21/Spring Boot/Spring MVC with JDBC/JdbcClient, Flyway, PostgreSQL,
  Testcontainers, and `identity`, `lesson`, `classroom` roots.
- Accepted V001 and module-owned PostgreSQL persistence adapters cover the
  first protected-slice durable model.
- IVS-03 provides accepted provider-neutral OIDC infrastructure,
  existing-account Teacher resolution, PostgreSQL-authoritative browser
  sessions, secure session/CSRF cookies, and `GET /api/teacher-session`.
- IVS-04 adds ownership-scoped LessonVersion eligibility resolution,
  session-bound Teacher CSRF authorization, and authoritative
  `POST /api/classroom-sessions` creation.
- Separate Teacher and Display React/TypeScript/Vite applications exist.
- Role-scoped generated transport declarations exist.
- OpenAPI `0.8.0` and the closed Display schema are authoritative.
- IVS-04 Classroom Session start behavior is COMPLETE after human review.

## 6. Implementation Decision Register

`LOCKED` is frozen now. `LOCK IN BATCH` is an owning-batch physical choice.
`DEFERRED` is outside this slice. `BLOCKING QUESTION` requires human/security
input before the owning batch can complete.

| ID | Concern | Decision / Status | Owning Batch | Rationale |
|---|---|---|---|---|
| ID-01 | Persistence subset | `LOCKED`: persist Teacher identity/session, Lesson/LessonVersion, Classroom Session, PairingGrant, participant sessions, and accepted command outcomes. SSE and Display acknowledgement stay process-local. | IVS-02 | Correctness, revocation, and idempotency must survive restart; live connections cannot be persisted meaningfully. |
| ID-02 | First migration | `LOCKED`: one coherent `V001` first-slice schema before repositories, with module-owned tables and constraints. | IVS-02 | Empty database and cross-cutting first-slice integrity favor one audited baseline. |
| ID-03 | LessonVersion prerequisite | `LOCKED`: real Lesson/LessonVersion persistence; automated records are test fixtures only. No production seed/create endpoint. | IVS-02/04 | Meets the prerequisite without inventing authoring behavior. |
| ID-04 | Teacher authentication | `LOCKED`: Spring Security OAuth2 Client/OIDC, Authorization Code + PKCE S256, provider-neutral configuration, and test-only provider support. Production provider `DEFERRED`. | IVS-03 | Implements ADR-0011 without production mock auth or an unapproved provider. |
| ID-05 | Teacher session store | `LOCKED`: PostgreSQL opaque sessions with verifier-only credential storage, Teacher binding, CSRF association, rotation, activity, revocation, a 30-minute sliding idle timeout, and a fixed 8-hour absolute timeout from creation. | IVS-02/03 | Durable revocation/restart authority and bounded server-owned lifetimes are required; Redis is not justified. |
| ID-06 | Participant sessions | `LOCKED`: PostgreSQL opaque sessions with verifier-only storage, session/role binding, active/revoked state, Controller Teacher binding, one-active-role constraints, and a fixed eight-hour absolute lifetime from creation with no idle timeout. | IVS-02/05 | Authority/revocation must survive restart and concurrency, while a non-sliding maximum lifetime bounds credential reuse exposure. |
| ID-07 | Pairing secret | `LOCKED`: generate 32 CSPRNG bytes (256 bits), encode as 43-character unpadded Base64URL, return once, persist only the lowercase 64-character SHA-256 verifier plus metadata, and consume atomically. Five-minute expiry and single-use/revocation semantics remain unchanged. | IVS-05 | High-entropy machine-transferred token verification needs no reusable plaintext or password work factor. |
| ID-08 | Command idempotency | `LOCKED`: unique `(ClassroomSessionId, CommandId)` record with original request identity, participant context, and resulting Revision. | IVS-02/06 | Accepted outcomes must survive restart and uncertain acknowledgement. |
| ID-09 | Classroom authority | `LOCKED`: persist selected LessonVersion, lifecycle, current position, and session-scoped monotonic Revision. Initial/increment mechanics `LOCK IN BATCH`, not client guarantees. | IVS-02/04/06 | Preserves one authority and the Revision/position distinction. |
| ID-10 | Display projection | `LOCKED`: derive from Classroom Session + immutable LessonVersion; no second mutable persisted projection. | IVS-07 | Prevents divergent truths. |
| ID-11 | SSE registry | `LOCKED`: Spring MVC emitters, stream generation, dispatch state, and lifecycle are process-local behind Classroom publisher boundary. | IVS-08 | Stream objects are process resources and restart must fail closed. |
| ID-12 | Display acknowledgement | `LOCKED`: process-local, bound to active Display + stream generation; invalidate on new Revision, termination, revocation/replacement, end, authority loss, or restart. | IVS-08 | Acknowledgement must never outlive its qualifying stream. |
| ID-13 | Transactions | `LOCKED`: application use cases own session start, grant redeem/revoke, participant slot, `NEXT`, and acknowledgement transaction boundaries. | IVS-04–08 | Transactions belong with orchestration, not HTTP or generic repositories. |
| ID-14 | Time | `LOCKED`: inject `java.time.Clock`; persist UTC instants; no scattered `Instant.now()`. | IVS-03–06 | Deterministic expiry/lifecycle tests. |
| ID-15 | Randomness | `LOCKED`: module-owned ports with `SecureRandom` production and deterministic test fakes. | IVS-03/05 | Secure credentials without a global utility dumping ground. |
| ID-16 | Backend wire models | `LOCKED`: handwritten adapter-local Java models/mappers; no server-stub generation. | IVS-03–08 | Existing generation strategy is frontend transport-only. |
| ID-17 | Frontend state/routing | `LOCKED`: explicit role-local state machines with `fetch`/`EventSource`; no router/query/state library without evidence. | IVS-09 | Each app has one bounded journey. |
| ID-18 | Browser E2E tooling | `DEFERRED`: reassess Playwright in IVS-10 only if existing test layers leave an evidence gap. | IVS-10 | Avoid premature browser-harness dependency. |

## 7. First-Slice Physical Data Boundary

### Classification

| Concept | Classification | Owner | Reason |
|---|---|---|---|
| TeacherAccount | `PERSISTENT NOW` | Identity | Stable ownership/account state. |
| ExternalIdentityLink | `PERSISTENT NOW` | Identity | Unique validated `(issuer, subject)`. |
| AuthenticatedBrowserSession | `PERSISTENT NOW` | Identity | Durable revocation, expiry, rotation, CSRF. |
| Lesson | `PERSISTENT NOW` | Lesson | Owns immutable prerequisite. |
| LessonVersion | `PERSISTENT NOW` | Lesson | Ready immutable scenes for session start. |
| First-slice lesson records | `TEST FIXTURE ONLY` | Lesson tests | No production seed/authoring feature. |
| ClassroomSession | `PERSISTENT NOW` | Classroom | Durable backend authority. |
| PairingGrant | `PERSISTENT NOW` | Classroom + Identity coordination | Atomic expiry/revocation/single use. |
| Participant sessions | `PERSISTENT NOW` | Identity + Classroom coordination | Durable role authority/revocation. |
| Accepted command outcome | `PERSISTENT NOW` | Classroom | Restart-safe original outcome. |
| SSE emitter/connection | `EPHEMERAL NOW` | Classroom SSE adapter | Process-local stream. |
| Display dispatched/ack state | `EPHEMERAL NOW` | Classroom SSE adapter | Current stream only. |
| OIDC state/nonce/PKCE verifier | `EPHEMERAL NOW` | Identity OIDC adapter | Short-lived auth transaction. |
| Upstream OIDC tokens | `DEFERRED` unless adapter-required | Identity | No provider API need; never browser credentials. |
| Authoring/generation/provenance | `DEFERRED` | Lesson/later modules | Outside slice. |
| Save/history/export/deletion | `DEFERRED` | Classroom | Outside contract. |
| Rich scene/ink/AI/speech/curriculum data | `DEFERRED` | Later owners | Outside proof. |

### Planned Records and Migration Boundary

No SQL or exact column inventory is defined here. IVS-02 creates records for
Teacher account, external identity link, Teacher session, Lesson, LessonVersion,
Classroom Session, PairingGrant, participant session, and accepted command.
Critical constraints include unique external identity/credential verifiers,
five-minute atomic grant consumption, one active Controller/Display slot,
unique session/CommandId, ownership foreign keys, valid states/roles, and
Revision/position bounds.

- First migration: `V001__create_first_protected_slice.sql`.
- Use lower `snake_case` table/constraint names and preserve module write
  ownership; no generic shared persistence module.
- Default physical primary keys are application-generated PostgreSQL `uuid`.
  Public identifiers remain opaque and expose no UUID guarantee.
- Use `timestamptz` for UTC instants. JSONB, database enums, arrays, generated
  columns, and extension-dependent ID functions are not needed now.
- Cross-module foreign keys may protect shared-database integrity, but Java
  modules communicate through supported application interfaces, not adapters.

## 8. Backend Ownership and Package Materialization

Create packages only when a real batch responsibility exists:

```text
identity/{domain,application/port/in,application/port/out,
          adapter/in/http,adapter/out/oidc,adapter/out/persistence/postgres}
lesson/{domain,application/port/in,application/port/out,
        adapter/out/persistence/postgres}
classroom/{domain,application/port/in,application/port/out,
           adapter/in/http,adapter/out/persistence/postgres,adapter/out/sse}
```

- **Identity** owns Teacher/external identity, Teacher sessions/CSRF,
  participant credential resolution/role authority, revocation, and credential
  generation. Spring/provider details stay in adapters/composition.
- **Lesson** owns immutable lesson/version readiness, ownership, ordered
  first-slice scenes, and `PLAIN_TEXT`; its first use case is read-only
  resolution for session start. No lesson HTTP adapter is needed.
- **Classroom** owns session state, PairingGrant coordination, Controller
  reconciliation, deterministic command/Revision/idempotency, Display
  projection/snapshot, SSE dispatch, and synchronization gate. Do not create a
  monolithic `ClassroomService`.

## 9. HTTP and Contract Mapping Strategy

- Implement exactly the eleven operations with adapter-local wire models.
- Framework validation owns shape; application/domain owns semantics and auth.
- Use explicit wire-to-application mapping; wire models never become domain.
- Centralize RFC 9457 mapping using existing operation-specific statuses/codes
  without leaking exception, SQL, identity, or ownership details.
- Protected JSON APIs return problems, not OIDC redirects. Login/callback are
  an authentication adapter boundary outside the eleven operations.
- Frontends consume existing role-scoped declarations and map into local state.
- Do not generate backend server stubs; prove conformance with HTTP integration
  tests and the existing contract harness.

## 10. Frontend Integration Strategy

- **Teacher Web:** bootstrap session/CSRF, use an existing LessonVersion ID,
  start session, manage grants, redeem Controller, reconcile Revision, submit
  `NEXT`, reuse the same CommandId only for uncertain retry, and freeze/reconcile
  on stale or unavailable authority.
- **Display Web:** redeem Display, retrieve snapshot, open SSE, validate closed
  projection, replace full state, acknowledge after apply, preserve last safe
  projection, and reconnect/acknowledge before mutation resumes.
- Use `@penatika/transport-teacher`, `@penatika/transport-display`, same-origin
  cookies, `fetch`, `EventSource`, and explicit reducers/state transitions.
- No router or query/state framework is required. IVS-09 selects a deterministic
  contract-derived runtime projection validator; no handwritten schema copy.

## 11. Security Runtime Strategy

- Add Spring Security/OAuth2 Client in IVS-03 with provider-neutral issuer/client
  configuration, Authorization Code, PKCE S256, state, nonce, redirect, issuer,
  audience, signature, and time validation.
- Resolve pre-provisioned active TeacherAccount/ExternalIdentityLink only;
  onboarding and auto-provisioning are deferred.
- Upstream tokens stay server-side and are discarded unless adapter-required.
  Production never accepts mock-auth headers/test identity shortcuts.
- Teacher cookie: opaque `__Host-penatika-session`, `Secure`, `HttpOnly`,
  `Path=/`, no `Domain`, `SameSite=Strict` for the same-origin slice; persist
  only the lowercase 64-character SHA-256 verifier for a credential generated
  from exactly 32 CSPRNG bytes (256 bits) and encoded as 43-character unpadded
  Base64URL; rotate after authentication.
- Teacher session idle expiry is 30 minutes and slides only from qualifying
  successful authenticated Teacher application activity. Absolute expiry is
  fixed at eight hours from creation and never slides. A session is unusable
  when revoked, its TeacherAccount is not active, or current server time is at
  or after either expiry. Client-provided timestamps never extend authority.
  Static assets, health/readiness probes, anonymous requests, failed
  authentication, rejected malformed requests, passive Display/SSE heartbeat,
  and unrelated keep-alive polling do not qualify as Teacher activity. Any
  idle refresh remains capped by the fixed absolute expiry.
- Teacher CSRF: session-bound synchronizer token from `GET /api/teacher-session`,
  generated from exactly 32 CSPRNG bytes (256 bits), encoded as 43-character
  unpadded Base64URL, and persisted only as a lowercase 64-character SHA-256
  verifier. It is unique to the Teacher session, rotates with session
  replacement/rotation, is exposed raw only through that authenticated
  contract-approved bootstrap boundary, and is returned in
  `X-Penatika-CSRF`. It never enters URLs/query strings or logs. Combine it
  with strict origin/CORS policy.
- Participant sessions created in IVS-05 use the injected server `Clock`:
  `createdAt = Clock.instant()` and `expiresAt = createdAt + 8 hours`. This
  fixed, non-sliding absolute lifetime applies to both `TEACHER_CONTROLLER` and
  `CLASSROOM_DISPLAY`. IVS-05 runtime creation always persists a non-null
  `expiresAt` even though the accepted V001 column remains physically nullable;
  OIQ-03 requires neither a V001 change nor a V002 migration.
- Participant sessions have no idle timeout and no participant `lastActiveAt`
  expiry semantics in this slice. Controller HTTP requests or reconciliation,
  commands, Display snapshots, SSE connections or heartbeats, reconnects,
  browser polling, synchronization acknowledgements, and unrelated traffic
  never move `createdAt` or `expiresAt` and never renew the credential.
- Server time is authoritative: `now < expiresAt` is potentially usable and
  `now >= expiresAt` is expired, including exact equality. Client timestamps
  never control or extend participant authority.
- Eight hours is an upper bound, not a guarantee of authority. Explicit
  revocation, replacement/handoff, loss of the current role slot, a Classroom
  Session that no longer permits the role, Classroom Session end when
  implemented, and any other canonical invalidation rule make the participant
  unusable earlier.
- Effective Controller authority requires a valid participant credential,
  `now < expiresAt`, no revocation/replacement, the current
  `TEACHER_CONTROLLER` role slot, valid owning Classroom Session authority, an
  `ACTIVE` linked `TeacherAccount`, a currently usable linked
  `TeacherBrowserSession`, and a matching Classroom Session ownership binding.
  Evaluate Teacher authority independently; do not persist
  `min(participantExpiry, teacherBrowserSessionExpiry)` as participant expiry.
- Effective Display authority requires a valid participant credential,
  `now < expiresAt`, no revocation/replacement, the current
  `CLASSROOM_DISPLAY` role slot, and an owning Classroom Session that still
  permits Display authority. Display requires no `TeacherAccount` or
  `TeacherBrowserSession` and receives no Teacher privilege.
- A reconnect using the same still-valid, non-revoked, non-replaced credential
  may retain the same participant identity, role authority, and original
  `expiresAt`; it does not start another eight-hour period. A successful new
  PairingGrant redemption establishes a new participant identity and credential
  with a new `createdAt` and fixed eight-hour `expiresAt`, subject to the
  existing role-slot and replacement rules.
- Participant cookie: protected `__Host-penatika-participant`, verifier-only,
  bound to one session/role, using the same 32-byte/256-bit CSPRNG,
  43-character unpadded Base64URL, and lowercase 64-character SHA-256 verifier
  baseline. When issued, cookie `Max-Age` must not exceed the participant's
  remaining absolute lifetime and should equal that remaining lifetime. Cookie
  expiry is client cleanup only; PostgreSQL authority remains decisive, and a
  stale cookie grants nothing after expiry, revocation, replacement, or other
  session invalidation.
- Participant credentials are security-sensitive browser credentials, not
  Teacher authentication tokens. Their finite absolute lifetime bounds but
  does not eliminate theft/reuse exposure; omitting an idle timeout avoids
  ambiguous activity semantics for continuously connected classroom devices,
  while the fixed deadline cannot be extended by attacker-generated traffic.
  IVS-05 introduces no automatic rotation, renewal, refresh-token equivalent,
  or activity/SSE/reconnect-based extension.
- Display acknowledgement requires `X-Penatika-Display-Intent: synchronize`
  plus strict origin policy.
- Pairing token is a machine-transferred credential generated from exactly 32
  CSPRNG bytes (256 bits), encoded as 43-character unpadded Base64URL, returned
  once with `no-store`, never logged, and stored only as a lowercase
  64-character SHA-256 verifier. It expires exactly five minutes from injected
  `Clock` and retains existing single-use/revocation semantics. Do not
  introduce a human-entered short pairing code without a separate reviewed
  guessing/rate/attempt-control design.
- Production security-sensitive randomness uses `java.security.SecureRandom`
  behind module-owned application boundaries; deterministic test fakes are
  permitted. Do not introduce a global random/security utility or substitute
  `java.util.Random`, timestamps, UUID text, predictable counters, or
  frontend-generated authoritative secrets.
- OIDC `state`, `nonce`, and PKCE verifier remain ephemeral, transaction-
  specific, cryptographically secure values; PKCE uses S256. They are not
  persisted in V001 or converted into browser-session credentials.

## 12. Transaction and Idempotency Strategy

- **Session start:** one application transaction resolves active Teacher and
  authorized ready LessonVersion, creates initial durable Classroom authority,
  and then returns.
- **Grant redemption:** one transaction claims the verifier, validates expiry/
  consume/revoke/session/role/Teacher/slot, establishes participant session,
  consumes grant, and commits. Database uniqueness is authoritative.
- **`NEXT`:** validate Teacher + Controller; look up accepted session/CommandId;
  return original outcome only for complete equivalent retry; reject changed
  reuse; for unseen command require acknowledged active Display at current
  Revision and matching expectedRevision; resolve next position; advance state
  and Revision; store original request/outcome; commit once; dispatch full
  projection after commit; leave new Revision unacknowledged.
- Do not call SSE while holding database locks. Dispatch failure leaves durable
  state advanced but synchronization closed, preventing later mutation.
- **Display acknowledgement:** validate active Display/intent and match request
  Revision to durable current Revision plus process-local stream/dispatch state.
  Acceptance changes only the process-local gate, never content/Revision.

## 13. Recovery Strategy

| Scenario | Required Behavior |
|---|---|
| Healthy | Display acknowledged at `R`; Controller reconciled at `R`; `NEXT` commits `R2`; HTTP returns `R2`; projection `R2` sent/applied/acknowledged. |
| Equivalent duplicate | Return stored original outcome; no second mutation. |
| Changed CommandId reuse | Reject conflict; preserve original outcome. |
| Stale Controller | Reject; Controller GET reconciles; new CommandId only for retained new intent. |
| Uncertain HTTP outcome | Retry exact original request with same CommandId. |
| Display loss | Termination invalidates gate; mutation blocks; reconnect gets current full projection and re-acknowledges. |
| Backend restart | Durable authority/outcomes remain; stream/ack state is absent and fails closed. |
| Backend unavailable | Preserve safe rendering, queue no new command, freeze and reconcile. |
| Invalid Last-Event-ID | Reject before stream establishment. |
| Valid behind/equal/ahead Last-Event-ID | Advisory only; send current full projection. |

## 14. Testing Strategy

- **Domain/unit:** pairing lifecycle with fake Clock; deterministic `NEXT`;
  Revision invariants; command equivalence; projection allow-list/privacy.
- **Application:** Teacher/ownership/Controller combinations; ready LessonVersion;
  grant issue/revoke/redeem/replay/role/slot; stale/duplicate/uncertain command;
  Display gate before each mutation and after reconnect; participant creation
  persists a fixed non-null eight-hour expiry; exact expiry boundary; no
  traffic-driven sliding; Controller Teacher-session/account invalidation;
  Display independence; reconnect preserves expiry while re-pair creates a new
  participant lifetime.
- **PostgreSQL:** clean `V001`; identity/credential uniqueness; concurrent grant
  consumption; active role slots; Revision update; unique session/CommandId;
  outcome reload after restart.
- **HTTP:** all eleven operations and relevant `400`/`401`/`403`/`404`/`409`
  problems; missing/disabled Teacher; wrong owner/role; Controller without
  Teacher; invalid grant/CSRF/intent/Last-Event-ID/Display acknowledgement;
  stale Revision and changed CommandId reuse; non-disclosure.
- **SSE:** initial/current full state; absent/behind/equal/ahead valid
  Last-Event-ID; invalid ID; stream cleanup; post-commit dispatch; gate
  invalidation and current-stream acknowledgement; reconnect.
- **Frontend:** Teacher mapping/CSRF/retry/reconciliation/freeze; Display role
  isolation/pairing/validation/replacement/ack/safe fallback/reconnect;
  existing boundary tests.
- **End-to-end:** real PostgreSQL and real HTTP/SSE from authenticated Teacher
  fixture and ready LessonVersion through pairing (both role orders), Display
  acknowledgement, Controller reconcile, one accepted `NEXT`, equivalent retry,
  resulting projection/ack, stream-loss block, and reconnect restore. OIDC may
  use a test-only fake provider, never a production bypass.

## 15. Implementation Dependency Map

```text
IVS-02 schema + persistence contracts
→ IVS-03 Teacher identity + browser session
→ IVS-04 LessonVersion prerequisite + Classroom Session start
→ IVS-05 Pairing grants + participant sessions
→ IVS-06 Controller reconciliation + NEXT revision/idempotency
→ IVS-07 Display projection + snapshot
→ IVS-08 Display SSE + synchronization gate
→ IVS-09 Teacher/Display frontend integration
→ IVS-10 end-to-end readiness audit
```

IVS-02 provides Identity, Lesson, Classroom, Pairing, participant, and command
persistence contracts required by later batches. Backend behavior precedes UI
integration so clients consume stable contract-conforming behavior.

## 16. IVS Batch Sequence

Each batch must stay within its allowed scope and run focused tests before
broader applicable validation.

### IVS-01 — Implementation Plan + Decision Freeze

- **Objective/outputs:** this plan, project routing, no production behavior.
- **Inputs:** §2. **Allowed:** planning/status/README only.
- **Forbidden:** source, contract, migration, ADR, checkpoint changes.
- **Prerequisite:** synchronized Source Scaffolding merge.
- **Validation/completion:** template/diff/foundation checks; IVS-02 routable.

### IVS-02 — First-Slice Physical Schema + Persistence Contracts

- **Objective/outputs:** `V001`, module-owned persistence ports/adapters,
  PostgreSQL constraint/concurrency tests, test fixture builders.
- **Inputs:** DATA_MODEL, ADR-0014, persistence standard, §6–7.
- **Allowed:** schema/SQL/mapping/repository contracts/tests.
- **Forbidden:** endpoints, security chain, OIDC, sessions, Pairing use cases,
  commands, SSE, frontend.
- **Prerequisite:** accepted IVS-01; no open question blocks IVS-02.
- **Validation/completion:** clean Flyway/PostgreSQL integration and architecture
  tests prove all durable records/constraints without production fixture data.

### IVS-03 — Teacher Identity + Backend Browser Session Runtime

- **Status:** `COMPLETE` after human review.
- **Objective/outputs:** provider-neutral OIDC, Teacher resolution, durable
  sessions/cookies/CSRF, `GET /api/teacher-session`.
- **Inputs:** ADR-0011, OpenAPI security, Threat Model/security standard.
- **Allowed:** Identity/bootstrap security. **Forbidden:** provider selection,
  passwords, auto-provisioning, Classroom behavior.
- **Prerequisites:** IVS-02 COMPLETE; OIQ-01 and OIQ-02 resolved.
- **Tests/completion:** login, rotation/revocation/expiry, disabled account,
  cookies/CSRF/HTTP/PostgreSQL; real provider-neutral session works.

### IVS-04 — LessonVersion Prerequisite + Classroom Session Start

- **Status:** `COMPLETE` after human review.
- **Objective/outputs:** authorized ready LessonVersion resolution, Classroom
  state/start, `POST /api/classroom-sessions`.
- **Inputs:** lesson/session requirements and start contract.
- **Allowed:** read-only prerequisite/start. **Forbidden:** authoring/generation,
  Pairing, commands, SSE.
- **Prerequisites:** IVS-02/03.
- **Tests/completion:** ownership/readiness/non-disclosure, scenes, initial
  state/Revision, transaction/HTTP; only legitimate fixture version starts.
- **Internal first-slice mechanics:** new sessions start at lifecycle
  `CREATED`, scene position `0`, and Revision `0`, with an application-generated
  UUID and `startedAt` from the injected `Clock`. These are internal owning-batch
  mechanics, not additional OpenAPI representation or universal revision
  guarantees; Controller, Display, synchronization, and command behavior remain
  outside IVS-04.

### IVS-05 — Pairing Grants + Participant Sessions

- **Status:** `READY TO EXECUTE`.
- **Objective/outputs:** four Pairing/participant operations, protected cookies,
  participant authority.
- **Inputs:** pairing/authority requirements and contracts.
- **Allowed:** Pairing/participant authority. **Forbidden:** replacement API,
  command, SSE.
- **Prerequisites:** IVS-03/04 COMPLETE; OIQ-02 secret entropy/encoding and
  OIQ-03 participant-session lifetime are resolved.
- **Tests/completion:** five-minute expiry, secrets, concurrency, replay,
  revocation, role/owner/session/slot/CSRF/non-disclosure; roles pair either
  order; fixed non-null eight-hour participant expiry, exact boundary, no idle
  sliding or silent renewal, cookie lifetime cap, earlier authority
  invalidation, and reconnect-versus-re-pair semantics.

### IVS-06 — Controller Reconciliation + NEXT Revision/Idempotency

- **Objective/outputs:** Controller GET, command POST, durable Revision mutation
  and accepted outcome.
- **Inputs:** recovery/revision requirements and command contract.
- **Allowed:** reconciliation + `NEXT`. **Forbidden:** other actions, AI,
  Display transport, offline queue.
- **Prerequisite:** IVS-05. Test double may represent Display gate, but
  production fails closed until IVS-08.
- **Tests/completion:** authority/stale/at-most-once/equivalent and changed
  duplicate/uncertain outcome/position; durable at-most-once accepted `NEXT` mutation with equivalent accepted-outcome replay.

### IVS-07 — Display Projection + Snapshot

- **Objective/outputs:** derived safe projection and snapshot GET.
- **Inputs:** Display schema/projection requirements/snapshot contract.
- **Allowed:** schema `1.0` `PLAIN_TEXT`. **Forbidden:** richer/private/stored
  projection and SSE.
- **Prerequisites:** IVS-04/05/06.
- **Tests/completion:** schema/privacy/unsupported content/auth/replacement;
  durable authority yields only canonical safe state.

### IVS-08 — Display SSE + Synchronization Gate

- **Objective/outputs:** Display events GET, synchronization PUT, publisher,
  process-local stream registry/gate.
- **Inputs:** ADR-0012 and SSE/reconnect contracts.
- **Allowed:** Spring MVC Display SSE. **Forbidden:** Controller SSE, reactive/
  socket/broker/delta scope or invented numeric timeout policy.
- **Prerequisites:** IVS-06/07 and resolved heartbeat/dead-timeout values.
- **Tests/completion:** initial/reconnect/current state, Last-Event-ID, dispatch/
  ack/termination/restart/`NEXT` gating; current-stream ack alone restores gate.

### IVS-09 — Teacher/Display Frontend Integration

- **Objective/outputs:** minimal Teacher and Display UI/state/adapters.
- **Inputs:** browser architecture/transports/schema/§10.
- **Allowed:** existing apps/transports and justified generated validator.
- **Forbidden:** authoring/AI/speech/ink, broad design, unjustified libraries.
- **Prerequisites:** IVS-03–08.
- **Tests/completion:** npm tests/build/boundaries, mapping/validation/reconnect/
  accessibility; both UIs complete the role-separated flow.

### IVS-10 — End-to-End Vertical Slice Readiness Audit

- **Objective/outputs:** integrated proof, in-scope remediation, final status.
- **Inputs:** canonical sources and §19 DoD.
- **Allowed:** slice conformance fixes. **Forbidden:** deferred scope,
  deployment/CI, pilot claims, unrelated refactor.
- **Prerequisites:** IVS-02–09.
- **Validation/completion:** all affected Maven/npm/contract/architecture/
  template/security/recovery evidence; browser E2E only if an actual gap exists;
  complete slice ready for deliberate final checkpoint/merge review.

## 17. Open Implementation Questions

| ID | Question | Why | Owner | Resolution Path | Classification |
|---|---|---|---|---|---|
| OIQ-01 | Teacher session idle/absolute lifetimes? | `RESOLVED`: 30-minute sliding idle timeout and fixed 8-hour absolute timeout from session creation, with the server-authority and qualifying-activity semantics in §11. | IVS-03 | Human-reviewed security decision recorded in this implementation plan. | RESOLVED; no longer blocks IVS-03 |
| OIQ-02 | Minimum entropy/encoded lengths for Teacher, participant, PairingGrant, and CSRF secrets? | `RESOLVED`: each uses exactly 32 CSPRNG bytes (256 bits), encoded as 43-character unpadded Base64URL; persistence stores only a lowercase 64-character SHA-256 verifier. | IVS-03/05 | Human-reviewed common first-slice credential baseline recorded in §11; OIDC transaction values remain ephemeral. | RESOLVED; no longer blocks IVS-03/05 |
| OIQ-03 | Independent participant-session expiry beyond revocation/session lifecycle? | `RESOLVED`: both participant roles receive a fixed, non-sliding eight-hour absolute lifetime from creation, with no idle timeout; server time and all earlier canonical authority invalidations remain authoritative as defined in §11. | IVS-05 | Human-reviewed security and implementation decision recorded in this implementation plan. | RESOLVED; no longer blocks IVS-05 |
| OIQ-04 | Display SSE heartbeat interval/dead timeout? | ADR-0012 requires heartbeat behavior but defers numbers. | IVS-08 | Reliability/security review chooses configurable values; tune later with evidence. | UNRESOLVED; blocks IVS-08 |

OIQ-01, OIQ-02, and OIQ-03 are resolved. IVS-05 is ready to execute; OIQ-04
remains unresolved and blocks IVS-08, not IVS-05. IVS-04 has locked its internal
initial state as `CREATED`, scene position `0`, and Revision `0` without creating
an additional wire guarantee. Exact later query shapes, revision increment
mechanics, Java class names, and validator packaging remain normal owning-batch
decisions when contracts and invariants stay intact.

## 18. Checkpoint and Merge Policy

Normal flow is `change → tests → commit → push/review → next batch`. A completed
IVS batch does not create a checkpoint. Create a final first-slice handoff
checkpoint only after IVS-10 proves the complete slice and the human accepts
the merge-readiness handoff. The IVS-01 implementation task itself does not perform commit or push; repository commit/push occurs through the normal human-reviewed batch workflow.

## 19. Definition of Done

The complete slice requires all eleven operations; unchanged canonical
contracts unless separately approved; provider-neutral OIDC and durable
Teacher sessions with rotation/revocation/expiry/CSRF; legitimate fixture-only
ready LessonVersion; durable Classroom authority; correct Pairing constraints;
Controller reconciliation; deterministic `NEXT`; expectedRevision enforcement;
at-most-once accepted CommandId and original equivalent replay; changed reuse
conflict; derived safe projection; snapshot; current-full-state SSE with
advisory Last-Event-ID; current-stream acknowledgement gate; authoritative
reconnect/restart recovery; no offline new-command replay; role-separated
Teacher/Display UI flow; real PostgreSQL constraint evidence; all relevant
backend/frontend/contract/architecture/template/security/recovery tests; and no
AI/speech/curriculum/rich-canvas/ink/deployment/pilot scope leakage.

Passing this DoD proves the first protected architecture slice. It does not by
itself satisfy Pilot Stage A or Stage B readiness.
