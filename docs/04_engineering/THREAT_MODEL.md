# Threat Model — Penatika

> Initial threat model for teacher identity, cloud classroom sessions, private/public projections, AI integrations, curriculum data, and saved teaching content. It does not claim compliance with a specific regulation.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft baseline |
| Version | `0.1` |
| Last Updated | `2026-09-06` |
| Review Trigger | Identity, provider, contract, deployment, retention, or student-data decisions |

## 1. Scope

In scope:

- preparation client;
- private teacher controller;
- classroom display;
- backend and persistence;
- pairing and realtime session communication;
- AI and speech providers;
- Mathematics validation;
- curriculum source and ingestion;
- telemetry and saved lessons/sessions.

Out of scope for this baseline: payment, student accounts, school administration, and physical classroom security.

## 2. Assets

- Teacher identity and authorization.
- Pairing credentials and session access tokens.
- Teacher-private prompts, commands, suggestions, validation details, and progress.
- Lesson content, saved sessions, and annotations.
- Authoritative session state and revision ordering.
- Raw push-to-talk audio and transcripts.
- AI/provider credentials and payloads.
- Curriculum source integrity and version metadata.
- Mathematics validation rules and results.
- Operational logs and analytics.

## 3. Threat Actors

- Unauthenticated external attacker.
- Unauthorized person in or near the classroom.
- Authorized user attempting access to another teacher's data or session.
- Malicious or compromised client.
- Compromised external provider or dependency.
- Malicious content embedded in lesson, curriculum, or AI output.
- Accidental teacher action causing privacy exposure or destructive change.

## 4. Trust Boundaries

1. Browser/client to backend.
2. Pairing credential to participant authorization.
3. Teacher-private projection to classroom-safe projection.
4. Backend to persistence and secret store.
5. Backend to AI and speech providers.
6. Curriculum ingestion/retrieval to controlled reference data.
7. AI proposal to authoritative lesson or session state.
8. Telemetry pipeline to operational users and storage.

## 5. Threat Register

| ID | Threat | Impact | Initial Mitigation |
|---|---|---|---|
| T-001 | Pairing code guessing or replay | Unauthorized classroom viewing or control | High-entropy, expiring, single-purpose, replay-resistant pairing; server authorization; rate limits |
| T-002 | Controller or display role escalation | Unauthorized state mutation or private-data access | Backend role enforcement; role-specific projections; contract tests |
| T-003 | Cross-teacher object access | Lesson/session disclosure or modification | Object-level authorization on every protected operation |
| T-004 | Private teacher state leaks to display | Classroom privacy and trust failure | Projection allow-list, separate schemas, regression tests, no shared raw payload |
| T-005 | Arbitrary AI-generated HTML/script | XSS, content manipulation, credential theft | Versioned structured model, allow-listed elements, safe encoding, CSP where applicable |
| T-006 | Prompt injection through lesson/curriculum content | Policy bypass, data disclosure, unsafe tools | Treat content as untrusted data, isolate instructions, tool allow-lists, scoped credentials, adversarial tests |
| T-007 | Mathematically incorrect AI content accepted | Student misinformation | Deterministic validation for supported scope, explicit unsupported state, teacher warning/control |
| T-008 | False, stale, or authority-confused curriculum grounding | Misaligned instruction | Layered authority, controlled source/version, provenance, integrity checks, stale-result invalidation, historical provenance preservation |
| T-009 | Raw audio retained or logged | Sensitive-data exposure | Ephemeral processing by default, log filters, retention tests, provider data-use review |
| T-010 | Session command replay, duplication, or reordering | Divergent or destructive classroom state | Authorized revision-aware commands, idempotency, ordering and stale detection |
| T-011 | Stale client overwrites current session | Loss of authoritative teaching state | Backend authority and explicit reconciliation |
| T-012 | AI/speech provider data leakage | Teacher or lesson data exposure | Data minimization, provider review, scoped requests, retention configuration, secrets management |
| T-013 | Resource exhaustion or provider cost abuse | Outage or unexpected spend | Authentication, quotas, rate limits, context bounds, timeouts, concurrency controls |
| T-014 | Secret exposure in client or logs | Backend/provider compromise | Server-side secrets, redaction, build scanning, least privilege |
| T-015 | Unauthorized curriculum modification | Systematic misinformation | Controlled ingestion, integrity/version checks, restricted write paths, audit evidence |
| T-016 | Destructive clear/end action | Loss of classroom work | Confirmation or recoverable undo, idempotent save, clear status |
| T-017 | Dependency outage causes unsafe fallback | Stale or unvalidated content displayed as current | Explicit degraded states, safe last-known projection, no silent acceptance |
| T-018 | Supply-chain compromise | Client/backend compromise | Locked dependencies, provenance, scanning, review, controlled builds after stack selection |

