# Feature Specification — Live AI Adaptation

> Defines teacher requests that produce bounded, structured content proposals during an active classroom session.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.1` |
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

1. Teacher starts a push-to-talk or direct adaptation action.
2. Penatika captures the minimum request context and, for voice, transcribes ephemeral audio.
3. Penatika converts the request into a supported adaptation intent.
4. AI orchestration requests a structured proposal with bounded lesson, scene, grade, topic, and curriculum context.
5. Penatika validates schema, policy, curriculum provenance, and applicable Mathematics claims.
6. Teacher receives private status and the resulting proposal or warning.
7. Teacher accepts, modifies, rejects, retries, or cancels according to the approval policy.
8. Accepted content becomes a revision-aware classroom command.

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

Until `OPD-001` defines otherwise, the safe baseline is explicit teacher approval before a proposal changes student-facing content.

### FR-ADAPT-007 — Keep Working State Private

Prompts, transcripts, provider output, chain-of-thought-like internal data, progress, and private warnings shall not appear on the classroom display.

### FR-ADAPT-008 — Detect Stale Proposals

A proposal generated for an earlier scene or session revision shall not overwrite newer authoritative state without an explicit supported reconciliation flow.

### FR-ADAPT-009 — Support Cancellation and Timeout

The teacher shall be able to cancel an in-flight request where supported. Timeouts and provider failures shall produce a clear private failure state and leave current classroom content unchanged.

### FR-ADAPT-010 — Apply Resource Controls

Requests shall be bounded by input size, context size, execution time, concurrency, and rate controls appropriate to classroom use and provider cost.

## 5. State Model

```text
REQUESTED → TRANSCRIBING → GENERATING → VALIDATING → PROPOSED
     │            │             │            │          ├→ ACCEPTED
     ├────────────┴─────────────┴────────────┴──────────┼→ REJECTED
     └──────────────────────────────────────────────────┼→ CANCELLED
                                                        └→ FAILED
```

Only `ACCEPTED` proposals may produce a classroom state command.

## 6. Trust and Safety Rules

- Provider output is untrusted input.
- Prompt injection in lesson or curriculum content must not grant system authority or bypass policy.
- Tool or retrieval access, if introduced, must use explicit allow-lists and scoped credentials.
- Unsupported or failed validation must be visible to the teacher.
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

Failure must leave the current classroom projection unchanged unless the teacher independently performs another safe action.

## 8. Observability

Record correlation-safe operational events for request lifecycle, latency, cancellation, provider failure category, schema failure, validation outcome, and teacher decision. Do not log raw audio, secrets, full prompts, or unnecessary classroom content.

## 9. Minimum Test and Evaluation Scenarios

- Valid direct and push-to-talk adaptation requests.
- No raw audio persistence or logging.
- Malformed and adversarial provider output.
- Prompt injection embedded in lesson content.
- Stale proposal after scene change.
- Validation failure and unsupported validation.
- Provider timeout, cancellation, retry, and rate-limit handling.
- Private state absent from classroom projection.
- Deterministic replay tests using recorded sanitized fixtures.
- AI quality evaluation for scoped Mathematics tasks before pilot.

## 10. Open Questions

- Which adaptation types require explicit approval, preview, or may use another teacher-authorized flow?
- Which speech and AI providers or models are acceptable?
- What latency budget maintains teaching flow?
- What sanitized request/proposal data may be retained for evaluation?
- What capability remains when AI or speech dependencies are unavailable?

## 11. Definition of Done

- Lifecycle, privacy, validation, stale-result, timeout, and teacher-control behavior are implemented and tested.
- Provider adapters are isolated from domain authority.
- Evaluation evidence exists for the initial Mathematics scope.
- Resource, cost, and observability controls are defined.

