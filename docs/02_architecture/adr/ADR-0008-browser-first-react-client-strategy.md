# ADR-0008 — Use Browser-First React Clients with Separate Teacher and Classroom Display Boundaries

| Field | Value |
|---|---|
| ADR | `ADR-0008` |
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-LESSON-001`, `CAP-SESSION-001`, `CAP-SESSION-002`, `CAP-CANVAS-001`, `CAP-INK-001`, `CAP-ADAPT-001`, `CAP-MATH-001` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

Penatika has three experience contexts with different visibility and
interaction needs: teacher lesson preparation, a private teacher controller,
and a student-facing classroom display. The product already requires backend
authority for session state, structured classroom content, teacher approval,
and graceful degradation.

The client technology decision needs to establish what kind of application
surfaces will be built without becoming a backend architecture decision.
This decision is required before source scaffolding.

## Decision

Penatika MVP uses a browser-first web client strategy:

- **React** for complex interactive application UI;
- **TypeScript** for contract/type discipline across structured state;
- **Vite** for a client-focused development and build tool.

Use the currently supported stable major lines at source scaffolding time.
The current decision baseline is React 19.x and Vite 8.x. Patch versions are
not hard-coded into architecture policy; security and support patches are
selected when dependencies are installed.

One frontend technology stack serves two explicit client application
boundaries:

1. **Teacher Web**
2. **Classroom Display Web**

They may live in one future frontend workspace and share safe reusable
packages, but they are distinct application surfaces. Workspace and source
directories are not created by this ADR.

## Client Application Boundaries

### Teacher Web

Teacher Web owns the teacher-private browser experience and contains two
experience modes:

#### Preparation

Target environment: laptop or PC browser.

Responsibilities:

- lesson preparation;
- lesson review/edit;
- curriculum provenance;
- assurance status;
- saved lesson selection;
- teacher-owned history where applicable.

#### Private Controller

Target environment: smartphone browser, and potentially another authorized
teacher surface where appropriate.

Responsibilities:

- session control;
- navigation;
- digital-ink controls;
- push-to-talk;
- private AI progress;
- proposal preview;
- approval or warned override;
- degraded/reconnecting status.

The controller experience must be responsive and optimized for touch and
one-handed use where practical.

Preparation and Controller are separate UX modes/routes within Teacher Web.
They do not require separate frontend frameworks or native applications.

### Classroom Display Web

Classroom Display is a separate browser application entry point and build
boundary for the student-facing classroom projection.

Responsibilities:

- render classroom-safe structured scenes;
- render accepted annotations;
- preserve the safe last-known projection where permitted by ADR-0007;
- reconnect and resynchronize against backend authority;
- support classroom-scale responsive presentation.

It must not:

- expose teacher-private AI state;
- contain teacher authorization behavior;
- issue teacher commands;
- own session authority;
- publish AI proposals;
- provide hidden teacher controls as a shortcut.

Backend role-specific projection remains the real security boundary. The
separate client build/entry boundary is defense-in-depth and reduces
accidental coupling between teacher-private UI and classroom-public UI.

## PWA Boundary

Teacher Web should be PWA-capable so the private controller may be installed
to a teacher smartphone home screen and launched in an app-like shell.

PWA capability means:

- web application manifest;
- HTTPS in deployed environments;
- installable/app-like browser experience where supported.

A service worker is not required for the initial client architecture.
Offline-first semantics are not introduced.

If a service worker is later introduced, it may cache safe static application
assets only under a separately reviewed implementation decision. It must
never:

- create local classroom authority;
- queue new authoritative classroom mutations;
- queue AI publications;
- replay new offline state changes;
- bypass ADR-0007.

PWA installability must remain distinct from offline authority.

## Shared Code Boundary

Future client applications may share safe code such as:

- visual design tokens;
- generic UI primitives;
- structured scene renderer;
- Mathematics display components;
- transport-neutral contract types once contracts exist;
- non-sensitive utility code.

Teacher-private feature/state modules must not become dependencies of the
Classroom Display merely for code reuse. Shared code must not weaken the
private/public projection boundary.

Exact workspace/package structure is deferred until source scaffolding.

## Backend Authority Boundary

For MVP, the frontend framework is a browser application layer.

Do not introduce Next.js Server Actions, frontend-owned business persistence,
or another frontend BFF as an additional product authority.

The Penatika Backend remains responsible for:

- authentication/authorization enforcement;
- authoritative session state;
- lesson persistence;
- AI orchestration;
- Mathematics assurance;
- curriculum authority;
- publication policy;
- data lifecycle.

Client code consumes explicit backend contracts. A future edge/BFF layer
requires separate architecture evidence.

Core Penatika application surfaces do not require SSR as an architecture
requirement. Preparation, Controller, and Classroom Display are application
experiences, not public content-discovery pages. A future public
marketing/SEO website is outside OAD-001 and may use a different delivery
approach without redefining the core application clients.

## Consequences

### Positive

- One web stack covers laptop, mobile-responsive controller, and classroom
  display experiences.
- Separate application entry/build boundaries reduce accidental
  private/public UI coupling.
- TypeScript improves structured contract and versioned scene/proposal/session
  type discipline.
- Vite avoids requiring a second frontend application server or BFF.
- No native application pipeline is required for MVP.

### Negative / Cost

- Browser capabilities must be validated for push-to-talk, touch/stylus,
  controller ergonomics, lifecycle behavior, and supported school devices.
- Shared client packages require explicit boundary discipline so private
  feature/state code does not leak into the classroom display.
- The team must later select exact runtime, package manager, workspace layout,
  routing, state management, styling, testing, and rendering libraries.

### Risks

- A future school-device matrix may reveal a browser limitation that would
  justify a native or hybrid client; that would require a new ADR and delivery
  pipeline decision.

## Alternatives Considered

### Next.js App Router

Rejected for the core MVP application baseline because:

- SSR/SEO is not required for core surfaces;
- Server Actions/BFF behavior would introduce an unnecessary additional
  application boundary;
- the existing Penatika backend already owns authoritative application
  behavior.

Next.js remains a valid framework generally and could be reconsidered for a
future public site or if new requirements justify server-rendered frontend
behavior.

### Native / React Native / Flutter Controller

Rejected for MVP because:

- browser-first satisfies the current hardware-agnostic requirement;
- native delivery creates a second platform/release pipeline;
- no current evidence requires native APIs strongly enough.

### One Undifferentiated SPA/Bundle for All Surfaces

Rejected as the preferred architecture because:

- Teacher and Classroom Display cross a meaningful privacy boundary;
- explicit application entry separation improves defense-in-depth;
- it reduces accidental private/public UI coupling.

### Three Fully Independent Applications

Rejected for MVP because:

- Preparation and Controller are both teacher-private;
- unnecessary duplication and deployment complexity;
- they can safely share one responsive Teacher Web application.

## Follow-Up Decisions

Before or during source scaffolding, choose only after separate evidence or
product need:

- package manager and exact runtime version;
- workspace/monorepo tooling;
- React Router mode/package;
- client state-management and query/cache library;
- component/UI and CSS/styling approach;
- Mathematics renderer;
- client testing libraries;
- WebSocket/SSE implementation;
- authentication library;
- PWA service-worker library;
- hosting/deployment.

OAD-002 through OAD-012 remain open and are not resolved by this ADR.

## Related Requirements / ADRs

- [Product Requirements Document](../../00_product/PRD.md)
- [UX Flows](../../03_design/UX_FLOWS.md)
- [Design System](../../03_design/DESIGN_SYSTEM.md)
- [System Architecture](../SYSTEM_ARCHITECTURE.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0003 — Use Versioned Structured Classroom Content](./ADR-0003-structured-classroom-content.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-06` | Accepted | Adopt browser-first React client strategy with separate Teacher Web and Classroom Display Web boundaries |
