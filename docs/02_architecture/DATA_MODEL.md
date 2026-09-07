# Conceptual Data Model — Penatika

> Defines persistent domain concepts, ownership, relationships, classifications, and invariants. It is not a physical database schema.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft conceptual baseline |
| Version | `0.6` |
| Last Updated | `2026-09-07` |
| Database Technology | PostgreSQL 18.x |

## 1. Modeling Principles

- Model teacher intent, structured classroom state, provenance, and assurance explicitly.
- Give mutable authoritative data one owning module.
- Separate lesson drafts from stable lesson versions used by sessions.
- Keep teacher-private and classroom-safe projections distinct.
- Treat AI proposals and validation results as lifecycle records, not truth by implication.
- Minimize personal data and do not persist raw push-to-talk audio by default.
- Model retention by purpose and data class rather than applying one indefinite history policy to every entity.
- Distinguish active data, deletion processing, primary-store purge, and backup expiry without claiming completed deletion prematurely.
- Add physical schema and migrations only after persistence technology is selected.
- This document remains CONCEPTUAL: no physical schema exists yet, even though the persistence technology (PostgreSQL, Flyway, Spring JDBC/JdbcClient) is now selected by [ADR-0014](./adr/ADR-0014-postgresql-flyway-sql-first-persistence.md).

### Conceptual Retention State

Relevant entities may conceptually carry lifecycle information such as:

- retention class;
- expiry timestamp where applicable;
- deletion-requested timestamp;
- primary-purge due timestamp;
- deletion or purge status;
- legal/security hold reference only when a real documented hold exists.

This is a conceptual baseline. It does not require every entity to carry identical fields physically and does not select a database or storage model.

## 2. Core Concepts

### TeacherAccount

Represents the stable internal Penatika teacher identity used for ownership and
authorization.

Known conceptual attributes:

- stable Penatika account identity;
- lifecycle state equivalent to `ACTIVE`, `DISABLED`, `DELETION_REQUESTED`, or
  `CLOSED`;
- minimal profile/contact attributes legitimately required by the product;
- ownership references for lessons, classroom sessions, and teacher data;
- account, security, and deletion audit metadata required by policy.

No local password, password hash, password-reset token, recovery question, or
local credential-MFA field is required for MVP. Teacher account/profile data
remains active until teacher-requested deletion or legitimate account closure.
Normal access is revoked immediately on accepted account deletion,
teacher-owned personal product data is deleted or anonymized from primary
active storage within `30 days`, and protected backup copies expire within
`30 additional days` unless a documented narrow preservation requirement
applies.

**Owner:** Identity and Access module
**Classification:** Personal data

### ExternalIdentityLink

Associates a `TeacherAccount` with one validated upstream OIDC identity.

Known conceptual attributes:

- owning `TeacherAccount`;
- validated OIDC issuer (`iss`);
- validated OIDC subject (`sub`);
- minimal required provider-link metadata;
- lifecycle or unlinking state where later supported.

The `(issuer, subject)` pair is unique and is the authoritative external
identity key. Email address, display name, and provider username are not stable
identity keys. Multiple links may be modeled for future explicit account
linking, but email-based or automatic cross-provider merging is prohibited.

**Owner:** Identity and Access module
**Classification:** Personal data

### Authenticated Browser Session

Represents revocable server-managed authentication for Teacher Web.

Known conceptual attributes:

- opaque session identity/reference;
- authenticated `TeacherAccount`;
- created, last-active, idle-expiry, and absolute-expiry concepts;
- revocation and logout state;
- security context required to validate the browser session.

The browser receives only an opaque protected session reference. Upstream
OAuth/OIDC access, refresh, and ID tokens remain server-side and are not
ordinary teacher profile or domain data. Their exact secure storage remains an
implementation decision.

**Owner:** Identity and Access module
**Classification:** Secret security state

### Lesson

Stable aggregate identity for teacher-authored teaching material.

