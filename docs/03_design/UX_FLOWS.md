# UX Flows — Penatika

> Defines cross-surface user journeys and visibility rules. Detailed feature behavior remains in feature specifications.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft baseline |
| Version | `0.1` |
| Last Updated | `2026-09-06` |

## 1. Experience Model

Penatika has three distinct experience contexts:

1. **Preparation workspace** — teacher creates, reviews, and edits lessons.
2. **Private controller** — teacher controls the active session and sees private AI/validation status.
3. **Classroom display** — students see only appropriate accepted teaching content.

These contexts may share implementation, but their visibility and interaction responsibilities must remain distinct.

## 2. Visibility Matrix

| Information or Action | Preparation | Private Controller | Classroom Display |
|---|:---:|:---:|:---:|
| Lesson editing | Yes | Limited or not required | No |
| AI prompt or request details | Teacher only | Teacher only | No |
| AI progress and provider failure | Teacher only | Teacher only | No |
| Validation status and warning detail | Teacher only | Teacher only | Classroom-safe result only if required |
| Current accepted lesson content | Yes | Yes | Yes |
| Session navigation | Optional | Yes | Display result only |
| Ink controls | Optional | Yes or teacher surface | No private controls |
| Pairing credentials | Teacher only | Teacher only | Only bounded join affordance if required |
| Internal diagnostics or secrets | No user surface | No user surface | Never |

## 3. Flow UX-01 — Prepare a Lesson

### Entry

Teacher chooses to create a new lesson.

### Flow

1. Provide supported grade, Mathematics topic, and learning intent.
2. Review available curriculum source/version context or a clear unavailable state.
3. Start generation.
4. Continue seeing progress without ambiguous frozen state.
5. Review structured content, validation state, and warnings.
6. Edit, accept, reject, or regenerate supported sections.
7. Save a classroom-ready lesson version.

### Required States

- empty input;
- invalid or unsupported scope;
- generating;
- validating;
- validation warning;
- generation failure;
- unsaved edits;
- ready and saved.

### Success

Teacher can identify which lesson version is ready for classroom use.

## 4. Flow UX-02 — Start and Pair a Session

### Entry

Teacher selects a reviewed lesson version.

### Flow

1. Start classroom session.
2. Open or connect the classroom display.
3. Present a bounded pairing mechanism.
4. Pair the private controller.
5. Confirm active session and participant status on the teacher surface.
6. Show only classroom-ready content on the display.

### Required States

- creating session;
- waiting for display;
- waiting for controller;
- pairing credential expired;
- unauthorized join;
- connected;
- partial connection;
- reconnecting;
- failed with retry or exit path.

### Success

Teacher understands which devices are connected and students never see private controller state.

## 5. Flow UX-03 — Teach, Annotate, and Navigate

### Flow

1. Teacher navigates lesson scenes or steps.
2. Display updates to the authoritative accepted revision.
3. Teacher writes, highlights, erases, undoes, redoes, or clears annotations.
4. Input feedback remains responsive while synchronization status remains understandable.
5. If synchronization fails, the display preserves a safe projection and the teacher sees the degraded state.

### Interaction Expectations

- Equivalent mouse, touch, stylus, and keyboard actions behave consistently.
- Destructive clear action is recoverable or confirmed.
- Controls do not obscure classroom content unnecessarily.
- Orientation and viewport changes preserve the logical scene.

## 6. Flow UX-04 — Request Live Adaptation

### Flow

1. Teacher initiates push-to-talk or direct adaptation.
2. Controller shows listening/transcribing state only while explicitly active.
3. Controller shows generating and validating progress.
4. Teacher receives structured proposal, validation status, or failure.
5. Until approval policy is resolved, teacher explicitly accepts, modifies, rejects, retries, or cancels.
6. Accepted content updates the classroom display through authoritative session state.

### Privacy Rule

Raw transcript, prompt, provider response, internal reasoning, and private warnings never appear on the classroom display.

### Failure Behavior

Timeout, cancellation, provider failure, invalid output, or stale result leaves current classroom content unchanged.

## 7. Flow UX-05 — Degraded Connectivity and Recovery

1. Teacher surface shows whether the issue affects client connection, synchronization, AI, speech, validation, or another dependency.
2. Classroom display retains the last safe projection where possible.
3. Actions that cannot be committed are disabled, queued only if explicitly safe, or rejected with a clear state.
4. On reconnect, clients reconcile against backend authority.
5. Teacher receives recovery outcome without exposing private diagnostics to students.

Exact offline/degraded capability remains an Open Product Decision.

## 8. Flow UX-06 — End and Save

1. Teacher requests session end.
2. Penatika warns about unresolved generation, unsaved changes, or synchronization issues that affect save integrity.
3. Teacher confirms end when needed.
4. Backend performs an idempotent save.
5. Teacher sees saved, retryable failure, or unresolved state.
6. Classroom display moves to a safe ended-session presentation.

## 9. Cross-Flow UX Rules

- Teacher control is explicit at material decision points.
- Draft, proposed, accepted, displayed, invalid, unsupported, and failed states are visually and semantically distinct.
- Errors explain what happened and what the teacher can do next.
- The system does not fabricate success when state is unknown.
- Long-running AI work does not block safe navigation or annotation unless consistency requires it.
- Classroom display avoids notifications or controls unrelated to students.
- Product language for the initial market is Indonesian; final terminology and localization policy remain open.

## 10. Accessibility Baseline

- Logical focus order and visible focus.
- Keyboard access to core actions where keyboard input exists.
- Labels for icon-only controls.
- Adequate target sizes for touch and stylus-adjacent controls.
- Status communication not dependent on color alone.
- Reduced-motion behavior for non-essential animation.
- Classroom readability tested at realistic viewing distance.
- Accessible Mathematics semantics where supported by the chosen renderer.

## 11. Open UX Decisions

- Exact navigation model for lesson scenes and progressive reveal.
- Controller layout and one-handed interaction priorities.
- Approval interaction per live adaptation type.
- Formal accessibility conformance target.
- Supported viewport, browser, input-device, and display matrix.
- Indonesian terminology for assurance states and AI actions.

## 12. Related Documents

- [PRD](../00_product/PRD.md)
- [Design System](./DESIGN_SYSTEM.md)
- [Classroom Session](../01_features/classroom-session.md)
- [Classroom Canvas](../01_features/classroom-canvas.md)
- [Live AI Adaptation](../01_features/live-ai-adaptation.md)

## 13. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.1` | `2026-09-06` | Initial multi-surface UX baseline | Codex |