## 6. Security Invariants

- Classroom display cannot receive or derive teacher authority.
- Pairing is not equivalent to broad teacher-account authentication.
- AI and speech providers cannot authorize product actions.
- AI output cannot bypass structured schema, policy, assurance, or teacher-control gates.
- Raw audio is not persisted by default.
- Secrets and tokens never enter classroom projections or ordinary logs.
- Client visibility is not authorization.
- Curriculum provenance cannot be supplied solely by the AI provider.
- Official guidance or local context cannot be promoted to national normative authority by AI, retrieval ranking, or implementation convenience.

## 7. Privacy Baseline

- No student identity is required for core MVP.
- Minimize teacher personal data and lesson/session content sent to providers.
- Define purpose, retention, deletion, export, and access before production collection.
- Do not use raw classroom audio for model training or analytics without a new explicit product, legal, and consent decision.
- Evaluation fixtures should be synthetic or sanitized.
- Analytics must avoid content and high-cardinality personal identifiers where possible.

## 8. Authentication and Session Security

The exact identity solution is open. Any selected design must provide:

- protected teacher account/session behavior;
- secure recovery and revocation;
- object-level authorization;
- bounded classroom pairing;
- token expiration and rotation appropriate to client type;
- CSRF, CORS, browser storage, and session fixation controls appropriate to the chosen transport.

These details require threat-model revision after `OAD-004` and `OAD-005` are selected.

## 9. External Provider Review

Before selecting AI or speech providers, evaluate:

- data retention and training use;
- processing location and subprocessors where relevant;
- authentication and credential scope;
- rate and cost controls;
- timeout, availability, and fallback;
- model/version traceability;
- deletion and incident procedures;
- contract terms for classroom content and personal data.

## 10. Security Testing Priorities

- Pairing brute-force, replay, expiry, and role-escalation tests.
- Cross-teacher authorization tests.
- Private/public projection leak tests.
- Structured-rendering injection tests.
- Prompt-injection and malicious-content evaluations.
- Sensitive logging, telemetry, and build-artifact scans.
- Raw-audio non-retention tests.
- Command replay, concurrency, and stale-state tests.
- Provider failure and degraded-mode abuse tests.

## 11. Open Security and Privacy Decisions

- Identity/account model and authentication mechanisms.
- Pairing lifetime and participant revocation behavior.
- Data retention, deletion, export, and audit requirements.
- Whether any text transcript or provider payload may be retained for evaluation.
- AI/speech provider data-processing terms.
- Curriculum data integrity and publishing process.
- Applicable Indonesian legal or institutional obligations; no compliance claim is made yet.
- Production incident response and vulnerability-reporting process.

## 12. Review Gates

Review this threat model before:

- creating authentication or pairing contracts;
- selecting AI, speech, curriculum, analytics, or storage providers;
- storing transcripts, prompts, or student-related data;
- deploying to a real classroom or public environment;
- adding student accounts, school administration, billing, import, or external media.

## 13. Related Documents

- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [Non-Functional Requirements](../02_architecture/NON_FUNCTIONAL_REQUIREMENTS.md)
- [PRD](../00_product/PRD.md)
- [Test Strategy](./TEST_STRATEGY.md)
- [Security Standard](../standards/08_SECURITY_STANDARD.md)

## 14. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.1` | `2026-09-06` | Initial multi-surface and AI threat model | Codex |