Known conceptual attributes:

- lesson identity;
- owning teacher identity;
- subject and grade scope;
- topic and learning intent;
- lifecycle status;
- retention and deletion-processing state where applicable;
- created and updated timestamps.

**Owner:** Lesson module

Teacher-owned lessons remain retained until the teacher deletes the lesson. Committed deletion removes ordinary teacher access and enters the canonical primary-purge and backup-expiry process.

### Lesson Version

Immutable identity for a saved lesson content revision suitable for review or classroom use.

Known conceptual attributes:

- lesson version identity and parent lesson;
- structured content schema version;
- teacher-review state;
- curriculum authority level and provenance references;
- assurance summary;
- generation and teacher-edit provenance needed by policy;
- creation timestamp.

**Owner:** Lesson module

Stable lesson versions remain historically immutable while retained and follow the parent lesson lifecycle. If a retained saved session needs reproducibility after lesson deletion, that session may retain only the allowed stable snapshot or reference required for its own remaining lifecycle.

### Classroom Session

Backend-authoritative aggregate for an active or saved teaching session.

Known conceptual attributes:

- session identity;
- teacher identity and selected lesson version;
- lifecycle state;
- current authoritative revision;
- active scene or lesson position;
- degradation and save status;
- retention class and expiry for saved history;
- deletion and purge state where applicable;
- started, ended, and saved timestamps.

**Owner:** Classroom Session module

A successfully saved session is eligible for retained teacher history for `90 days` after session end/save and may be deleted earlier by the teacher.

### PairingGrant

Represents a short-lived single-use authorization grant for joining one
classroom session in one intended participant role.

Known conceptual attributes:

- classroom session identity;
- intended participant role/purpose;
- issued and expiry timestamps;
- single-use redemption state;
- revocation state;
- secret verification material that does not require reusable plaintext.

The initial MVP lifetime is five minutes. Successful redemption, explicit
revocation, expiry, or classroom-session end invalidates the grant.

**Owner:** Identity and Access with Classroom Session coordination
**Classification:** Secret

### SessionParticipant

Represents an authorized session-scoped participant, separate from product
account identity.

Known conceptual attributes:

- session-scoped participant identity;
- session identity;
- role: `TEACHER_CONTROLLER` or `CLASSROOM_DISPLAY`;
- authorization state;
- linked `TeacherAccount` and browser-session authorization where required for
  a teacher controller;
- replacement and revocation state;
- joined, last-seen, disconnected, and revoked timestamps where needed.

`CLASSROOM_DISPLAY` has no teacher-account authority. A
`TEACHER_CONTROLLER` requires an active authenticated teacher and classroom-
session authorization. MVP permits at most one active mutation-authorized
controller and one active display participant per classroom session.

**Owner:** Identity and Access with Classroom Session coordination

### Classroom Scene

Structured, versioned classroom content state selected or derived from a lesson version.

Known conceptual attributes:

- scene identity and schema version;
- supported ordered content elements;
- classroom-safe projection data;
- current scene revision.

**Owner:** Classroom Scene for model semantics; Classroom Session for accepted active state

### Annotation Operation

Revision-aware ink or highlight command applied to a scene.

Known conceptual attributes:

- operation identity;
- session and scene identities;
- actor and base revision;
- supported tool and geometry payload;
- order or accepted revision;
- undo/redo relationship where required.

The physical representation and compaction strategy remain open. Retained annotations follow the owning saved-session lifecycle, expire after `90 days` by default, and are deleted when the session is deleted earlier. There is no independent indefinite ink history.

### Adaptation Request

Teacher-authorized request to change supported classroom content.

Known conceptual attributes:

- request identity;
- teacher, session, lesson, scene, and base revision context;
- request type and normalized intent;
- lifecycle state;
- provider correlation metadata excluding secrets;
- timestamps and failure category.

