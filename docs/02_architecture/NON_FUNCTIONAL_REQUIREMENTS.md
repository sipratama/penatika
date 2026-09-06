# Non-Functional Requirements — Penatika

> Defines cross-product quality requirements. Numerical targets remain open until prototype measurements and pilot environments provide a baseline.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft baseline |
| Version | `0.1` |
| Last Updated | `2026-09-06` |

## 1. Quality Priorities

1. Teacher control and classroom safety.
2. Mathematical and curriculum trustworthiness.
3. Session consistency and privacy between surfaces.
4. Teaching-flow responsiveness.
5. Graceful degradation and recoverability.
6. Accessibility and classroom readability.
7. Maintainability and provider independence.

## 2. Correctness and Content Assurance

- Supported structured content must pass schema validation before persistence or display.
- Supported Mathematics claims must use deterministic validation where available.
- Validation status must distinguish valid, invalid, unsupported, inconclusive, and error.
- Curriculum claims must identify authority level, controlled source, source version, relevant phase/scope, and provenance.
- Saved lesson provenance must remain historically stable when a newer curriculum source version is activated.
- Content edits must invalidate affected stale assurance results.
- AI self-reported confidence is not correctness evidence.

Release evidence must include a versioned corpus for Grade 5 fractions and Grade 7 basic algebra or linear equations.

## 3. Session Consistency

- The backend owns one authoritative session revision.
- State-changing commands must be authorized, revision-aware, bounded, and safe to retry where applicable.
- Duplicate or stale commands must not silently create conflicting state.
- Reconnection must converge on authoritative backend state.
- Role-specific projections must be derived consistently from the same accepted state.

## 4. Responsiveness

- Local navigation and drawing feedback should feel immediate enough not to interrupt teaching.
- Authoritative synchronization and live adaptation need separate latency budgets.
- AI generation must expose progress, cancellation, timeout, and non-blocking failure behavior.
- Numerical percentile targets must be set after instrumented prototypes run on target devices and networks.

Before pilot, define and measure:

- local input-to-feedback latency;
- command-to-authoritative-projection latency;
- reconnect convergence time;
- push-to-talk transcription and adaptation latency;
- lesson generation latency.

## 5. Availability and Graceful Degradation

- A recoverable AI failure must not remove current classroom content.
- The display should retain a safe last-known projection during temporary update failure.
- Teacher surfaces must identify offline, reconnecting, degraded dependency, and failed states.
- Retry ownership, timeout, fallback, and circuit-break behavior must be explicit per dependency.
- Minimum degraded-mode capability is an Open Product Decision and must be resolved before pilot.

No production availability percentage or SLO is set before deployment and support maturity are known.

## 6. Security

- All protected actions require backend-enforced authentication and authorization.
- Pairing credentials must be short-lived, purpose-bound, non-guessable, and replay-resistant.
- Structured rendering must prevent arbitrary script execution.
- Secrets, access tokens, and provider credentials must not ship in untrusted clients.
- Sensitive traffic requires transport protection in production.
- Expensive AI, speech, pairing, and session operations require abuse and resource controls.

See [THREAT_MODEL.md](../04_engineering/THREAT_MODEL.md).

## 7. Privacy

- Student identity and student-device data are not required for core MVP.
- Raw push-to-talk audio is not stored by default.
- Logs and analytics must avoid full prompts, raw provider payloads, lesson content, session secrets, and private teacher state unless a specific approved purpose requires otherwise.
- Retention, deletion, export, and evaluation-data policies must be decided before production use.
- Teacher-private information must never appear in classroom projections.

## 8. Accessibility and Classroom Usability

- Core teacher actions must support keyboard access where a keyboard is available.
- Focus, labels, error communication, target sizes, contrast, and reduced-motion behavior must meet the selected accessibility baseline.
- Information cannot depend on color alone.
- Classroom display content must be tested at realistic viewing distance and display resolution.
- Mathematics content requires accessible semantics where the selected rendering approach supports it.
- Mouse, touch, and stylus interactions require target-device testing.

The formal conformance target remains an Open Product/Architecture Decision, but accessibility is not optional.

## 9. Compatibility

Supported browsers, operating systems, devices, stylus behavior, screen resolutions, and network conditions are not yet selected. A compatibility matrix must be approved before pilot and verified using real target hardware where feasible.

The product must not depend on a proprietary smart board.

## 10. Scalability and Capacity

Initial classroom, teacher, lesson, and concurrent-session volumes are unknown. Architecture should scale vertically and through stateless application replication where the selected technology permits, but no distributed decomposition is justified yet.

Before production capacity planning, define:

- expected concurrent sessions and participants;
- command and annotation rates;
- AI and speech request frequency;
- lesson/session storage growth;
- curriculum corpus size;
- telemetry volume and cost.

## 11. Observability

- Correlate session, command, proposal, validation, and save operations without exposing secrets.
- Measure state divergence attempts, stale commands, pairing failures, reconnects, dependency latency/failure, assurance outcomes, and degraded-mode recovery.
- Use bounded metric cardinality.
- Provide health checks and dependency status appropriate to selected runtime.
- Define alerting and runbooks before production deployment.

## 12. Maintainability and Portability

- Product rules remain independent of UI, transport, database, and provider SDKs.
- AI, speech, curriculum, and validation integrations use explicit adapters.
- Structured models and cross-component contracts are versioned.
- Material architecture changes require ADRs.
- Database changes require migrations once persistence is selected.
- Source structure must follow documented module ownership.

## 13. AI Quality and Cost

- AI behavior requires versioned evaluation scenarios for initial lesson and adaptation tasks.
- Evaluation must include malformed output, prompt injection, incorrect Mathematics, curriculum mismatch, and provider failure.
- Provider usage must have time, concurrency, size, and cost bounds.
- Model/provider selection must compare quality, latency, privacy, reliability, and cost against product needs.

## 14. Release Quality Gates

Before a classroom pilot:

- no critical teacher-private/classroom projection leakage;
- no known session authorization bypass;
- structured content and command contracts pass compatibility tests;
- required Mathematics corpus passes selected assurance rules;
- raw audio retention tests pass;
- reconnect and degraded-state scenarios are exercised;
- target-device accessibility and input evidence exists;
- unresolved high risks have an owner and explicit disposition.

## 15. Open Decisions

- Numerical latency and reliability targets.
- Formal accessibility conformance target.
- Supported device/browser/network matrix.
- Production availability and recovery objectives.
- Data retention and deletion targets.
- Capacity and cost budgets.

## 16. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.1` | `2026-09-06` | Initial quality baseline grounded in MVP constraints | Codex |
