# Non-Functional Requirements — Penatika

> Defines cross-product quality requirements. Numerical targets remain open until prototype measurements and pilot environments provide a baseline.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft baseline |
| Version | `0.4` |
| Last Updated | `2026-09-07` |

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

Penatika MVP is resilience-oriented, not offline-first. Dependency failure degrades the affected capability while preserving one backend-authoritative session state.

- A recoverable AI failure must disable new semantic AI generation without removing current reviewed classroom content or healthy deterministic teaching controls.
- Speech recognition failure must disable push-to-talk while supported non-voice teacher interaction remains available.
- A disconnected teacher controller must not mutate state; the display retains the current authoritative projection, and another teacher surface may act only when separately authorized.
- A disconnected classroom display pauses student-facing mutation until it reconnects and synchronizes; safe teacher-private work may continue.
- Clients may retain a last-known safe classroom projection during backend connectivity loss, but cached state is not authoritative.
- Loss of backend authority must freeze new authoritative mutations, including `DIRECT_ACTION`, digital ink commits, AI publication, session lifecycle changes, and successful save completion.
- Newly created offline state-changing commands must not be queued for automatic replay in the MVP.
- A command sent before connection loss with uncertain acknowledgement may be reconciled using existing command identity, idempotency, and revision semantics.
- Recovery must retrieve and reconcile backend-authoritative state before mutation resumes.
- Save status must remain pending, failed, or retryable until durable authoritative persistence acknowledges success.
- Degraded mode must not bypass Q-02 approval, Mathematics assurance, curriculum provenance, authorization, privacy boundaries, structured-content validation, or stale/revision checks.
- Teacher surfaces must identify offline, reconnecting, degraded dependency, and failed states without exposing private diagnostics on the classroom display.
- Retry ownership, timeout, fallback, and circuit-break behavior must be explicit per dependency after implementation technologies are selected.

No production availability percentage or SLO is set before deployment and support maturity are known.

## 6. Security

- Teacher authentication must use OIDC Authorization Code flow with PKCE `S256` and required state, nonce, redirect, issuer, subject, audience/client, signature, and time validation.
- OAuth/OIDC access, refresh, and ID tokens must remain server-side and must not be exposed to or stored by ordinary browser application JavaScript.
- Protected teacher actions require a bounded, revocable backend-managed browser session represented by an opaque protected cookie.
- Cookie-authenticated state-changing requests require explicit CSRF protection; SameSite alone is not sufficient.
- All protected actions require backend-enforced object, ownership, classroom-session, participant, and revision authorization as applicable; authentication or a coarse role alone is insufficient.
- External identity linkage uses validated `(issuer, subject)` and must not automatically merge accounts by email.
- Penatika MVP must not store local teacher passwords or password-recovery credentials.
- Pairing grants must be five-minute bounded, session-bound, role/purpose-bound, single-use, non-guessable, revocable, and replay-resistant.
- Pairing alone must never authenticate a teacher, and a display participant must never obtain teacher authority.
- MVP permits at most one active mutation-authorized teacher controller and one active classroom display participant per classroom session.
- Structured rendering must prevent arbitrary script execution.
- Secrets, access tokens, and provider credentials must not ship in untrusted clients.
- Sensitive traffic requires transport protection in production.
- Expensive AI, speech, pairing, and session operations require abuse and resource controls.

See [THREAT_MODEL.md](../04_engineering/THREAT_MODEL.md).

## 7. Privacy

