# Penatika Contracts

This directory owns Penatika's authoritative machine-readable
cross-component wire definitions.

Implementation DTOs, generated clients/types, documentation renderings, and
framework classes are derived consumers. They must not become competing
sources of truth.

## Contract-First Workflow

```text
contract
→ compatibility review
→ implementation
→ conformance evidence
```

Prefer backward-compatible additive evolution. Breaking changes require
impact analysis, migration/coexistence strategy, an appropriate contract or
schema version change, and a consumer rollout plan.

## Contract Families

- `openapi/` — synchronous HTTPS/JSON application API using OpenAPI 3.1.x;
  current baseline OpenAPI 3.1.2.
- `schemas/` — justified reusable structured JSON wire schemas using JSON
  Schema Draft 2020-12.
- `asyncapi/` — inactive. ADR-0012 selects synchronous HTTP commands plus SSE
  projection push and keeps the SSE contract in OpenAPI/JSON Schema rather
  than activating AsyncAPI.

Identity semantics are selected by ADR-0011. Realtime transport, credential
carriage, and reconnect semantics are selected by ADR-0012: state-changing
commands remain synchronous HTTPS/JSON and authoritative role-specific
projection push uses SSE with `Last-Event-ID` as the resynchronization entry
point.

Field-level definitions are active under Contract Foundation. The first
vertical slice scope is maintained in the
[Contract Foundation working plan](../docs/04_engineering/CONTRACT_FOUNDATION_PLAN.md).
The authoritative [`openapi/openapi.yaml`](./openapi/openapi.yaml) root contains
shared HTTP primitives, the RFC 9457 Problem Details foundation, and the
minimum authenticated Teacher/session-start boundary. Further application
operation contracts are being added incrementally; no standalone production
JSON Schema currently exists.

## Ownership

Each wire structure has one canonical schema owner:

- HTTP-only structures may live in OpenAPI components.
- Independently reusable/lifecycle-managed structures live in `schemas/` and
  are referenced by other contracts.
- The same shape must not be independently redefined in multiple families.

## Sources of Truth

- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](../docs/02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0012 — Use Server-Sent Events for Realtime Push with Existing HTTP Commands](../docs/02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md)
- [System Architecture](../docs/02_architecture/SYSTEM_ARCHITECTURE.md)
- [API and Integration Standard](../docs/standards/06_API_INTEGRATION_STANDARD.md)
- [Project Status](../docs/PROJECT_STATUS.md)
