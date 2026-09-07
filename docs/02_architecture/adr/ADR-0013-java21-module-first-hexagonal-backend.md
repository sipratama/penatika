# ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture

| Field | Value |
|---|---|
| ADR | `ADR-0013` |
| Status | Accepted |
| Date | `2026-09-07` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-LESSON-001`, `CAP-SESSION-001`, `CAP-SESSION-002`, `CAP-CANVAS-001`, `CAP-INK-001`, `CAP-ADAPT-001`, `CAP-MATH-001` |
| Supersedes | [ADR-0009](./ADR-0009-java-spring-boot-backend.md) |
| Superseded By | N/A |

## Context

[ADR-0009](./ADR-0009-java-spring-boot-backend.md) selected Java 25 LTS and
Spring Boot 4.x for the authoritative backend, reinforcing the modular
monolith established by [ADR-0001](./ADR-0001-modular-monolith-backend.md).
That decision remains valid in spirit: a strongly typed, long-lived JVM
platform with a mature Spring ecosystem is the right foundation for
Penatika's stateful domain workflows, revision semantics, authorization, and
assurance orchestration.

Two refinements are needed before OAD-003 and source scaffolding:

1. **Java runtime baseline.** Java 25 LTS is a valid newer LTS, but Penatika
   does not currently require any Java 25-specific language or runtime
   capability. Selecting a newer LTS than necessary adds toolchain/ecosystem
   movement without a corresponding product benefit.
2. **Internal backend organization.** ADR-0001 and ADR-0009 already imply
   ports/adapters-style dependency direction (domain does not depend on
   transport, persistence, or provider SDKs) and already reject one global
   horizontal `controller` / `service` / `repository` / `entity` structure.
   However, neither ADR makes the organizing principle explicit: business
   capability/module boundaries come first, and Hexagonal (ports/adapters)
   structure applies locally inside each module that is complex enough to
   benefit from it. This ADR makes that principle an explicit, binding rule
   before source scaffolding begins, so early implementation does not default
   to an unstructured monolith or a global technical-layer structure.

This ADR also defines how ECC (an optional AI coding-assistant skill system)
may be used during backend and frontend implementation without silently
overriding Penatika's own architecture and product decisions.

This ADR does not resolve OAD-003 (database/migration technology) and does
not select any persistence technology, JDK distribution, AI/speech provider,
or deployment platform.

## Decision

Penatika will implement its authoritative backend as one deployable modular
monolith using:

- **Java 21 LTS** as the language/runtime baseline;
- **Spring Boot 4.x** as the backend application framework (unchanged from
  ADR-0009);
- **module-first Hexagonal Architecture**: business-capability modules are
  the primary organizing boundary, and each module that is meaningfully
  complex applies Hexagonal (ports/adapters) structure internally.

This ADR supersedes ADR-0009. It does not introduce microservices, does not
change the modular monolith decision (ADR-0001), and does not select
persistence, identity provider, realtime, AI, speech, or deployment
technology.

## Java Runtime Baseline

Java 21 LTS is selected because:

- it is an LTS release with a mature ecosystem and broad production adoption;
- it already provides the modern Java language/runtime capabilities Penatika
  needs (records, sealed types, pattern matching, virtual threads, and a
  mature concurrency/runtime platform);
- Spring Boot 4.x supports Java 21;
- Penatika does not currently require any Java 25-specific language or
  runtime feature;
- using Java 21 reduces unnecessary runtime/toolchain movement while
  retaining a supported, modern LTS baseline.

Java 25 is a valid newer LTS; it is simply not the selected Penatika
baseline. This ADR does not describe Java 25 as unsuitable.

No JDK distribution is selected by this ADR. A supported OpenJDK-compatible
Java 21 distribution will be selected during toolchain/deployment setup. The
architecture must remain JDK-vendor neutral and must not depend on
vendor-specific Java behavior (Oracle JDK, Eclipse Temurin, Amazon Corretto,
Azul Zulu, or Microsoft Build of OpenJDK are all deferred choices, not
selections made here).

JVM deployment remains the default conceptual runtime. GraalVM/native-image
is not a requirement.

## Spring Boot Boundary

Spring Boot remains **Spring Boot 4.x**; it is not downgraded merely because
the Java baseline moved to Java 21. Spring Boot selection does not
automatically select every Spring project or adapter — this ADR does not
choose Spring Data JPA, Spring Data JDBC, R2DBC, Spring MVC, Spring WebFlux,
Spring AI, Spring Integration, Spring Batch, Spring Cloud, Spring Kafka,
Spring AMQP, Spring Authorization Server, Spring Session, or Spring
Modulith. Spring MVC versus WebFlux remains open, as established by
ADR-0009.

## Modular Monolith Boundary

The backend remains one deployable modular monolith per
[ADR-0001](./ADR-0001-modular-monolith-backend.md). It must not be split into
microservices without the evidence and architecture review required by
ADR-0001 and the [Architecture Standard](../../standards/03_ARCHITECTURE.md).

## Module-First Organization

The primary backend organizing boundary is **business capability/module**,
not technical layer. Conceptually, and consistent with the responsibilities
already defined in
[SYSTEM_ARCHITECTURE.md](../SYSTEM_ARCHITECTURE.md):

```text
penatika/
  identity/
    domain/
    application/
    adapter/

  lesson/
    domain/
    application/
    adapter/

  classroom/
    domain/
    application/
    adapter/

  ai/
    domain/
    application/
    adapter/

  mathematics/
    domain/
    application/
    adapter/

  curriculum/
    domain/
    application/
    adapter/
