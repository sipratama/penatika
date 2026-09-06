# ADR-0003 — Use Versioned Structured Classroom Content

| Field | Value |
|---|---|
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |

## Context

Penatika must render AI-assisted content consistently across teacher and classroom surfaces, support direct manipulation and digital ink, prevent private state leakage, validate Mathematics, and recover synchronized sessions. Arbitrary generated HTML or executable content cannot provide a safe, stable contract.

## Decision

Represent lessons, classroom scenes, elements, annotations, commands, and role-specific projections with versioned structured models. Only allow-listed element types and operations may be rendered or committed. AI output must be validated and transformed into this model before use.

## Consequences

### Positive

- Enables deterministic rendering, validation, testing, migration, and accessibility work.
- Reduces script injection and arbitrary-content risk.
- Supports revision-aware commands and safe projection filtering.
- Separates product semantics from any frontend framework.

### Negative

- Requires schema governance and compatibility rules.
- Limits AI output to supported capabilities.
- New teaching representations require explicit model evolution.

## Alternatives Considered

- **AI-generated HTML:** rejected because it creates execution, consistency, privacy, and portability risks.
- **Provider-specific rich-text payloads:** rejected because provider formats would define product semantics.
- **Canvas bitmap snapshots as the primary model:** rejected because they lose semantic structure and make validation/accessibility difficult.

## Revisit When

A new content type cannot be represented safely and the structured model has been evaluated for extension before considering another rendering boundary.

