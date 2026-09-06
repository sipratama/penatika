# Penatika MVP Pilot Plan

> Canonical detailed source for the first Penatika pilot design, execution gates, metrics, and evidence interpretation.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft / Approved Baseline |
| Version | `0.3` |
| Date | `2026-09-06` |
| Owner | Open — named product/requirement approver required before real-classroom Stage B |

## 1. Purpose

The first Penatika pilot validates the core teacher-to-classroom product hypothesis in a deliberately narrow scope. It evaluates whether the complete workflow can support teaching while preserving teacher control, structured content integrity, Mathematics assurance, curriculum provenance, privacy boundaries, and safe recovery.

The core principle is:

> Validate depth of the teacher workflow before expanding breadth of content.

The pilot produces directional product evidence. It is not designed to demonstrate broad curriculum coverage, statistical market efficacy, or student learning outcomes.

## 2. Validation Questions

The pilot must answer:

1. Can a teacher complete the full Penatika workflow without the product becoming a distraction?
2. Does the private-controller and classroom-display model preserve teacher control better than operating a generic AI chat during teaching?
3. Can teachers use live AI adaptation during an actual or realistically simulated teaching flow?
4. Can structured classroom content, Q-02 approval policy, Mathematics assurance, and curriculum provenance work together without unacceptable friction?
5. Can the classroom session recover safely from realistic AI, speech, display, controller, network, and save failures?
6. Do teachers perceive enough value to want to use Penatika again for the supported teaching context?

## 3. Pilot Scope

The pilot evaluates:

- `CAP-LESSON-001` — prepare and review an AI-assisted structured lesson;
- `CAP-SESSION-001` — start and pair a classroom session;
- `CAP-CANVAS-001` — present and navigate structured classroom content;
- `CAP-INK-001` — annotate using supported digital ink;
- `CAP-ADAPT-001` — request and decide on live AI adaptation proposals;
- `CAP-MATH-001` — apply Mathematics assurance and curriculum provenance;
- `CAP-SESSION-002` — end and save the session.

The pilot uses staged teacher rehearsal followed by limited classroom validation. Controlled failure sessions may intentionally degrade or terminate selected capabilities according to ADR-0007.

## 4. Explicit Non-Scope

The first pilot does not include:

- Mathematics topics or grades beyond the two defined pilot areas;
- subjects other than Mathematics;
- student accounts, student devices, or student profiling;
- attendance or school-administration workflows;
- advanced graphing or 3D visualization;
- lesson import, including direct Pendago content or contract migration;
- commercial, billing, or payer workflows;
- autonomous teaching;
- learning-outcome efficacy claims;
- statistically representative nationwide teacher-demand or market-validation claims.

A request for another topic is recorded as expansion evidence rather than silently added to pilot scope.

## 5. Participants

- Target `6–8` Mathematics teachers.
- Include at least `3` teachers representing a Grade 5 teaching context.
- Include at least `3` teachers representing a Grade 7 teaching context.
- Stage B uses the same teachers or a subset of the Stage A cohort.
- Record relevant teaching context without collecting unnecessary personal identifiers.

Across Stage A and Stage B, target at least `12` evaluated teacher sessions. This is an evidence target, not a statistical sample-size claim.

## 6. Stage A — Teacher Rehearsal

### Purpose

Validate workflow, usability, content behavior, instrumentation, and failure handling before real-classroom exposure.

### Structure

- Each participating teacher completes at least one moderated rehearsal in one supported topic area.
- Stage A may use teacher-only or researcher-facilitated classroom simulation.
- Real students are not required.
- The cohort must represent both Grade 5 Fractions and Grade 7 Basic Algebra / Linear Equations.
- Across the Stage A cohort, exercise at least one controlled degraded-mode scenario covering a relevant AI, speech, display, controller, backend/network, or save failure.

### Required Rehearsal Coverage

- prepare and review lesson;
- start classroom session;
- pair teacher controller and classroom display;
- present and navigate content;
- use digital ink;
- perform at least one live AI adaptation;
- preview and approve, reject, regenerate, modify, or explicitly warn-override the proposal where applicable;
- end and save;
- observe instrumentation and failure behavior.

## 7. Stage B — Limited Classroom Pilot

