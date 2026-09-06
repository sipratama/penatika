# Design System — Penatika

> Defines initial visual and interaction principles for teacher-private and classroom-facing surfaces. Brand identity and implementation technology are not yet selected.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft foundation |
| Version | `0.1` |
| Last Updated | `2026-09-06` |

## 1. Design Principles

### Teacher Control Is Visible

The interface clearly shows when AI is listening, generating, validating, proposing, accepted, rejected, or failed. Material classroom changes are not hidden automation.

### Classroom Content Is Calm and Focused

Student-facing presentation prioritizes lesson content, readability, and stable layout over application chrome.

### Private and Public Surfaces Are Distinct

Teacher controls and warnings use a private surface model. Visual similarity must not cause private content to be projected accidentally.

### Input Methods Are Equal Citizens

Mouse, touch, stylus, and keyboard interactions use consistent semantics and do not assume proprietary hardware.

### Trust States Are Honest

Validated, invalid, unsupported, inconclusive, ungrounded, and failed content states use distinct labels and do not imply certainty through decorative confidence indicators.

## 2. Surface Roles

### Preparation Surface

- information-dense enough for review and editing;
- supports provenance and assurance details;
- separates source material, generated proposal, and teacher-authored changes.

### Private Controller Surface

- prioritizes fast session control and one-handed use where practical;
- shows connection, AI, and validation states;
- avoids small critical targets or deep navigation during teaching.

### Classroom Display Surface

- maximizes legibility at distance;
- avoids private controls, diagnostics, and distracting progress indicators;
- maintains a safe visual state during recoverable failure.

## 3. Semantic State Vocabulary

The design system must provide tokens and components for:

- neutral/inactive;
- active/live;
- draft;
- generating/processing;
- proposed;
- accepted/ready;
- validated;
- warning;
- invalid/error;
- unsupported/inconclusive;
- degraded/offline/reconnecting;
- private/teacher-only.

Exact colors are not selected. Color must not be the only state indicator.

## 4. Typography and Mathematics

- Classroom typography must be tested at realistic viewing distance.
- Preparation and controller typography must remain readable under dense status information.
- Mathematical notation requires a rendering approach that supports clear layout and accessible semantics.
- Font families, scale, line lengths, and notation renderer remain Open Design/Architecture Decisions.

## 5. Layout and Spacing

- Classroom layout favors stable composition and large readable content regions.
- Controller layout favors reachable primary actions and clear destructive-action separation.
- Preparation layout supports comparison and editing without hiding assurance states.
- Responsive behavior must preserve semantic hierarchy instead of only shrinking desktop layout.

Exact breakpoints and token values follow target-device selection.

## 6. Initial Component Families

The first implementation is expected to need:

- lesson intent form;
- lesson section editor;
- curriculum provenance indicator;
- assurance status and warning panel;
- session status and participant indicator;
- pairing affordance;
- scene navigation controls;
- push-to-talk control and listening state;
- AI proposal preview and decision controls;
- structured classroom content renderer;
- ink tool palette;
- connection/degraded-state banner;
- destructive confirmation and recoverable undo feedback.

Components must not embed authorization assumptions that belong to the backend.

## 7. Digital Ink Interaction

- Tool state must remain visible to the teacher.
- Write, highlight, and erase must have distinguishable cursors or affordances.
- Undo/redo scope must be understandable.
- Clear is visually separated from frequent actions.
- Touch gestures and stylus strokes must not conflict without a defined rule.
- Ink feedback and committed synchronization state may be distinct but must not mislead the teacher.

## 8. Motion

- Use motion to explain state change, not to decorate the classroom display.
- Long-running progress must remain understandable without continuous distracting animation.
- Support reduced-motion preferences.
- Avoid motion that shifts lesson content unexpectedly during teaching.

## 9. Accessibility Requirements

- Visible focus and logical keyboard order.
- Programmatic names for controls and state messages.
- Sufficient text and non-text contrast.
- Touch targets appropriate to the selected baseline.
- Error and assurance state conveyed with text/icon semantics in addition to color.
- No essential interaction dependent exclusively on hover, pressure, or gesture.
- Classroom projection remains understandable for students who cannot perceive color differences.

Formal conformance target remains open but must be chosen before pilot readiness.

## 10. Content and Language

- Initial product language is Indonesian.
- Teacher-facing copy should be concise, actionable, and avoid anthropomorphizing AI authority.
- Validation copy distinguishes wrong, unsupported, inconclusive, and system failure.
- Classroom copy must be appropriate for the selected grade context.
- Final glossary and localization strategy remain open.

## 11. Open Design Decisions

- Brand identity, logo, palette, and typography.
- Component implementation framework.
- Mathematics notation renderer.
- Exact target-device breakpoints and touch-target baseline.
- Scene navigation and content-block visual grammar.
- Controller information density and adaptation approval pattern.

## 12. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.1` | `2026-09-06` | Initial design principles and component scope | Codex |
