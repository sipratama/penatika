# ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries

| Field | Value |
|---|---|
| ADR | `ADR-0010` |
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-SESSION-001`, `CAP-SESSION-002`, `CAP-CANVAS-001`, `CAP-INK-001`, `CAP-ADAPT-001`, `CAP-MATH-001` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

Penatika's browser-first React clients and authoritative Java/Spring Boot
backend require stable, language-neutral cross-component wire definitions.
Existing architecture decisions require revision-aware commands,
role-specific projections, structured classroom content, bounded AI proposals,
teacher-authorized publication, assurance/provenance linkage, and deterministic
recovery behavior.

Those interfaces must not be defined independently by TypeScript types, Java
DTOs, Spring annotations, generated code, or reverse-engineered
implementation behavior. OAD-012 must establish the contract families,
ownership, compatibility, and tooling strategy before application source
scaffolding while leaving identity, realtime, and field-level semantics open.

## Decision

Penatika adopts contract-first cross-boundary design:

```text
contract
→ compatibility review
→ implementation
→ conformance evidence
```

Machine-readable contract files are authoritative for cross-component wire
interfaces. Generated artifacts, handwritten DTOs, documentation renderings,
client types, and implementation classes are derived consumers.

Penatika uses three contract families:

1. OpenAPI 3.1.x for synchronous HTTP application APIs.
2. JSON Schema Draft 2020-12 for justified reusable structured JSON wire
   schemas.
3. AsyncAPI 3.1.x conditionally, only if OAD-005 selects a realtime/message
   interface where AsyncAPI provides useful semantics.

No field-level API or schema contract is defined by this ADR.

## Contract Ownership

Machine-readable contract files own cross-component transport shapes and
semantics. They do not own domain authority; the Penatika Backend remains the
authoritative application boundary.

Use one canonical schema owner for each wire model:

- a structure used only inside one HTTP contract may remain in OpenAPI
  components;
- a structure reused across contracts/transports, independently consumed, or
  independently versioned should live as standalone JSON Schema;
- OpenAPI or a future AsyncAPI contract should reference the standalone schema
  instead of redefining it.

Contracts use stable domain terminology and `camelCase` JSON property names.
They must not leak Java class names, database columns, Spring concepts, React
component names, or provider SDK types. Identifiers are opaque unless the
contract explicitly defines additional semantics.

## Synchronous HTTP Contract

The default synchronous application boundary is HTTPS + JSON + OpenAPI 3.1.x
between Teacher Web, Classroom Display Web, and the Penatika Backend.

Use HTTP semantics deliberately. Prefer resource-oriented operations where
natural and explicit action/command operations where meaningful domain
commands cannot be represented honestly as CRUD. This ADR does not invent
endpoint paths.

GraphQL, gRPC, tRPC, frontend RPC frameworks, and proprietary JSON-RPC are not
part of the MVP baseline. A future protocol change requires architecture
evidence.

Wire timestamps use RFC 3339-compatible `date-time` representations. Backend
machine timestamps use UTC unless a domain requirement explicitly preserves
another timezone context.

## OpenAPI Baseline

Penatika uses OpenAPI 3.1.x, with OpenAPI 3.1.2 as the current baseline.
OpenAPI 3.2 is not the initial baseline because Penatika does not need a
3.2-specific capability and relevant validation, documentation, diff, and
generation tooling support remains uneven. OpenAPI 3.1 provides stronger
current interoperability and aligns with JSON Schema Draft 2020-12.

A future OpenAPI 3.2 migration requires explicit tooling evidence and is not
automatic.

The preferred authoring format is YAML 1.2 and the contract entry document is
named `openapi.yaml`. Multi-file decomposition may be introduced when contract
size justifies it; the entry document remains authoritative.

OpenAPI 3.1 and JSON Schema semantics must distinguish absent, present-null,
empty string, and empty array/object states. Penatika does not use OpenAPI
3.0's legacy `nullable` keyword as its baseline. Fields must not become
nullable merely to avoid modeling lifecycle states.

OpenAPI `info.version` identifies the contract description version. Penatika
does not introduce `/v1` or another major API version mechanism until
incompatible contract generations genuinely need to coexist.

## JSON Schema Baseline

Standalone structured schemas use JSON Schema Draft 2020-12 in JSON files.
Each schema must explicitly identify the dialect through `$schema`.

Standalone schemas are appropriate when a structure:

- is reused by multiple contracts/transports;
- has an independent compatibility lifecycle;
- is safety-critical enough to deserve an explicit schema boundary;
- is consumed independently of one HTTP operation.

Possible future candidates include structured classroom scenes, content
element unions, role-specific projections, command envelopes, AI proposals,
Mathematics assurance results, and curriculum provenance. These are candidate
boundaries only; this ADR defines none of their fields.

Safety-critical classroom and AI structures may intentionally use closed,
discriminated, allow-listed schemas that reject unknown element types or
unsupported fields. Ordinary clients should tolerate compatible additive
response fields where safe. Unknown-field behavior must be explicit per
schema rather than imposed globally.

## Error Contract

HTTP APIs use RFC 9457 Problem Details with media type
`application/problem+json`.

Standard Problem Details members retain their RFC semantics. Penatika may add
documented extensions such as `code`, `correlationId`, and bounded
`fieldErrors` where required.

- `code` is machine-stable.
- Clients must not parse human-readable `title` or `detail` for application
  logic.
- Stack traces, Java exceptions, SQL details, provider internals, secrets, and
  private diagnostics must not enter public errors.
- Field validation details must be bounded and safe.

The exact problem-type URI strategy is deferred to field-level API design; no
public domain or URL is invented by this ADR.

## Realtime / AsyncAPI Boundary

OAD-005 remains open and continues to decide the realtime transport and
reconnect protocol. This ADR does not select WebSocket, SSE, STOMP, polling,
Socket.IO, MQTT, or a broker protocol.

Do not create an AsyncAPI document or directory yet. If OAD-005 selects a
meaningful message-oriented/realtime interface, AsyncAPI 3.1.x is the default
machine-readable contract family unless OAD-005 documents a reason to use
another representation. Payloads should reuse standalone Draft 2020-12 JSON
Schemas where that prevents duplicate ownership.

Future session command and projection contracts must preserve the existing
authorization, command identity/idempotency where required, authoritative
session identity, revision, stale/conflict rejection, reconciliation, and
operational correlation concepts without this ADR inventing field names.

## Compatibility and Evolution

Prefer backward-compatible additive evolution. Generally compatible changes
include adding optional fields, operations, or response members without
changing existing semantics.

Potentially breaking changes include:

- removing or renaming fields;
- changing field types or null semantics;
- making optional fields required;
- changing status/error semantics;
- narrowing accepted values;
- changing authorization assumptions;
- changing command/revision semantics.

Breaking changes require explicit impact analysis, migration or coexistence
strategy, appropriate contract/schema version change, and consumer rollout
plan. Browser and backend deployments must not be assumed atomic.

## Code Generation Policy

Generated artifacts are derived and never canonical contracts. Code
generation may later produce TypeScript transport types/client adapters, Java
transport DTOs/interfaces, or documentation.

- Generated classes must not become domain models.
- Generated persistence entities are prohibited.
- Provider SDK types must not leak into Penatika contracts.
- Generation must not force Spring MVC versus WebFlux before OAD-005.
- Generated output must be reproducible from committed contracts and
  configuration.

This ADR does not select OpenAPI Generator or another mandatory generator.
Generator compatibility changes independently from the specifications, so the
exact generator and configuration must be selected and tested during
contract/source scaffolding.

## Contract Tooling

Redocly CLI is the baseline OpenAPI authoring tool for structural/spec
validation, linting, reference resolution, and bundling. Architecture policy
does not pin a Redocly patch version; repository tooling/CI will pin an exact
tested version when configured.

Standalone schemas require a standards-compliant Draft 2020-12 validator.
Ajv 8.x is the initial contract-tooling reference validator. It is not thereby
a required React or Java application runtime dependency.

If AsyncAPI becomes active after OAD-005, use a specification-compatible tool
such as the official AsyncAPI CLI and/or the selected contract linting tool.
No tooling is installed or configured by this ADR.

Once implementations exist, contract evidence must include:

1. contract lint/schema validity;
2. examples validating against their schemas;
3. backend provider conformance;
4. frontend transport adapter conformance;
5. structural rejection of invalid safety-critical payloads;
6. breaking-change detection and review before merge.

Schema validity does not replace feature, integration, security, or end-to-end
tests. Contract-diff tooling and CI platform remain future tooling decisions.

## Repository Layout

Activate the following documentation-only contract boundary:

```text
contracts/
  README.md
  openapi/
    README.md
  schemas/
    README.md
