# Threat Model — Penatika

> Initial threat model for teacher identity, cloud classroom sessions, private/public projections, AI integrations, curriculum data, and saved teaching content. It does not claim compliance with a specific regulation.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft baseline |
| Version | `0.8` |
| Last Updated | `2026-09-10` |
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
| T-019 | OIDC login CSRF, state manipulation, or nonce confusion | Attacker binds or injects the wrong external authentication result | Authorization Code + PKCE `S256`; validated `state`, nonce, issuer, subject, audience/client, signature, redirect URI, and time constraints |
| T-020 | OAuth redirect or authorization-code interception | External identity or token compromise | Exact redirect validation, PKCE `S256`, secure token endpoint, bounded transaction state, no implicit/password grants |
| T-021 | OAuth token theft from browser storage or application responses | Reusable upstream bearer credential compromise | Backend is confidential OIDC client; OAuth/OIDC access, refresh, and ID tokens remain server-side and are never exposed to ordinary browser JavaScript |
| T-022 | Browser session fixation or hijacking | Unauthorized teacher-account access | Rotate identifier after authentication; opaque `Secure`/`HttpOnly` narrowly scoped cookie; bounded lifetime; server revocation; transport protection |
| T-023 | CSRF against cookie-authenticated operations | Unauthorized state mutation under a teacher session | Explicit anti-forgery defense, no state changes through `GET`, strict origin/CORS validation, SameSite as defense in depth |
| T-024 | Automatic email-based account linking | Cross-provider account takeover | Resolve by validated `(issuer, subject)`; no automatic email merge; explicit authenticated verification required for future linking |
| T-025 | Controller pairing credential used without teacher authentication | Unauthenticated attacker gains teacher mutation authority | Controller binding requires active authenticated `TeacherAccount`, session ownership/authorization, valid role-bound grant, and participant constraints |
| T-026 | Pairing or participant credential replay | Re-entry after grant consumption, replacement, or revocation | Five-minute single-use pairing grants; revocable participant sessions; replay rejection; attempt/rate limits |
| T-027 | Stale or replaced controller continues sending commands | Concurrent or unauthorized classroom mutation | At most one active mutation-authorized controller; explicit replacement revokes prior authority; authorization and revision checks on every command |
| T-028 | Classroom display attempts teacher-role escalation | Private-data access or classroom control | Separate display participant session; classroom-safe projection only; backend role/object enforcement; no teacher-account privilege |
| T-029 | Curriculum source-artifact tampering during acquisition/ingestion | Corrupted or falsified content presented as official curriculum | SHA-256 source/corpus integrity digest; controlled source registry; acquisition provenance |
| T-030 | Malicious or corrupted imported curriculum content | Unsafe or incorrect grounding presented to teachers/students | Schema/integrity validation before human review; human review before activation |
| T-031 | Curriculum normalization error (wrong phase/topic mapping, meaning-changing typo) | Misaligned or incorrect curriculum grounding | Human review boundary; new immutable corpus version required for correction; no in-place mutation |
| T-032 | Curriculum authority-level escalation (guidance/local context promoted to normative) | False national-authority claim | Structurally distinct `NORMATIVE`/`OFFICIAL_GUIDANCE`/`LOCAL_CONTEXT` levels; retrieval rank/similarity/AI confidence cannot promote authority |
| T-033 | Unauthorized activation of a national curriculum source/corpus version | Unreviewed content becomes runtime authority | Explicit accountable human review and activation step; no crawler/AI self-activation |
| T-034 | Stale or superseded corpus accidentally used for new generation | Outdated or incorrect curriculum grounding | Explicit activation state per corpus version; supersession requires deliberate review-and-activate, not automatic replacement |
| T-035 | Teacher local-context cross-account access | Unauthorized read/use of another teacher's local curriculum context | `LocalCurriculumContextVersion` ownership/authorization by `TeacherAccount`; object-level authorization |
| T-036 | Local context presented as normative | Teacher/classroom misled about national curriculum truth | Explicit `LOCAL_CONTEXT` classification; conflict/warning state instead of silent merge with normative claims |
| T-037 | Provenance stripping from grounded curriculum claims | Ungrounded claim presented as grounded | Retrieval provenance required to reconstruct source/corpus/entry/match method; explicit `NO_MATCH`/`UNGROUNDED` state when absent |
| T-038 | AI/provider substitutes model knowledge for controlled curriculum source | Fabricated curriculum grounding | Curriculum retrieval reads only activated PostgreSQL corpus data; AI provider adapters do not read curriculum tables directly; AI never self-authorizes curriculum truth |
| T-039 | Crafted mathematical expression causes parser/CPU/memory exhaustion | Denial of service via adversarial deterministic input | Allow-listed restricted grammar; explicit resource limits on expression length, nesting depth, literal size, and operation count; safe failure |
| T-040 | Parser accepts executable/general scripting syntax | Remote code execution or arbitrary computation via a "Mathematics expression" | Data-only parser; no Java evaluation, script engine, reflection, filesystem, or network access; strict allow-listed grammar |
| T-041 | AI-generated unsupported/nonlinear expression bypasses the validator | Fabricated or unchecked Mathematics correctness claim | Validator routing restricted to `EXACT_RATIONAL`/`AFFINE_EXPRESSION`/`LINEAR_EQUATION`; anything outside scope returns `UNSUPPORTED`, never `VALID` |
| T-042 | Floating-point rounding produces false correctness/incorrectness | Incorrect Mathematics assurance result | Exact rational arithmetic (`BigFraction`); floating-point equality is never the correctness authority |
| T-043 | Stale Mathematics validation result reused after content edit | Outdated result presented as current assurance | `FR-MATH-007` content-edit invalidation; result is bound to exact content/claim version |
| T-044 | AI self-evaluation substitutes for deterministic Mathematics validation | Unverified content presented as validated | ADR-0004/ADR-0006 binding: AI cannot convert `INVALID`/`UNSUPPORTED` to `VALID`; a post-`INVALID` AI correction is a new proposal requiring revalidation |
| T-045 | PostgreSQL port exposed publicly on the pilot VPS | Direct database compromise bypassing application authorization | ADR-0019: PostgreSQL binds only to the private Docker/internal host boundary; port never published publicly |
| T-046 | Leaked VPS SSH credential or root account misuse | Full host compromise, data exfiltration, service disruption | SSH key authentication only, no password login, root password login disabled, restricted administrative access, firewall, least privilege |
| T-047 | Leaked CI/CD deployment credential | Unauthorized deployment or host access from a compromised pipeline | Dedicated restricted deployment identity (not personal/root SSH key), protected GitHub Environment secrets, no provider API credentials committed to the repository |
| T-048 | Secrets committed to Git or baked into a container image/frontend bundle | Credential compromise, unauthorized provider/database access | Server-side-only secrets, restrictive filesystem permissions, Compose file-mounted/root-owned secret files, build-artifact scanning, no secrets in browser bundles |
| T-049 | Backup credential leakage or backup stored only on the primary VPS | Loss of the only recoverable copy of pilot data on host compromise/failure | Off-host, provider-replaceable backup destination; backup-destination credential treated as a server-side secret; primary-server failure must not destroy the only usable data copy |
| T-050 | Unencrypted off-host backup | Sensitive pilot data exposure if the backup destination is compromised | Backups encrypted before or during off-host storage per the ADR-0019 backup model |
| T-051 | Cross-environment data leakage (LOCAL/PILOT/PROD) | Production-like or pilot data exposed in a lower-trust environment | No cross-environment sharing of database, secrets, or sessions; production-like data not copied into lower environments without an approved protected process |
| T-052 | Outdated OS/container packages on the pilot VPS | Known-vulnerability exploitation | Automatic/security OS patch discipline, minimal installed services, timely security updates |
| T-053 | Unrestricted Docker socket access | Container escape / host takeover | Least-privilege host access; direct SSH into application containers is not a normal operational workflow |
| T-054 | Disk exhaustion on the pilot VPS (logs, backups, images) | Service outage, failed writes, masked failures | Log rotation, disk-space visibility, backup lifecycle management |
| T-055 | Sensitive data in logs on the pilot host | Credential/PII exposure via log access | Structured logs excluding credentials, tokens, raw audio, confidential tokens, database passwords, and unrestricted prompts/transcripts by default |
| T-056 | Real classroom pilot traffic served over plain HTTP | Credential/session interception, tampering | HTTPS required before real pilot activation; valid domain/hostname required; TCP 80 used only for redirect/certificate bootstrap |
| T-057 | Compromised or tampered deployment artifact (image/registry) | Malicious code reaching the pilot host | Immutable/versioned release identity (commit SHA or image digest), OCI-compatible registry, no mutable `latest` as authoritative release identity |
| T-058 | Provider outage or single-host loss | Full application unavailability; potential data loss without off-host backup | Explicit single-failure-domain acknowledgement (ADR-0019); off-host backup boundary; provider portability so the deployment can move to another compatible Linux VPS provider |

