# Conceptual Data Model — Penatika

> Defines persistent domain concepts, ownership, relationships, classifications, and invariants. It is not a physical database schema.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft conceptual baseline |
| Version | `0.1` |
| Last Updated | `2026-09-06` |
| Database Technology | Open Architecture Decision |

## 1. Modeling Principles

- Model teacher intent, structured classroom state, provenance, and assurance explicitly.
- Give mutable authoritative data one owning module.
- Separate lesson drafts from stable lesson versions used by sessions.
- Keep teacher-private and classroom-safe projections distinct.
- Treat AI proposals and validation results as lifecycle records, not truth by implication.
- Minimize personal data and do not persist raw push-to-talk audio by default.
- Add physical schema and migrations only after persistence technology is selected.

## 2. Core Concepts

### Teacher Identity

Represents the teacher principal used for ownership and authorization. Exact account fields, identity provider linkage, organization membership, and lifecycle are not yet decided.

**Owner:** Identity and Access module
**Classification:** Personal data

### Lesson

Stable aggregate identity for teacher-authored teaching material.

Known conceptual attributes:

- lesson identity;
- owning teacher identity;
- subject and grade scope;
- topic and learning intent;
- lifecycle status;
- created and updated timestamps.

**Owner:** Lesson module

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

### Classroom Session

Backend-authoritative aggregate for an active or saved teaching session.

Known conceptual attributes:

- session identity;
- teacher identity and selected lesson version;
- lifecycle state;
- current authoritative revision;
- active scene or lesson position;
- degradation and save status;
- started, ended, and saved timestamps.

**Owner:** Classroom Session module

### Session Participant

Represents an authorized participant and role in a session, such as teacher controller or classroom display.

Known conceptual attributes:

- participant identity or session-scoped identity;
- session identity;
- role;
- authorization state;
- joined, last-seen, disconnected, and revoked timestamps where needed.

Pairing credentials are separate ephemeral secrets and are not stored as reusable plaintext.

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

The physical representation, compaction, and retention strategy remain open.

### Adaptation Request

Teacher-authorized request to change supported classroom content.

Known conceptual attributes:

- request identity;
- teacher, session, lesson, scene, and base revision context;
- request type and normalized intent;
- lifecycle state;
- provider correlation metadata excluding secrets;
- timestamps and failure category.

Raw audio is ephemeral and excluded from default persistence.

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

### Mathematics Validation Result

Reproducible assurance record for supported content.

Known conceptual attributes:

- result identity;
- content identity and version;
- normalized validation input;
- validator and rule version;
- status: valid, invalid, unsupported, inconclusive, or error;
- bounded diagnostic and timestamp.

**Owner:** Mathematics Assurance module

### Curriculum Source Version

Controlled reference describing curriculum authority level, origin, and version.

Known conceptual attributes:

- authority level: `NORMATIVE`, `OFFICIAL_GUIDANCE`, or `LOCAL_CONTEXT`;
- stable source identity, title, and source version or decision number;
- jurisdiction, subject, phase or effective scope, and relevant reference identity;
- retrieval or matching provenance;
- local-context identity/version when applicable;
- integrity and licensing metadata;
- activation status.

**Owner:** Curriculum module

### Curriculum Reference

Links lesson or content claims to an authority level, controlled curriculum source version, relevant phase/scope, reference identity, retrieval or matching provenance, and optional local-context version.

**Owner:** Curriculum module

### Session Snapshot or Save Record

Represents a recoverable or final saved view of allowed session state.

Known conceptual attributes:

- session and final revision;
- lesson version;
- accepted structured scene and retained annotations;
- save state and idempotency identity;
- retention metadata.

Exact history and snapshot strategy remain open.

## 3. Relationships

```text
Teacher Identity 1 ── * Lesson
Lesson 1 ── * Lesson Version
Lesson Version 1 ── * Classroom Session
Classroom Session 1 ── * Session Participant
Classroom Session 1 ── * Classroom Scene revision/state
Classroom Scene 1 ── * Annotation Operation
Classroom Session 1 ── * Adaptation Request
Adaptation Request 1 ── 0..* AI Proposal
Lesson Version / AI Proposal / Scene Element 1 ── 0..* Mathematics Validation Result
Lesson Version / Scene Element * ── * Curriculum Reference
Curriculum Reference * ── 1 Curriculum Source Version
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
- An expired or consumed pairing credential cannot authorize a new participant.
- A save operation is idempotent for the same session and intended final revision.

## 5. Data Classification

| Data | Classification | Notes |
|---|---|---|
| Teacher identity and account metadata | Personal | Minimize and protect |
| Lesson and session content | Potentially sensitive educational/work product | Access-controlled |
| Raw push-to-talk audio | Sensitive transient input | Do not persist by default |
| Text command or transcript | Potentially sensitive | Retention decision required |
| Pairing credentials and tokens | Secret | Short-lived; never log plaintext |
| AI prompts and raw provider payloads | Potentially sensitive and untrusted | Minimize retention and logging |
| Student identity | Not required for core MVP | Do not introduce without product decision |
| Operational metrics | Internal | Avoid content and high-cardinality personal labels |

## 6. Retention and Deletion Open Decisions

- Lesson and lesson-version retention.
- Session history and annotation retention.
- Text-command and AI-proposal retention for evaluation.
- Teacher account deletion and data export.
- Curriculum version archival.
- Curriculum source supersession and re-evaluation behavior without historical provenance mutation.
- Audit requirements for teacher overrides and assurance warnings.

No production retention period is established by this document.

## 7. Physical Model and Migrations

No physical tables, collections, indexes, or migrations are created during initialization. After database selection:

- define schema from this ownership model;
- document transaction and concurrency boundaries;
- add version-controlled migrations;
- define uniqueness, foreign-key, and revision constraints;
- test migration safety and rollback/forward-fix strategy.

## 8. Related Documents

- [System Architecture](./SYSTEM_ARCHITECTURE.md)
- [ADR-0005 — Layered Curriculum Authority](./adr/ADR-0005-layered-curriculum-authority.md)
- [PRD](../00_product/PRD.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)
- [Data Persistence Standard](../standards/07_DATA_PERSISTENCE_STANDARD.md)

## 9. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.1` | `2026-09-06` | Initial conceptual domain model | Codex |
