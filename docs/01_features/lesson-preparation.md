# Feature Specification — Lesson Preparation

> Defines teacher-controlled creation and review of an AI-assisted lesson before classroom delivery.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.1` |
| Last Updated | `2026-09-06` |
| PRD Capability | `CAP-LESSON-001` |

## 1. Feature Intent

### Problem Addressed

Teachers need a structured lesson starting point but must not be forced to use raw AI chat output as classroom material.

### Desired Outcome

The teacher creates a curriculum-aware lesson draft, understands its generation and validation status, edits it, and saves a teacher-reviewed lesson version.

### Actors

- Teacher
- External AI capability
- Curriculum source
- Mathematics assurance capability

## 2. Scope

### In Scope

- Capture grade, topic, and learning intent.
- Generate a structured lesson proposal.
- Show curriculum and validation status.
- Let the teacher edit, accept, reject, or regenerate supported sections.
- Save a version suitable for starting a classroom session.

### Out of Scope

- PDF or PowerPoint import.
- Collaborative multi-teacher editing.
- Automatic publication without teacher review.
- Lesson marketplace or discovery.

## 3. Primary Flow

1. Teacher starts a new lesson draft.
2. Teacher selects a supported grade and Mathematics topic and supplies learning intent.
3. Penatika resolves available controlled curriculum context.
4. Penatika requests an AI-generated structured proposal.
5. Penatika validates the proposal schema and applicable Mathematics content.
6. Teacher reviews status, warnings, and content.
7. Teacher edits, accepts, rejects, or regenerates supported sections.
8. Teacher saves a new lesson version.

## 4. Functional Requirements

### FR-LESSON-001 — Capture Lesson Intent

The system shall capture the minimum supported lesson context: grade, Mathematics topic, and teacher learning intent.

### FR-LESSON-002 — Use Controlled Curriculum Context

The system shall identify the curriculum authority level, controlled source, source version, relevant phase/scope, and provenance used for grounded claims. For MVP Mathematics, normative claims resolve to BSKAP 046/H/KR/2025; official guidance and local school/teacher context retain distinct authority levels. It shall not represent ungrounded provider output as authoritative.

### FR-LESSON-003 — Generate Structured Proposal

The system shall request and accept only a supported structured lesson representation. Arbitrary executable or AI-generated HTML shall not be accepted as lesson content.

### FR-LESSON-004 — Expose Proposal State

The teacher surface shall distinguish at least generating, validating, warning, failed, draft, and ready states.

### FR-LESSON-005 — Support Teacher Revision

The teacher shall be able to edit supported content directly and accept, reject, or regenerate supported proposal sections.

### FR-LESSON-006 — Preserve Provenance

Saved lesson versions shall retain enough provenance to identify teacher edits, relevant generation output, curriculum authority level, controlled source, source version, phase/scope, matching provenance, local-context version when applicable, and validation status without retaining raw audio. Activating a newer curriculum source version shall not silently rewrite a saved lesson version's historical provenance.

### FR-LESSON-007 — Prevent Unreviewed Classroom Use

A lesson proposal shall not be considered classroom-ready until it reaches the defined teacher-reviewed state.

### FR-LESSON-008 — Save Immutable Version Identity

Saving shall create or identify a lesson version that can be selected by a classroom session without silently changing when the draft is edited later.

## 5. Business Rules

- `BR-LESSON-001`: Teacher review is required before a generated draft is marked ready.
- `BR-LESSON-002`: Validation warning and unsupported validation are different from successful validation.
- `BR-LESSON-003`: Missing, ungrounded, stale, or non-normative curriculum context must be represented with its actual authority and provenance state.
- `BR-LESSON-004`: AI provider text is never the authoritative storage format.
- `BR-LESSON-005`: Official guidance and local sequencing cannot be promoted to national normative authority.

## 6. State Model

```text
EMPTY → DRAFT_INPUT → GENERATING → VALIDATING
                              ├→ FAILED
                              └→ REVIEW_DRAFT → READY
                                      ├→ REJECTED
                                      └→ GENERATING
```

### State Invariants

- `READY` always references a saved lesson version.
- `FAILED` and `REJECTED` content cannot start a classroom session.
- A validation warning remains attached until resolved or explicitly accepted under a product policy.

## 7. Data and Privacy

Inputs include grade, topic, learning intent, teacher edits, and optional commands. Outputs include structured lesson content, provenance, curriculum references, and validation results.

Teacher identity and lesson content may be sensitive. Raw audio is not stored by default. Retention and deletion policy remain open.

## 8. Failure and Edge Cases

- AI provider unavailable or times out.
- Curriculum source unavailable or has no matching scope.
- Proposal violates the structured schema.
- Mathematics validation fails or is unsupported.
- Teacher loses connectivity while editing.
- Concurrent or stale save attempt.

The system must preserve the last safe teacher-authored state and must not mark failed generation as ready.

## 9. Security Requirements

- Only an authorized teacher may create or change a lesson.
- Provider output must be treated as untrusted input.
- Curriculum and validation metadata must not be forgeable solely by the client.
- Logs must exclude raw audio, secrets, and unnecessary lesson content.

## 10. Minimum Test Scenarios

- Create a valid draft and save a reviewed version.
- Reject unsupported grade or topic input.
- Reject malformed AI structured output.
- Preserve teacher edits after generation failure.
- Show failed and unsupported validation distinctly.
- Prevent an unreviewed draft from starting a session.
- Verify raw audio is absent from persistent data and logs.

## 11. Open Questions

- How should BSKAP 046/H/KR/2025 be ingested, normalized, integrity-checked, and mapped to the supported topic taxonomy?
- How should official guidance and school/teacher local context be licensed, modeled, versioned, and retrieved?
- What warning types may a teacher explicitly override?
- What lesson history, duplication, deletion, or export behavior is required?
- What content blocks are included in the first structured lesson model?

## 12. Related Decisions

- [ADR-0005 — Use Layered Curriculum Authority and Versioned Provenance](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md)

## 13. Definition of Done

- Requirements and state transitions are implemented and tested.
- Structured content and provenance contracts are versioned.
- Required warning and failure states are usable.
- Curriculum and Mathematics assurance dependencies are resolved for MVP topics.
- Relevant PRD acceptance and privacy rules are satisfied.