Raw audio is ephemeral and excluded from ordinary persistence, logs, analytics, and session history. Full raw command or transcription content is transient by default and may use at most a `24-hour` diagnostic window where genuinely required; normalized intent or action metadata is preferred.

**Owner:** AI Orchestration module

### AI Proposal

Structured, non-authoritative result associated with an adaptation request or lesson generation.

Known conceptual attributes:

- proposal identity and schema version;
- source request;
- structured proposed changes;
- provider/model and prompt-policy version references when retained;
- schema, policy, assurance, and teacher decision states;
- target session or lesson revision.

**Owner:** AI Orchestration until accepted; owning domain module applies accepted commands

Rejected, regenerated, abandoned, failed, and blocked proposal bodies are transient by default and may use at most the `24-hour` diagnostic window where necessary. Privacy-minimized lifecycle and teacher-decision metadata may follow an associated saved session for up to `90 days`. Accepted structured content follows the lifecycle of the lesson/session artifact it becomes part of; a redundant raw-provider copy is not retained.

### Mathematics Validation Result

Reproducible assurance record for supported content. Per
[ADR-0016](./adr/ADR-0016-scoped-deterministic-mathematics-validation.md).

Known conceptual attributes:

- result identity;
- content/claim identity and content version;
- validator family (for example, `EXACT_RATIONAL`, `AFFINE_EXPRESSION`, `LINEAR_EQUATION`);
- validator/ruleset version;
- normalized mathematical representation where safe/useful;
- validation status: valid, invalid, unsupported, inconclusive, or error;
- bounded diagnostic;
- validation timestamp.

**Owner:** Mathematics Assurance module

Assurance references required to explain or reproduce retained accepted content follow the owning lesson/session lifecycle. Unaccepted content retains only privacy-minimized result metadata where needed for bounded diagnostics or pilot evidence.

### Curriculum Source

Stable controlled source identity, independent of any particular version. Per
[ADR-0015](./adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md).

Known conceptual attributes:

- authority level and source kind;
- stable source identifier and official title;
- issuing authority and jurisdiction;
- subject;
- official location/reference;
- applicable scope;
- content-use / licensing review status.

**Owner:** Curriculum module

A registered source is not automatically active; registration is metadata
acquisition, not authority activation.

### Curriculum Source Version

One immutable official source version belonging to a `Curriculum Source`.

Known conceptual attributes:

- owning `Curriculum Source`;
- official version / decision number;
- publication/effective metadata where applicable;
- subject and applicable phase/scope;
- acquisition provenance;
- source-artifact integrity digest (SHA-256 baseline);
- verification, activation, and supersession state.

**Owner:** Curriculum module

Controlled curriculum source/version metadata may be archived beyond teacher-content retention for provenance, integrity, supersession tracking, and historical reproducibility. Teacher-specific or local-context data follows teacher-owned data policy. Large source binaries are not required to be stored in the primary database; raw-artifact archival remains a separate implementation/operations decision.

### Curriculum Corpus Version

Penatika's own immutable normalization of one or more `Curriculum Source Version` records for a defined scope. Distinct from, and versioned separately from, the official source version — a normalization correction does not change the official source version.

Known conceptual attributes:

- corpus identity/version;
- source-version references;
- normalization/schema version;
- corpus integrity digest;
- creation/review information;
- validation, activation, and supersession state.

**Owner:** Curriculum module

Once activated, a corpus version is immutable. A correction creates a new `Curriculum Corpus Version`; the old version is not edited in place, and historical lesson provenance remains bound to the corpus version that was active when it was created.

### Curriculum Entry

Atomic grounding unit inside a `Curriculum Corpus Version`. The unit boundary preserves curriculum meaning and source reference rather than arbitrary chunking.

Known conceptual attributes:

- owning `Curriculum Corpus Version`;
- authority level;
- subject and phase/scope;
- curriculum element/domain where applicable;
- controlled source reference;
- normalized topic classification and controlled topic aliases/bindings;
- source-supported statement/content allowed by source policy;
- source/corpus provenance.

