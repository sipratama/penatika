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
| Version | 0.10 |
| Last Updated | 2026-09-07 |
| Current Phase | Architecture & Technology Decisioning |
| Implementation State | Pre-source / Pre-scaffolding |
| Product Owner | sipratama |

The final product-baseline consistency audit passed before this checkpoint was
created.

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
| Initial architecture principles | ESTABLISHED | [SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md) + ADR-0001–ADR-0012 |
| Architecture / technology decisions | ACTIVE | [SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md) OAD-006–OAD-008, OAD-010–OAD-011 |
| Contract strategy | COMPLETE | [ADR-0010](./02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md) / [contracts](../contracts/README.md) |
| Field-level contracts | PENDING | identity and realtime transport semantics are now selected (ADR-0011, ADR-0012); field-level OpenAPI/JSON Schema work has not started |
| Source scaffolding | PENDING | after blocking OADs/contracts |
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
| OAD-006 | AI provider/model strategy and fallback | PENDING |
| OAD-007 | Speech recognition strategy | PENDING |
| OAD-008 | Mathematics validator approach per content type | NEXT |
| OAD-009 | Curriculum ingestion, normalization, integrity/versioning, local-context modeling, and retrieval | COMPLETE |
| OAD-010 | Deployment platform, environments, secret management, and regional requirements | PENDING |
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

## Immediate Next Step

Immediate next decision work:

1. OAD-008 — Mathematics Validator Approach per Content Type

Curriculum authority storage/retrieval is now defined independently of AI
(ADR-0015). The next trust dependency should be the deterministic
Mathematics validator strategy before AI provider selection (OAD-006), so
the AI integration remains subordinate to deterministic trust boundaries
rather than the reverse. Teacher identity, browser authentication, pairing,
participant roles, and authorization are defined by ADR-0011. Realtime
transport, credential carriage, and reconnect mechanics are defined by
ADR-0012 (SSE push with existing HTTP commands; AsyncAPI stays inactive).
Field-level contracts remain PENDING: the blocking architecture semantics
now exist, but the OpenAPI/JSON Schema field-level work itself has not
started. Source scaffolding also remains PENDING. `SYSTEM_ARCHITECTURE.md`
remains authoritative for `Needed Before` rules.

## Open Non-Product Follow-Ups

These are **not** unresolved Q-01–Q-07 / OPD-001–OPD-007 decisions.

### Architecture / Implementation

- OAD-006–OAD-008 and OAD-010–OAD-011 as applicable;
- field-level structured contracts;
- physical persistence model/migrations;
- identity implementation;
- realtime/reconnect transport implementation;
- provider integrations.

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

[Architecture Foundation Checkpoint — After OAD-004](./checkpoints/2026-09-07-architecture-foundation-after-oad004.md)
was created as a historical cross-device / AI handoff snapshot before OAD-005.
It is not authoritative and does not replace the canonical project, product,
architecture, ADR, contract, or policy documents.

That checkpoint is historical and predates ADR-0013. Any Java 25 reference in
that checkpoint reflects the state at checkpoint creation and is superseded
by the current canonical architecture (ADR-0013: Java 21 LTS). The checkpoint
document itself is not modified.

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