Stage B begins only after Stage A satisfies every hard safety/trust gate and all applicable entry dependencies.

### Target

- Use the same or a subset of the `6–8` participating teachers.
- Each participating teacher completes at least one realistic or real classroom session.
- Retain representation of both supported topic areas.
- Target at least `6` evaluated classroom sessions.
- Maintain at least `12` evaluated teacher sessions across Stage A and Stage B combined.

If real students are involved, Stage B must not begin until the approved [DATA_RETENTION_POLICY.md](./DATA_RETENTION_POLICY.md) is implemented, tested, reflected in participant/privacy notices where applicable, and supported operationally; a named product/requirement approver exists; privacy and consent requirements are reviewed; deployment/support readiness is accepted; target environment evidence is reviewed; and high-impact risks have explicit disposition.

## 8. Required Session Workflow

Every normal-path evaluated session should exercise:

1. prepare and review a lesson;
2. start a classroom session;
3. pair the teacher controller and classroom display;
4. present and navigate structured content;
5. perform at least one digital-ink interaction;
6. submit at least one semantic AI adaptation request;
7. make an explicit teacher decision on the resulting proposal;
8. publish to the classroom only when the teacher chooses and policy permits;
9. end and save the session.

The core workflow-completion metric treats facilitator rescue for a core Penatika action as non-completion. A deliberately injected failure session is not counted as a normal-path failure.

## 9. Mathematics Content Scope

### Grade 5 — Fractions

Representative activities may include:

- comparing fractions;
- equivalent fractions;
- simple fraction operations within the selected curriculum scope;
- explanation or example adaptation;
- progressively harder related problems;
- structured visual representations supported by the MVP scene model.

### Grade 7 — Basic Algebra / Linear Equations

Representative activities may include:

- algebraic expressions;
- simple unknown-value reasoning;
- basic linear equations;
- step-by-step explanation;
- similar-problem generation;
- difficulty adaptation.

The first pilot does not expand content scope merely to demonstrate breadth.

## 10. Hard Safety / Trust Gates

Every gate must pass. Failure of any gate blocks broader pilot expansion until the root cause is corrected and the affected gate is revalidated.

### HARD-01 — Private Projection Integrity

Target: `0` teacher-private AI state, credentials, private warnings, prompts, or internal diagnostics exposed on the classroom display.

### HARD-02 — Authorization Integrity

Target: `0` unauthorized classroom-control or session-join successes.

### HARD-03 — AI Publication Integrity

- Target: `100%` of student-facing AI-generated semantic publications have required post-generation teacher approval or a valid warned override.
- Target: `0` `BLOCKED` proposals published.

### HARD-04 — Mathematics / Content Safety

- Known deterministic Mathematics `INVALID` results never become student-facing authoritative content.
- Required schema, structured-content, and curriculum-provenance failures remain blocked under existing policy.

### HARD-05 — Session Authority Integrity

- Target: `0` cases where a disconnected client becomes authoritative.
- Target: `0` newly created offline state-changing commands automatically replayed after reconnect.

### HARD-06 — Recovery Integrity

Every required controlled reconnect or degradation scenario restores or preserves a safe state without authoritative divergence.

### HARD-07 — Save Truthfulness

Target: `100%` of user-visible `SAVED` states correspond to durable authoritative save acknowledgement.

### HARD-08 — Audio Privacy

Target: `0` raw push-to-talk audio retained by Penatika by default.

## 11. Primary Product Metrics

### PRIMARY-01 — Core Teaching Workflow Completion Rate

Definition: percentage of normal-path evaluated sessions completing prepare/review → start → pair → present → navigate → annotate → live adaptation → teacher publication decision → end → save without facilitator rescue for a core Penatika action.

Initial success threshold: `>= 80%`.

Deliberately injected failure sessions are excluded from the normal-path denominator.

### PRIMARY-02 — Teacher Control Confidence

Prompt: “I remained in control of what students would see while using Penatika.”

- Scale: `1–5` Likert.
- Initial success threshold: median `>= 4/5`.
- Retain the full rating distribution.

### PRIMARY-03 — Teaching-Flow Fit

Prompt: “Using Penatika allowed me to adapt the lesson without significantly breaking my teaching flow.”

- Scale: `1–5`.
- Initial success threshold: median `>= 4/5`.

