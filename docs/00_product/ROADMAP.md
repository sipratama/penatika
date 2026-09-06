# Product Roadmap — Penatika

> Outcome-oriented sequencing for Penatika. This roadmap does not commit to calendar dates.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.5` |
| Owner | `sipratama — Product Owner` |
| Last Updated | `2026-09-06` |

## 1. Current Product Stage

Penatika is in post-discovery, pre-implementation initialization. The Minimum Product Context Gate has passed, the narrow first-pilot design is defined, the Pendago legacy baseline review is complete, founder-led product authority is established in [PRODUCT_GOVERNANCE.md](./PRODUCT_GOVERNANCE.md), and the teacher-first freemium commercial baseline is resolved in [BUSINESS_MODEL.md](./BUSINESS_MODEL.md), but classroom usability, pilot results, market hypotheses, and technology choices remain unvalidated.

## 2. Roadmap Principle

Prioritize evidence that the complete teacher-to-classroom workflow improves teaching flow while preserving teacher control and content trust. Do not expand feature count before the core workflow is usable and observable.

## 3. Now — MVP Foundation and Vertical Slice

### Objective

Turn the initialized product and architecture baseline into one safe end-to-end vertical slice for the two prioritized Mathematics areas.

### Included Capabilities

- `CAP-LESSON-001` — prepare and review a structured lesson.
- `CAP-SESSION-001` — start and pair a classroom session.
- `CAP-CANVAS-001` and `CAP-INK-001` — render, navigate, and annotate classroom content.
- `CAP-ADAPT-001` — request one or more bounded live adaptations.
- `CAP-MATH-001` — validate scoped fraction and algebra content and attach curriculum provenance.
- `CAP-SESSION-002` — end and save the session.

### Required Decisions

- Curriculum ingestion and provenance contract for BSKAP 046/H/KR/2025, local-context modeling, and official-guidance usage/licensing.
- Client, backend, persistence, identity, realtime, AI, speech, validation, and deployment technology baseline.
- Versioned field-level contracts for lesson, scene, session, adaptation, and assurance.
- Privacy review, implementation/evidence for the approved retention policy, deployment/support, target-device, and risk-readiness gates required before real-classroom Stage B. Product ownership, requirement approval authority, and business model are resolved.
- Any future Pendago reuse must be explicitly classified and mapped through the approved [legacy decision migration review](../06_delivery/PENDAGO_MIGRATION_REVIEW.md); legacy contracts do not become active Penatika contracts without re-derivation and validation.

### Exit Evidence

- A teacher can complete prepare, review, start, pair, present, adapt, annotate, and save in a controlled environment.
- Private teacher state never appears on the classroom projection.
- Supported Mathematics validation detects representative valid and invalid cases.
- Failure and reconnect paths preserve safe authoritative state.
- Architecture, contracts, and tests match the implemented vertical slice.

## 4. Next — Classroom Usability and Pilot Readiness

### Objective

Evaluate whether Penatika improves teacher flow and confidence in realistic classroom environments.

### Candidate Work

- Execute the staged [Penatika MVP Pilot Plan](../06_delivery/PILOT_PLAN.md) within Grade 5 Fractions and Grade 7 Basic Algebra / Linear Equations.
- Refine interaction speed, controller ergonomics, and classroom readability.
- Expand validation corpus within agreed MVP topics.
- Validate supported devices, browsers, display resolutions, and network conditions.
- Complete applicable privacy review and implement the approved retention, support, deployment, and operational procedures for pilot use.
- Instrument pilot evidence without student profiling.
- Use Pendago material only through the approved [legacy decision migration review](../06_delivery/PENDAGO_MIGRATION_REVIEW.md), without promoting deferred capabilities into the first-pilot scope.

### Entry Condition

Stage A may begin when the MVP vertical slice supports the required workflow, relevant rehearsal release blockers are absent, pilot instrumentation is available, the initial Mathematics evaluation corpus exists, and safe mock or sanitized data can be used.

Real-classroom Stage B additionally requires Stage A hard safety gates to pass, applicable privacy review, implemented and tested retention/deletion/export/backup-expiry procedures, accepted deployment/support readiness, reviewed target device/browser/network evidence, explicit disposition of high-impact pilot risks, and the named Product Owner (`sipratama`) to perform the product go/no-go decision after all required entry evidence is satisfied. `Q-07` resolves who approves Stage B; it does not remove or weaken any entry gate.

### Exit Evidence

The staged pilot targets 6–8 Mathematics teachers, representation of both supported topic contexts, at least 6 evaluated classroom sessions in Stage B, and at least 12 evaluated teacher sessions across both stages. Evidence is classified as `PROCEED`, `ITERATE`, or `BLOCKED` using the hard gates and primary thresholds in `PILOT_PLAN.md`.

Expansion to additional topics may be considered only when all hard gates pass, primary product thresholds support `PROCEED`, and no unresolved high-impact risk blocks expansion.

## 5. Then — Post-Pilot Commercialization and Pricing Validation

After classroom and pilot validation, commercialization proceeds through the evidence-gated phases in [BUSINESS_MODEL.md](./BUSINESS_MODEL.md), without calendar dates:

- post-pilot early access with cost, quota, unit-economics, and willingness-to-pay validation;
- individual Teacher Pro commercial launch only after the commercial launch gates are satisfied;
- institutional licensing only after teacher adoption and administrator needs are evidenced and validated.

Commercial launch is not automatic after pilot `PROCEED`. The roadmap remains focused on product evidence before revenue experimentation.

## 6. Later — Candidate Directions

These directions are not commitments:

| Direction | Opportunity | Revisit Trigger |
|---|---|---|
| Additional Mathematics topics and grades | Broader teacher usefulness | Hard gates pass, product thresholds support `PROCEED`, and no unresolved high-impact risk blocks expansion |
| Additional subjects | Wider market applicability | Subject-specific assurance and curriculum model is understood |
| Lesson import | Reduce migration effort from existing materials | Core creation/editing workflow is stable |
| Student participation | Interactive classroom learning | Teacher-first core succeeds and privacy/identity implications are resolved |
| Advanced graphing or visualization | Richer Mathematics explanation | Structured scene model and assurance support it safely |
| School workflows | Institutional adoption | Business model and administrator needs are validated |

## 7. Explicitly Parked

- Autonomous AI teaching.
- Attendance management.
- Student devices as a core prerequisite.
- Proprietary smart-board dependence.
- Feature expansion that bypasses teacher-control or content-assurance evidence.

## 8. Key Product Risks

- The controller may still distract teachers.
- Classroom connectivity may be insufficient for the intended flow.
- AI latency may interrupt teaching.
- Deterministic validation coverage may be too narrow.
- Curriculum data may be unavailable, ambiguous, or restricted.
- Private and public projections may leak across surfaces.

See [RISKS.md](../06_delivery/RISKS.md) for the active risk register.

## 9. Roadmap Changes

| Version | Date | Change | Author |
|---|---|---|---|
| `0.5` | `2026-09-06` | Record the resolved business-model baseline and staged commercialization sequence | Codex |
| `0.4` | `2026-09-06` | Record resolved founder-led product authority while preserving remaining Stage B evidence gates | Codex |
| `0.3` | `2026-09-06` | Record completed Pendago baseline review and controlled future legacy reuse | Codex |
| `0.2` | `2026-09-06` | Lock staged first-pilot scope, evidence targets, and expansion trigger | Codex |
| `0.1` | `2026-09-06` | Initial outcome-based roadmap after Product Context Gate | Codex |
