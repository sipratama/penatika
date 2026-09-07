# System Architecture — Penatika

> **Peran dokumen:** Canonical baseline untuk system boundaries, dependency direction, data ownership, trust boundaries, dan architecture invariants Penatika.

## Document Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft baseline |
| Version | `0.12` |
| Last Updated | `2026-09-07` |
| Base Profile | `fullstack` |
| Modifiers | `ai-enabled` |
| Deployment Maturity | Pre-implementation; target environment not decided |

## 1. Architecture Summary

### System Purpose

Penatika coordinates teacher preparation, a private teacher controller, a student-facing classroom display, structured lesson content, live AI adaptation, deterministic Mathematics assurance, curriculum grounding, and saved classroom sessions.

### Selected Baseline

- Multiple client surfaces interact with one backend-owned application boundary.
- Browser-first client strategy uses React, TypeScript, and Vite.
- Teacher Web contains Preparation and private Controller modes; Classroom Display is a separate browser application entry/build boundary.
- No native mobile client is used in the MVP.
- Frontend clients are browser application layers and do not become a BFF or application authority.
- The authoritative backend uses Java 21 LTS and Spring Boot 4.x.
- The backend remains one deployable modular monolith with explicit domain/application modules and no microservice split.
- Backend internal organization is module-first Hexagonal Architecture: business-capability modules are the primary boundary, and Hexagonal (ports/adapters) structure applies locally inside each module that is complex enough to benefit from it.
- Domain/application core remains framework-light; inbound and outbound adapters contain delivery and infrastructure detail.
- Ports protect meaningful boundaries and are not created mechanically for every dependency.
- The primary authoritative database is PostgreSQL 18.x (current stable at decision time: PostgreSQL 18.6), used as one database for the modular monolith with module-owned logical ownership.
- Physical schema evolution uses Flyway 13.x version-controlled SQL migrations.
- The initial data-access baseline is Spring JDBC / JdbcClient-style explicit SQL; no JPA/Hibernate baseline is selected.
- Persistence adapters live behind module-owned output ports per the Hexagonal persistence boundary; persistence is relational-first with selective PostgreSQL JSONB for justified structured content.
- No Redis, cache, or vector database is selected; physical schema and migrations do not yet exist.
- Teacher authentication uses OpenID Connect Authorization Code flow with PKCE `S256`; the Penatika Backend is the confidential OIDC client and relying party.
- OAuth/OIDC access, refresh, and ID tokens remain server-side; Teacher Web receives only an opaque protected backend-session cookie.
- Penatika owns a stable internal `TeacherAccount` linked to external identity by validated `(issuer, subject)`; email is not an identity key.
- Penatika stores no teacher passwords for MVP and does not make provider claims the application authorization model.
- Teacher controller authority requires an authenticated active `TeacherAccount` plus a session-scoped `TEACHER_CONTROLLER` participant binding.
- Classroom Display uses a distinct session-scoped `CLASSROOM_DISPLAY` participant authorization and receives no teacher-account privilege.
- MVP permits at most one active mutation-authorized controller and one active classroom display per classroom session.
- Pairing grants are classroom-session-bound, role/purpose-bound, single-use, revocable, and expire after five minutes.
- Core domain/application behavior remains framework-light Java where practical; Spring-specific, persistence, provider, and transport concerns belong at composition and adapter boundaries.
- Persistence, realtime, AI, speech, Mathematics, curriculum, concrete identity provider, and deployment technologies remain open adapter-level decisions where applicable.
- Cross-component wire interfaces follow contract-first design: contract, compatibility review, implementation, then conformance evidence.
- Synchronous HTTPS/JSON APIs use OpenAPI 3.1.x; justified reusable wire structures use JSON Schema Draft 2020-12.
- RFC 9457 Problem Details is the HTTP error baseline.
- Teacher-initiated state-changing commands remain on the synchronous HTTPS/JSON OpenAPI boundary; the backend pushes authoritative revision/projection updates to the Controller and Classroom Display over per-participant Server-Sent Events (SSE), authorized by the existing backend-managed session/participant credential.
- SSE reconnect uses the browser-native `Last-Event-ID` mechanism, carrying the authoritative revision, as the resync entry point into the existing ADR-0007 reconciliation policy.
- AsyncAPI 3.1.x remains inactive; the SSE push channel is documented as an OpenAPI `text/event-stream` operation referencing standalone JSON Schema event payloads rather than a separate contract family.
- Contract files are authoritative for wire interfaces; generated and implementation types are derived consumers.
- Classroom session state is authoritative on the backend and projected differently to teacher and display surfaces.
- Classroom content uses a versioned structured model; arbitrary generated HTML or executable AI output is not supported.
- AI providers are proposal generators, not authorities for session state, mathematical correctness, curriculum truth, authorization, or policy.
- AI semantic generation and classroom publication are separate authorization steps: teacher request authorizes generation, while teacher approval authorizes publication.
- Deterministic non-generative direct actions use explicitly supported command classes rather than the AI proposal path.
- Penatika is resilience-oriented, not offline-first; dependency failures degrade affected capabilities without creating competing state authority.
- Loss of backend authority freezes new authoritative mutations instead of promoting a client to temporary authority.

