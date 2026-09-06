# ADR-0007 — Graceful Degradation Without Offline Authority

| Field | Value |
|---|---|
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `PR-019`–`PR-025`, `FR-SESSION-011`–`FR-SESSION-017`, `FR-CANVAS-005`–`FR-CANVAS-006`, `FR-INK-006`–`FR-INK-007`, `FR-ADAPT-017`–`FR-ADAPT-021`, `INV-017`–`INV-022` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

Penatika supports an active classroom session across a private teacher controller, a student-facing classroom display, backend-owned session state, AI and speech dependencies, and durable persistence. Classroom continuity matters because a dependency failure should not unnecessarily turn a reviewed lesson and supported teaching controls into an unusable classroom application.

AI generation, speech recognition, network connectivity, individual clients, and durable save dependencies may fail independently. Those failures require explicit capability degradation, teacher-visible status, and deterministic recovery behavior.

[ADR-0002](./ADR-0002-backend-authoritative-session-state.md) already establishes the backend as the owner of classroom session lifecycle, accepted state, authoritative revision, participant roles, and save outcome. A naive offline-first mutation model would introduce a competing client authority, conflicting revisions, replay ambiguity, authorization risk, and complex merge behavior across controller and display surfaces.

[ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md) separately requires post-generation publication authorization and prohibits blocked proposals from becoming authoritative. Degraded mode cannot become a bypass for that policy or for Mathematics assurance, curriculum provenance, structured-content validation, privacy, authorization, or revision checks.

The MVP therefore requires graceful degradation without offline authority.

## Decision

Penatika MVP is resilience-oriented, not offline-first.

Dependency failure degrades only the affected capability while backend-authoritative classroom behavior continues where it remains safe and available.

### AI Provider Failure

When the AI provider is unavailable:

- current reviewed lesson presentation remains available;
- accepted classroom content remains visible;
- supported navigation, deterministic `DIRECT_ACTION`, digital ink, synchronization, session lifecycle, and save remain available when their own dependencies are healthy;
- new semantic AI generation, explanations, exercises, visuals, and rewrites are unavailable.

AI failure degrades Penatika into a structured digital teaching surface rather than failing the entire classroom session.

### Speech Recognition Failure

When speech recognition is unavailable:

- push-to-talk transcription is unavailable;
- supported tap, button, keyboard/text, mouse, touch, stylus, digital ink, and deterministic `DIRECT_ACTION` remain available;
- semantic AI adaptation may remain available through supported non-voice input.

Speech is an interaction channel, not an authority or required single point of failure.

### Teacher Controller Disconnect

When the teacher controller disconnects:

- the classroom display keeps the current authoritative projection;
- the backend session remains authoritative;
- no automatic mutation occurs;
- the disconnected controller cannot issue new commands, AI requests, approval/publication actions, end actions, or save actions;
- another teacher surface may act only when separately authorized by the backend.

No cached controller or classroom display is promoted into authority.

### Classroom Display Disconnect

When the classroom display disconnects:

- backend-authoritative session state remains available;
- safe teacher-private generation, proposal preview, warnings, and preparation may continue;
- student-facing publication pauses;
- projection-changing `DIRECT_ACTION` mutations pause;
- AI proposal publication pauses.

After reconnect, the display must synchronize against the backend-authoritative revision and restore a classroom-safe projection before student-facing mutations resume.

### Backend Authority Loss

When backend-authoritative session state cannot be reached:

- clients may preserve the last-known safe classroom projection;
- new authoritative navigation, `DIRECT_ACTION`, digital ink commits, AI publication, proposal approval that would mutate classroom state, pairing, start/join, authoritative session end, and successful save are unavailable;
- no client becomes temporary session authority;
- clients do not create new offline state-changing commands for automatic replay.

Backend authority loss causes an authoritative mutation freeze.

### Recovery and Command Reconciliation

Recovery follows:

```text
DISCONNECTED
→ preserve safe current projection
→ reconnect
→ retrieve authoritative backend revision
→ reconcile client state
→ reject stale/conflicting local assumptions
→ resume classroom mutations
```

Newly created offline mutations are not automatically applied after reconnect.

A command sent before connection loss whose acknowledgement is uncertain may be reconciled through its existing command identity, idempotency, and revision semantics. This reconciliation is not permission to accept new commands offline or replay them later.

### Durable Save Failure

