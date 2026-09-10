# Penatika Project Status

This document is a living project-phase and completion index for Penatika.

It is NOT the authoritative source for product requirements, architecture
decisions, feature behavior, contracts, or policies.

Its purpose is to help contributors and AI agents quickly understand the
current project phase and route themselves to the correct canonical source.

## Core Design Rule

`PROJECT_STATUS.md` tells us **WHERE WE ARE**.

Canonical documents tell us **WHAT THE DECISION IS**.

If this document conflicts with a canonical document, the canonical document
wins and this document must be corrected.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Active |
| Version | 0.36 |
| Last Updated | 2026-09-10 |
| Current Phase | Source Scaffolding — Complete / Merge-Ready |
| Implementation State | Source Scaffolding is complete; product/application implementation and product persistence have not started |
| Product Owner | sipratama |

The final product-baseline consistency audit passed before the Architecture
Foundation handoff checkpoint was created.

## Status Legend

- `COMPLETE` — milestone/gate completed.
- `ESTABLISHED` — baseline exists, but later decisions/implementation remain.
- `ACTIVE` — current phase of work.
- `NEXT` — immediate planned decision/work.
- `PENDING` — not started and dependent on prior work.
- `CONDITIONAL` — only activated when its trigger exists.
- `EVIDENCE_REQUIRED` — decision/policy exists but real-world readiness still
  needs implementation or evidence.

## Project Phase Matrix

| Area | Status | Canonical Source / Evidence |
|---|---|---|
| Project Discovery | COMPLETE | [PROJECT_INITIALIZATION.md](./PROJECT_INITIALIZATION.md) / [Product Brief](./00_product/PRODUCT_BRIEF.md) |
| Minimum Product Context Gate | COMPLETE / PASS | [PROJECT_INITIALIZATION.md](./PROJECT_INITIALIZATION.md) |
| Project Initialization | COMPLETE | [PROJECT_INITIALIZATION.md](./PROJECT_INITIALIZATION.md) |
| Product decision baseline Q-01–Q-07 | COMPLETE | [Product Brief](./00_product/PRODUCT_BRIEF.md) / [PRD](./00_product/PRD.md) / specialized canonical docs |
| Product-baseline consistency audit | COMPLETE | current canonical repository baseline |
| Product governance | COMPLETE | [PRODUCT_GOVERNANCE.md](./00_product/PRODUCT_GOVERNANCE.md) |
| Business-model baseline | COMPLETE | [BUSINESS_MODEL.md](./00_product/BUSINESS_MODEL.md) |
| Data-lifecycle policy | COMPLETE | [DATA_RETENTION_POLICY.md](./06_delivery/DATA_RETENTION_POLICY.md) |
| First-pilot definition | COMPLETE | [PILOT_PLAN.md](./06_delivery/PILOT_PLAN.md) |
| Pendago legacy review | COMPLETE | [PENDAGO_MIGRATION_REVIEW.md](./06_delivery/PENDAGO_MIGRATION_REVIEW.md) |
| Initial architecture principles | ESTABLISHED | [SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md) + Accepted ADR register |
| Architecture / technology decisions | COMPLETE (unconditional) | [SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md); OAD-011 remains CONDITIONAL |
| Architecture Foundation merge to `main` | COMPLETE | merge commit `7cd6a2d` |
| Contract strategy | COMPLETE | [ADR-0010](./02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md) / [contracts](../contracts/README.md) |
| Field-Level Contract Foundation | COMPLETE | [Contract Foundation Plan](./04_engineering/CONTRACT_FOUNDATION_PLAN.md) / [Contract Foundation Checkpoint](./checkpoints/2026-09-09-contract-foundation-complete.md) |
| First vertical slice contract scope | COMPLETE | [Contract Foundation Plan](./04_engineering/CONTRACT_FOUNDATION_PLAN.md) |
| Shared wire primitives / RFC 9457 HTTP problem foundation | COMPLETE | [Reusable schemas](../contracts/schemas/README.md) / [OpenAPI](../contracts/openapi/openapi.yaml) |
| Authenticated Teacher / session-start API | COMPLETE | [OpenAPI](../contracts/openapi/openapi.yaml) |
| Pairing contract | COMPLETE | [OpenAPI](../contracts/openapi/openapi.yaml) |
| Command / revision contract | COMPLETE | [OpenAPI](../contracts/openapi/openapi.yaml) |
| Display projection contract | COMPLETE | [Display projection schema](../contracts/schemas/classroom-display-projection.schema.json) / [OpenAPI](../contracts/openapi/openapi.yaml) |
| SSE / reconnect plus Display synchronization contract | COMPLETE | [OpenAPI](../contracts/openapi/openapi.yaml) |
| Contract Foundation final audit | COMPLETE | current canonical contract and validation evidence |
| Final Contract Foundation checkpoint | COMPLETE | [Contract Foundation Checkpoint](./checkpoints/2026-09-09-contract-foundation-complete.md) |
| Contract Foundation merge to `main` | COMPLETE | merge commit `7995290b07c755425020b939fb669f9c4a0b2ce0` |
| Source scaffolding | COMPLETE / MERGE-READY | [Source Scaffolding Plan](./04_engineering/SOURCE_SCAFFOLDING_PLAN.md) / [Source Scaffolding Checkpoint](./checkpoints/2026-09-10-source-scaffolding-complete.md) |
| SS-01 plan / repository layout | COMPLETE | [Source Scaffolding Plan](./04_engineering/SOURCE_SCAFFOLDING_PLAN.md) |
| SS-02 backend build / module skeleton | COMPLETE | backend Maven wrapper/POM + Java 21 Spring Boot shell and module package boundaries |
| SS-03 frontend workspace / teacher-display shells | COMPLETE | npm workspace/lockfile plus independently buildable and testable React/TypeScript/Vite Teacher and Display shells |
| SS-04 test + contract validation harness | COMPLETE | ArchUnit fitness rules, frontend boundary guard, Redocly/Ajv contract harness, and accepted role-scoped generation proof |
| SS-05 configuration + persistence mechanism baseline | COMPLETE | typed safe configuration, disabled-by-default persistence, PostgreSQL 18.6/Testcontainers, Spring JDBC/JdbcClient, and Flyway 13.5.0 empty-baseline evidence |
| SS-06 consistency / readiness audit | COMPLETE | full backend, frontend, contract, persistence-mechanism, documentation, and merge-readiness evidence |
| Final Source Scaffolding handoff checkpoint | COMPLETE | [Source Scaffolding Checkpoint](./checkpoints/2026-09-10-source-scaffolding-complete.md) |
| Source Scaffolding merge to `main` | NEXT | after final checkpoint review/commit/push and remote verification |
| Application implementation | PENDING | after source scaffolding |
| Pilot Stage A execution | PENDING | after required vertical slice/evidence |
| Pilot Stage B real classroom | EVIDENCE_REQUIRED / PENDING | [PILOT_PLAN.md](./06_delivery/PILOT_PLAN.md) Stage B gates |
| Commercial launch | PENDING | [BUSINESS_MODEL.md](./00_product/BUSINESS_MODEL.md) launch gates |