### PRIMARY-04 — Reuse Intent

Prompt: “If Penatika supported this topic reliably, would you choose to use it again for a similar lesson?”

Responses: definitely yes, probably yes, unsure, probably no, definitely no.

Initial success threshold: `>= 70%` of participating teachers answer definitely yes or probably yes.

### PRIMARY-05 — Live Adaptation Usefulness

At least one live AI adaptation must be attempted in each normal-path session. Record whether the teacher accepted, modified then accepted, rejected, regenerated, or abandoned the proposal.

Initial evidence threshold: `>= 70%` of participating teachers report that at least one live adaptation was useful enough to support their teaching.

Raw proposal acceptance rate is not equivalent to quality. Rejection may demonstrate healthy teacher control.

## 12. Diagnostic Metrics

Collect the following for understanding and future target-setting without creating new pass/fail thresholds:

- AI proposal acceptance rate;
- AI proposal modification rate;
- AI rejection rate;
- warned-override rate;
- `BLOCKED` proposal rate and reason;
- semantic adaptation latency p50/p95;
- transcription latency p50/p95;
- command-to-authoritative-projection latency;
- reconnect convergence time;
- degradation events per session;
- save retry/failure rate;
- controller disconnect frequency;
- display disconnect frequency;
- speech fallback usage;
- teacher cancellation/retry frequency;
- facilitator intervention count;
- number and duration of obvious teaching-flow interruptions;
- Mathematics assurance outcome distribution;
- curriculum-provenance failure count.

Numerical diagnostic latency thresholds remain evidence-driven and open until pilot measurement provides a baseline.

## 13. Instrumentation and Privacy

Pilot telemetry must be privacy-minimized and purpose-bound.

Do not log or retain for ordinary pilot instrumentation:

- raw audio;
- secrets or credentials;
- full provider payloads;
- unnecessary student information;
- unnecessary classroom content.

Instrumentation must distinguish hard-gate evidence, primary product metrics, and diagnostic metrics. It must preserve sufficient correlation to reconstruct session workflow, proposal decisions, degradation/recovery, and save outcomes without requiring student profiling.

Event-level operational/security logs default to `30 days`. Privacy-minimized event-level pilot telemetry and identifiable or pseudonymous research evidence may be retained through pilot analysis and up to `90 days` after final pilot-report acceptance, then must be deleted or appropriately de-identified when no longer required. Participant/contact mapping should be separated from product telemetry where practical.

The approved [DATA_RETENTION_POLICY.md](./DATA_RETENTION_POLICY.md) must be implemented and evidenced before collecting real-student data in Stage B. Retention does not authorize broader collection.

## 14. Stage Entry Gates

### Stage A Entry

Stage A may begin when:

- a prototype or vertical slice supports the required workflow;
- release blockers relevant to the rehearsal are absent;
- pilot instrumentation is available;
- the initial Mathematics evaluation corpus exists;
- safe mock or sanitized data can be used.

### Stage B Real-Classroom Entry

Stage B with a real classroom must not begin until:

- Stage A hard safety/trust gates pass;
- applicable privacy review is complete;
- the approved `DATA_RETENTION_POLICY` is implemented, tested, reflected in participant/privacy notices where applicable, and supported operationally;
- retention expiry, teacher deletion, account deletion, authorized export, and backup-expiry procedures have evidence appropriate to the pilot;
- a named product/requirement approver exists;
- the deployment and support plan is accepted;
- the target device/browser/network baseline is reviewed;
- high-impact pilot risks have explicit disposition.

These dependencies remain open until separately resolved and evidenced.

## 15. Pilot Decision Rule

After the pilot, classify the outcome as `PROCEED`, `ITERATE`, or `BLOCKED`.

### `PROCEED`

Eligible for broader pilot or topic-expansion consideration only when:

- every hard safety/trust gate passes;
- Core Teaching Workflow Completion is `>= 80%`;
- Teacher Control Confidence median is `>= 4/5`;
- Teaching-Flow Fit median is `>= 4/5`;
- Reuse Intent is `>= 70%`;
- Live Adaptation Usefulness is `>= 70%`.

`PROCEED` does not automatically authorize additional subjects, student devices, commercial launch, or nationwide scaling. It creates evidence to consider the next scope.