When the durable save or persistence path is unavailable but authoritative runtime session state remains healthy:

- classroom teaching and otherwise permitted mutations may continue;
- AI adaptation remains subject to normal Q-02 and assurance policy;
- save remains `SAVE_PENDING`, `SAVE_FAILED`, or `RETRY_REQUIRED` as applicable;
- the product must not report `SAVED` until durable authoritative persistence acknowledges success.

### Safety and Trust Rules during Degradation

Degraded mode never bypasses:

- Q-02 AI publication approval;
- Mathematics assurance;
- curriculum provenance;
- authorization;
- privacy boundaries;
- structured-content validation;
- stale and revision checks.

An unavailable assurance dependency is not automatic success. Existing execution-class policy continues to determine whether the result requires approval, may permit warned override, or must remain blocked.

## Consequences

### Positive

- Preserves one authoritative classroom session state.
- Avoids conflict-heavy offline merge and replay behavior in the MVP.
- Allows AI or speech outages to degrade specific capabilities without necessarily stopping teaching.
- Preserves a safe, classroom-appropriate projection during recoverable disruption.
- Makes reconnect and mutation resumption deterministic through authoritative reconciliation.
- Keeps save status truthful and distinguishable from runtime session health.
- Preserves Q-02 approval and existing assurance, authorization, privacy, provenance, validation, and revision boundaries during failure.

### Negative / Cost

- Complete internet or backend-authority loss limits interactive classroom mutations.
- A teacher may temporarily need to continue with physical board work, verbal teaching, or other non-Penatika methods.
- Classroom display disconnect pauses student-facing publication even when teacher-private work can continue.
- The MVP does not provide rich offline-first classroom editing.
- Clients and backend require explicit connection, degradation, reconciliation, uncertain-acknowledgement, and save-status behavior.
- Target school network conditions require dedicated testing before pilot.

## Alternatives Considered

### Full offline-first local authority

Rejected because controller and display clients could create competing authoritative state, weaken backend authorization, and require conflict-heavy merge semantics that are not justified for the MVP.

### Queue all mutations and replay automatically

Rejected because stale navigation, ink, publication, or lifecycle commands could be applied after classroom context changes. Only a command sent before disconnect with uncertain acknowledgement may be reconciled through its existing identity and revision context.

### Fail the whole session on AI or speech outage

Rejected because AI and speech are capability dependencies rather than session authorities. Reviewed lesson presentation and supported deterministic controls should continue when backend authority remains healthy.

### Let the display or controller become temporary authority

Rejected because it conflicts with ADR-0002, creates divergent session state, weakens authorization boundaries, and makes recovery ambiguous.

## Architecture Invariants

- `INV-017`: Loss of backend authority must freeze new authoritative classroom mutations; no client becomes temporary session authority.
- `INV-018`: Clients may preserve the last-known safe classroom projection during recoverable connectivity loss, but cached state is not authoritative.
- `INV-019`: MVP must not automatically replay newly created offline state-changing commands after reconnect.
- `INV-020`: Mutation may resume only after reconciliation with backend-authoritative state.
- `INV-021`: A durable save is successful only after authoritative persistence acknowledgement.
- `INV-022`: Degraded mode cannot bypass approval, assurance, authorization, privacy, structured-content, curriculum provenance, or revision policy.

## Required Follow-Up

- Define the exact connection and dependency health model.
- Define the reconnect and authoritative synchronization protocol.
- Define idempotent command identity, acknowledgement, and revision contracts.
- Define save retry, terminal failure, and teacher recovery behavior.
- Define degradation and reconnecting UX for controller and classroom display surfaces.
- Define privacy-minimized degradation, reconciliation, and save instrumentation.
- Test the policy against representative target-school network conditions and devices.

## References

- [Product Brief](../../00_product/PRODUCT_BRIEF.md)
- [Product Requirements Document](../../00_product/PRD.md)
- [Classroom Session](../../01_features/classroom-session.md)
- [Classroom Canvas](../../01_features/classroom-canvas.md)
- [Live AI Adaptation](../../01_features/live-ai-adaptation.md)
- [Non-Functional Requirements](../NON_FUNCTIONAL_REQUIREMENTS.md)
- [System Architecture](../SYSTEM_ARCHITECTURE.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-06` | Accepted | Adopt graceful degradation without offline authority for the MVP |