Approved product policy does not imply that implementation evidence is complete.

## Completed Product Decision Register

This register maps decisions to their authoritative sources only. It does not
duplicate the full decision content.

| ID | Concern | Canonical Source |
|---|---|---|
| Q-01 | Curriculum authority and provenance | [Product Brief](./00_product/PRODUCT_BRIEF.md) / [PRD](./00_product/PRD.md) / [ADR-0005](./02_architecture/adr/ADR-0005-layered-curriculum-authority.md) |
| Q-02 | AI generation vs teacher-authorized publication | [ADR-0006](./02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md) / [live-ai-adaptation.md](./01_features/live-ai-adaptation.md) |
| Q-03 | Graceful degradation without offline authority | [ADR-0007](./02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md) |
| Q-04 | Pilot scope and metrics | [PILOT_PLAN.md](./06_delivery/PILOT_PLAN.md) |
| Q-05 | Pendago legacy migration | [PENDAGO_MIGRATION_REVIEW.md](./06_delivery/PENDAGO_MIGRATION_REVIEW.md) |
| OPD-005 | Retention/history/export/deletion | [DATA_RETENTION_POLICY.md](./06_delivery/DATA_RETENTION_POLICY.md) |
| Q-07 / OPD-006 | Product ownership / requirement approval | [PRODUCT_GOVERNANCE.md](./00_product/PRODUCT_GOVERNANCE.md) |
| Q-06 / OPD-007 | Business model / commercial path | [BUSINESS_MODEL.md](./00_product/BUSINESS_MODEL.md) |

There are no remaining unresolved decisions from the initialized Q-01–Q-07 /
OPD-001–OPD-007 set.

Future product decisions may still arise through normal product governance.

## Architecture Decision Status

The existing OAD list is authoritative in
[SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md). This section
tracks status only and does not redefine the OAD descriptions.