**Owner:** Curriculum module

### Local Curriculum Context Version

Teacher-owned `LOCAL_CONTEXT` overlay (for example, ATP sequencing, KSP/KOSP-derived context, or explicit teacher sequencing decisions).

Known conceptual attributes:

- owning `TeacherAccount`;
- explicit `LOCAL_CONTEXT` classification;
- version and lifecycle state;
- sequencing/context content;
- traceability reference when used for lesson grounding.

**Owner:** Curriculum module, teacher-authorized

Local context can never mutate or be promoted to `NORMATIVE` corpus entries. When local context affects sequencing without contradicting the normative source, both provenances are retained; a genuine contradiction produces an explicit conflict/warning state rather than a silent merge.

### Curriculum Reference

Links a retained lesson/content claim to a `Curriculum Source Version`, a `Curriculum Corpus Version`, a `Curriculum Entry`, the relevant phase/scope, retrieval or matching provenance, and an optional `Local Curriculum Context Version`.

**Owner:** Curriculum module

### Session Snapshot or Save Record

Represents a recoverable or final saved view of allowed session state.

Known conceptual attributes:

- session and final revision;
- lesson version;
- accepted structured scene and retained annotations;
- save state and idempotency identity;
- retention class, expiry, and deletion-processing metadata where applicable.

Saved history retains only the allowed stable lesson reference/snapshot, accepted structured scene, retained annotations, save outcome, required assurance/provenance references, and privacy-minimized action/decision metadata. The physical history and snapshot strategy remains open.

## 3. Relationships

```text
TeacherAccount 1 ── * ExternalIdentityLink
TeacherAccount 1 ── * Authenticated Browser Session
TeacherAccount 1 ── * Lesson
Lesson 1 ── * Lesson Version
Lesson Version 1 ── * Classroom Session
Classroom Session 1 ── * PairingGrant
Classroom Session 1 ── * SessionParticipant
TeacherAccount 1 ── 0..* SessionParticipant (TEACHER_CONTROLLER only)
Classroom Session 1 ── * Classroom Scene revision/state
Classroom Scene 1 ── * Annotation Operation
Classroom Session 1 ── * Adaptation Request
Adaptation Request 1 ── 0..* AI Proposal
Lesson Version / AI Proposal / Scene Element 1 ── 0..* Mathematics Validation Result
Lesson Version / Scene Element * ── * Curriculum Reference
Curriculum Source 1 ── * Curriculum Source Version
Curriculum Source Version * ── * Curriculum Corpus Version
Curriculum Corpus Version 1 ── * Curriculum Entry
Curriculum Reference * ── 1 Curriculum Entry
Curriculum Reference * ── 0..1 Local Curriculum Context Version
TeacherAccount 1 ── * Local Curriculum Context Version
Classroom Session 1 ── 0..* Session Snapshot or Save Record
```

## 4. Lifecycle Invariants

