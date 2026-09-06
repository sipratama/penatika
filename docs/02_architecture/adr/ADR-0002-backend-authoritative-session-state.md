# ADR-0002 — Keep Classroom Session State Backend-Authoritative

| Field | Value |
|---|---|
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |

## Context

Teacher preparation, smartphone controller, and classroom display may run on different networks and reconnect independently. Screen mirroring and shared-Wi-Fi dependence are explicitly excluded. Client-owned authority would create conflicting state, weak authorization, privacy leakage, and unreliable recovery.

## Decision

The backend owns classroom session lifecycle, participant roles, current authoritative revision, accepted scene state, and save outcome. Clients submit authorized, revision-aware commands and render role-specific projections. Reconnection reconciles against backend state.

## Consequences

### Positive

- Gives one consistency and authorization authority.
- Supports devices on different networks.
- Enables explicit private and classroom-safe projections.
- Makes stale, duplicate, and replayed operations detectable.

### Negative

- Core synchronized behavior depends on backend reachability.
- Realtime transport, revision semantics, and degraded mode require careful design.
- Offline authority is intentionally limited and remains a product decision.

## Alternatives Considered

- **Screen mirroring:** rejected because it exposes private teacher state and conflicts with product constraints.
- **Peer-to-peer or local-network authority:** rejected for MVP because shared network availability is not guaranteed and authorization/recovery become more complex.
- **Client-authoritative state with later upload:** rejected because concurrent surfaces could diverge during active teaching.

## Revisit When

Confirmed classroom environments require offline-first operation beyond safe cached projection and a product decision defines conflict and authority rules.