```

`contracts/openapi/` owns synchronous HTTP definitions when created.
`contracts/schemas/` owns justified reusable wire schemas. An
`contracts/asyncapi/` directory activates only if OAD-005 requires it.

No `openapi.yaml`, standalone schema, AsyncAPI document, generated artifact,
or application implementation is created by this ADR.

## Consequences

### Positive

- Establishes one language-neutral source of truth across React and Java.
- Makes compatibility review precede implementation.
- Aligns synchronous HTTP and reusable structures through modern JSON Schema
  semantics.
- Supports generated consumers without making generated code authoritative.
- Gives safety-critical classroom and AI payloads explicit strict-schema
  options.
- Establishes conformance and breaking-change evidence expectations.

### Negative / Costs

- Contract changes require deliberate compatibility review and synchronized
  consumer/provider work.
- Authors must maintain references and avoid duplicate schema ownership.
- Tooling, validation, examples, conformance tests, and diff review add setup
  and CI cost.
- Identity, realtime, and domain field decisions still block complete
  contracts and source scaffolding.
- OpenAPI/JSON Schema tooling interoperability must be tested as versions
  evolve.

## Alternatives Considered

### OpenAPI 3.2 as the Immediate Baseline

OpenAPI 3.2 is a valid newer specification. It is not selected initially
because current Penatika requirements do not require a 3.2-only feature,
relevant generation/documentation tooling support remains uneven, and 3.1
provides stronger present interoperability. Revisit after tooling evidence
improves.

### OpenAPI 3.0

OpenAPI 3.0 has broad tooling maturity. It is not selected because OpenAPI 3.1
aligns substantially better with modern JSON Schema semantics, reusable
structured schemas, and explicit type/nullability modeling.

### GraphQL

GraphQL provides flexible client query composition. It is not selected because
Penatika's initial workflows are command/state oriented, no current requirement
needs client-defined graph queries, and it would add schema/runtime complexity
without product evidence.

### gRPC / Protobuf

gRPC and Protobuf provide strong generated contracts and binary transport.
They are not selected for the browser-facing MVP boundary because browser
clients would require additional transport/tooling complexity, human-readable
JSON better supports initial integration/debugging, and no performance evidence
requires binary RPC.

### TypeScript Types as Canonical Contracts

Rejected because the backend is Java, the boundary must remain
language-neutral, and TypeScript compile-time types do not independently define
HTTP semantics or runtime validation.

### Java DTO Classes as Canonical Contracts

Rejected because they would make one implementation language/framework the
source of truth and force browser clients to depend on generated
interpretation.

### Code-First OpenAPI from Spring Annotations

Convenient for documentation generation, but rejected as the canonical
strategy because it lets implementation silently define the contract and
reverses the required contract-first workflow. Generated implementation
documentation may be used as a conformance comparison only.

## Deferred Field-Level Decisions

This ADR intentionally does not define:

- endpoint paths, operations, request/response fields, pagination, or
  idempotency headers;
- the OpenAPI security scheme, OAuth/OIDC flow, JWT/cookie model, or pairing
  credentials pending OAD-004/OAD-005;
- realtime transport, bindings, channels, or messages pending OAD-005;
- database/persistence representation pending OAD-003;
- provider payloads or SDK representations pending OAD-006/OAD-007;
- exact classroom scene, command, projection, proposal, assurance, or
  provenance schema fields;
- Spring MVC versus WebFlux, controller implementation, frontend HTTP client,
  runtime JSON Schema libraries, code generator, build tool, package manager,
  CI provider, or contract-diff tool.

Provider-specific contracts remain external integration evidence. Penatika
adapters normalize only the required provider subset into Penatika-owned
application ports/contracts.

## Follow-Up Decisions

1. Resolve OAD-004 — Identity, Authentication, and Account Model before final
   protected HTTP security definitions and realtime participant authorization.
2. Resolve OAD-005 before activating AsyncAPI or defining realtime bindings.
3. Define field-level OpenAPI and standalone schemas only after their dependent
   semantics are resolved.
4. Configure and pin tested contract tooling during contract/source
   scaffolding.
5. Add lint, example validation, conformance, and breaking-change checks when
   implementations and CI exist.

## Related Requirements / ADRs

- [API and Integration Standard](../../standards/06_API_INTEGRATION_STANDARD.md)
- [Product Requirements Document](../../00_product/PRD.md)
- [Classroom Session](../../01_features/classroom-session.md)
- [Classroom Canvas](../../01_features/classroom-canvas.md)
- [Live AI Adaptation](../../01_features/live-ai-adaptation.md)
- [Mathematics Assurance](../../01_features/mathematics-assurance.md)
- [System Architecture](../SYSTEM_ARCHITECTURE.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0003 — Use Versioned Structured Classroom Content](./ADR-0003-structured-classroom-content.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0008 — Use Browser-First React Clients with Separate Teacher and Classroom Display Boundaries](./ADR-0008-browser-first-react-client-strategy.md)
- [ADR-0009 — Use Java 25 LTS and Spring Boot for the Authoritative Backend](./ADR-0009-java-spring-boot-backend.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-06` | Accepted | Adopt contract-first OpenAPI 3.1.x and JSON Schema Draft 2020-12 boundaries |