| ID | Decision | Status |
|---|---|---|
| OAD-001 | Client application strategy and frontend framework(s) | COMPLETE |
| OAD-002 | Backend language and framework | COMPLETE |
| OAD-003 | Database and migration technology | COMPLETE |
| OAD-004 | Identity, authentication, and account model | COMPLETE |
| OAD-005 | Realtime transport and reconnect protocol | COMPLETE |
| OAD-006 | AI provider/model strategy and fallback | COMPLETE |
| OAD-007 | Speech recognition strategy | COMPLETE |
| OAD-008 | Mathematics validator approach per content type | COMPLETE |
| OAD-009 | Curriculum ingestion, normalization, integrity/versioning, local-context modeling, and retrieval | COMPLETE |
| OAD-010 | Deployment platform, environments, secret management, and regional requirements | COMPLETE |
| OAD-011 | Background execution and queue needs | CONDITIONAL |
| OAD-012 | Contract protocols and schema tooling | COMPLETE |

OAD-011 remains evidence-triggered and must not be selected merely because a
queue is common or familiar.

The exact OAD definitions and `Needed Before` rules remain authoritative in
[SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md).

Architecture reasoning is not duplicated here.

OAD-002's current implementation baseline is
[ADR-0013](./02_architecture/adr/ADR-0013-java21-module-first-hexagonal-backend.md):
Java 21 LTS + Spring Boot 4.x + module-first Hexagonal Architecture.
[ADR-0009](./02_architecture/adr/ADR-0009-java-spring-boot-backend.md) is
historical/superseded.

OAD-003 is resolved by
[ADR-0014](./02_architecture/adr/ADR-0014-postgresql-flyway-sql-first-persistence.md):
PostgreSQL 18.x + Flyway 13.x + Spring JDBC/JdbcClient SQL-first persistence
adapters behind module-owned output ports (ADR-0013's Hexagonal persistence
boundary). No JPA/Hibernate, Redis, cache, or vector database is selected.

OAD-009 is resolved by
[ADR-0015](./02_architecture/adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md):
a controlled, versioned, human-verified curriculum corpus with deterministic
metadata-first retrieval. Runtime authority never depends on AI model
memory, live web retrieval, or vector/embedding similarity. No vector
database is selected; Official Guidance substantial content remains
unactivated pending licensing/usage review.

OAD-008 is resolved by
[ADR-0016](./02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md):
scoped deterministic validators (`EXACT_RATIONAL`, `AFFINE_EXPRESSION`,
`LINEAR_EQUATION`) using exact rational arithmetic for the Grade 5
Fractions / Grade 7 Basic Algebra / Linear Equations pilot scope. No
general-purpose CAS, external mathematical service, or LLM-as-validator is
selected; deterministic validation requires no network dependency.

OAD-006 is resolved by
[ADR-0017](./02_architecture/adr/ADR-0017-openrouter-bounded-generation-and-usage-controls.md):
OpenRouter as the initial controlled generative-model gateway behind a
Penatika-owned `GenerativeModelPort`, server-owned `ROUTER`/`FAST`/`QUALITY`
model profiles, a pre-provider scope/capability/resource pipeline that
rejects unsupported and over-scale requests before expensive generation, an
application-owned per-teacher daily AI allowance with atomic usage
reservation, privacy-constrained provider routing (`zdr=true`,
`data_collection=deny`, no unapproved automatic fallback), and graceful
degradation as the MVP fallback policy. This also resolves the pre-provider
request-scope/budget-control and per-teacher usage-accounting concern
previously flagged against OAD-006.

OAD-007 is resolved by
[ADR-0018](./02_architecture/adr/ADR-0018-deepgram-push-to-talk-speech-recognition.md):
Deepgram Nova-3 (Indonesian, AU regional endpoint, `mip_opt_out=true`) as
the initial speech provider, backend-mediated pre-recorded/completed-
utterance push-to-talk transcription with browser `MediaRecorder` capture,
deterministic `DIRECT_ACTION`-first transcript routing that falls back to
the ADR-0017 semantic pipeline only when no direct action matches, raw
audio excluded from durable storage and from the generative gateway, and
speech usage/cost tracked separately from the generative `AIAllowanceWindow`.

OAD-010 is resolved by
[ADR-0019](./02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md):
a portable single-Linux-VPS MVP/pilot deployment baseline — Docker Engine +
Docker Compose, Caddy for HTTPS, PostgreSQL colocated on the same host for
`PILOT`, an off-host backup boundary, and a `LOCAL`/`PILOT`/`PROD`
environment model — initially deployed on Tencent Cloud Lighthouse,
Jakarta, as a replaceable provider. The architecture decision (deployment
class) is locked; the provider and commercial plan remain explicitly
replaceable and are not treated as architecture. No VPS is provisioned, and
no Dockerfile, Compose file, Caddy configuration, CI/CD pipeline, or backup
implementation exists yet.

## Immediate Next Step