### `ITERATE`

Use `ITERATE` when every hard gate passes but one or more primary product thresholds are missed. Improve and retest the failing workflow or usability hypothesis within the same two-topic scope rather than automatically expanding features or content.

### `BLOCKED`

Use `BLOCKED` when any hard safety/trust gate fails. Broader pilot expansion stops until the root cause is understood, remediation is complete, and the affected gate is revalidated.

## 16. Evidence Interpretation Limits

- The pilot provides directional product evidence only.
- The participant and session targets are not statistical sample-size claims.
- Results must not be represented as statistically valid learning-outcome evidence.
- Results must not be represented as proof of nationwide teacher demand, market representativeness, or educational efficacy.
- Student learning-outcome claims are outside the first pilot.
- Findings must retain participant, grade/topic, environment, device, network, protocol, and limitation context.
- Expansion requires evidence and a separate scoped decision; it is not automatic after a passing result.

## 17. Risks and Dependencies

Relevant active risks include:

- `R-001` teacher-controller distraction;
- `R-002` classroom connectivity;
- `R-003` AI latency;
- `R-004` incorrect Mathematics;
- `R-006` private projection leakage;
- `R-008` multi-device divergence;
- `R-012` missing product owner;
- `R-014` device/browser/stylus variation;
- `R-017` retention/deletion implementation or operational failure;
- `R-018` pilot evidence overgeneralization.

See [RISKS.md](./RISKS.md) for ownership and current responses.

Real-classroom Stage B remains dependent on privacy and consent review, implementation and operational evidence for [DATA_RETENTION_POLICY.md](./DATA_RETENTION_POLICY.md), product ownership, deployment/support, target-environment, and risk-readiness decisions. Q-04 does not resolve those dependencies.

## 18. Pilot Report Template / Required Evidence

Each pilot report must record at minimum:

- participant context without unnecessary personal identifiers;
- grade and topic;
- environment, device, display, input, and network context;
- stage and session type, including normal-path or controlled failure;
- required workflow completion and any incomplete step;
- AI adaptation decisions: accepted, modified then accepted, rejected, regenerated, abandoned, warned override, or blocked;
- hard-gate outcomes and supporting evidence;
- primary metric results and denominator definitions;
- diagnostic metrics;
- observed teaching-flow interruptions and duration where available;
- degraded-mode events and recovery outcomes;
- teacher ratings and qualitative feedback;
- facilitator intervention and whether it constituted rescue for a core action;
- save status and durable acknowledgement evidence;
- evidence that raw audio is not retained in ordinary persistence, logs, analytics, or session history;
- saved-session expiry and early teacher-deletion evidence;
- teacher lesson/session/account deletion path and primary-purge state evidence;
- authorized export procedure evidence, including rejection of unauthorized export;
- event-level pilot telemetry expiry or de-identification evidence;
- participant/contact mapping separation evidence where applicable;
- backup-expiry procedure evidence without exposing deleted sensitive content;
- known limitations;
- deviations from the pilot protocol.

The aggregate pilot report must also include participant/session counts, topic representation, rating distributions, threshold calculations, hard-gate summary, diagnostic distributions, unresolved risks, and the final `PROCEED`, `ITERATE`, or `BLOCKED` classification.

## 19. Open Follow-Ups

- `Q-06` / `OPD-007`: business model and commercial path.
- `Q-07` / `OPD-006`: named product owner and requirement approval authority.
- Implementation, testing, operational procedures, and Stage B evidence for the approved `DATA_RETENTION_POLICY`.
- Applicable privacy and consent requirements for real-classroom use.
- Deployment and support readiness for Stage B.
- Target device, browser, display, stylus, and network baseline.
- Evidence-driven numerical latency and reliability targets.
- AI, speech, validation, identity, persistence, realtime, and deployment technology decisions.

## 20. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.3` | `2026-09-06` | Resolve `OPD-005` policy and replace the open decision with implementation, operational, and evidence gates | Codex |
| `0.2` | `2026-09-06` | Remove resolved Q-05 legacy migration follow-up without changing pilot scope or metrics | Codex |
| `0.1` | `2026-09-06` | Establish narrow staged MVP pilot scope, gates, metrics, and evidence rules | Codex |