- Student identity and student-device data are not required for core MVP.
- Retention must be enforced by purpose and data class according to [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md).
- Raw push-to-talk audio has zero default persistence and must not appear in ordinary persistence, logs, analytics, or session history.
- Full raw transcription/command content, full prompts, raw provider payloads, and unaccepted proposal bodies are transient; any genuinely required diagnostic persistence must expire within the maximum `24-hour` default window.
- Saved session history and retained annotations must expire after `90 days` by default unless the teacher deletes them earlier.
- Accepted deletion requests must remove ordinary access when committed, complete primary purge within `30 days`, and expire backup remnants within `30 additional days` unless a documented narrow preservation requirement applies.
- Teacher account deletion must revoke normal access immediately and apply the same primary-purge and backup-expiry expectations to teacher-owned personal product data.
- Deletion state must be auditable without logging deleted sensitive content, and product UI must not claim completed deletion while data is only hidden or pending purge.
- Export of retained teacher-owned lesson/session data must verify authorization and exclude secrets, internal security data, and raw provider payloads that are not retained product data.
- Logs and analytics must avoid lesson/classroom content, session secrets, private teacher state, student profiling, and unnecessary personal or high-cardinality identifiers.
- Event-level pilot telemetry and identifiable research evidence must expire or be appropriately de-identified no later than `90 days` after final pilot-report acceptance.
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
- Measure state divergence attempts, stale commands, pairing failures, reconnects, dependency latency/failure, assurance outcomes, degradation entry/exit, mutation freezes, reconciliation outcomes, uncertain acknowledgements, rejected offline replay attempts, and save retry/recovery.
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

- OIDC login-flow integrity covers state/nonce handling, PKCE `S256`, exact redirect validation, and validated issuer/subject/client/signature/time semantics;
- browser storage and application responses contain no OAuth/OIDC access or refresh tokens;
- successful authentication rotates the backend session identifier and session fixation attempts fail;
- logout, session revocation, account disablement, and accepted account deletion terminate teacher access and active controller authority;
- cross-teacher lesson/session/controller authorization attempts fail;
- cookie-authenticated mutations reject missing or invalid CSRF proof and disallowed origins;
- expired, replayed, role-incompatible, and already-consumed pairing grants fail safely;
- controller replacement revokes prior mutation authority and display replacement revokes the prior participant credential;
- reconnect does not recreate revoked controller or display authority;
- no critical teacher-private/classroom projection leakage;
- no known session authorization bypass;
- structured content and command contracts pass compatibility tests;
- required Mathematics corpus passes selected assurance rules;
- raw audio retention tests pass;
- prohibited raw transcription, prompt, raw-provider, and unaccepted-proposal persistence tests pass, including the `24-hour` maximum diagnostic expiry where temporary retention is enabled;
- saved-session and retained-annotation `90-day` expiry is implemented and evidenced;
- teacher lesson, saved-session, and account deletion paths remove ordinary access and evidence primary purge within `30 days`;
- protected backup remnants expire within `30 additional days`, and the backup restoration procedure prevents expired or deleted records from being reactivated;
- authorized teacher export succeeds for retained teacher-owned data and unauthorized export is rejected;
- deletion state remains auditable without storing deleted sensitive content or falsely reporting completed deletion;
- event-level pilot telemetry and identifiable research evidence have an operational expiry or de-identification procedure;
- reconnect and degraded-state scenarios are exercised;
- backend-authority loss preserves a safe projection and freezes authoritative mutations;
- AI outage isolates semantic generation failure from healthy classroom capabilities;
- speech outage preserves supported non-voice interaction;
- display disconnect pauses mutation and reconnect requires synchronization;
- controller disconnect preserves the current projection without automatic mutation or authority promotion;
- save dependency failure never produces false success and supports pending/failed/retry evidence;
- uncertain acknowledgement of a pre-disconnect command is reconciled without accepting new offline commands;
- target-device accessibility and input evidence exists;
- unresolved high risks have an owner and explicit disposition.

## 15. Open Decisions

- Numerical latency and reliability targets.
- Formal accessibility conformance target.
- Supported device/browser/network matrix.
- Production availability and recovery objectives.
- Physical retention enforcement, backup-expiry mechanism, and export implementation/format.
- Capacity and cost budgets.

## 16. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.4` | `2026-09-07` | Add OIDC, backend-session, CSRF, object-authorization, and scoped-pairing security evidence requirements | Codex |
| `0.3` | `2026-09-06` | Add enforceable privacy and release gates for the approved data-lifecycle policy | Codex |
| `0.2` | `2026-09-06` | Define resilience-oriented degradation, reconciliation, and truthful save requirements | Codex |
| `0.1` | `2026-09-06` | Initial quality baseline grounded in MVP constraints | Codex |