### Why This Shape

The MVP has tightly coupled workflows and shared consistency rules but no confirmed independent deployment or scaling requirement. A modular monolith minimizes distributed-system overhead while preserving boundaries that can be extracted later only if evidence justifies it.

## 2. System Context

```text
Teacher
  └─ Teacher Web
       ├─ Preparation mode
       └─ Private Controller mode
             │
             │ authorized application + realtime interactions
             ▼
      Penatika Backend
      Java 21 / Spring Boot 4.x
      one deployable modular monolith
       ├─ Identity & Access
       ├─ Lesson
       ├─ Classroom Session
       ├─ Classroom Scene
       ├─ AI Orchestration
       ├─ Mathematics Assurance
       └─ Curriculum
             │
             ├─ Persistence
             ├─ AI Provider
             ├─ Speech Recognition
             └─ Controlled Curriculum Source
             │
             ▼
     Classroom Display Web
       classroom-safe projection only
```

Students consume the classroom display but do not require a Penatika device identity in the MVP.

Teacher Web is one browser application containing Preparation and private
Controller modes. Classroom Display Web is a separate browser application
entry/build boundary.

## 3. Main Runtime Components

### 3.1 Preparation Client (Teacher Web)

Responsibilities:

- capture lesson intent;
- present structured generation and validation state;
- support teacher review and editing;
- save and select lesson versions.

It does not own authoritative lesson or curriculum provenance.

### 3.2 Private Controller Client (Teacher Web)

Responsibilities:

- pair with an active classroom session;
- show private AI progress, suggestions, warnings, and controls;
- show the actual proposal in a private preview;
- submit approval or explicit warned override for the current proposal;
- show blocked-state feedback without offering a publication override;
- submit navigation, annotation, and adaptation commands;
- show degraded and reconnecting state;
- support non-voice fallback when speech recognition is unavailable;
- avoid local-authority behavior or creation of offline mutation queues.

It must be treated as an untrusted client for authorization.

### 3.3 Classroom Display Web

Responsibilities:

- render classroom-safe structured scene projections;
- show current lesson content and accepted annotations;
- preserve a safe last-known projection during recoverable disruption;
- resynchronize against the backend-authoritative revision before accepting new student-facing mutations after reconnect;
- exclude teacher-private and internal AI state.

It has no authority to issue teacher commands.

### 3.4 Penatika Backend

The backend is one deployable Java 21 LTS / Spring Boot 4.x application with explicit internal modules and infrastructure adapters. It remains a modular monolith; no microservice split is selected. Internal organization is module-first Hexagonal Architecture, per [ADR-0013](./adr/ADR-0013-java21-module-first-hexagonal-backend.md).

Core domain and application behavior should remain framework-light Java where practical. Spring-specific composition, delivery, persistence, and provider integration concerns belong primarily at adapter boundaries. Domain models must not depend on transport models, database entities, provider SDKs, or AI SDK types.

#### Identity and Access Module

- acts as the backend OIDC relying party and confidential client;
- validates external OIDC authentication and resolves `(issuer, subject)` to a local `TeacherAccount` through an `ExternalIdentityLink`;
- applies explicit account-provisioning policy and rejects non-active accounts;
- creates, rotates, expires, and revokes opaque backend-managed `BrowserSession` authority;
- retains upstream OAuth/OIDC tokens server-side and never exposes them to ordinary browser JavaScript;
- issues and redeems five-minute, role-bound, single-use `PairingGrant` records;
- creates and revokes session-scoped `SessionParticipant` authority;
- enforces object ownership, session roles, active participant constraints, and access to role-specific projections;
- revokes controller authority when its teacher account or browser authorization becomes invalid.

