# Feature Specification — Live AI Adaptation

> Defines teacher requests that produce bounded, structured content proposals during an active classroom session.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.3` |
| Last Updated | `2026-09-06` |
| PRD Capability | `CAP-ADAPT-001` |

## 1. Feature Intent

### Problem Addressed

Teachers must adapt explanations, examples, questions, or visuals in real time without leaving the teaching surface to operate a generic chatbot.

### Desired Outcome

The teacher expresses an adaptation intent through push-to-talk or direct controls, receives private progress and validation status, and decides how a safe structured proposal affects classroom content.

## 2. Scope

### In Scope

- Push-to-talk and direct adaptation requests.
- Request context derived from the active lesson and scene.
- Structured AI proposal generation.
- Schema, policy, Mathematics, and curriculum checks as applicable.
- Teacher-private progress, warnings, acceptance, rejection, retry, and cancellation.
- Safe timeout and degraded behavior.

### Out of Scope

- Autonomous lesson delivery.
- Always-listening microphone behavior.
- Raw free-form AI output rendered directly to students.
- Background changes that bypass teacher control.

## 3. Primary Flow

1. Teacher starts an authorized action bound to the active session and scene revision.
2. Penatika classifies the supported intent before invoking AI Orchestration. Ambiguous intent uses the safer semantic AI proposal path.
3. Penatika follows the applicable path below.

### `DIRECT_ACTION` Path

1. Penatika validates teacher/session authorization for the supported direct-action command.
2. Penatika validates the deterministic, non-generative command and its current revision context.
3. The valid command becomes an accepted revision-aware classroom command.
4. The Classroom Session publishes updated role-specific projections.

This path does not invoke AI Orchestration and does not create or imitate an AI proposal.

### Semantic AI Adaptation Path

1. Penatika transcribes ephemeral audio if applicable and captures the minimum bounded request context.
2. AI Orchestration produces a structured proposal using bounded lesson, scene, grade, topic, and curriculum context.
3. Penatika validates schema and policy, then performs curriculum and applicable Mathematics assurance.
4. Publication policy assigns `APPROVAL_REQUIRED`, `APPROVAL_WITH_WARNING`, or `BLOCKED` to the proposal outcome.
5. Teacher receives the actual proposal, warning, or blocked result through a private surface.
6. Teacher approves the proposal or explicitly overrides an eligible warning; blocked results cannot be overridden.
7. A proposal with valid publication authorization and proposal/revision binding becomes an accepted revision-aware classroom command.
8. The Classroom Session publishes updated role-specific projections.

Transcription, generation progress, proposal preview, assurance results, warnings, alternatives, and other `PRIVATE_ONLY` work remain teacher-private and do not mutate student-facing authoritative state.

## 4. Functional Requirements

### FR-ADAPT-001 — Capture Explicit Teacher Intent

The system shall initiate adaptation only from an authorized teacher action. Always-listening behavior is not part of MVP.

### FR-ADAPT-002 — Minimize Audio Retention

Push-to-talk audio shall be processed ephemerally and shall not be persisted or logged by default.

### FR-ADAPT-003 — Bind Request Context

Each request shall identify the active session, scene revision, lesson version, grade/topic context, and requested adaptation type needed to detect stale results.

### FR-ADAPT-004 — Generate Structured Proposals

The AI integration shall return or be transformed into a supported structured proposal. Raw provider output shall not directly mutate classroom state.

### FR-ADAPT-005 — Validate before Acceptance

The system shall perform schema and policy checks and shall invoke curriculum and Mathematics assurance where applicable before a proposal can be accepted.

### FR-ADAPT-006 — Preserve Teacher Control

Teacher request authorizes generation. Teacher approval authorizes publication. An AI-generated student-facing semantic proposal shall not change authoritative classroom state until the actual generated proposal has completed applicable checks, been privately previewed, and received the required post-generation approval or explicit warned override.

### FR-ADAPT-007 — Keep Working State Private

Prompts, transcripts, provider output, chain-of-thought-like internal data, progress, and private warnings shall not appear on the classroom display.

### FR-ADAPT-008 — Detect Stale Proposals

A proposal generated for an earlier scene or session revision shall not overwrite newer authoritative state without an explicit supported reconciliation flow.

### FR-ADAPT-009 — Support Cancellation and Timeout

The teacher shall be able to cancel an in-flight request where supported. Timeouts and provider failures shall produce a clear private failure state and leave current classroom content unchanged.

### FR-ADAPT-010 — Apply Resource Controls

Requests shall be bounded by input size, context size, execution time, concurrency, and rate controls appropriate to classroom use and provider cost.

### FR-ADAPT-011 — Separate Generation and Publication Authorization

The original teacher generation request shall authorize generation only and shall not approve an unseen future AI output for publication.

### FR-ADAPT-012 — Require Post-Generation Approval

AI-generated semantic content that will be student-facing shall be privately previewed and approved after generation and applicable checks. Approval may use a supported tap, button, keyboard/controller, or voice interaction, but it shall unambiguously select the current proposal.

### FR-ADAPT-013 — Support Deterministic Direct Actions

Direct execution shall be limited to explicitly supported, authorized, deterministic, non-generative presentation or annotation commands that introduce no new semantic teaching content. Ambiguous classification shall use the proposal and approval flow.

### FR-ADAPT-014 — Support Explicit Warned Override

An `UNSUPPORTED` or `INCONCLUSIVE` assurance result may use explicit warned override only where product policy permits teacher judgment. The warning and override decision shall be visible to the teacher and traceable.

### FR-ADAPT-015 — Block Non-Overridable Failures

Hard structural, policy, security, authorization, or privacy failures; known deterministic Mathematics `INVALID`; required missing curriculum provenance; and stale proposals without supported reconciliation shall be `BLOCKED` and shall not be overridden.

### FR-ADAPT-016 — Bind Approval to Proposal and Revision

Publication approval shall bind to the proposal identity/version, authorized teacher/session authority, session identity, relevant scene/current revision, and applicable assurance result/version. Approval for one proposal shall not be replayed against another, replaced, or stale proposal.

### FR-ADAPT-017 — Isolate AI Provider Failure

When the AI provider is unavailable, new semantic AI generation shall be unavailable, but the current reviewed lesson and supported deterministic teaching controls may continue while backend authority remains healthy.

### FR-ADAPT-018 — Provide Non-Voice Fallback

When speech recognition is unavailable, push-to-talk transcription shall be unavailable while supported tap, button, keyboard/text, mouse, touch, stylus, digital ink, `DIRECT_ACTION`, and non-voice semantic AI request paths may remain available.

### FR-ADAPT-019 — Pause Publication while Display Is Unavailable

When the classroom display is disconnected, safe private generation, proposal preview, warnings, and other teacher-private work may continue, but AI publication shall pause until the display has reconnected and synchronized.

### FR-ADAPT-020 — Freeze Publication without Backend Authority

When backend-authoritative session state cannot be reached, no AI proposal, approval, warned override, or publication command shall mutate classroom state. The MVP shall not queue new publication commands for automatic replay.

### FR-ADAPT-021 — Preserve Assurance Policy during Degradation

Unavailable assurance shall remain an explicit non-success state and shall continue through the existing Q-02 execution-class policy. Degraded mode shall not convert unavailable assurance into automatic success or bypass schema, policy, curriculum provenance, Mathematics assurance, authorization, privacy, or revision requirements.

### Execution Classes

| Class | Required Behavior |
|---|---|
| `PRIVATE_ONLY` | Teacher-private AI/system work may proceed without publication approval and shall not mutate student-facing authoritative state. |
| `DIRECT_ACTION` | An explicitly supported deterministic, non-generative presentation/annotation command may execute directly after authorization and command validation. |
| `APPROVAL_REQUIRED` | AI-generated student-facing semantic content requires private preview and explicit post-generation approval. |
| `APPROVAL_WITH_WARNING` | An eligible unsupported/inconclusive result requires a visible warning and explicit traceable override before publication. |
| `BLOCKED` | A hard failure cannot produce an authoritative classroom command and cannot be overridden. |

## 5. State Model

```text
AUTHORIZED_ACTION → INTENT_CLASSIFIED
                        ├→ DIRECT_ACTION → AUTHORIZATION_VALIDATED
                        │                    → COMMAND_VALIDATED
                        │                    → REVISION_VALIDATED
                        │                    → ACCEPTED → DISPLAYED
                        │
                        ├→ SEMANTIC_AI_ADAPTATION
                        │      → TRANSCRIBING_IF_APPLICABLE
                        │      → GENERATING → ASSURING → PUBLICATION_CLASSIFIED
                        │           ├→ APPROVAL_REQUIRED → PROPOSAL_READY
                        │           │      → APPROVED → ACCEPTED → DISPLAYED
                        │           ├→ APPROVAL_WITH_WARNING → PROPOSAL_WITH_WARNING
                        │           │      → WARNED_OVERRIDE → ACCEPTED → DISPLAYED
                        │           └→ BLOCKED
                        │
                        └→ PRIVATE_ONLY → PRIVATE_RESULT
