# Penatika Architecture Foundation Checkpoint — After OAD-004

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Checkpoint Date | `2026-09-07` |
| Branch | `feat/architecture-foundation` |
| Checkpoint Head | `e3a7b807` |
| Phase | Architecture & Technology Decisioning |
| Implementation State | Pre-source / Pre-scaffolding |
| Product Owner | `sipratama` |
| Checkpoint Type | Cross-device / AI handoff |
| Status | Stable Handoff Point |

> **Historical snapshot:** This document is a historical handoff snapshot. It
> does not replace [PROJECT_STATUS.md](../PROJECT_STATUS.md), the
> [Product Brief](../00_product/PRODUCT_BRIEF.md), the
> [PRD](../00_product/PRD.md), the
> [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md), accepted
> ADRs, or specialized policy documents. If this checkpoint later conflicts
> with a newer canonical document, the newer canonical document wins.

## 1. Baseline Branch Model

- `main` is the stable product/initialization checkpoint branch.
- Active architecture work occurs on `feat/architecture-foundation`.
- The architecture branch derives from the `main` checkpoint created before
  OAD work began.
- Continue architecture work on `feat/architecture-foundation`, not directly
  on `main`.
- Do not merge to `main` merely because this handoff checkpoint exists.

## 2. Completed Project Phases

The following are complete:

- Project Discovery;
- Minimum Product Context Gate;
- Project Initialization;
- Q-01 through Q-07 product decisions;
- OPD-001 through OPD-007 baseline;
- final product-baseline consistency audit;
- product checkpoint/status setup.

The detailed product baseline remains authoritative in its existing canonical
documents and is not duplicated here.

## 3. Completed Architecture Decisions

### OAD-001 — Complete

[ADR-0008](../02_architecture/adr/ADR-0008-browser-first-react-client-strategy.md)
selects browser-first React, TypeScript, and Vite clients:

- Teacher Web with Preparation and Private Controller modes;
- a separate Classroom Display Web application boundary;
- no native application in the MVP.

### OAD-002 — Complete

[ADR-0009](../02_architecture/adr/ADR-0009-java-spring-boot-backend.md)
selects:

- Java 25 LTS;
- Spring Boot 4.x;
- one deployable modular monolith;
- framework-light domain/application behavior where practical;
- no microservice split.

### OAD-012 — Complete

[ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
selects:

- contract-first cross-component development;
- OpenAPI 3.1.x with OpenAPI 3.1.2 as the current baseline;
- JSON Schema Draft 2020-12;
- RFC 9457 Problem Details;
- AsyncAPI 3.1.x only conditionally after the realtime decision;
- authoritative contracts with generated implementation types as derived
  consumers.

The current contract boundary contains:

```text
contracts/
  README.md
  openapi/README.md
  schemas/README.md
```

No field-level OpenAPI or JSON Schema contract exists yet.

### OAD-004 — Complete

[ADR-0011](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md)
selects this identity baseline:

- OpenID Connect with Authorization Code flow and PKCE `S256`;
- the Penatika Backend as confidential OIDC client and relying party;
- OAuth/OIDC tokens retained backend-side;
- an opaque backend-managed browser session rather than browser access/refresh
  token storage;
- no local password database;
- a stable internal `TeacherAccount` linked externally by `(issuer, subject)`;
- email is not an identity key;
- backend object-level authorization;
- controller authority requires an authenticated `TeacherAccount` plus an
  active participant binding;
- Classroom Display has separate scoped participant authorization and no
  teacher authority;
- one active mutation-authorized controller and one active classroom display
  per classroom session for MVP;
- role/session-bound, single-use, revocable pairing grants with a five-minute
  lifetime.

The concrete OIDC provider remains unresolved. Refer to ADR-0011 rather than
expanding this checkpoint into a competing identity specification.

## 4. Existing Foundational ADRs

These accepted decisions remain authoritative:

- [ADR-0001 — Modular Monolith Backend](../02_architecture/adr/ADR-0001-modular-monolith-backend.md)
- [ADR-0002 — Backend-Authoritative Classroom Session State](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md)
- [ADR-0003 — Structured Classroom Content](../02_architecture/adr/ADR-0003-structured-classroom-content.md)
- [ADR-0004 — Separate AI Generation from Mathematics/Curriculum Authority](../02_architecture/adr/ADR-0004-ai-assurance-boundary.md)
- [ADR-0005 — Layered Curriculum Authority and Provenance](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0008 — Browser-First Client Strategy](../02_architecture/adr/ADR-0008-browser-first-react-client-strategy.md)
- [ADR-0009 — Java/Spring Boot Backend](../02_architecture/adr/ADR-0009-java-spring-boot-backend.md)
- [ADR-0010 — Contract-First API/Schema Strategy](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0011 — OIDC Identity, Session, and Pairing Architecture](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md)

## 5. Current OAD Status

### Complete

- OAD-001 — Client application strategy and frontend framework;
- OAD-002 — Backend language and framework;
- OAD-004 — Identity, authentication, and account model;
- OAD-012 — Contract protocols and schema tooling.

### Next

- OAD-005 — Realtime Transport and Reconnect Protocol.

### Pending

- OAD-003 — Database and Migration Technology;
- OAD-006 — AI Provider / Model Strategy and Fallback;
- OAD-007 — Speech Recognition Strategy;
- OAD-008 — Mathematics Validator Approach;
- OAD-009 — Curriculum Ingestion / Retrieval;
- OAD-010 — Deployment / Environment / Secret Management.

### Conditional

- OAD-011 — Background Execution and Queue Needs.

OAD-011 remains evidence-triggered. Do not add Kafka, RabbitMQ, or another
queue/message broker merely by convention.

## 6. Immediate Next Decision

The next architecture task is **OAD-005 — Realtime Transport and Reconnect
Protocol**.

OAD-005 must reuse:

- ADR-0002 backend authority;
- ADR-0007 reconnect and degradation rules;
- ADR-0011 participant identity and authorization;
- ADR-0010 contract strategy.

It must not invent a separate realtime identity model. After OAD-005, the
current intended sequence is OAD-003 persistence unless newly discovered
dependency evidence requires reconsideration. This sequence is planning
guidance; `SYSTEM_ARCHITECTURE.md` remains authoritative for `Needed Before`
rules.

## 7. Important Things Not Yet Started

The following have not started:

- application source scaffolding;
- React application source;
- Spring Boot source;
- physical database schema;
- migrations;
- `openapi.yaml`;
- field-level JSON Schemas;
- AsyncAPI contract;
- CI implementation;
- deployment configuration;
- pilot Stage A execution;
- pilot Stage B;
- commercial implementation.

Architecture-document completion does not mean implementation has begun.

## 8. Important Technologies Still Not Selected

The following remain unselected:

- concrete OIDC provider;
- physical browser/session store;
- Maven versus Gradle;
- JDK distribution;
- exact Spring Boot patch;
- frontend package manager;
- realtime protocol;
- Spring MVC versus WebFlux where dependent on transport/workload decisions;
- primary database;
- ORM/data-access mechanism;
- migration tool;
- AI provider/model;
- speech provider;
- Mathematics validation implementation;
- curriculum storage/retrieval implementation;
- deployment platform;
- secret manager;
- queue/message broker.

Do not infer these choices from developer or AI familiarity.

## 9. Architecture Invariants the Next AI Must Preserve

- The backend owns authoritative classroom state.
- Client cache is never authority.
- Newly created offline mutations are not replayed automatically.
- A teacher request authorizes generation; teacher approval authorizes
  publication.
- A `BLOCKED` AI proposal cannot publish.
- Teacher-private projection never leaks to Classroom Display.
- Mathematics and curriculum authority remain independent from AI provider
  confidence.
- Contracts are contract-first and language-neutral.
- OAuth tokens stay out of browser JavaScript.
- Pairing is not teacher authentication.
- Classroom Display has no teacher authority.
- Source boundaries remain modular-monolith boundaries unless evidence
  justifies an accepted architecture change.

## 10. Canonical Reading Route for Claude

When continuing on Windows with Claude:

1. Read `AGENTS.md`.
2. Read `docs/PROJECT_STATUS.md`.
3. Read this checkpoint only for historical handoff context.
4. Read `docs/02_architecture/SYSTEM_ARCHITECTURE.md`.
5. For OAD-005, read ADR-0002, ADR-0007, ADR-0010, ADR-0011,
   `docs/01_features/classroom-session.md`, and
   `docs/04_engineering/THREAT_MODEL.md`.
6. Read additional canonical documents only when relevant to the task.

Do not load the entire repository blindly.

## 11. Windows Handoff

Repository: `https://github.com/sipratama/penatika`

Required working branch: `feat/architecture-foundation`

Example PowerShell workflow:

```powershell
git clone https://github.com/sipratama/penatika.git
cd penatika
git fetch origin
git switch feat/architecture-foundation
git pull --ff-only origin feat/architecture-foundation
git status
git log --oneline --decorate -8
```

Expected after the checkpoint is committed and pushed by the user:

- the branch is `feat/architecture-foundation`;
- the latest handoff/checkpoint commit is the checkpoint commit created from
  this task;
- the working tree is clean.

No technology-specific setup commands are provided because application source
does not exist.

## 12. Claude Starting Prompt

```text
Read:

1. AGENTS.md
2. docs/PROJECT_STATUS.md
3. docs/checkpoints/2026-09-07-architecture-foundation-after-oad004.md

Then inspect the relevant canonical sources for the task.

Do not modify main.
Continue only on feat/architecture-foundation.

The next architecture decision is:
OAD-005 — Realtime Transport and Reconnect Protocol

Before proposing or changing anything, verify the existing OAD/ADR state and
preserve all Accepted architecture invariants.

Do not scaffold source code yet.
Do not resolve OAD-003 in the OAD-005 task.
```

## 13. Checkpoint Validation

This snapshot was prepared against:

- `docs/PROJECT_STATUS.md` version `0.5` at checkpoint head `e3a7b807`;
- `docs/02_architecture/SYSTEM_ARCHITECTURE.md` version `0.9`;
- accepted ADR-0008 through ADR-0011;
- branch `feat/architecture-foundation` with pre-source/pre-scaffolding state;
- OAD-005 as `NEXT`, OAD-003 as `PENDING`, and OAD-011 as `CONDITIONAL`.

The checkpoint captures the state before this checkpoint file and its
navigation links are committed. Canonical documents continue to govern future
changes.