The concrete OIDC provider, physical session store, account-linking UX, and
field-level authentication contracts remain open implementation decisions.

#### Lesson Module

- owns lesson identity, drafts, immutable version identity, and readiness state;
- coordinates generation without depending directly on provider SDKs;
- retains supported provenance and validation references.

#### Classroom Session Module

- owns session lifecycle, participants, roles, current lesson position, authoritative revision, accepted classroom state, degradation status, and save outcome;
- orders and reconciles state-changing commands;
- freezes or rejects new authoritative mutations when backend authority is unavailable;
- reconciles against backend-authoritative state before mutation resumes;
- distinguishes uncertain acknowledgement for a pre-disconnect command from a newly created offline command;
- reports save success only after durable authoritative persistence acknowledgement;
- requires valid proposal, approval or warned-override, session, assurance, and revision binding before accepting AI publication commands;
- produces role-specific projections.

#### Classroom Scene Module

- owns the supported structured scene model and schema version;
- validates scene and annotation commands;
- accepts proposal-derived scene changes only through a Classroom Session command with valid publication binding and revision context;
- derives student-facing content projections;
- rejects arbitrary executable content.

#### AI Orchestration Module

- translates application intents into bounded provider requests;
- isolates provider/model-specific behavior;
- enforces context, timeout, cost, resource, privacy, and output-schema controls;
- produces structured proposals and assurance inputs;
- returns proposals, never authoritative session mutations, and does not own publication authorization.

#### Mathematics Assurance Module

- classifies content requiring deterministic validation;
- owns normalized inputs, validation result semantics, and validator version references;
- remains independent of AI provider confidence.

#### Curriculum Module

- owns curriculum authority levels, controlled source metadata, versions, supported scope, and provenance;
- provides grounded reference context through supported interfaces;
- distinguishes national normative authority, official interpretive guidance, and local school/teacher context;
- does not permit provider output to redefine curriculum truth.

### 3.5 Infrastructure Adapters

Adapters implement persistence, realtime communication, AI generation, speech recognition, curriculum ingestion/retrieval, deterministic validation engines, telemetry, and clocks. Domain/application modules depend on ports rather than vendor SDKs.

## 4. Architecture Style and Dependency Direction

```text
Client / Transport Adapters
            ↓
      Application Use Cases
            ↓
        Domain Modules
            ↑
      Defined Ports
            ↑
 Infrastructure / Providers
```

Rules:

- Domain rules do not depend on HTTP, realtime library, database ORM, UI framework, or provider SDK.
- Infrastructure may depend inward on application/domain ports.
- Modules communicate through explicit supported interfaces, not another module's internal persistence representation.
- Cross-module mutations occur through the owning module.
- Technology selection must preserve these boundaries or document an ADR change.

## 5. Module and Data Ownership

| Data / State | Authoritative Owner | Notes |
|---|---|---|
| `TeacherAccount` and `ExternalIdentityLink` | Identity and Access | Stable local account; external key is validated `(issuer, subject)` |
| Authenticated `BrowserSession` | Identity and Access | Opaque, revocable backend session; upstream OAuth/OIDC tokens remain server-side |
| Lesson draft and lesson version | Lesson | Version identity must remain stable |
| Classroom session lifecycle and revision | Classroom Session | Backend authoritative |
| `PairingGrant` and `SessionParticipant` authority | Identity and Access with Session coordination | Five-minute single-use grant; one active controller/display per session for MVP |
| Structured scene and supported element schema | Classroom Scene | Versioned contract required before implementation |
| Accepted scene state and annotations | Classroom Session through Scene commands | Client cache is not authoritative |
| AI request/proposal lifecycle | AI Orchestration | Proposal remains untrusted and teacher-private until checks and publication authorization complete |
| Mathematics validation semantics/results | Mathematics Assurance | Independent of AI provider |
| Curriculum authority/source/version/provenance | Curriculum | Layered controlled reference data; saved lesson provenance remains historically stable |
| Operational telemetry | Observability infrastructure | Must exclude unnecessary sensitive payloads |