```

These module names are conceptual boundaries derived from the existing
Identity and Access, Lesson, Classroom Session, Classroom Scene, AI
Orchestration, Mathematics Assurance, and Curriculum modules in
`SYSTEM_ARCHITECTURE.md`. This ADR does not create these source directories,
does not freeze every future package name, and does not invent new product
modules. Physical structure is deferred (see "Deferred Physical Structure"
below).

**Reject global technical layer structure.** The backend must not be
organized globally as:

```text
controller/
service/
repository/
entity/
```

because that groups unrelated business capabilities by technical role
instead of by responsibility. Hexagonal layering is local to the module, not
a global top-level structure.

## Hexagonal Dependency Direction

Within a module complex enough to benefit from it, dependency direction
follows:

```text
Inbound Adapter
       ↓
Input Port / Use Case
       ↓
Application
       ↓
Domain
       │
       ▼
Output Port
       ▲
Outbound Adapter
```

External technology depends toward application-owned boundaries. Core
business behavior must not depend directly on Spring MVC, the SSE
implementation, PostgreSQL, JDBC, the OIDC provider SDK, an AI provider SDK,
a speech provider SDK, HTTP DTOs, or the database row model. This refines,
without changing, the dependency-direction rule already stated in
`SYSTEM_ARCHITECTURE.md` §4 and ADR-0009's Domain and Framework Boundary.

## Domain Boundary

Domain contains business concepts and invariants where meaningful. Domain
code should be plain/framework-light Java. Domain types must not carry
Spring annotations added merely for dependency injection or persistence
convenience — avoid `@Entity`, `@Component`, `@Service`, and `@Repository` on
core domain types.

Do not force a rich domain model where behavior is genuinely trivial.
Hexagonal architecture must not become ceremony: a module with little real
business logic does not need a full domain/application/adapter split
manufactured for its own sake.

## Application Layer

Application owns use-case orchestration — for example (conceptually):
`StartClassroomSession`, `ApproveAIProposal`, `SaveLesson`,
`RedeemPairingGrant`.

Application services may depend on domain and application ports. They must
not depend on concrete infrastructure adapters. Transaction boundaries may
be coordinated at application use-case boundaries when persistence
implementation later requires it.

## Inbound Ports and Adapters

Use an input port/use-case boundary when it improves supported application
interface clarity, testability, isolation from delivery mechanisms, or reuse
across more than one inbound adapter. Do not mechanically create an
interface for every application class — a simple application use case may
itself be the stable boundary where an additional interface adds no value.

Inbound adapters translate external interaction into application semantics
(for example: an HTTP controller, an SSE endpoint, or a scheduled/operational
trigger if later justified). Inbound adapters may contain framework-specific
code; domain does not.

## Outbound Ports and Adapters

Create output ports for meaningful external/technical boundaries. Expected
examples may eventually include persistence, OIDC integration, an AI
provider, a speech provider, curriculum source/retrieval, a clock where
deterministic behavior requires it, and external object storage if future
scope requires it. Do not create ports for trivial internal helpers.

Rule: a port exists because it protects a meaningful boundary, not because
Hexagonal Architecture requires more interfaces.

Outbound adapters implement application-owned ports — for example, a
PostgreSQL persistence adapter after OAD-003, an OIDC provider adapter, an
AI provider adapter, or a speech provider adapter. Outbound adapters may
contain framework-specific code; domain does not.

## Cross-Module Interaction

Modules communicate through explicit supported application/module
boundaries. Avoid direct access to another module's internal classes,
cross-module repository calls, cross-module table writes, and circular
dependencies. Within the modular monolith, interactions remain in-process;
this ADR does not introduce internal HTTP calls or message brokers between
modules.

Shared code must represent genuinely stable cross-cutting concepts. Avoid a
broad `common` module that becomes a dumping ground for unrelated DTOs,
utilities, constants, or domain behavior from different capabilities.

## Framework Boundary

Transport DTOs belong to inbound/outbound transport adapters, not to the
domain or application core. OpenAPI/JSON Schema (per
[ADR-0010](./ADR-0010-contract-first-openapi-json-schema.md)) remains
authoritative for wire contracts; generated OpenAPI DTOs must not become
core domain objects. Mapping between the wire model and the
application/domain model occurs at the appropriate boundary; do not create
mapping abstractions when direct explicit mapping is sufficient.

Future persistence-specific rows/entities/records belong inside the
persistence adapter and do not become the domain model. OAD-003 remains
unresolved; this ADR does not select JPA, Hibernate, Spring Data JPA, Spring
Data JDBC, JDBC, jOOQ, or R2DBC. ECC or framework conventions must not
silently determine persistence strategy.

## Port Creation Rules

- A port exists to protect a meaningful boundary (replaceability, testing,
  security, or policy), not because Hexagonal Architecture mandates more
  interfaces.
- Do not mechanically wrap every class or every library call.
- Input ports are created when more than one inbound adapter must share a
  use case, or when isolation/testability clearly benefits.
- Output ports are created for genuine external/technical boundaries
  (persistence, identity provider, AI/speech providers, curriculum
  source, clock, object storage), not for trivial internal helpers.

## Testing and Architecture Fitness

When source scaffolding begins, important Hexagonal/module rules should be
enforced where practical — for example, using ArchUnit, build/module
dependency rules, package visibility, or dedicated architecture tests. This
ADR does not select or configure ArchUnit; it establishes the rule that
architecture-fitness tooling may be selected during source scaffolding
because the underlying architecture rule now exists.

## ECC Skill Interaction

ECC is optional AI execution assistance and is advisory, not authoritative.
ECC skills must never override product/feature requirements, Accepted ADRs
(including this one), System Architecture, contracts, or Penatika
engineering standards. In particular:

- ECC's generic Spring/Java skill examples may contain layered/JPA examples;
  those examples do not override the module-first Hexagonal Architecture or
  the deferred persistence decision in this ADR;
- no persistence technology may be inferred by ECC before OAD-003 is
  resolved;
- no microservices may be introduced by ECC suggestion without a separate
  Accepted ADR changing ADR-0001.

The full Penatika ECC Skill Profile, including which ECC skills are
preferred for backend/frontend tasks and how they are scoped, is defined in
[14_AI_ASSISTED_DEVELOPMENT.md](../../standards/14_AI_ASSISTED_DEVELOPMENT.md).

## Consequences

### Positive

- Reduces JDK/toolchain movement relative to Java 25 while keeping a
  supported, modern LTS baseline.
- Makes explicit an organizing principle (module-first, Hexagonal locally)
  that was already implied by ADR-0001/ADR-0009 but not previously stated as
  a binding rule, reducing the risk that early scaffolding defaults to a
  global technical-layer structure.
- Keeps domain/application code framework-light and testable independent of
  Spring, persistence, and provider SDKs.
- Keeps persistence technology, JDK distribution, and other OAD-003/OAD-006
  through OAD-011 decisions open, consistent with ADR-0009.

### Negative / Costs

- Introduces one additional ADR to track alongside ADR-0009's now-historical
  Java 25 decision; readers must follow the supersession chain.
- Module and Hexagonal-boundary discipline must be enforced during source
  scaffolding to avoid both an unstructured monolith and mechanical,
  ceremony-driven ports/adapters proliferation.
- Requires explicit ECC skill-profile guidance so AI-assisted implementation
  does not default to ECC's generic layered/JPA examples.

## Alternatives / Prior Decision

**Keep Java 25 LTS (ADR-0009's original selection).** Rejected for the
current baseline: Java 25 remains a valid newer LTS, but no current Penatika
requirement depends on a Java 25-specific capability, and Java 21 provides
the same Spring Boot 4.x compatibility and the modern language/runtime
features Penatika needs with less unnecessary toolchain movement. This is a
baseline preference, not a suitability judgment against Java 25.

**Global technical-layer structure (`controller/service/repository/entity`).**
Rejected because it groups unrelated business capabilities by technical role
rather than by responsibility, which is the same reasoning ADR-0009 already
applied when it rejected "one global horizontal" structure.

**Mechanical ports-for-everything Hexagonal Architecture.** Rejected as a
default: creating an interface for every class or a port for every
dependency adds ceremony without protecting a meaningful boundary. Ports are
created only where they protect replaceability, testing, security, or policy
boundaries.

All other alternatives considered for the backend language/framework
selection remain as recorded in ADR-0009 (TypeScript/Node.js, Kotlin + Spring
Boot, C#/ASP.NET Core, Go, Python/FastAPI); this ADR does not revisit them
because the language/framework family (Java + Spring Boot) is unchanged.

## Deferred Physical Structure

This ADR does not create source, package, or module directories. The
module names listed under "Module-First Organization" are conceptual and
derived from existing `SYSTEM_ARCHITECTURE.md` responsibilities; they are
not a frozen package plan. Exact physical module/package layout, build tool
(Maven/Gradle), JDK distribution, exact Spring Boot patch version, and
architecture-fitness tooling (for example ArchUnit) selection remain deferred
to source scaffolding, consistent with ADR-0009's existing Follow-Up
Decisions.

## Related ADRs

- [ADR-0001 — Use a Modular Monolith for the Initial Backend](./ADR-0001-modular-monolith-backend.md)
- [ADR-0009 — Use Java 25 LTS and Spring Boot for the Authoritative Backend](./ADR-0009-java-spring-boot-backend.md) (superseded by this ADR)
- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](./ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0012 — Use Server-Sent Events for Realtime Push with Existing HTTP Commands](./ADR-0012-sse-realtime-push-with-existing-http-commands.md)
- [System Architecture](../SYSTEM_ARCHITECTURE.md)
- [Architecture Standard](../../standards/03_ARCHITECTURE.md)
- [AI-Assisted Development Standard](../../standards/14_AI_ASSISTED_DEVELOPMENT.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-07` | Accepted | Adopt Java 21 LTS and make module-first Hexagonal Architecture explicit, superseding ADR-0009 |
