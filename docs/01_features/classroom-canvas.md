# Feature Specification — Classroom Canvas and Digital Ink

> Defines student-facing structured rendering and teacher interaction with classroom content.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.1` |
| Last Updated | `2026-09-06` |
| PRD Capabilities | `CAP-CANVAS-001`, `CAP-INK-001` |

## 1. Feature Intent

### Problem Addressed

Static slides are difficult to adapt, while raw AI or mirrored interfaces can expose irrelevant or unsafe content to students.

### Desired Outcome

The classroom display renders a predictable, readable structured scene. The teacher navigates and annotates it using available input devices without exposing private controls.

## 2. Scope

### In Scope

- Render supported structured content blocks.
- Navigate lesson scenes or steps.
- Apply teacher-authorized structured updates.
- Write, highlight, erase, undo, redo, and clear digital ink.
- Normalize mouse, touch, and stylus input for equivalent operations.
- Display connection and content states appropriate for students.

### Out of Scope

- Arbitrary HTML or executable content.
- Advanced graphing and 3D visualization.
- General-purpose collaborative whiteboard.
- Student-authored annotations from personal devices.

## 3. Functional Requirements

### FR-CANVAS-001 — Render Supported Structured Content

The classroom surface shall render only content that conforms to a supported, versioned scene/content model.

### FR-CANVAS-002 — Preserve Classroom-Safe Projection

The display shall not render prompts, raw model output, private suggestions, teacher-only warnings, credentials, or internal diagnostics.

### FR-CANVAS-003 — Navigate Authoritative Scene State

Teacher navigation shall update the authoritative session revision, and the display shall converge on that revision.

### FR-CANVAS-004 — Handle Unsupported Content Safely

Unknown or unsupported content types shall render a safe fallback and shall not execute embedded code.

### FR-INK-001 — Create Ink Strokes

The teacher shall be able to create writing and highlight strokes using supported pointer input.

### FR-INK-002 — Erase and Clear

The teacher shall be able to erase selected ink and clear the current supported annotation scope with appropriate confirmation when destructive.

### FR-INK-003 — Undo and Redo

Undo and redo shall operate against an explicit session and scene revision and shall not silently affect another scene or stale state.

### FR-INK-004 — Normalize Input

Equivalent mouse, touch, and stylus actions shall produce consistent supported commands while preserving relevant pressure or tool data only when justified.

### FR-INK-005 — Synchronize Ink

Accepted annotation changes shall synchronize through the authoritative session state. Clients shall not permanently diverge through local-only edits.

## 4. Business Rules

- `BR-CANVAS-001`: Only supported structured elements may become classroom content.
- `BR-CANVAS-002`: Private teacher state is never part of the classroom projection.
- `BR-CANVAS-003`: Scene and annotation updates are revision-aware.
- `BR-CANVAS-004`: Unsupported content fails closed to a safe visual state.
- `BR-INK-001`: Destructive clear behavior must be recoverable through undo when feasible or require confirmation.

## 5. State Model

Each classroom scene has an authoritative revision and a projection status:

```text
LOADING → READY → UPDATING → READY
   └→ SAFE_FALLBACK
READY ↔ DEGRADED
```

Annotation commands are accepted, rejected as stale/unauthorized, or reconciled against the current revision.

## 6. UX and Accessibility

- Student-facing content must remain legible at classroom viewing distance.
- Focus, controls, cursors, and private action affordances belong on teacher surfaces unless classroom context requires otherwise.
- Core navigation must support keyboard input where available.
- Touch targets must support smartphone and touch-display usage.
- Color must not be the only carrier of validation or content meaning.
- Motion must not distract from teaching and must respect reduced-motion settings where available.
- Mathematics rendering must expose an accessible representation where technically feasible.

## 7. Failure and Edge Cases

- Unsupported scene element or schema version.
- Lost connection during an ink stroke.
- Duplicate, stale, or out-of-order annotation command.
- Viewport resize, orientation change, or display reconnect.
- Stylus and touch input arrive simultaneously.
- Clear action is triggered accidentally.

The last safe classroom projection should remain visible when a recoverable update fails.

## 8. Security and Privacy

- Structured content must be encoded and rendered without script execution.
- URLs or external media references require allow-list and policy decisions before use.
- Client-side hidden controls are not authorization controls.
- Classroom projection telemetry must avoid unnecessary lesson or teacher content.

## 9. Minimum Test Scenarios

- Render every supported content type and safe fallback.
- Prove arbitrary HTML/script cannot execute.
- Prove teacher-private fields are absent from display payloads.
- Navigate and synchronize scene revisions.
- Exercise write, highlight, erase, undo, redo, and clear with mouse, touch, and stylus simulations.
- Reconcile stale and duplicated annotation commands.
- Preserve a safe projection during an update failure.
- Run accessibility checks for keyboard, focus, contrast, labels, and reduced motion.

## 10. Open Questions

- Which structured content blocks are required for the first two Mathematics topics?
- Are annotations stored as vector operations, snapshots, or another portable representation?
- What is the exact undo/redo scope across scene navigation?
- Which classroom display browsers and resolutions form the support baseline?

## 11. Definition of Done

- Scene and ink behavior satisfy functional, authorization, accessibility, and failure requirements.
- The structured model is versioned and contract-tested.
- Target classroom devices have evidence for input and rendering compatibility.