## 6. Primary Runtime Flows

### 6.1 Lesson Preparation

1. Preparation client submits lesson intent.
2. Lesson module resolves controlled curriculum context.
3. AI Orchestration requests a structured proposal.
4. Classroom Scene validates structure.
5. Mathematics Assurance validates supported claims.
6. Lesson module exposes proposal and assurance state to the teacher.
7. Teacher edits and saves a stable lesson version.

### 6.2 Session Start and Pairing

1. Teacher authenticates through OIDC Authorization Code flow with PKCE `S256`; the backend validates the external identity and resolves an active local `TeacherAccount`.
2. The backend creates or rotates the revocable authenticated browser session while retaining OAuth/OIDC tokens server-side.
3. The authorized teacher starts a session from a reviewed lesson version.
4. Classroom Session creates the authoritative state and revision.
5. Identity and Access issues role/session-bound, single-use pairing grants that expire after five minutes.
6. A controller grant may establish `TEACHER_CONTROLLER` authority only when redeemed from an authenticated, session-authorized teacher browser session.
7. A display grant establishes only a distinct `CLASSROOM_DISPLAY` participant session and cannot grant teacher authority.
8. Identity and Access enforces one active controller and one active display per session; explicit replacement revokes prior participant authority.
9. Session module returns role-specific projections.

### 6.3 Live Adaptation

1. Controller sends an authorized teacher action bound to the session and scene revision.
2. The application classifies the supported intent before invoking AI Orchestration. Ambiguous intent uses the safer semantic AI proposal path.
3. The action follows one of the paths below.

#### Deterministic `DIRECT_ACTION` Path

1. Identity and Access and Classroom Session validate teacher/session authorization.
2. Classroom Scene validates the explicitly supported deterministic, non-generative command.
3. Classroom Session validates the current revision and accepts the classroom command.
4. Classroom Session publishes updated role-specific projections.

This path does not enter AI Orchestration and does not create or imitate an AI proposal.

#### Semantic AI Adaptation Path

1. Speech adapter transcribes ephemeral audio if applicable.
2. AI Orchestration produces a structured proposal from bounded request context.
3. Scene, Curriculum, and Mathematics modules perform applicable schema, policy, provenance, and assurance checks.
4. Publication policy assigns `APPROVAL_REQUIRED`, `APPROVAL_WITH_WARNING`, or `BLOCKED` to the proposal outcome.
5. Teacher surface receives the actual proposal, warning, or blocked result through a private projection.
6. Teacher approves the actual proposal or explicitly overrides an eligible warning; blocked results cannot be overridden.
7. A proposal with valid proposal/approval/assurance/revision binding becomes an accepted revision-aware classroom command.
8. Classroom Session publishes updated role-specific projections.

Transcription, generation progress, proposal preview, assurance results, warnings, alternatives, and other `PRIVATE_ONLY` work remain teacher-private and do not mutate student-facing authoritative state.

### 6.4 Realtime Push (SSE)

1. Controller and Classroom Display each hold one authorized `text/event-stream` connection, established with their existing backend-managed session/participant credential, carrying only their own role-specific projection.
2. The backend never multiplexes both projections onto one stream.
3. State-changing commands never travel over the push channel; they remain on the synchronous HTTP command boundary (section 6.3).
4. Each pushed event's `id` carries the authoritative revision/sequence. On browser-native SSE reconnect, `Last-Event-ID` lets the backend emit a bounded catch-up delta or direct the client to fetch a full authoritative snapshot before push resumes.
5. A pushed event's revision/sequence identifier is advisory for reconnect resync only; mutation resumes only after backend-authoritative reconciliation (section 6.5, ADR-0007).

### 6.5 Reconnect and Save

1. The application identifies which dependency or participant is degraded and exposes an appropriate teacher-private status.
2. AI or speech failure disables only the affected capability while backend-authoritative classroom behavior continues where healthy.
3. Display disconnect pauses student-facing publication and direct projection mutations; safe teacher-private work may continue.
4. Controller disconnect leaves the current authoritative display projection unchanged; another teacher surface may act only when separately authorized.
5. Loss of backend authority preserves the last-known safe classroom projection and freezes new authoritative commands, AI publication, lifecycle changes, and successful save completion. Clients do not create new offline mutation queues.
6. On reconnect, the client presents its last observed revision and the Session module returns current authoritative state or a supported delta.
7. The Session module reconciles uncertain acknowledgement for commands sent before disconnect through command identity, idempotency, and revision semantics, while rejecting stale, conflicting, or newly created offline commands.
8. Authoritative mutation resumes only after synchronization completes.
9. Session save remains pending, failed, or retryable until durable authoritative persistence acknowledges success.