## 6. Security Invariants

- Classroom display cannot receive or derive teacher authority.
- Pairing is not equivalent to broad teacher-account authentication.
- Controller authority requires both an active authenticated `TeacherAccount` and an active session-scoped controller participant binding.
- External identity is resolved by validated `(issuer, subject)` and is not automatically linked by email.
- OAuth/OIDC access, refresh, and ID tokens remain server-side and are not browser application credentials.
- Browser and participant sessions are bounded and revocable.
- AI and speech providers cannot authorize product actions.
- AI output cannot bypass structured schema, policy, assurance, or teacher-control gates.
- Raw audio is not persisted by default.
- Secrets and tokens never enter classroom projections or ordinary logs.
- Client visibility is not authorization.
- Curriculum provenance cannot be supplied solely by the AI provider.
- Official guidance or local context cannot be promoted to national normative authority by AI, retrieval ranking, or implementation convenience.
- Curriculum content is not active runtime authority merely because it was downloaded or normalized; activation requires explicit accountable human review.
- An activated curriculum corpus version is immutable; corrections and official-source supersession both require a new version and explicit re-activation, never in-place mutation.
- Deterministic Mathematics validation uses exact rational arithmetic and a data-only, resource-bounded parser; it never uses floating-point equality as correctness authority and never executes arbitrary code.
- AI cannot convert a deterministic `INVALID`, `UNSUPPORTED`, or `INCONCLUSIVE` Mathematics result into `VALID`; a correction proposed after `INVALID` must be revalidated as a new proposal.
- OpenRouter and any generative-model provider cannot become authorization, quota, curriculum, Mathematics, or publication authority.
- A generation request must pass the deterministic capability/scope guard and hard resource guard before any expensive `FAST`/`QUALITY` provider call.
- Generative traffic uses only an explicitly approved, privacy-compliant (`zdr=true`, `data_collection=deny`) provider route; unapproved automatic fallback is disabled.
- PostgreSQL and the backend port are never exposed as the normal public application entry point; the reverse proxy is the sole public HTTPS boundary (ADR-0019).
- Runtime secrets remain server-side only and are never committed to Git, baked into container images, or shipped in frontend artifacts.

