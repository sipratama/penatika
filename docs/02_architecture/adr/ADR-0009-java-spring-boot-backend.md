# ADR-0009 — Use Java 25 LTS and Spring Boot for the Authoritative Backend

| Field | Value |
|---|---|
| ADR | `ADR-0009` |
| Status | Superseded |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-LESSON-001`, `CAP-SESSION-001`, `CAP-SESSION-002`, `CAP-CANVAS-001`, `CAP-INK-001`, `CAP-ADAPT-001`, `CAP-MATH-001` |
| Supersedes | N/A |
| Superseded By | [ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) |

## Context

Penatika requires a long-lived authoritative backend for stateful domain
workflows, authorization and policy enforcement, revision-aware classroom
sessions, role-specific projections, persistence orchestration, AI proposal
lifecycle, Mathematics assurance, curriculum provenance, and data-lifecycle
behavior.

[ADR-0001](./ADR-0001-modular-monolith-backend.md) already establishes one
deployable modular monolith. [ADR-0002](./ADR-0002-backend-authoritative-session-state.md),
[ADR-0004](./ADR-0004-ai-assurance-boundary.md),
[ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md), and
[ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md)
establish authority, trust, publication, and recovery responsibilities that
must remain independent of browser clients and provider SDKs.

OAD-002 must select a backend language and application framework before source
scaffolding without prematurely selecting persistence, identity, realtime,
provider, deployment, background execution, contract, or build tooling.

## Decision

Penatika will implement its authoritative backend as one deployable modular
monolith using:

- **Java 25 LTS** as the language/runtime baseline;
- **Spring Boot 4.x** as the backend application framework.

The current implementation baseline at the date of this decision is Java 25
LTS and the Spring Boot 4.1.x stable line. The exact Spring Boot patch version
will be selected during source scaffolding using the latest compatible
stable/security patch from the approved Spring Boot 4.x line.

This ADR reinforces ADR-0001. It does not introduce microservices or a second
backend authority.

## Java Runtime Baseline

Java 25 LTS is selected because it provides:

- a current LTS lifecycle suitable for a long-lived authoritative service;
- a strong static type system;
- records, sealed types, and enums where appropriate for structured state and
  workflow models;
- a mature concurrency/runtime platform;
- mature security, persistence, HTTP, observability, and testing ecosystems;
- an appropriate foundation for Penatika's stateful application and policy
  responsibilities.

The architecture does not require Java 27 merely because it is newer.

No JDK distribution is selected by this ADR. The implementation must remain
compatible with a supported OpenJDK-compatible Java 25 distribution selected
during toolchain/scaffolding decisions. Vendor-specific Java features must not
become architecture dependencies.

JVM deployment is the default conceptual runtime. GraalVM/native-image is not
a requirement and may be evaluated later only when startup, memory,
deployment, or cost evidence justifies the additional complexity.

## Spring Boot Baseline

Spring Boot 4.x provides the application foundation for:

- HTTP application endpoints;
- security integration;
- configuration;
- dependency injection;
- validation;
- persistence adapters;
- health and observability;
- realtime-capable adapters;
- external provider integration.

Spring Boot selection does not automatically select every Spring project or
adapter. This ADR does not choose Spring Data JPA, Spring Data JDBC, R2DBC,
Spring MVC, Spring WebFlux, Spring AI, Spring Integration, Spring Batch,
Spring Cloud, Spring Kafka, Spring AMQP, Spring Authorization Server, Spring
Session, or Spring Modulith.

Spring Modulith may be evaluated during source scaffolding as optional
module-boundary verification/tooling. It is not required by OAD-002.

Spring Security may later enforce the identity and authorization architecture
selected by OAD-004, but this ADR does not choose an identity provider,
session/token model, or authentication design.

Spring MVC versus WebFlux remains open because the appropriate transport and
concurrency adapter depends partly on OAD-005 and measured workload behavior.
Slow AI calls do not automatically require WebFlux, and Java virtual threads
do not require every operation to use them.

## Modular Monolith Boundary

The backend remains one deployable modular monolith. It must not be split into
microservices without the evidence and architecture review required by
ADR-0001 and the Architecture Standard.

Future source organization should follow cohesive domain/application concerns
rather than one global horizontal `controller` / `service` / `repository` /
`entity` structure across the whole application.

A module may conceptually separate application, domain, inbound adapters, and
outbound adapters where complexity justifies those boundaries. This is not a
requirement to create four folders mechanically for every small module.

The exact physical module and package structure is deferred to source
scaffolding and must follow the responsibilities already defined in
[SYSTEM_ARCHITECTURE.md](../SYSTEM_ARCHITECTURE.md). This ADR does not invent
new product modules.

## Domain and Framework Boundary

Core domain and application behavior should remain framework-light Java where
practical. Spring-specific concerns belong primarily at application
composition and adapter boundaries.

Domain models must not depend on:

- provider SDKs;
- transport models;
- database entities or ORM assumptions;
- AI SDK types;
- client framework types.

External systems and technical mechanisms must be reached through explicit
application-owned ports/adapters where replacement, testing, security, or
policy boundaries justify them.

## Authority Responsibilities

The Spring Boot backend—not the React clients—owns and enforces:

- teacher/account-facing application behavior;
- authentication and authorization enforcement after OAD-004 defines the
  identity model;
- lessons and lesson versions;
- classroom session lifecycle and authoritative revision;
- role-specific projections;
- classroom command acceptance and revision checks;
- digital-ink authoritative state;
- AI orchestration and proposal lifecycle;
- teacher approval and publication policy;
- Mathematics assurance orchestration;
- curriculum authority and provenance enforcement;
- save/history lifecycle;
- retention and deletion orchestration;
- observability and policy enforcement.

[ADR-0008](./ADR-0008-browser-first-react-client-strategy.md) does not create
a frontend BFF authority, and this ADR does not introduce one.

## Deferred Technology Decisions

This ADR intentionally does not select:

- OAD-003 persistence database, access pattern, ORM, or migration tooling;
- OAD-004 identity provider, session/token model, or authentication library;
- OAD-005 realtime transport or reconnect adapter;
- OAD-006 AI provider, model, orchestration library, or provider SDK;
- OAD-007 speech provider or SDK;
- OAD-008 Mathematics validator implementation;
- OAD-009 curriculum ingestion/retrieval implementation;
- OAD-010 deployment platform, environments, or secret management;
- OAD-011 background execution or queue infrastructure;
- OAD-012 contract protocols and schema tooling;
- Maven versus Gradle, wrappers, formatting/static-analysis plugins, or
  dependency-lock strategy;
- exact JDK distribution or Spring Boot patch version.

Persistence, identity, realtime, AI, speech, Mathematics, and curriculum
implementations must remain adapter-level concerns until their relevant
decisions are resolved.

OAD-011 remains conditional. Kafka, RabbitMQ, SQS, Redis queues, Spring Batch,
or another job broker must not be added merely because AI work may be
long-running. The current architecture can represent long-running request
lifecycle states without preselecting a durable queue. Queue infrastructure
requires measured duration, reliability, buffering, fan-out, or recovery
evidence.

## Consequences

### Positive

- Provides a strongly typed platform for structured domain state, revision
  semantics, policy states, and role-specific projections.
- Aligns the implementation platform with the existing one-deployable modular
  monolith decision.
- Provides mature security, validation, integration, health, observability,
  persistence-adapter, and testing foundations without selecting their final
  implementations prematurely.
- Keeps browser clients and external providers outside the authoritative
  application boundary.
- Supports framework-light domain/application logic with explicit adapters.

### Negative / Costs

- The team must operate a Java/Spring toolchain and maintain framework/version
  compatibility.
- Spring's broad ecosystem creates a risk of accidental technology selection
  through defaults or convenience starters; later decisions must remain
  explicit.
- Module and dependency discipline must be enforced to prevent an
  unstructured monolith.
- JVM resource, startup, concurrency, and deployment characteristics must be
  measured against the selected environment after OAD-010.
- Build tool, exact versions, physical packages, contracts, and all adapter
  technologies still require follow-up work before implementation.

## Alternatives Considered

### TypeScript / Node.js Backend

NestJS, Fastify, and other Node frameworks are credible backend choices.
They are not selected because sharing TypeScript with the frontend is not
sufficient architecture benefit by itself. Penatika's backend contains
stateful domain workflows, policy/state machines, revision semantics,
authorization, persistence, and assurance orchestration. Java/Spring is the
stronger fit for the intended long-lived authoritative service and project
delivery context, and one-language frontend/backend is not a product
requirement.

### Kotlin + Spring Boot

Kotlin with Spring Boot is technically strong. It is not selected for the
initial baseline because it does not materially improve current Penatika
requirements over Java, adds another language/tooling learning dimension, and
Java 25 already provides the required type and runtime capabilities.

### C# / ASP.NET Core

C# and ASP.NET Core offer similarly strong enterprise/backend capabilities.
They are not selected because there is no product-driven advantage over the
chosen Java/Spring ecosystem for Penatika, while another ecosystem would add
delivery and tooling variance without a requirement forcing that change.

### Go

Go offers operational simplicity and strong concurrency. It is not selected
because Penatika is primarily a domain/application workflow system rather than
a minimal network service. Current requirements benefit from the richer
application, security, persistence, and domain-modeling ecosystem of
Java/Spring, and no current operational constraint requires Go's smaller
runtime model.

### Python / FastAPI

Python and FastAPI provide an excellent AI/data ecosystem. They are not
selected for the authoritative main backend because Penatika is not primarily
an ML model-serving system. AI providers remain integrations behind adapters,
while session, revision, security, and domain behavior remain in the selected
Java/Spring application platform.

Python remains possible for a future specialized adapter or engine only when
evidence justifies an additional runtime or deployment boundary. No Python
microservice is created preemptively.

## Follow-Up Decisions

1. Resolve OAD-012 — Contract Protocols and Schema Tooling before source
   scaffolding.
2. Resolve OAD-003 through OAD-010 according to their existing `Needed Before`
   rules and dependency/evidence requirements.
3. Keep OAD-011 conditional until measured evidence activates it.
4. During source scaffolding, select a supported OpenJDK-compatible Java 25
   distribution, exact Spring Boot security patch, build tool, and physical
   module/package layout without changing the decisions in this ADR.
5. Define architecture-boundary verification appropriate to the chosen source
   layout; Spring Modulith may be evaluated but is not mandatory.

## Related Requirements / ADRs

- [Product Requirements Document](../../00_product/PRD.md)
- [Non-Functional Requirements](../NON_FUNCTIONAL_REQUIREMENTS.md)
- [System Architecture](../SYSTEM_ARCHITECTURE.md)
- [ADR-0001 — Use a Modular Monolith for the Initial Backend](./ADR-0001-modular-monolith-backend.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./ADR-0004-ai-assurance-boundary.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0008 — Use Browser-First React Clients with Separate Teacher and Classroom Display Boundaries](./ADR-0008-browser-first-react-client-strategy.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-06` | Accepted | Adopt Java 25 LTS and Spring Boot 4.x for the authoritative modular-monolith backend |
| `2026-09-07` | Superseded | Superseded by [ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md), which revises the Java baseline to Java 21 LTS and makes module-first Hexagonal Architecture explicit; this decision body remains unchanged as historical evidence |