## 7. Trust Boundaries

| Boundary | Trust Position |
|---|---|
| Teacher and display clients → backend | Untrusted input; authenticate, authorize, validate, and bound |
| OIDC provider → Identity and Access | External authentication source; validate issuer, subject, audience/client, signature, nonce, state, redirect, and time constraints before local use |
| OIDC claims → Penatika account/authorization | Untrusted until validated and normalized; provider roles/groups and email do not become application authority |
| Pairing credential → session access | Five-minute single-use proof for one session role/purpose; never teacher-account authentication |
| Browser/participant session reference → backend | Opaque revocable credential; validate lifecycle, account, role, ownership, and session state on use |
| AI/speech provider → application | Untrusted external dependency and output |
| Curriculum source → curriculum module | Normative source is selected for MVP Mathematics; each ingested source still requires version, integrity, authority-level, provenance, and applicable usage policy |
| Backend → persistence | Privileged boundary using least-privilege credentials |
| Teacher-private projection → classroom display | Explicit confidentiality boundary |

## 8. Security and Privacy Baseline

- Teacher authentication uses OIDC Authorization Code flow with PKCE `S256`, `state`, `nonce`, exact redirect validation, and applicable issuer, subject, client/audience, signature, and time validation.
- The backend is the confidential OIDC client; OAuth/OIDC access, refresh, and ID tokens remain server-side and are not stored by browser application JavaScript.
- Teacher Web uses a revocable opaque backend session cookie that is `Secure`, `HttpOnly`, narrowly scoped, and host-only where practical; `SameSite=Strict` is preferred when compatible with the final OIDC deployment.
- Cookie-authenticated state changes require explicit CSRF protection; SameSite alone is insufficient, state-changing behavior must not use `GET`, and credentialed CORS must use explicit trusted origins rather than wildcards.
- Server-side object/session authorization is required for every protected action; authentication or a coarse teacher role alone is insufficient.
- Pairing grants must be purpose/role-bound, session-bound, five-minute bounded, single-use, non-guessable, revocable, and replay-resistant.
- Pairing alone cannot authenticate a teacher, and a display participant can never derive teacher authority.
- Browser and participant session identifiers rotate or revoke as applicable, expire after bounded lifetimes, and terminate local authority independently of upstream logout.
- Penatika stores no local teacher passwords for MVP and does not automatically link accounts by email.
- Raw push-to-talk audio is ephemeral by default and excluded from logs and ordinary persistence.
- Secrets, tokens, raw provider payloads, and private teacher state must not appear in classroom projections.
- AI output, user input, lesson content, and curriculum content are untrusted for rendering and prompt control.
- Structured rendering must use allow-listed element types and safe encoding.
- Resource limits apply to uploads if introduced, AI requests, context, sessions, commands, and payloads.
- The product data-lifecycle policy is defined in [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md); implementation, physical enforcement, export mechanics, backup expiry, security audit evidence, and applicable legal/privacy review remain architecture/implementation/operational follow-ups.

## 9. Reliability and Degradation Baseline

Penatika is resilience-oriented, not offline-first. The degradation policy preserves one backend-authoritative session state and limits failure to the affected capability where possible.