## 7. Privacy Baseline

- No student identity is required for core MVP.
- Minimize teacher personal data and lesson/session content sent to providers.
- Implement, test, and operationally evidence [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md) before real-world collection where required; legal/privacy review still applies.
- Do not use raw classroom audio for model training or analytics without a new explicit product, legal, and consent decision.
- Evaluation fixtures should be synthetic or sanitized.
- Analytics must avoid content and high-cardinality personal identifiers where possible.

## 8. Authentication and Session Security

Teacher authentication uses OpenID Connect 1.0 with OAuth 2.0 Authorization
Code flow and PKCE `S256`. The Penatika Backend is the confidential OIDC client
and relying party. It validates state, nonce, exact redirect URI, issuer,
subject, audience/client, signature, expiry, and applicable time constraints
before resolving a local `TeacherAccount` by `(issuer, subject)`.

OAuth/OIDC access, refresh, and ID tokens remain server-side and are not
exposed to ordinary application JavaScript or browser storage. Penatika stores
no local teacher passwords for MVP and does not automatically link accounts by
email.

Teacher Web uses an opaque revocable backend session cookie. The authenticated
cookie is `Secure`, `HttpOnly`, narrowly scoped, and host-only where practical;
`SameSite=Strict` is preferred when compatible with the selected deployment.
The session identifier rotates after authentication and sessions have bounded
idle and absolute lifetimes. Local logout and server revocation terminate local
authority even when upstream logout is unavailable.

Cookie-authenticated mutations require explicit CSRF protection. SameSite is
defense in depth rather than the whole strategy. Credentialed cross-origin
deployment requires explicit trusted-origin allowlists and never wildcard
CORS.

Controller authority requires an authenticated active `TeacherAccount`,
object/session authorization, and an active `TEACHER_CONTROLLER` participant.
Pairing alone cannot authenticate a teacher. Classroom Display uses a separate
`CLASSROOM_DISPLAY` participant session with no teacher privilege. Pairing
grants are session/role-bound, single-use, revocable, and expire after five
minutes. MVP permits one active controller and one active display per classroom
session; explicit replacement revokes prior participant authority.

