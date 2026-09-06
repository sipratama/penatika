# Pendago → Penatika Legacy Decision Migration Review

> Canonical record of which Pendago / AI Teaching Canvas decisions remain valid inputs to the Penatika product baseline.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Approved Baseline |
| Date | `2026-09-06` |
| Legacy Source | `sipratama/pendago` |
| Canonical Destination | `sipratama/penatika` |

## 1. Purpose

This review resolves `Q-05` by classifying the approved Pendago legacy decision set and mapping compatible ideas to current Penatika canonical sources. It preserves useful product evidence without making Pendago a parallel source of truth, importing legacy documents, or activating legacy contracts.

The governing migration sequence is:

> Review → classify → map → adopt only when compatible.

A legacy document is never, by itself, a Penatika requirement.

## 2. Authority Rule

- Penatika canonical documents and Accepted ADRs are authoritative.
- Pendago is legacy input, not a parallel source of truth.
- No Pendago decision enters the Penatika baseline unless it is explicitly reviewed and mapped.
- When a Pendago decision conflicts with a current Penatika Product Brief, PRD, feature specification, System Architecture rule, or Accepted ADR, Penatika wins.
- Legacy field-level contracts, taxonomies, and implementation mechanics remain inactive until re-derived and revalidated against current Penatika requirements.
- Legacy material may remain useful as design or historical evidence without becoming an active requirement.

## 3. Legacy Source Inventory

The reviewed source set is limited to the following Pendago documents.

### Product Discovery

- `v0.1-product-definition.md`
- `v0.2-classroom-experience.md`
- `v0.3-teaching-intelligence-command-model.md`
- `v0.4-curriculum-mathematics-intelligence.md`
- `v0.5-physical-classroom-device-experience.md`

### Architecture

- `v0.1-system-architecture.md`
- `v0.2-session-lifecycle.md`
- `v0.3-teaching-action-contract.md`
- `v0.4-teaching-scene-contract.md`
- `v0.5-question-package-contract.md`

These files were reviewed as legacy evidence only. Their text and contracts are not copied into Penatika.

## 4. Classification Definitions

| Classification | Meaning |
|---|---|
| `ADOPTED` | The legacy decision is compatible with and already represented by the canonical Penatika baseline. |
| `ADOPTED_WITH_REFINEMENT` | The underlying idea remains valid, but Penatika narrows, strengthens, or redefines its behavior. |
| `REFERENCE_ONLY` | The material may inform later design or contracts but is not an active Penatika requirement or architecture decision. |
| `SUPERSEDED` | A newer Penatika decision explicitly replaces the legacy behavior. |
| `DEFERRED` | The capability may be reconsidered later but is outside the current MVP or first-pilot scope. |

## 5. Adopted Decisions

The following legacy decision families are already owned by current Penatika sources and require no duplicated requirements.

