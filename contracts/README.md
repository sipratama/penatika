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
- `asyncapi/` — not active. AsyncAPI 3.1.x is conditional on OAD-005 selecting
  a realtime/message transport for which AsyncAPI provides useful semantics.

Field-level contracts are intentionally not created yet. Identity, realtime,
and other dependent semantics still require architecture decisions.

## Ownership

Each wire structure has one canonical schema owner:

- HTTP-only structures may live in OpenAPI components.
- Independently reusable/lifecycle-managed structures live in `schemas/` and
  are referenced by other contracts.
- The same shape must not be independently redefined in multiple families.

## Sources of Truth

- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
- [System Architecture](../docs/02_architecture/SYSTEM_ARCHITECTURE.md)
- [API and Integration Standard](../docs/standards/06_API_INTEGRATION_STANDARD.md)
- [Project Status](../docs/PROJECT_STATUS.md)