| Failure | `AVAILABLE` | `LIMITED` | `UNAVAILABLE` / Paused |
|---|---|---|---|
| AI provider unavailable | Reviewed lesson presentation, accepted content display, navigation, supported `DIRECT_ACTION`, digital ink, synchronization, session lifecycle, and save when backend/persistence are healthy | None required | New semantic AI generation, AI explanations, exercises, visuals, and rewrites |
| Speech recognition unavailable | Tap/button, keyboard/text, mouse, touch, stylus, digital ink, `DIRECT_ACTION`, and supported non-voice semantic AI requests | None required | Push-to-talk transcription |
| Teacher controller disconnected | Classroom display retains current authoritative projection; backend session remains authoritative | Another teacher surface may act only when separately backend-authorized | Commands, AI requests, publication approvals, end, or save initiated from the disconnected controller |
| Classroom display disconnected | Backend session state and safe teacher-private proposal work | Teacher-private work only when it does not mutate student-facing state | Student-facing publication, projection-changing `DIRECT_ACTION`, and AI proposal publication until display resynchronizes |
| Backend authority unavailable | Last-known safe classroom projection may remain visible | Read-only cached projection | New authoritative navigation, `DIRECT_ACTION`, digital ink commit, AI publication, pairing/session lifecycle mutation, successful save, and automatic replay of new offline commands |
| Durable save dependency unavailable | Teaching, navigation, permitted mutations, and Q-02-compliant AI adaptation while runtime authority is healthy | Save remains pending, failed, or retryable | Successful durable save acknowledgement |

- Failed AI, speech, assurance, display, controller, backend, or persistence operations must not bypass existing authorization, approval, validation, provenance, privacy, or revision policy.
- Commands that may be retried require idempotency and revision checks.
- An uncertain acknowledgement for a command sent before disconnect may be reconciled; newly created offline state-changing commands are never automatically replayed in the MVP.
- Reconnection resolves against backend authority and mutation resumes only after synchronization completes.
- Dependency timeouts, retry ownership, and circuit-breaking policy must be explicit after providers are selected.
- Latency objectives require prototype measurement before numerical targets are set.

## 10. AI and Assurance Boundaries

- Provider prompts and models are replaceable adapters.
- Prompt and policy versions must be traceable when they materially affect output.
- AI evaluation is required for scoped lesson and adaptation tasks before pilot.
- Authorization to request generation is not authorization to publish the generated result.
- Successful assurance does not remove post-generation teacher approval for MVP student-facing semantic AI content.
- Deterministic validation is preferred for supported Mathematics claims.
- Unsupported/inconclusive validation is an explicit state, not success.
- Hard structural, authorization, policy, security, privacy, stale-state, provenance, or known-invalid results cannot be made publishable through teacher override.
- Curriculum claims identify authority level, controlled source, version, scope, and provenance.
- AI must not receive unnecessary teacher or student data.

## 11. Persistence Baseline

Persistent domain data is required for lesson versions, curriculum provenance, session saves, assurance results, and authorization-related records. Per [ADR-0014](./adr/ADR-0014-postgresql-flyway-sql-first-persistence.md), the primary authoritative database is PostgreSQL 18.x (one database for the modular monolith, with module-owned logical ownership), physical schema evolution uses Flyway 13.x version-controlled SQL migrations, and the initial data-access baseline is Spring JDBC / JdbcClient-style explicit SQL. Persistence adapters implement module-owned output ports per the Hexagonal persistence boundary in ADR-0013/ADR-0014; no JPA/Hibernate, Redis, cache, or vector database is selected. Physical schema, migrations, and physical database credentials/deployment do not yet exist.

The conceptual model is defined in [DATA_MODEL.md](./DATA_MODEL.md). Persistent data follows the approved retention classes in [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md); persistence does not imply indefinite retention for AI or assurance records.

## 12. Contracts

Penatika uses contract-first cross-boundary design. Machine-readable contract
files are authoritative for wire interfaces; generated code, handwritten DTOs,
documentation rendering, client types, and implementation classes are derived
consumers.

Contract ownership is divided by responsibility:

- `contracts/openapi/` owns synchronous HTTPS/JSON application API definitions
  using OpenAPI 3.1.x when field-level contracts are created;
- `contracts/schemas/` owns justified reusable structured wire schemas using
  JSON Schema Draft 2020-12;
- `contracts/asyncapi/` stays inactive; ADR-0012 documents the SSE push
  channel within `contracts/openapi/` and `contracts/schemas/` instead.

RFC 9457 Problem Details with `application/problem+json` is the HTTP error
baseline. Each wire shape has one canonical schema owner; other contracts and
derived code reference that owner rather than redefining the same structure.

Cross-component field-level contracts are required before relevant application
implementation, including:

- structured lesson and scene schema;
- role-specific session projections;
- session command and revision semantics;
- AI proposal envelope;
- assurance result and curriculum provenance.

