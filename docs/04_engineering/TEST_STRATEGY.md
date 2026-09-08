# Test Strategy — Penatika

> Defines the evidence required to trust Penatika's product behavior, contracts, AI boundaries, session consistency, and classroom risks. Tooling will be selected with the implementation stack.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft baseline |
| Version | `0.4` |
| Last Updated | `2026-09-08` |
| Test Tooling | Open Architecture Decision |

## 1. Testing Objectives

- Prove teacher-control and privacy rules, not only happy-path UI behavior.
- Prove authoritative session state and revision semantics under retry, reconnect, and concurrency.
- Prove structured content contracts reject malformed and executable output.
- Prove Mathematics assurance states with deterministic corpora.
- Evaluate AI quality and failure behavior without treating nondeterministic output as a conventional unit test.
- Exercise realistic classroom devices, input methods, displays, and network conditions.

## 2. Traceability

Tests should reference stable PRD, feature, product-rule, architecture-invariant, and threat identifiers where relevant.

Minimum traceability targets:

- `CAP-*` capability acceptance;
- `FR-*` functional requirements;
- `PR-*` product-wide rules;
- `INV-*` architecture invariants;
- active threat mitigations;
- release blockers and NFR quality gates.

## 3. Test Layers

### Domain Unit Tests

Cover lesson readiness, session lifecycle, role-specific projection, revision rules, proposal lifecycle, validation-state semantics, curriculum provenance, and idempotency rules without network or provider dependencies.

### Contract and Schema Tests

Cover structured lesson/scene models, commands, projections, AI proposal envelopes, and assurance results. Include compatibility tests for supported schema versions and rejection tests for unknown executable content.

### Integration Tests

Cover persistence transactions, identity/authorization integration, realtime adapter behavior, AI/speech adapters using controlled fakes or recorded sanitized fixtures, curriculum retrieval, and validation engine integration.

### Frontend Component Tests

Cover visibility rules, status states, keyboard interaction, input controls, destructive confirmation, safe fallback rendering, and prevention of private data in classroom components.

### End-to-End Tests

Cover prepare → review → start → pair → teach → adapt → annotate → reconnect → save. Run across distinct teacher-controller and classroom-display contexts.

### Manual and Device Tests

Use real or representative smartphones, laptops/PCs, classroom displays, mouse, touch, and stylus. Validate viewing distance, controller ergonomics, orientation, and network variation.

## 4. Priority Regression Suites

### Teacher-Control Suite

- unreviewed lesson cannot start;
- AI proposal cannot display without the active policy path;
- classroom display cannot issue teacher commands;
- stale or unauthorized command cannot mutate session state.

### Privacy Projection Suite

- prompts, transcripts, raw provider output, warnings, credentials, and private progress are absent from display payloads and rendered output;
- raw audio is absent from persistence, logs, fixtures, and analytics by default.

### Session Reliability Suite

- duplicate, stale, and out-of-order commands;
- disconnect and reconnect of each surface;
- pairing expiry, replay, and revocation;
- idempotent end/save;
- safe last-known projection during recoverable failure.

### Structured Content Suite

- every supported content element;
- schema-version compatibility;
- malformed, oversized, unknown, and adversarial payloads;
- no script or arbitrary HTML execution;
- accessible rendering state where supported.

### Mathematics Assurance Suite

- correct and incorrect Grade 5 fraction cases;
- correct and incorrect Grade 7 basic algebra/linear-equation cases;
- equivalent forms;
- ambiguous, malformed, unsupported, and inconclusive cases;
- stale validation invalidation after content edits;
- normative Mathematics grounding resolves to BSKAP 046/H/KR/2025;
- official guidance cannot be surfaced as a national normative requirement;
- local sequencing remains `LOCAL_CONTEXT`;
- authority level, source version, phase/scope, and matching provenance are retained;
- saved lesson provenance remains stable after a newer source version is introduced;
- exact rational arithmetic properties (per [ADR-0016](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md)): `(a + b) - b = a`, `(a / b) * b = a` for supported nonzero values;
- Grade 5 fraction corpus: equivalent/non-equivalent fractions (`2/3 = 4/5` must be `INVALID`), reduction, addition, subtraction, multiplication, division, negative values, zero numerator, zero-denominator rejection, mixed numbers, large exact values within limits;
- Grade 7 affine/linear-equation corpus: affine equivalence, distributive simplification, exact rational coefficients, valid/invalid solutions, equivalent transformations, identity, contradiction/no-solution, malformed expressions, nonlinear expressions, multiple variables, variable denominators, unsupported functions, extreme/nested resource-bound input;
- unsupported nonlinear and multi-variable cases return `UNSUPPORTED`, never `VALID`;
- resource-bound adversarial expressions fail safely without unbounded CPU/memory use;
- validator/ruleset-version reproducibility;
- AI cannot override a deterministic `INVALID` result;
- content edit invalidates prior validation (`FR-MATH-007`).

### Deployment Verification Suite (Future, Per ADR-0019)

Once a real `PILOT` deployment exists, verification must cover:

- application health-check endpoint responds correctly;
- Docker Compose stack starts successfully from a clean host;
- Teacher Web and Classroom Display static assets are accessible through the reverse proxy;
- the backend is reachable only behind the reverse proxy, never directly as the public entry point;
- HTTPS is enforced for real pilot traffic;
- the SSE realtime channel functions correctly through the reverse proxy;
- PostgreSQL is not publicly reachable;
- secrets are absent from the frontend bundle, the backend image, and the repository;
- a failing database migration blocks deployment progression;
- the deployed artifact identity is immutable (commit SHA or image digest, never `latest`);
- automated PostgreSQL backup creation succeeds and is observable;
- the off-host backup copy exists at the approved destination;
- a restore exercise succeeds before meaningful reliance on pilot data;
- containers restart/recover correctly after failure;
- log rotation functions as configured;
- disk-pressure visibility/alerting is observable;
- an end-to-end deployment smoke test passes after each release.

No implementation or test exists yet for this suite; it defines future evidence requirements only.

## 5. AI Evaluation Strategy

AI behavior requires a versioned evaluation set separate from deterministic application tests.

Evaluate:

- adherence to structured output contract;
- grade and topic appropriateness;
- response to bounded adaptation intent;
- hallucinated curriculum claims;
- mathematical errors caught or missed;
- prompt-injection resistance at the application boundary;
- privacy-sensitive input minimization;
- latency, failure, and refusal behavior;
- quality difference across candidate providers/models.

Evaluation datasets must use sanitized or synthetic content unless an approved policy permits otherwise. Provider output snapshots alone are not sufficient acceptance evidence.

Per ADR-0017, a model/profile/provider route is activated for pilot/production only after passing Penatika's versioned evaluation corpus; `ROUTER` additionally requires classification-accuracy evaluation for `SUPPORTED`/`OUT_OF_SCOPE`/`UNSUPPORTED_CAPABILITY`/`NEEDS_CLARIFICATION` outcomes.

## 6. Security Testing

- authorization matrix and object-level access;
- session and pairing credential guessing, replay, expiry, and role escalation;
- injection and unsafe rendering attempts;
- prompt injection and malicious curriculum/lesson content;
- rate and resource-limit enforcement;
- sensitive logging and projection leakage;
- secret exposure in clients or build artifacts;
- dependency and supply-chain scanning after stack selection.

## 7. Accessibility Testing

- automated accessibility checks for supported surfaces;
- keyboard and focus-path tests;
- screen-reader semantics for status and Mathematics where supported;
- color-independent status communication;
- reduced-motion behavior;
- target-size and touch interaction review;
- classroom readability review on target displays.

Automated checks do not replace manual assistive-technology and classroom-context testing.

## 8. Performance and Reliability Testing

After a measurable prototype exists:

- baseline local input feedback;
- command-to-projection latency percentiles;
- reconnect convergence;
- AI and speech latency categories;
- concurrent session and command load;
- soak tests for long classroom sessions;
- dependency timeout, retry, and recovery scenarios.

Do not set pass thresholds before workload and target environments are defined.

## 9. Test Data

- Prefer deterministic builders and synthetic teacher/lesson/session data.
- Do not use production personal data.
- Maintain versioned Mathematics and curriculum fixtures with provenance.
- Keep provider fixtures sanitized and free of secrets or raw audio.
- Separate valid, invalid, unsupported, adversarial, and degradation datasets.

## 10. Environments

Required test environments will include:

- local deterministic development tests;
- integration environment with replaceable provider fakes;
- controlled provider-evaluation environment;
- multi-device classroom simulation;
- pilot-like device and network environment.

Environment and deployment technology remain open.

## 11. Completion Evidence

For each implementation increment, report exact commands, test scope, results, skipped checks, and unresolved failures. No test is considered passed unless executed.

Before pilot, evidence must cover all release blockers, high threats, architecture invariants, feature acceptance, AI evaluations, and target-device checks.

## 12. Open Testing Decisions

- Language/framework test tools and CI platform.
- Formal coverage expectations.
- AI evaluation scoring and acceptance thresholds.
- Target-device/browser/network matrix.
- Performance and reliability thresholds.
- Pilot observation and qualitative research protocol.

## 13. Related Documents

- [PRD](../00_product/PRD.md)
- [Feature Specifications](../01_features/FEATURE_TEMPLATE.md)
- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [Non-Functional Requirements](../02_architecture/NON_FUNCTIONAL_REQUIREMENTS.md)
- [Threat Model](./THREAT_MODEL.md)
- [ADR-0016 — Scoped Deterministic Mathematics Validators with Exact Arithmetic](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md)
- [ADR-0017 — Use OpenRouter for Bounded Generative AI with Scope, Quota, and Privacy Routing Controls](../02_architecture/adr/ADR-0017-openrouter-bounded-generation-and-usage-controls.md)
- [ADR-0019 — Use a Portable Single-Linux-VPS Deployment Baseline for MVP/Pilot, Initially on Tencent Cloud Lighthouse Jakarta](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md)

## 14. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.4` | `2026-09-08` | Add future Deployment Verification Suite concepts (health checks, reverse-proxy boundary, HTTPS, SSE, non-public database, secret absence, migration-blocking, immutable artifact identity, backup/restore, container recovery, log rotation, disk-pressure visibility, deployment smoke test) for the portable single-Linux-VPS MVP/pilot baseline (ADR-0019); no implementation exists yet | Claude |
| `0.3` | `2026-09-07` | Add AI Generation Gateway Suite and model/route evaluation-gate requirements for the OpenRouter bounded-generation baseline (ADR-0017) | Claude |
| `0.2` | `2026-09-07` | Expand Mathematics Assurance Suite with exact-arithmetic properties, Grade 5/7 corpora, resource-bound adversarial cases, and validator-version reproducibility (ADR-0016) | Claude |
| `0.1` | `2026-09-06` | Initial risk-based test strategy | Codex |