Architecture Foundation is complete and merged to `main` in `7cd6a2d`.
Contract strategy and Field-Level Contract Foundation are complete, and the
Contract Foundation merge is verified on `main` at
`7995290b07c755425020b939fb669f9c4a0b2ce0`. The
[Contract Foundation Plan](./04_engineering/CONTRACT_FOUNDATION_PLAN.md)
records the completed first classroom vertical slice sequence. Shared wire
primitives are established in
[`contracts/schemas/wire-primitives.schema.json`](../contracts/schemas/wire-primitives.schema.json),
and the base RFC 9457 HTTP problem contract remains in
[`contracts/openapi/openapi.yaml`](../contracts/openapi/openapi.yaml).

The first protected classroom vertical-slice contract remains defined by the
[Contract Foundation Plan](./04_engineering/CONTRACT_FOUNDATION_PLAN.md) and
the canonical contracts. Source Scaffolding SS-01 through SS-06 is complete on
`feat/source-scaffolding`, including the backend and frontend shells,
executable architecture/privacy checks, contract validation and deterministic
role-scoped transport declarations, typed configuration, and the
disabled-by-default PostgreSQL/Flyway/JdbcClient mechanism. The
[Final Source Scaffolding Handoff Checkpoint](./checkpoints/2026-09-10-source-scaffolding-complete.md)
records the reviewed phase boundary. The immediate next step is deliberate
merge of `feat/source-scaffolding` into `main` after human review, commit,
push, and remote verification. Product/application implementation remains
PENDING and has not started.
`SYSTEM_ARCHITECTURE.md` remains authoritative for `Needed Before` rules.

## Open Non-Product Follow-Ups

These are **not** unresolved Q-01–Q-07 / OPD-001–OPD-007 decisions.

### Architecture / Implementation

- OAD-011 as applicable (CONDITIONAL only);
- physical product persistence model/migrations;
- identity implementation;
- realtime/reconnect transport implementation;
- provider integrations;
- deployment implementation evidence (VPS provisioning, Dockerfile/Compose/Caddy, CI/CD, backup) per ADR-0019.

### UX / Compatibility

- exact structured content blocks;
- target device/browser/display/stylus matrix;
- formal accessibility conformance target;
- detailed controller and approval interaction design.

### Evidence / Operations

- numerical latency/reliability targets;
- target-school network evidence;
- Stage A execution;
- Stage B entry evidence;
- deployment/support readiness.

### Legal / Privacy

- applicable privacy/consent/legal review;
- official guidance usage/licensing implementation review.

### Commercial

- numeric Teacher Pro price;
- unit-economics evidence;
- willingness-to-pay evidence;
- commercial launch gates.

## Latest Handoff Checkpoint

[Source Scaffolding Checkpoint — Complete](./checkpoints/2026-09-10-source-scaffolding-complete.md)
is the latest handoff checkpoint. It records completed Source Scaffolding,
reviewed SS-06 evidence, and the merge-readiness boundary for
`feat/source-scaffolding`. Application implementation has not started.

The previous [Contract Foundation Checkpoint — Complete](./checkpoints/2026-09-09-contract-foundation-complete.md)
remains immutable and historically correct. Its subsequent merge is recorded
by merge commit `7995290b07c755425020b939fb669f9c4a0b2ce0`.

The previous [Architecture Foundation Checkpoint — Complete](./checkpoints/2026-09-08-architecture-foundation-complete.md)
remains immutable and historically correct for its phase handoff.

The prior [Architecture Foundation Checkpoint — After OAD-004](./checkpoints/2026-09-07-architecture-foundation-after-oad004.md)
remains historical and is retained unmodified. It predates ADR-0013 and
ADR-0019; any Java 25 or "deployment not selected" reference in that
checkpoint reflects the state at its own creation and is superseded by the
current canonical architecture (ADR-0013: Java 21 LTS; ADR-0019: portable
single-Linux-VPS deployment). Historical checkpoint documents are not modified
after creation except through an explicit correction task.

## AI / Contributor Reading Route

For "what is next?", phase planning, architecture sequencing, or project
orientation:

`AGENTS.md` → `PROJECT_STATUS.md` → relevant canonical source

For feature implementation:

`AGENTS.md` → relevant PRD section → feature spec → architecture / ADR →
contracts → standards → source/tests

`PROJECT_STATUS.md` is not required for every small implementation task once the
relevant scope is already known.

## Status Update Rule

Update this document when:

- a major OAD is resolved;
- contract-definition phase begins/completes;
- source scaffolding begins;
- implementation phase materially changes;
- Stage A or Stage B gate status changes;
- a major product decision is explicitly superseded;
- commercial phase changes.

Do not update it for every minor code or documentation edit.

Do not copy detailed decision text into this document.
