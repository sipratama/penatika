# ADR-0001 — Use a Modular Monolith for the Initial Backend

| Field | Value |
|---|---|
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |

## Context

Penatika requires tightly coordinated lesson, session, scene, AI orchestration, Mathematics assurance, curriculum, identity, and persistence behavior. No confirmed requirement exists for independent service deployment, team ownership, or isolated scaling. Distributed services would add network failure, consistency, deployment, and observability costs before product validation.

## Decision

Implement the initial backend as one deployable application with explicit internal modules and inward dependency direction. Modules own their data and rules and communicate through supported interfaces. Infrastructure and provider adapters remain replaceable boundaries.

## Consequences

### Positive

- Simplifies consistency for classroom sessions and lesson versions.
- Reduces operational overhead during MVP validation.
- Preserves module ownership and future extraction paths.
- Enables in-process interaction where immediate consistency is valuable.

### Negative

- Modules share one deployment and failure domain initially.
- Discipline is required to prevent arbitrary cross-module access.
- Independent scaling is deferred until evidence justifies extraction.

## Alternatives Considered

- **Microservices:** rejected for now because no confirmed independent scaling/deployment need offsets the added complexity.
- **Unstructured monolith:** rejected because session, assurance, AI, and curriculum trust boundaries require explicit ownership.

## Revisit When

A module has measured independent scaling, availability, deployment, data-ownership, or team-ownership requirements that cannot be met safely inside the modular monolith.

