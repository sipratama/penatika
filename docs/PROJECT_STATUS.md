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
| Version | 0.3 |
| Last Updated | 2026-09-06 |
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
| Initial architecture principles | ESTABLISHED | [SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md) + ADR-0001–ADR-0009 |
| Architecture / technology decisions | ACTIVE | [SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md) OAD-003–OAD-012 |
| Field-level contracts | PENDING | create only after relevant OAD decisions |
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
| OAD-003 | Database and migration technology | PENDING |
| OAD-004 | Identity, authentication, and account model | PENDING |
| OAD-005 | Realtime transport and reconnect protocol | PENDING |
| OAD-006 | AI provider/model strategy and fallback | PENDING |
| OAD-007 | Speech recognition strategy | PENDING |
| OAD-008 | Mathematics validator approach per content type | PENDING |
| OAD-009 | Curriculum ingestion, normalization, integrity/versioning, local-context modeling, and retrieval | PENDING |
| OAD-010 | Deployment platform, environments, secret management, and regional requirements | PENDING |
| OAD-011 | Background execution and queue needs | CONDITIONAL |
| OAD-012 | Contract protocols and schema tooling | NEXT |

OAD-011 remains evidence-triggered and must not be selected merely because a
queue is common or familiar.

The exact OAD definitions and `Needed Before` rules remain authoritative in
[SYSTEM_ARCHITECTURE.md](./02_architecture/SYSTEM_ARCHITECTURE.md).

Architecture reasoning is not duplicated here.

## Immediate Next Step

Immediate next decision work:

1. OAD-012 — Contract Protocols and Schema Tooling

Client and backend platform boundaries are now selected. Cross-component
contract strategy must be established before source scaffolding. Contracts
remain PENDING until OAD-012 is resolved, and source scaffolding remains
PENDING. Later OAD sequencing will follow dependency and evidence requirements.

## Open Non-Product Follow-Ups

These are **not** unresolved Q-01–Q-07 / OPD-001–OPD-007 decisions.

### Architecture / Implementation

- OAD-003 through OAD-012 as applicable;
- field-level structured contracts;
- physical persistence model/migrations;
- identity implementation;
- reconnect/transport protocol;
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