- A classroom session always references a stable lesson version.
- A proposal never becomes authoritative solely by being generated.
- Accepted session mutations advance an authoritative revision.
- A stale validation result cannot prove changed content valid.
- Every grounded curriculum claim references an explicit authority level, controlled source, source version, relevant phase/scope, and provenance.
- A newer curriculum source version does not silently rewrite provenance stored by an existing lesson version.
- A classroom projection excludes teacher-private fields by construction.
- An external identity resolves by unique validated `(issuer, subject)`; email does not identify or merge accounts.
- An authenticated browser session and each participant session are revocable.
- OAuth/OIDC access, refresh, and ID tokens are never browser-stored application credentials or ordinary profile data.
- Pairing alone cannot authenticate a teacher or grant teacher-account authority.
- A `CLASSROOM_DISPLAY` participant has no teacher authority.
- A `TEACHER_CONTROLLER` participant requires an active authenticated teacher authorization.
- MVP permits at most one active mutation-authorized controller and one active display participant per classroom session.
- An expired, consumed, or revoked pairing grant cannot authorize a participant and every grant expires after five minutes.
- A save operation is idempotent for the same session and intended final revision.
- A retained lesson exists until teacher deletion; deletion processing does not mutate the historical content of stable versions while they remain retained.
- A saved session expires after `90 days` by default, and retained annotations cannot outlive that owning session.
- Raw audio never becomes persistent history by default; full raw AI and speech working data remains transient.
- Accepted AI content follows the owning lesson/session lifecycle, while rejected or unaccepted proposal bodies do not become durable history.
- Required assurance and curriculum provenance cannot be independently removed while a retained artifact depends on it.
- Product state distinguishes ordinary-access removal, primary purge, and backup expiry.
- An activated `Curriculum Corpus Version` is immutable; a normalization correction creates a new corpus version rather than mutating the activated one in place.
- A newer official `Curriculum Source Version` does not automatically become active, and source supersession does not rewrite the provenance of previously saved lesson/session `Curriculum Reference` records.
- A `Local Curriculum Context Version` can never become or override a `NORMATIVE` `Curriculum Entry`.
- Curriculum retrieval that finds no adequate grounding produces an explicit `NO_MATCH`/`UNGROUNDED` state rather than falling back to unverified AI knowledge.
- A `Mathematics Validation Result` applies only to the exact content/claim version it evaluated; if that content changes, the prior result becomes stale and must be recalculated against the new version.

## 5. Data Classification

| Data | Classification | Notes |
|---|---|---|
| Teacher identity and account metadata | Personal | Retain until account deletion/closure; minimize and protect |
| External identity issuer/subject link | Personal | Stable external identity key; email is not the key |
| Authenticated browser and participant sessions | Secret security state | Opaque, bounded, revocable; never log reusable values |
| Lesson and lesson-version content | Potentially sensitive educational/work product | Retain until teacher deletion; access-controlled |
| Saved session content and retained annotations | Potentially sensitive educational/work product | `90-day` default; teacher may delete earlier |
| Raw push-to-talk audio | Sensitive transient input | Zero default persistence |
| Text command or transcript | Potentially sensitive transient input | Maximum `24-hour` diagnostic window where genuinely required |
| Pairing credentials and tokens | Secret | Short-lived; never log plaintext |
| AI prompts and raw provider payloads | Potentially sensitive and untrusted | Transient; maximum `24-hour` diagnostic window where genuinely required |
| Unaccepted AI proposal bodies | Potentially sensitive and untrusted | Transient; maximum `24-hour` diagnostic window where genuinely required |
| Privacy-minimized AI lifecycle/decision metadata | Internal and potentially personal | May follow the associated saved session for up to `90 days` |
| Assurance and curriculum provenance | Internal integrity record | Follow retained accepted content; controlled source versions may be archived independently |
| Student identity | Not required for core MVP | Do not introduce without product decision |
| Event-level operational/security logs | Internal | Privacy-minimized; `30-day` default |
| Event-level pilot telemetry and identifiable research evidence | Internal and potentially personal | Through analysis and up to `90 days` after final pilot-report acceptance, then delete or appropriately de-identify |

## 6. Conceptual Retention and Deletion Baseline

The canonical policy is [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md). The conceptual baseline is:

- teacher-owned lessons and stable versions remain until teacher deletion;
- saved session history and retained annotations expire after `90 days` by default;
- raw audio has zero default persistence;
- full transcription/command content, full prompts, raw provider payloads, and unaccepted proposal bodies are transient, with a maximum `24-hour` diagnostic window where genuinely necessary;
- accepted AI content and required assurance/provenance follow the owning retained lesson/session artifact;
- controlled curriculum source/version metadata may be archived beyond teacher-account or teacher-content deletion, while teacher-specific local context follows teacher-owned data policy;
- accepted deletion requests make data inaccessible from ordinary use, require primary purge within `30 days`, and require backup expiry within `30 additional days` unless a real documented narrow hold applies;
- teacher account deletion applies the same primary-purge and backup-expiry expectations to teacher-owned personal product data;
- event-level operational/security logs default to `30 days`;
- event-level pilot telemetry and identifiable research evidence expire or are appropriately de-identified no later than `90 days` after final pilot-report acceptance.