| # | Adopted Decision | Current Penatika Canonical Owner |
|---|---|---|
| 1 | AI-native teaching copilot rather than a generic chatbot | [Product Brief](../00_product/PRODUCT_BRIEF.md) |
| 2 | Teacher remains the classroom decision-maker | [Product Brief](../00_product/PRODUCT_BRIEF.md), [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md) |
| 3 | Initial market is Indonesian teachers | [Product Brief](../00_product/PRODUCT_BRIEF.md) |
| 4 | Initial subject is Mathematics | [Product Brief](../00_product/PRODUCT_BRIEF.md), [PRD](../00_product/PRD.md) |
| 5 | Core grade range is Grade 4 SD through Grade 9 SMP | [Product Brief](../00_product/PRODUCT_BRIEF.md), [PRD](../00_product/PRD.md) |
| 6 | Product is software-first and hardware-agnostic | [Product Brief](../00_product/PRODUCT_BRIEF.md) |
| 7 | Minimum physical concept is teacher smartphone, laptop/PC, classroom display, and internet | [Product Brief](../00_product/PRODUCT_BRIEF.md), [Classroom Session](../01_features/classroom-session.md) |
| 8 | Primary surfaces are teacher preparation, private teacher controller, and classroom display | [Product Brief](../00_product/PRODUCT_BRIEF.md), [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) |
| 9 | Controller/display sessions are cloud-mediated | [Classroom Session](../01_features/classroom-session.md), [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md) |
| 10 | Classroom operation does not require screen mirroring | [Classroom Session](../01_features/classroom-session.md), [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md) |
| 11 | Pairing does not require devices to share Wi-Fi | [Classroom Session](../01_features/classroom-session.md), [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md) |
| 12 | Student devices are not required for the core experience | [Product Brief](../00_product/PRODUCT_BRIEF.md), [PRD](../00_product/PRD.md) |
| 13 | Classroom content is structured rather than arbitrary AI-generated HTML | [Classroom Canvas](../01_features/classroom-canvas.md), [ADR-0003](../02_architecture/adr/ADR-0003-structured-classroom-content.md) |
| 14 | Teacher-private and classroom-public information use separate projections | [Classroom Canvas](../01_features/classroom-canvas.md), [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md) |
| 15 | Classroom state remains backend-authoritative | [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md), [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) |
| 16 | AI output is a proposal and never authoritative state | [Live AI Adaptation](../01_features/live-ai-adaptation.md), [ADR-0004](../02_architecture/adr/ADR-0004-ai-assurance-boundary.md), [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md) |
| 17 | Deterministic Mathematics assurance is applied when applicable | [Mathematics Assurance](../01_features/mathematics-assurance.md), [ADR-0004](../02_architecture/adr/ADR-0004-ai-assurance-boundary.md) |
| 18 | Curriculum grounding is controlled and versioned rather than inferred from model memory | [Mathematics Assurance](../01_features/mathematics-assurance.md), [ADR-0005](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md) |
| 19 | Push-to-talk is an important teaching interaction | [Live AI Adaptation](../01_features/live-ai-adaptation.md) |
| 20 | Manual and non-voice controls remain available | [Live AI Adaptation](../01_features/live-ai-adaptation.md), [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md) |
| 21 | Raw teacher push-to-talk audio is not stored by default | [PRD](../00_product/PRD.md), [Live AI Adaptation](../01_features/live-ai-adaptation.md) |
| 22 | MVP digital ink includes write, highlight, erase, undo, redo, and clear | [Classroom Canvas](../01_features/classroom-canvas.md) |
| 23 | Live teaching adaptation is a core product differentiator | [Product Brief](../00_product/PRODUCT_BRIEF.md), [Live AI Adaptation](../01_features/live-ai-adaptation.md) |
| 24 | Initial validation slices are Grade 5 Fractions and Grade 7 Basic Algebra / Linear Equations | [Product Brief](../00_product/PRODUCT_BRIEF.md), [MVP Pilot Plan](./PILOT_PLAN.md) |
| 25 | AI publication is atomic from the classroom perspective; incomplete generation does not appear as classroom content | [Live AI Adaptation](../01_features/live-ai-adaptation.md), [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md) |

## 6. Adopted with Refinement

| Legacy Concept | Penatika Refinement | Current Canonical Owner |
|---|---|---|
| Teacher smartphone as the center of teacher identity | The smartphone is retained as the private teacher controller, but it is not locked as identity authority. Identity and authentication remain open architecture decisions. | [Product Brief](../00_product/PRODUCT_BRIEF.md), [Classroom Session](../01_features/classroom-session.md), [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) |
| Auto Mode and Approval Mode | The valid principle is split by action type: supported deterministic non-generative actions may execute directly, while AI semantic generation follows the Q-02 publication policy. The legacy two-mode product behavior is not retained. | [Live AI Adaptation](../01_features/live-ai-adaptation.md), [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md) |
| Natural teacher intent becomes structured application intent | The structured-intent principle remains valid, but the legacy intent taxonomy, field names, and schema are not active Penatika contracts. | [Live AI Adaptation](../01_features/live-ai-adaptation.md), [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) |
| Structured, renderer-safe TeachingScene | The structured-scene principle remains valid, while exact legacy scene types, object families, fields, and schema remain reference-only. | [Classroom Canvas](../01_features/classroom-canvas.md), [ADR-0003](../02_architecture/adr/ADR-0003-structured-classroom-content.md) |
| Safe prepared lesson or projection cache | Safe already-prepared or already-displayed read material may remain locally available, but cache is never authority and cannot create or replay new offline authoritative mutations. | [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md), [Classroom Session](../01_features/classroom-session.md) |

## 7. Superseded Decisions