The contract repository boundary is active, but no field-level OpenAPI or JSON
Schema contract is created merely because the strategy is resolved. Identity
semantics are selected by ADR-0011; realtime transport and credential carriage
are selected by ADR-0012. AsyncAPI remains inactive; the SSE push channel is a
future OpenAPI/JSON Schema addition, not an AsyncAPI contract.

## 13. Observability Baseline

- Use correlation identifiers across session, command, AI request, validation, and save operations.
- Record structured lifecycle and failure-category events without sensitive payloads.
- Measure synchronization failures, stale commands, pairing failures, provider latency/failures, validation outcomes, degradation entry/exit, mutation freezes, display/controller disconnects, uncertain acknowledgements, reconciliation results, rejected offline replay attempts, save retries, and recovery.
- Avoid high-cardinality labels containing teacher content, prompts, or session secrets.
- Production SLOs and alert thresholds remain open until workload and deployment evidence exist.

## 14. Architecture Invariants

- `INV-001`: Only the Classroom Session module may commit authoritative session lifecycle and revision changes.
- `INV-002`: Clients never become authorization or classroom-state authorities.
- `INV-003`: Classroom display receives only a classroom-safe projection.
- `INV-004`: AI provider output never directly mutates authoritative state.
- `INV-005`: Only supported structured content may be rendered; arbitrary executable output is rejected.
- `INV-006`: Mathematics validation status cannot be derived solely from AI provider claims.
- `INV-007`: Every grounded curriculum claim identifies its authority level, controlled source, and source version.
- `INV-008`: Raw push-to-talk audio is not stored by default.
- `INV-009`: Every accepted state-changing command is bound to an authorized actor, session, and revision context.
- `INV-010`: Infrastructure and vendor adapters do not own product policy.
- `INV-011`: Official guidance and local context cannot be promoted to national normative authority by AI, retrieval ranking, or implementation convenience.
- `INV-012`: New curriculum source versions do not silently rewrite provenance of previously saved lesson versions.
- `INV-013`: An AI generation request does not itself authorize publication of the resulting semantic content.
- `INV-014`: AI-generated student-facing semantic content requires post-generation approval bound to the actual proposal and current revision, unless a later accepted ADR explicitly narrows this rule for a proven safe content class.
- `INV-015`: `BLOCKED` results cannot produce authoritative classroom commands through teacher override.
- `INV-016`: Deterministic non-generative presentation actions may execute without AI publication approval only through explicitly supported command classes.
- `INV-017`: Loss of backend authority must freeze new authoritative classroom mutations; no client becomes temporary session authority.
- `INV-018`: Clients may preserve the last-known safe classroom projection during recoverable connectivity loss, but cached state is not authoritative.
- `INV-019`: MVP must not automatically replay newly created offline state-changing commands after reconnect.
- `INV-020`: Mutation may resume only after reconciliation with backend-authoritative state.
- `INV-021`: A durable save is successful only after authoritative persistence acknowledgement.
- `INV-022`: Degraded mode cannot bypass approval, assurance, authorization, privacy, structured-content, curriculum provenance, or revision policy.
- `INV-023`: OAuth/OIDC access, refresh, and ID tokens are not exposed to ordinary browser JavaScript; protected teacher requests use a revocable opaque backend session.
- `INV-024`: External teacher identity is linked by validated `(issuer, subject)` and is never established or merged solely by email.
- `INV-025`: Pairing alone cannot authenticate a teacher or grant teacher-account authority.
- `INV-026`: A `CLASSROOM_DISPLAY` participant cannot obtain teacher-private projection or teacher mutation authority.
- `INV-027`: MVP permits at most one active mutation-authorized `TEACHER_CONTROLLER` and one active `CLASSROOM_DISPLAY` participant per classroom session.
- `INV-028`: Pairing grants are role/session-bound, single-use, revocable, and expire after five minutes.
- `INV-029`: Realtime push connections (for example, SSE) transmit projections only; state-changing commands must use the authorized synchronous HTTP command boundary, never the push channel.
- `INV-030`: A pushed event's revision/sequence identifier is advisory for reconnect resync only; only backend-authoritative revision retrieval and reconciliation determines whether mutation may resume.

## 15. Selected Architecture Decisions