Physical enforcement, transaction boundaries, field placement, indexes, storage tiers, backup mechanics, and export format remain open implementation decisions.

## 7. Physical Model and Migrations

No physical tables, collections, indexes, or migrations are created during initialization. After database selection:

- define schema from this ownership model;
- document transaction and concurrency boundaries;
- add version-controlled migrations;
- define uniqueness, foreign-key, and revision constraints;
- test migration safety and rollback/forward-fix strategy.

### Physical Persistence Baseline

[ADR-0014](./adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)
selects the persistence technology without yet creating a physical schema:

- PostgreSQL as the primary authoritative database;
- one database for the modular monolith;
- Flyway-controlled schema evolution through version-controlled SQL
  migrations;
- Spring JDBC/JdbcClient persistence adapters as the initial data-access
  baseline;
- module-owned persistence boundaries (each business-capability module owns
  its persistent structures behind its own output ports);
- relational-first modeling with selective PostgreSQL JSONB for justified
  structured content (see §2 and §1's modeling principles);
- executable migrations under `migrations/schema` plus Flyway history
  become the physical schema source of truth once they are created; this
  document remains the conceptual model.

Likely integrity expectations for physical schema design include:

- unique `(issuer, subject)` for `ExternalIdentityLink`;
- concurrency-safe single-use `PairingGrant` redemption;
- atomic enforcement of at most one active `TEACHER_CONTROLLER` and one
  active `CLASSROOM_DISPLAY` per classroom session;
- revision-aware atomic session mutation (optimistic conflict detection);
- queryable retention/expiry lifecycle fields to support
  [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md).

Table and column names are not defined here.

## 8. Related Documents

- [System Architecture](./SYSTEM_ARCHITECTURE.md)
- [ADR-0005 — Layered Curriculum Authority](./adr/ADR-0005-layered-curriculum-authority.md)
- [ADR-0011 — OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./adr/ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0014 — PostgreSQL with Flyway and SQL-First Hexagonal Persistence](./adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)
- [ADR-0015 — Versioned Controlled Curriculum Corpus with Deterministic Retrieval](./adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
- [ADR-0016 — Scoped Deterministic Mathematics Validators with Exact Arithmetic](./adr/ADR-0016-scoped-deterministic-mathematics-validation.md)
- [PRD](../00_product/PRD.md)
- [Data Retention, History, Export, and Deletion Policy](../06_delivery/DATA_RETENTION_POLICY.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)
- [Data Persistence Standard](../standards/07_DATA_PERSISTENCE_STANDARD.md)

## 9. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.6` | `2026-09-07` | Resolve OAD-008: refine Mathematics Validation Result with validator family/ruleset version and content-version-scoped invalidation | Claude |
| `0.5` | `2026-09-07` | Resolve OAD-009: refine curriculum concepts into Curriculum Source, Curriculum Source Version, Curriculum Corpus Version, Curriculum Entry, and Local Curriculum Context Version | Claude |
| `0.4` | `2026-09-07` | Resolve OAD-003: record PostgreSQL/Flyway/Spring JDBC physical persistence baseline and likely integrity expectations while remaining conceptual | Claude |
| `0.3` | `2026-09-07` | Define local teacher accounts, OIDC identity links, revocable browser sessions, scoped pairing grants, and participant authority | Codex |
| `0.2` | `2026-09-06` | Resolve the conceptual retention, history, export, deletion, and backup-expiry lifecycle baseline | Codex |
| `0.1` | `2026-09-06` | Initial conceptual domain model | Codex |
