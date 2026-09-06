# ADR-0006 — Teacher Approval and AI Publication Policy

| Field | Value |
|---|---|
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-ADAPT-001`, `FR-ADAPT-005`, `FR-ADAPT-006`, `FR-ADAPT-011`–`FR-ADAPT-016`, `PR-001`, `PR-005`, `PR-015`–`PR-018` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

Penatika allows a teacher to request live AI adaptation while teaching. The teacher may ask for a simpler example, a harder question, another explanation, or a visual. The resulting output can directly influence what students see.

An explicit teacher request proves that the teacher wants Penatika to **generate** something, but it does not prove that the teacher has reviewed and approved the actual generated output. Treating request intent as publication approval would allow unseen AI-generated semantic content to reach the classroom display.

At the same time, forcing confirmation for deterministic presentation actions such as navigation, resizing, undo, or clear annotation would add friction and undermine the teaching-flow goal.

The system therefore needs an explicit publication policy that separates:
- generation authorization;
- deterministic direct actions;
- teacher-private AI work;
- post-generation publication approval;
- warned override;
- non-overridable blocking.

## Decision

Penatika adopts five execution classes.

### 1. `PRIVATE_ONLY`

AI or system work may proceed without publication approval when its result remains teacher-private and does not mutate student-facing authoritative content.

Examples:
- speech transcription in the private workflow;
- AI generation progress;
- private proposal preview;
- validation results;
- warnings;
- alternative suggestions;
- teacher-only notes.

`PRIVATE_ONLY` content must never appear automatically on the classroom display.

### 2. `DIRECT_ACTION`

An authorized teacher command may execute directly when it is deterministic, non-generative, and does not introduce new semantic teaching content.

Examples may include:
- navigate next/previous;
- resize or zoom a supported element;
- show/hide an already-reviewed answer;
- undo/redo;
- clear annotation;
- erase/highlight through supported deterministic commands.

A `DIRECT_ACTION` must not be used to bypass AI proposal policy.

If command classification is ambiguous between presentation control and semantic generation, the safer proposal flow applies.

### 3. `APPROVAL_REQUIRED`

This is the default class for AI-generated semantic content that would become student-facing.

Examples:
- generate a simpler example;
- generate a harder related question;
- rewrite an explanation;
- produce step-by-step teaching content;
- generate a new classroom visual;
- generate additional exercises.

The teacher request authorizes generation only.

After generation and applicable validation:
1. the actual proposal is shown privately to the teacher;
2. the proposal is bound to a proposal identity and current session/scene revision;
3. the teacher explicitly approves that proposal;
4. only then may it become a classroom state command.

Approval may be expressed through a supported tap, button, keyboard/controller action, or explicit voice command such as “Tampilkan”, provided the approval refers unambiguously to the current proposal.

### 4. `APPROVAL_WITH_WARNING`

A proposal may be eligible for explicit warned override when the system does not know that it is invalid, but assurance is incomplete or inconclusive.

Examples:
- Mathematics assurance result is `UNSUPPORTED`;
- Mathematics assurance result is `INCONCLUSIVE`;
- a non-critical assurance signal cannot be completed but policy allows teacher judgment.

Before publication:
- warning state must be visible on the private teacher surface;
- the teacher must explicitly choose to proceed despite the warning;
- the override decision must be traceable;
- the proposal must still satisfy structural, authorization, privacy, and hard policy requirements.

Warned override is not equivalent to bypassing a known failure.

### 5. `BLOCKED`

A proposal or action cannot be published, even with teacher approval, when a hard safety, integrity, authorization, or structural rule fails.

Blocking conditions include, when applicable:
- schema or structured-content validation failure;
- unauthorized actor/action;
- stale proposal without a supported reconciliation flow;
- known deterministic Mathematics result `INVALID`;
- required curriculum provenance missing for a grounded curriculum claim;
- policy/security violation;
- prompt-injection outcome attempting to acquire application authority;
- privacy boundary violation;
- arbitrary executable or unsupported content;
- malformed command or proposal that cannot be safely interpreted.

The teacher may retry, edit the request, cancel, or use another supported action. There is no “display anyway” path for a `BLOCKED` result.

## Core Authorization Rule

> **Teacher request authorizes generation. Teacher approval authorizes publication.**

The two authorizations are separate.

A teacher cannot approve an unseen future AI output merely by submitting the generation request.

## Proposal Binding

Publication approval must be bound to:
- authorized teacher identity/session authority;
- proposal identity/version;
- session identity;
- relevant scene revision;
- assurance result/version relevant to the proposal.

A stale or replaced proposal cannot inherit approval from an earlier proposal.

## Consequences

### Positive
- Teacher retains meaningful control over actual student-facing AI content.
- Private AI work remains fast and does not require unnecessary confirmations.
- Deterministic presentation controls remain low-friction.
- Known-invalid content cannot be forced onto the display through a generic override.
- Inconclusive validation can remain usable through explicit teacher judgment.
- Approval decisions become testable, auditable, and contractable.
- Voice approval can support teaching flow without weakening proposal identity/revision safety.

### Negative / Cost
- AI semantic adaptation requires an extra teacher action before publication.
- Teacher-controller UX must make preview, warning, blocked, and approval states fast to understand.
- Proposal identity and revision binding add contract/state complexity.
- Classification between deterministic action and semantic generation must be explicit.
- Warning overrides require event/evidence design and retention decisions.

## Alternatives Considered

### Treat the original teacher request as approval to publish
Rejected because the teacher has authorized an intent, not reviewed the generated output.

### Require approval for every classroom action
Rejected because deterministic presentation and annotation commands would become unnecessarily disruptive.

### Auto-publish content that passes validation
Rejected for MVP because successful validation does not prove that generated wording, pedagogy, difficulty, or contextual suitability matches teacher intent.

### Allow teacher override for every failure
Rejected because authorization failures, structural failures, known-invalid Mathematics, privacy violations, and hard policy failures must not become optional safety controls.

### Block all unsupported or inconclusive assurance results
Rejected because deterministic assurance cannot cover every pedagogically valid Mathematics explanation or content type in the MVP.

## Architecture Invariants

- `INV-013`: An AI generation request does not itself authorize publication of the resulting semantic content.
- `INV-014`: AI-generated student-facing semantic content requires post-generation approval bound to the actual proposal and current revision, unless a later accepted ADR explicitly narrows this rule for a proven safe content class.
- `INV-015`: `BLOCKED` results cannot produce authoritative classroom commands through teacher override.
- `INV-016`: Deterministic non-generative presentation actions may execute without AI publication approval only through explicitly supported command classes.

## Required Follow-Up

- Define proposal, approval, warning-override, and blocked-state contracts.
- Define supported `DIRECT_ACTION` command taxonomy for MVP.
- Define assurance result mapping to `APPROVAL_REQUIRED`, `APPROVAL_WITH_WARNING`, or `BLOCKED`.
- Define private controller UX for preview and fast approval.
- Define explicit voice-approval disambiguation and timeout behavior.
- Define retention/audit policy for approval and warned-override evidence.
- Add stale proposal, duplicate approval, replay, and concurrent revision tests.

## References

- [Live AI Adaptation](../../01_features/live-ai-adaptation.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0003 — Use Versioned Structured Classroom Content](./ADR-0003-structured-classroom-content.md)
- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./ADR-0004-ai-assurance-boundary.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-06` | Accepted | Adopt teacher approval and AI publication policy for the MVP |