| Superseded Pendago Decision | Replacing Penatika Decision |
|---|---|
| Generative Auto Mode may publish unseen semantic AI content | Q-02 / [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md): teacher request authorizes generation; teacher approval authorizes publication. |
| Initial generation request is sufficient publication authorization | Q-02 / [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md): generated semantic content requires post-generation authorization. |
| Offline navigation or digital-ink changes may create authoritative or replayable local mutations while backend authority is unavailable | Q-03 / [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md): clients preserve safe state but do not create new authoritative mutations. |
| A display-local provisional mutation journal may automatically replay newly created offline state changes | Q-03 / [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md): no new offline mutation queue or automatic replay. |
| A controller or display may become temporary classroom authority | [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md): session authority remains in the backend. |
| The first pilot may broaden Mathematics scope to demonstrate coverage | Q-04 / [MVP Pilot Plan](./PILOT_PLAN.md): the first pilot remains limited to Grade 5 Fractions and Grade 7 Basic Algebra / Linear Equations. |

These decisions remain part of Pendago history but are not valid Penatika requirements.

## 8. Reference-Only Material

The following material may inform later design or contract work but is not active Penatika behavior:

- exact 11-intent `TeachingAction` taxonomy;
- legacy `TeachingAction` field-level contract;
- exact `TeachingScene` type taxonomy;
- exact scene object taxonomy;
- `QuestionPackage` contract, including the detailed question, answer, hint, solution, difficulty, provenance, and representation bundle;
- canonical-snapshot plus ordered-command-journal implementation shape;
- command-journal storage details;
- exact lifecycle protocol details;
- legacy idempotency fields and schema choices;
- exact display-epoch mechanics;
- exact target-resolution schema;
- detailed hint-ladder representation;
- detailed misconception model;
- detailed representation-engine taxonomy;
- generic curriculum-alignment override or “turn curriculum off” behavior.

`REFERENCE_ONLY` does not mean rejected. Any future adoption must be re-derived against Penatika Q-01 through Q-04, current feature requirements, current architecture, and Accepted ADRs. Future out-of-scope curriculum behavior must preserve authority level, warning, provenance, approval, assurance, and blocking rules.

## 9. Deferred Capabilities

The following capabilities remain outside the current MVP or first-pilot scope:

- PDF or PPT lesson import;
- AI handwriting interpretation or student handwritten-answer assessment;
- AI voice output;
- optional student-device interaction;
- attendance;
- school administration;
- teacher or class history beyond the separately unresolved retention/history policy;
- SMA expansion;
- advanced graphing;
- 3D visualization;
- broad geometry expansion;
- misconception intelligence as a standalone MVP capability;
- richer assessment workflows;
- advanced student-response analytics.

The following physical and device details remain future design or compatibility inputs rather than locked product requirements:

- exact QR plus numeric pairing UX;
- screen Wake Lock behavior;
- exact fullscreen behavior;
- display-switching UX;
- exact `1280x720` minimum support target;
- detailed smart-stylus behavior;
- palm-rejection expectations.

Deferred material must not be silently promoted into the MVP, the first pilot, or active contracts.

## 10. Legacy Architecture / Contract Handling

- No Pendago architecture document is automatically authoritative for Penatika.
- No legacy field-level contract is an active Penatika contract.
- The useful principles behind `TeachingAction`, `TeachingScene`, and `QuestionPackage` may inform future design, but names, fields, enums, schemas, lifecycle mechanics, idempotency choices, and persistence shapes must be re-derived.
- Re-derivation must begin from the current Product Brief, PRD, feature specifications, System Architecture, and Accepted ADRs.
- Any future REST, event, or persistent schema remains subject to Penatika's contract-first and migration rules.
- Legacy implementation details must not choose technology, providers, identity authority, transport, persistence, or deployment for Penatika.

## 11. Traceability Matrix