Realtime credential carriage and reconnect protocol are resolved by
[ADR-0012](../02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md)
(SSE push reusing this identity model; no new realtime-specific credential).
[ADR-0014](../02_architecture/adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)
selects PostgreSQL as the initial authoritative persistence for revocable
browser/session security state. The first-slice Teacher-session lifetime and
security-sensitive credential baseline is frozen in the
[First Protected Vertical Slice Implementation Plan](./FIRST_VERTICAL_SLICE_IMPLEMENTATION_PLAN.md).
The concrete OIDC provider and exact cookie/path/header and CSRF implementation
details remain implementation or deployment decisions.

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
- OIDC state, nonce, redirect, PKCE, issuer, subject, client/audience, signature, and time-validation tests.
- Browser OAuth-token non-exposure and session-fixation tests.
- CSRF and credentialed-origin enforcement tests.
- Logout, revocation, account-disablement, and account-deletion authority tests.
- Controller/display replacement and stale-participant rejection tests.
- Cross-teacher authorization tests.
- Private/public projection leak tests.
- Structured-rendering injection tests.
- Prompt-injection and malicious-content evaluations.
- Sensitive logging, telemetry, and build-artifact scans.
- Raw-audio non-retention tests.
- Command replay, concurrency, and stale-state tests.
- Provider failure and degraded-mode abuse tests.

## 11. Open Security and Privacy Decisions

- Concrete OIDC provider and its privacy, operational, logout, and revocation capabilities.
- Independent participant-session expiry beyond revocation/session lifecycle (OIQ-03).
- Exact cookie name/path, transient OIDC transaction mechanism, and CSRF implementation/header names.
- Future account-linking or identity-recovery UX if introduced.
- Technical retention enforcement and physical purge evidence.
- Backup expiry evidence.
- Export authorization mechanism.
- Audit/event evidence requirements without storing deleted sensitive content.
- Applicable legal obligations.
- Provider-side processing/retention configuration and technical enforcement for transient transcription, prompt, and provider payload data within the approved policy.
- AI/speech provider data-processing terms.
- Implementation evidence for curriculum activation review, source-artifact integrity verification, and exact operational authorization mechanism ([ADR-0015](../02_architecture/adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md) resolves the architecture; implementation/operational evidence remains open).
- Applicable licensing/legal review outcome for Official Guidance substantial-content usage.
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
- [Data Retention, History, Export, and Deletion Policy](../06_delivery/DATA_RETENTION_POLICY.md)
- [Product Governance and Decision Authority](../00_product/PRODUCT_GOVERNANCE.md)
- [Test Strategy](./TEST_STRATEGY.md)
- [Security Standard](../standards/08_SECURITY_STANDARD.md)
- [ADR-0011 — OIDC with Backend-Managed Browser Sessions and Scoped Pairing](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0015 — Versioned Controlled Curriculum Corpus with Deterministic Retrieval](../02_architecture/adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
- [ADR-0016 — Scoped Deterministic Mathematics Validators with Exact Arithmetic](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md)
- [ADR-0017 — Use OpenRouter for Bounded Generative AI with Scope, Quota, and Privacy Routing Controls](../02_architecture/adr/ADR-0017-openrouter-bounded-generation-and-usage-controls.md)
- [ADR-0019 — Use a Portable Single-Linux-VPS Deployment Baseline for MVP/Pilot, Initially on Tencent Cloud Lighthouse Jakarta](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md)

## 14. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.8` | `2026-09-10` | Record the resolved first-slice Teacher-session lifetime and credential-generation gate by reference to the implementation plan; retain participant-session lifetime as open | Codex |
| `0.7` | `2026-09-08` | Add deployment threats/mitigations (public database/backend exposure, SSH/CI credential leakage, secret handling, backup boundary, cross-environment leakage, patch discipline, Docker socket, disk/log exposure, plain HTTP, artifact integrity, provider outage/single-host loss) for the portable single-Linux-VPS MVP/pilot baseline (ADR-0019) | Claude |
| `0.6` | `2026-09-07` | Add AI-gateway threats/mitigations (privacy routing, provider fallback, server-controlled routing, scope abuse, validation shopping, credential exposure) for ADR-0017 | Claude |
| `0.5` | `2026-09-07` | Add Mathematics-validator threats/mitigations (parser safety, floating-point, staleness, AI self-evaluation) for ADR-0016 | Claude |
| `0.4` | `2026-09-07` | Add curriculum-specific threats/mitigations (ADR-0015); correct stale OAD-005/persistence wording; remove resolved curriculum-architecture item from Open Security Decisions | Claude |
| `0.3` | `2026-09-07` | Resolve identity/session threats with OIDC, backend-managed sessions, CSRF controls, local account linkage, and scoped pairing | Codex |
| `0.2` | `2026-09-06` | Align privacy and open-decision wording with the approved data-lifecycle baseline | Codex |
| `0.1` | `2026-09-06` | Initial multi-surface and AI threat model | Codex |