- [ADR-0001 — Use a Modular Monolith for the Initial Backend](./adr/ADR-0001-modular-monolith-backend.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./adr/ADR-0002-backend-authoritative-session-state.md)
- [ADR-0003 — Use Versioned Structured Classroom Content](./adr/ADR-0003-structured-classroom-content.md)
- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./adr/ADR-0004-ai-assurance-boundary.md)
- [ADR-0005 — Use Layered Curriculum Authority and Versioned Provenance](./adr/ADR-0005-layered-curriculum-authority.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./adr/ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./adr/ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0008 — Use Browser-First React Clients with Separate Teacher and Classroom Display Boundaries](./adr/ADR-0008-browser-first-react-client-strategy.md)
- [ADR-0009 — Use Java 25 LTS and Spring Boot for the Authoritative Backend](./adr/ADR-0009-java-spring-boot-backend.md) (Superseded by ADR-0013)
- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](./adr/ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./adr/ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0012 — Use Server-Sent Events for Realtime Push with Existing HTTP Commands](./adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md)
- [ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture](./adr/ADR-0013-java21-module-first-hexagonal-backend.md)
- [ADR-0014 — Use PostgreSQL with Flyway and SQL-First Hexagonal Persistence](./adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)

## 16. Open Architecture Decisions

| ID | Decision | Needed Before |
|---|---|---|
| OAD-006 | AI provider/model strategy and fallback | AI integration implementation |
| OAD-007 | Speech recognition strategy | Push-to-talk implementation |
| OAD-008 | Mathematics validator approach per content type | Assurance implementation |
| OAD-009 | Curriculum ingestion, normalization, integrity/versioning, local-context modeling, and retrieval approach | Curriculum implementation |
| OAD-010 | Deployment platform, environments, secret management, and regional requirements | Deployment planning |
| OAD-011 | Background execution and queue needs | When measured request duration or reliability requires it |

## 17. Architecture Evolution Rules

- Do not split services without evidence for independent deployment, scaling, failure isolation, or ownership.
- Add asynchronous infrastructure only when a concrete workflow requires buffering, durable long-running work, or fan-out.
- Create or update an ADR for material boundary, datastore, provider strategy, deployment topology, or authorization changes.
- Keep public/cross-component contracts synchronized with implementation.

## 18. Related Documents

- [Product Brief](../00_product/PRODUCT_BRIEF.md)
- [PRD](../00_product/PRD.md)
- [Data Model](./DATA_MODEL.md)
- [Data Retention, History, Export, and Deletion Policy](../06_delivery/DATA_RETENTION_POLICY.md)
- [Non-Functional Requirements](./NON_FUNCTIONAL_REQUIREMENTS.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)
- [Test Strategy](../04_engineering/TEST_STRATEGY.md)

## 19. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.12` | `2026-09-07` | Resolve OAD-003 with PostgreSQL 18.x, Flyway 13.x, and SQL-first Hexagonal persistence adapters | Claude |
| `0.11` | `2026-09-07` | Refine backend baseline to Java 21 LTS and explicit module-first Hexagonal Architecture; ADR-0013 supersedes ADR-0009 | Claude |
| `0.10` | `2026-09-07` | Resolve OAD-005 with SSE realtime push, existing HTTP commands, and inactive AsyncAPI | Claude |
| `0.9` | `2026-09-07` | Resolve OAD-004 with OIDC, backend-managed browser sessions, local teacher accounts, and scoped pairing | Codex |
| `0.8` | `2026-09-06` | Resolve OAD-012 with contract-first OpenAPI 3.1.x and JSON Schema Draft 2020-12 boundaries | Codex |
| `0.7` | `2026-09-06` | Resolve OAD-002 with Java 25 LTS / Spring Boot modular-monolith backend | Codex |
| `0.6` | `2026-09-06` | Resolve OAD-001 with browser-first React client architecture | Codex |
| `0.5` | `2026-09-06` | Align security and persistence wording with the approved product data-lifecycle baseline | Codex |
| `0.4` | `2026-09-06` | Apply resilience-oriented degradation and recovery architecture from ADR-0007 | Codex |
| `0.3` | `2026-09-06` | Apply separate AI generation and teacher-authorized publication architecture from ADR-0006 | Codex |
| `0.2` | `2026-09-06` | Apply layered curriculum authority and provenance architecture from ADR-0005 | Codex |
| `0.1` | `2026-09-06` | Initial technology-neutral architecture baseline | Codex |