```

Semantic AI processing remains `PRIVATE_ONLY` until publication authorization succeeds. Rejected, cancelled, and failed requests terminate without changing the classroom projection. Only an accepted proposal with valid publication authorization, or a validated `DIRECT_ACTION`, may produce a classroom state command. `DIRECT_ACTION` is a separate deterministic command path and is not represented as an AI proposal.

Degradation overlays may apply without changing the Q-02 proposal states:

```text
DEGRADED_AI       → new semantic generation unavailable
DEGRADED_SPEECH   → push-to-talk unavailable; supported non-voice paths remain
DISPLAY_UNAVAILABLE → private proposal work may continue; publication paused
AUTHORITY_UNAVAILABLE → publication mutation frozen; no new offline replay queue
```

## 6. Trust and Safety Rules

- Provider output is untrusted input.
- Prompt injection in lesson or curriculum content must not grant system authority or bypass policy.
- Tool or retrieval access, if introduced, must use explicit allow-lists and scoped credentials.
- Unsupported or failed validation must be visible to the teacher.
- `UNSUPPORTED` or `INCONCLUSIVE` assurance is not success and may use warned override only where policy permits.
- Schema/structured-content failure, unauthorized action, stale unsafe proposal, deterministic Mathematics `INVALID`, missing required curriculum provenance, policy/security/privacy violation, arbitrary executable/unsupported content, and malformed unsafe proposals are non-overridable.
- The system must not present provider confidence as mathematical correctness.
- Sensitive teacher or student data must not be sent unless required and permitted by policy.

## 7. Failure and Edge Cases

- Speech cannot be transcribed reliably.
- Teacher changes scene before generation completes.
- AI returns malformed or unsupported content.
- Curriculum context is unavailable.
- Mathematics validation fails.
- Provider is slow, unavailable, rate-limited, or returns a policy refusal.
- Duplicate request is submitted.
- Teacher attempts approval after the scene revision changes.
- Duplicate or replayed approval is submitted.
- Approval targets a replaced proposal or a proposal changed after preview.
- Voice approval does not unambiguously identify the current proposal.
- Teacher attempts to override a `BLOCKED` result.
- A command is ambiguous between deterministic direct action and semantic generation.
- AI provider becomes unavailable while the backend session remains healthy.
- Speech recognition becomes unavailable while non-voice input remains usable.
- Classroom display disconnects after private proposal generation but before publication.
- Backend authority becomes unavailable after teacher approval but before authoritative mutation.
- Assurance dependency is unavailable or errors during proposal evaluation.

Failure must leave the current classroom projection unchanged unless the teacher independently performs another safe action.

## 8. Observability

Record correlation-safe operational events for request lifecycle, execution-class decision, latency, cancellation, provider failure category, schema failure, assurance outcome, proposal/revision binding result, and teacher approval, rejection, or warned override. Do not log raw audio, secrets, full prompts, unnecessary proposal payloads, or unnecessary classroom content.

## 9. Minimum Test and Evaluation Scenarios

- Valid direct and push-to-talk adaptation requests.
- No raw audio persistence or logging.
- Malformed and adversarial provider output.
- Prompt injection embedded in lesson content.
- Stale proposal after scene change.
- Validation failure and unsupported validation.
- Generation request does not count as publication approval.
- Clean student-facing semantic proposal requires post-generation approval.
- Warned proposal requires explicit warned override.
- `BLOCKED` proposal cannot be overridden.
- `DIRECT_ACTION` bypasses the AI proposal flow safely.
- Ambiguous direct/semantic command uses the safe proposal path.
- Approval is bound to proposal identity/version and current revision.
- Stale, duplicate, and replayed approvals are rejected.
- Voice approval succeeds only when unambiguous and fails safely when ambiguous.
- Classroom display does not change before publication authorization.
- AI outage disables new semantic generation without disabling healthy reviewed lesson presentation or deterministic controls.
- Speech outage disables push-to-talk while supported non-voice request paths continue.
- Display disconnect permits only safe private proposal work and pauses publication until resynchronization.
- Backend authority loss prevents proposal publication and does not queue a new publication command for automatic replay.
- Assurance outage remains non-success and cannot bypass the existing execution-class policy.
- Provider timeout, cancellation, retry, and rate-limit handling.
- Private state absent from classroom projection.
- Deterministic replay tests using recorded sanitized fixtures.
- AI quality evaluation for scoped Mathematics tasks before pilot.

## 10. Open Questions

- Which speech and AI providers or models are acceptable?
- What latency budget maintains teaching flow?
- What sanitized request/proposal data may be retained for evaluation?

## 11. Related Decisions

- [ADR-0006 — Teacher Approval and AI Publication Policy](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md)

## 12. Definition of Done

- Lifecycle, privacy, validation, stale-result, timeout, and teacher-control behavior are implemented and tested.
- Provider adapters are isolated from domain authority.
- Evaluation evidence exists for the initial Mathematics scope.
- Resource, cost, and observability controls are defined.