| Legacy Source / Decision Family | Classification | Current Penatika Canonical Owner | Notes |
|---|---|---|---|
| `v0.1-product-definition.md` — teaching copilot, Indonesian Mathematics market, teacher control, software-first hardware model | `ADOPTED` | [Product Brief](../00_product/PRODUCT_BRIEF.md), [PRD](../00_product/PRD.md) | Product principles already exist in the current baseline. |
| `v0.2-classroom-experience.md` — three surfaces, cloud pairing, no mirroring/shared Wi-Fi, private/public separation, ink, voice input | `ADOPTED` | [Classroom Session](../01_features/classroom-session.md), [Classroom Canvas](../01_features/classroom-canvas.md), [Live AI Adaptation](../01_features/live-ai-adaptation.md) | Active behavior is owned by current feature specifications and ADRs. |
| `v0.2-classroom-experience.md` — smartphone as identity center | `ADOPTED_WITH_REFINEMENT` | [Classroom Session](../01_features/classroom-session.md), [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) | Smartphone is the private controller; identity authority remains undecided. |
| `v0.2-classroom-experience.md` and `v0.3-teaching-intelligence-command-model.md` — Auto Mode / Approval Mode | `SUPERSEDED` | [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md) | Replaced by separate deterministic direct-action and post-generation AI publication policy. |
| `v0.3-teaching-intelligence-command-model.md` and `v0.3-teaching-action-contract.md` — structured teacher intent | `ADOPTED_WITH_REFINEMENT` | [Live AI Adaptation](../01_features/live-ai-adaptation.md), [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) | Principle retained; exact taxonomy and contract remain reference-only. |
| `v0.4-curriculum-mathematics-intelligence.md` — deterministic assurance, curriculum grounding, Grade 5/7 slices | `ADOPTED` | [Mathematics Assurance](../01_features/mathematics-assurance.md), [ADR-0005](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md), [MVP Pilot Plan](./PILOT_PLAN.md) | Current Penatika authority hierarchy and pilot scope control interpretation. |
| `v0.4-curriculum-mathematics-intelligence.md` — QuestionPackage, detailed hints, misconceptions, representations, generic curriculum override | `REFERENCE_ONLY` | Future contract or product decision | Useful design input; not an active contract or generic override capability. |
| `v0.5-physical-classroom-device-experience.md` — prepared cache and degraded offline mutations | `ADOPTED_WITH_REFINEMENT` / `SUPERSEDED` | [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md) | Safe cached read material is compatible; offline authority and replayable new mutations are not. |
| `v0.1-system-architecture.md` and `v0.2-session-lifecycle.md` — backend authority, projections, atomic publication | `ADOPTED` | [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md), [ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md) | Current ADRs own the compatible invariants. |
| `v0.1-system-architecture.md` and `v0.2-session-lifecycle.md` — provisional journals, exact epochs, lifecycle, idempotency, snapshot/journal mechanics | `SUPERSEDED` / `REFERENCE_ONLY` | [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md), future Penatika contracts | Offline-authority behavior is superseded; exact mechanics require re-derivation. |
| `v0.4-teaching-scene-contract.md` — structured renderer-safe scene principle | `ADOPTED_WITH_REFINEMENT` | [Classroom Canvas](../01_features/classroom-canvas.md), [ADR-0003](../02_architecture/adr/ADR-0003-structured-classroom-content.md) | Principle retained; exact scene contract is reference-only. |
| `v0.5-question-package-contract.md` — field-level package contract | `REFERENCE_ONLY` | Future Penatika contract work | Must be re-derived from current requirements and assurance policy. |
| Legacy import, student interaction, administration, advanced visualization, and assessment capabilities | `DEFERRED` | [Product Roadmap](../00_product/ROADMAP.md), [Product Brief](../00_product/PRODUCT_BRIEF.md) | Remain later directions or non-goals and do not enter the first pilot. |

## 12. Rules for Future Legacy Reuse

Before reusing any additional Pendago material:

1. Identify the exact legacy source and decision being considered.
2. Identify the current Penatika source that owns the affected product behavior, architecture, contract, or delivery concern.
3. Classify the legacy decision using this review's five statuses.
4. Check compatibility with the Product Brief, PRD, feature specifications, System Architecture, and all relevant Accepted ADRs.
5. Re-derive field-level contracts and implementation mechanics instead of copying them.
6. Update this migration review when the classification, mapping, or approved reuse changes.
7. Update the actual Penatika authoritative source only when a separately approved product or architecture decision changes it.

No future legacy reuse may bypass current teacher-control, publication approval, Mathematics assurance, curriculum provenance, backend authority, graceful degradation, privacy, revision, or pilot-scope rules.

## 13. Remaining Open Decisions

This review does not resolve:

- `Q-06` / `OPD-007` — business model and commercial release path;
- `Q-07` / `OPD-006` — product ownership and requirement approval authority;
- `OPD-005` — lesson/session retention, history, export, and deletion expectations;
- identity, authentication, realtime, persistence, AI, speech, validation, deployment, or provider choices;
- future field-level lesson, action, scene, question, session, event, or persistence contracts;
- whether any deferred Pendago capability should enter a later Penatika scope.

## 14. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.1` | `2026-09-06` | Resolve Q-05 by classifying and mapping the approved Pendago legacy decision set | Codex |
