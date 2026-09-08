# ADR-0012 — Use Server-Sent Events for Realtime Push with Existing HTTP Commands

| Field | Value |
|---|---|
| ADR | `ADR-0012` |
| Status | Accepted |
| Date | `2026-09-07` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-SESSION-001`, `CAP-SESSION-002`, `FR-SESSION-006`–`FR-SESSION-008`, `FR-SESSION-011`–`FR-SESSION-017` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

[ADR-0002](./ADR-0002-backend-authoritative-session-state.md) makes the backend
the sole owner of classroom session lifecycle, revision, and accepted state.
[ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md)
already defines the degradation and reconciliation policy: clients may
preserve a last-known safe projection, must freeze new authoritative
mutations while backend authority is unreachable, and must reconcile against
backend-authoritative revision before mutation resumes; newly created offline
commands are never auto-replayed. [ADR-0010](./ADR-0010-contract-first-openapi-json-schema.md)
established contract-first OpenAPI/JSON Schema for the synchronous HTTP
boundary and left realtime transport and AsyncAPI activation open, pending
this decision. [ADR-0011](./ADR-0011-oidc-backend-managed-browser-sessions.md)
already defines who participants are: an authenticated `TeacherAccount`
browser session plus an active `TEACHER_CONTROLLER` participant binding for
the controller, and a separate `CLASSROOM_DISPLAY` participant session for
the display. That ADR explicitly leaves "how authenticated sessions are
carried into realtime communication" to OAD-005.

OAD-005 must therefore select a realtime transport and reconnect protocol
that:

- reuses the existing participant identity and credential model rather than
  inventing a new realtime authentication scheme;
- preserves backend authority and the existing reconciliation/no-replay
  policy from ADR-0007;
- does not require broker or queue infrastructure, since OAD-011 remains
  conditional and unresolved;
- does not force a Spring MVC vs. WebFlux choice or other implementation
  commitment beyond what the transport genuinely requires;
- decides whether AsyncAPI activates, per ADR-0010.

Two roles need realtime behavior: the `TEACHER_CONTROLLER`, which submits
authorized commands and receives a private projection (proposal preview, AI
progress, warnings), and `CLASSROOM_DISPLAY`, which only ever receives a
classroom-safe projection and never issues commands (already true per
ADR-0002/ADR-0011). No product requirement currently establishes that
controller-originated commands need sub-HTTP-round-trip latency; `SYSTEM_ARCHITECTURE.md`
§9 explicitly states latency objectives remain unmeasured pending prototype
evidence.

## Decision

Penatika separates the command direction from the push direction instead of
introducing one new bidirectional protocol:

- **Commands stay on the existing HTTP boundary.** Teacher-initiated,
  revision-aware, state-changing commands continue to use the synchronous
  HTTPS/JSON OpenAPI contract from ADR-0010. This ADR introduces no new
  command transport.
- **Push uses Server-Sent Events (SSE).** The backend pushes
  authoritative revision/projection updates to the Controller and to the
  Classroom Display over one `text/event-stream` connection per authorized
  participant session.
- **SSE connections are authorized exactly like other protected requests.**
  The browser's existing credential — the backend-managed `TeacherAccount`
  session cookie for the controller, the `CLASSROOM_DISPLAY` participant
  session cookie for the display — is sent automatically by `EventSource`
  for same-origin requests (or with `withCredentials` for an explicitly
  trusted cross-origin deployment per ADR-0011's cookie/CORS rules). No new
  realtime-specific token, ticket, or bearer credential is introduced.
- **Reconnect reuses the SSE `Last-Event-ID` mechanism as the resync entry
  point.** Each pushed event's `id` carries the Classroom Session's
  authoritative revision (or a monotonic sequence bound to it). On the
  browser's native SSE reconnect, the backend receives the client's
  last-observed revision via `Last-Event-ID` and either emits a bounded
  catch-up delta or instructs the client to retrieve a full authoritative
  snapshot over the existing HTTP boundary before push resumes. This is a
  mechanical convenience, not a policy change: it must still satisfy
  `INV-017`–`INV-020` — no authoritative mutation resumes until the client
  reconciles against backend-authoritative state, and no newly created
  offline command is replayed automatically.
- **Projections stay separated per role.** Each SSE connection carries only
  the projection its role is authorized to see (controller-private vs.
  classroom-safe). The backend must not multiplex both projections onto one
  shared stream.
- **Heartbeats detect silent connection death.** Periodic SSE keep-alive
  comments distinguish a dead connection from an idle-but-healthy one,
  feeding the existing `RECONNECTING` / `DISPLAY_DISCONNECTED` /
  `CONTROLLER_DISCONNECTED` overlays already defined in
  [classroom-session.md](../../01_features/classroom-session.md).
- **AsyncAPI stays inactive.** The push channel is a single logical
  one-directional stream per role with a small, bounded set of event types.
  It is documented as an operation in the existing OpenAPI contract
  (`text/event-stream` response content, referencing standalone JSON Schema
  event-payload definitions per ADR-0010) rather than by activating a
  separate AsyncAPI contract family. AsyncAPI remains available later if a
  genuinely topic/broker-shaped messaging need emerges with evidence.
- **No broker, queue, or STOMP topic model is introduced.** OAD-011 remains
  conditional and unresolved.
- **No transport-forced reactive stack.** Spring MVC's existing
  synchronous/async request handling is sufficient to stream
  `text/event-stream` responses. This ADR does not require adopting Spring
  WebFlux; a reactive stack remains available later if evidence requires it.

This ADR does not define endpoint paths, exact event names/payload fields,
heartbeat intervals, or resync delta format — those remain field-level
contract decisions. No `openapi.yaml`, standalone JSON Schema, or AsyncAPI
document is created by this ADR.

## Consequences

### Positive

- No new authentication/authorization model; reuses ADR-0011 exactly as
  written.
- No new contract-tooling family; extends the existing OpenAPI/JSON Schema
  boundary from ADR-0010 instead of standing up AsyncAPI tooling.
- Reconnect leverages a browser-native mechanism (`Last-Event-ID`) instead of
  a bespoke resume protocol.
- The backend remains the sole broadcaster of authoritative projections;
  clients cannot mistake a push message for mutation permission.
- No broker/queue infrastructure is added, consistent with OAD-011 remaining
  conditional.

### Negative / Cost

- SSE is push-only. If a controller-originated interaction (for example,
  digital ink streaming) later proves to need sub-request-round-trip
  latency, per-action REST commands may be too chatty for that specific
  interaction class, and this decision would need revisiting for it. NFR
  latency targets remain unmeasured (`SYSTEM_ARCHITECTURE.md` §9).
- Long-lived SSE connections must be validated against target-school network
  conditions (proxies, corporate/school network intermediaries), which is
  already an open follow-up from ADR-0007.
- Because controller-private and classroom-safe projections cannot share one
  stream, a classroom session normally holds two concurrent SSE connections
  rather than one multiplexed channel; this is a deliberate confidentiality
  boundary, not an oversight.
- Browsers limit concurrent HTTP/1.1 connections per origin; deployment
  (OAD-010) must ensure HTTP/2 or equivalent multiplexing so simultaneous SSE
  streams do not starve other requests.

## Alternatives Considered

### Bidirectional WebSocket for both directions

A single persistent socket per participant could carry both commands and
push. Session-cookie auth still works at the WebSocket handshake. Rejected
as the default because it requires more custom framing, heartbeat, and
reconnect logic than SSE for no currently-evidenced latency benefit, and it
would reopen the Spring MVC vs. WebFlux question without justification. It
remains the leading alternative if controller-originated low-latency
streaming (e.g., ink) is later evidenced as required.

### STOMP over WebSocket (topic-based pub/sub)

Provides richer topic/subscription semantics. Rejected because it adds
framework machinery and pushes toward message-broker-shaped thinking that
OAD-011 explicitly says not to introduce without evidence; the MVP's two
fixed per-session projection streams do not need topic routing.

### Long-polling

Rejected because it adds latency and server load relative to SSE with no
offsetting simplicity benefit; SSE has equivalent or better browser support
for this one-directional use case and includes native reconnect semantics
long-polling would have to reimplement.

### Activating AsyncAPI immediately

Rejected for now because the selected channel shape (one bounded
one-directional stream per role) is adequately described as an OpenAPI
`text/event-stream` operation referencing standalone JSON Schemas; a second
contract-tooling family is not justified until a genuinely message/topic
-oriented need appears.

## Architecture Invariants Introduced

- `INV-029`: Realtime push connections (for example, SSE) transmit
  projections only; state-changing commands must use the authorized
  synchronous HTTP command boundary, never the push channel.
- `INV-030`: A pushed event's revision/sequence identifier is advisory for
  reconnect resync only; only backend-authoritative revision retrieval and
  reconciliation determines whether mutation may resume.

## Deferred Implementation Decisions

- Exact SSE endpoint paths, event names, and payload fields.
- Heartbeat interval and dead-connection timeout values.
- Exact catch-up-delta vs. force-full-resync decision rule on reconnect.
- Standalone JSON Schema(s) for push event payloads and their relationship to
  existing/future role-specific projection schemas.
- Load balancer / reverse proxy configuration for long-lived `text/event-stream`
  connections (OAD-010).
- Whether and how the controller's own uncertain-acknowledgement reconciliation
  (ADR-0007) surfaces through the same SSE channel or a dedicated HTTP query.

## Revisit When

- Prototype or pilot evidence shows a controller-originated interaction (for
  example, digital ink) needs sub-REST-round-trip latency.
- Target-school network evidence shows long-lived SSE connections are
  unreliable through representative intermediaries.
- A genuine multi-consumer, topic-oriented messaging need emerges that would
  justify AsyncAPI activation or broker infrastructure (OAD-011).

## Related Requirements / ADRs

- [Classroom Session Feature](../../01_features/classroom-session.md)
- [Threat Model](../../04_engineering/THREAT_MODEL.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0009 — Use Java and Spring Boot for the Backend](./ADR-0009-java-spring-boot-backend.md)
- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](./ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./ADR-0011-oidc-backend-managed-browser-sessions.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-07` | Accepted | Adopt SSE push with existing HTTP commands for OAD-005, reusing ADR-0011 identity and ADR-0007 reconciliation policy |
