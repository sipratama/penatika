# ADR-0014 — Use PostgreSQL with Flyway and SQL-First Hexagonal Persistence

| Field | Value |
|---|---|
| ADR | `ADR-0014` |
| Status | Accepted |
| Date | `2026-09-07` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-LESSON-001`, `CAP-SESSION-001`, `CAP-SESSION-002`, `FR-SESSION-021`–`FR-SESSION-025` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

OAD-003 selects the database and migration technology. Every prior
architecture decision that requires durable state — `TeacherAccount` and
`ExternalIdentityLink` ([ADR-0011](./ADR-0011-oidc-backend-managed-browser-sessions.md)),
backend-authoritative classroom session lifecycle and revision
([ADR-0002](./ADR-0002-backend-authoritative-session-state.md)), graceful
degradation and durable-save truthfulness
([ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md)),
and realtime reconnect resync
([ADR-0012](./ADR-0012-sse-realtime-push-with-existing-http-commands.md)) —
has been designed around an authoritative persistence boundary without
selecting its technology. [DATA_MODEL.md](../DATA_MODEL.md) already defines
the conceptual domain model, ownership, relationships, and retention
classes that this decision must support physically once implementation
begins.

[ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) already
establishes that persistence is an outbound adapter behind
application-owned ports, that ports exist only to protect meaningful
boundaries, and that no persistence technology may be inferred before
OAD-003. This ADR resolves OAD-003 within that binding boundary.

The critical authoritative paths this decision must serve include:
single-use `PairingGrant` redemption under concurrency, at-most-one-active
`TEACHER_CONTROLLER`/`CLASSROOM_DISPLAY` enforcement, atomic
optimistic-revision classroom mutation
([INV-009](../SYSTEM_ARCHITECTURE.md), `INV-020`, `INV-021`), truthful
durable-save acknowledgement (ADR-0007), and the retention/deletion
lifecycle in
[DATA_RETENTION_POLICY.md](../../06_delivery/DATA_RETENTION_POLICY.md).
These paths need direct visibility into SQL, constraints, and concurrency
behavior rather than an opaque object-relational mapping layer.

This ADR does not scaffold source, create physical tables or migrations,
install dependencies, resolve another OAD, or choose a deployment platform.

## Decision

Penatika selects:

- **PostgreSQL 18.x** as the primary authoritative relational database
  (current stable baseline at decision time: PostgreSQL 18.6);
- **Flyway 13.x** as the migration technology (current baseline at decision
  time: Flyway 13.5.x);
- **Spring JDBC / JdbcClient-style explicit SQL** as the data-access
  baseline;
- **SQL-first relational persistence**, with PostgreSQL JSONB used
  selectively for justified structured content.

Minor/patch versions are not frozen as permanent architecture requirements.
At implementation/scaffolding time, select the latest supported PostgreSQL
18 minor release and the latest compatible tested Flyway 13.x release.
PostgreSQL 19 must not be selected for the initial baseline while it
remains pre-release.

This ADR does not select JPA, Hibernate, Spring Data JPA, Spring Data JDBC,
jOOQ, R2DBC, Redis, a vector database, or an object-storage provider.

## PostgreSQL Baseline

PostgreSQL 18.x is selected because Penatika's authoritative data has
strong identity, ownership, session, revision, and provenance relational
structure (per [DATA_MODEL.md](../DATA_MODEL.md)), and PostgreSQL provides
mature transactional integrity, constraint enforcement, JSONB, and
concurrency-control primitives that directly serve those needs. The exact
minor release is an implementation-time selection, not an architecture
freeze.

## One-Database Modular-Monolith Boundary

Penatika initially uses **one PostgreSQL database** for the one-deployable
modular monolith ([ADR-0001](./ADR-0001-modular-monolith-backend.md)).

Shared physical database does not mean shared logical ownership. Each
business-capability module (per ADR-0013's module-first organization) owns
its persistent structures. Other modules must not bypass ownership through
direct table writes, another module's persistence adapter, or another
module's database row types; cross-module behavior uses supported
application/module boundaries.

This ADR does not introduce database-per-module, mandatory schema-per-module,
or microservice-style datastore separation. Whether PostgreSQL schemas are
useful organizationally can be evaluated later during physical model design.

## Hexagonal Persistence Boundary

Persistence is an outbound adapter, per ADR-0013:

```text
Domain / Application
        ↓
application-owned persistence port
        ↑
PostgreSQL persistence adapter
        ↓
Spring JDBC / JdbcClient
        ↓
PostgreSQL
```

Core domain/application code must not depend directly on `JdbcClient`,
`DataSource`, `ResultSet`, SQL, Flyway, PostgreSQL-specific driver types, or
database row records. Persistence technology stays at the outbound-adapter
boundary.

## Module Ownership

Persistence adapters are organized under the owning business module,
conceptually:

```text
identity/
  application/
    port/
      out/
        ...
  adapter/
    out/
      persistence/
        postgres/

lesson/
  application/
    port/
      out/
        ...
  adapter/
    out/
      persistence/
        postgres/

classroom/
  application/
    port/
      out/
        ...
  adapter/
    out/
      persistence/
        postgres/
```

These paths are conceptual; no source directories are created by this ADR.
A global repository package containing persistence for unrelated business
capabilities must not be created.

**Port granularity.** Do not create one generic `Repository<T, ID>`
abstraction for the entire application. Application-owned persistence ports
should reflect meaningful use-case/domain needs — conceptually
`LoadClassroomSession`, `SaveClassroomSession`, `ResolveExternalIdentity`,
`StoreLessonVersion`. Exact interfaces and names are deferred to source
scaffolding. An interface is not created merely because every adapter "must
have a port"; ADR-0013's meaningful-boundary rule remains binding.

## Relational / JSONB Boundary

Core authoritative data uses relational structures where meaningful,
including `TeacherAccount`, `ExternalIdentityLink`, the authenticated
browser session, `Lesson`, lesson-version metadata, `Classroom Session`,
`PairingGrant`, `SessionParticipant`, AI-proposal lifecycle metadata,
Mathematics-assurance metadata, curriculum source/version/provenance
metadata, and retention/deletion lifecycle state (per
[DATA_MODEL.md](../DATA_MODEL.md) §2). PostgreSQL constraints should
enforce fundamental persistent invariants where appropriate — for example,
unique `(issuer, subject)`, referential ownership, non-null required state,
security/session uniqueness, concurrency-safe single-use `PairingGrant`,
concurrency-safe participant replacement, and revision-conflict detection.
No DDL is defined by this ADR.

PostgreSQL JSONB is allowed selectively. Likely candidates include
versioned structured lesson content, structured classroom scene, accepted
structured AI content, and retained structured session snapshots. JSONB
must not become a generic "put everything in JSON" mechanism: identity,
ownership, lifecycle, authorization references, revision, retention/expiry
timestamps, searchable/indexable metadata, and foreign-key relationships
should normally remain explicit relational structure.

If a JSONB structure is also a cross-component wire structure, canonical
JSON Schema (per [ADR-0010](./ADR-0010-contract-first-openapi-json-schema.md))
remains authoritative. Database JSON representation does not replace
OpenAPI, JSON Schema, or application validation.

No vector database (pgvector, Pinecone, Qdrant, Weaviate,
Elasticsearch/OpenSearch vector search, or another) is introduced by this
ADR. OAD-009 still decides curriculum retrieval; "Penatika uses AI" is not
sufficient evidence for vector infrastructure. If OAD-009 later evidences a
genuine semantic/vector retrieval need, that is evaluated separately.

## Data Access Strategy

Spring JDBC / JdbcClient-style explicit SQL is the initial persistence
adapter baseline, because it provides explicit SQL visibility, explicit
transaction boundaries, explicit optimistic-revision semantics, predictable
persistence behavior, straightforward use of PostgreSQL
constraints/features, and avoids making ORM object graphs part of the
domain architecture. This decision belongs only inside persistence
adapters; domain/application code remains unaware of `JdbcClient`.

**No JPA/Hibernate baseline.** JPA, Hibernate, and Spring Data JPA are not
selected — not because they are incapable, but because critical Penatika
persistence paths require strong visibility into session revision,
optimistic concurrency, idempotency, security/session state,
lifecycle/purge queries, authoritative state mutation, database
constraints, and exact SQL behavior. This avoids persistence-context
behavior leaking into use cases, lazy-loading coupling, implicit cascades,
N+1 surprises, and persistence entities becoming domain models.
ECC `springboot-patterns` JPA examples are advisory examples only; the
Penatika ECC Skill Profile explicitly overrides them, and `jpa-patterns` is
not activated by default.

**Spring Data JDBC** is a credible alternative and is not selected as
mandatory baseline. Explicit SQL via Spring JDBC/JdbcClient remains
preferred initially because Penatika's authoritative mutation paths benefit
from direct SQL visibility. A future module may justify Spring Data JDBC if
persistence is genuinely simple, it does not weaken module/domain
boundaries, and it does not create competing persistence conventions
without justification. A material project-wide change requires architecture
review.

**jOOQ** is a strong SQL-first alternative and is not selected initially,
because it adds a schema/code-generation lifecycle and more build/tooling
decisions that current query complexity does not yet justify. Revisit if
SQL/query complexity becomes large enough that compile-time query modeling
provides measurable value.

**R2DBC** is not selected. ADR-0012 chose SSE for push but explicitly did
not require a reactive backend; long-lived SSE connections do not make
database operations reactive by default. JDBC remains the initial
database-access model.

## Transaction Boundaries

Transactions align with application use-case consistency boundaries —
conceptually: `PairingGrant` redemption plus participant
establishment/replacement; authoritative classroom mutation plus revision
advance; AI publication acceptance plus authoritative state transition;
durable session save; and account lifecycle/revocation transitions requiring
atomic consistency. The application/use-case layer may define transaction
intent; the concrete Spring transaction mechanism remains
infrastructure/composition detail.

A database transaction must not remain open while waiting for teacher
input, calling an AI provider, calling speech recognition, retrieving
remote curriculum data, or maintaining an SSE stream.

## Concurrency / Revision Model

The persistence model must support atomic optimistic conflict detection,
conceptually equivalent to:

```sql
UPDATE classroom_session
SET revision = revision + 1, ...
WHERE session_id = ?
  AND revision = ?
```

The actual table/column/query is not defined here. Architecture invariant:
a matching base revision may commit the mutation; a mismatched base
revision must produce zero authoritative overwrite and an explicit
stale/conflict result. The read-current-revision-then-compare-in-Java-then
unconditionally-update pattern must not be used, because competing requests
could race. Use conditional SQL updates, constraints, atomic operations,
and row locking only where justified. `SERIALIZABLE` isolation is not
globally selected.

Database design must eventually enforce safely, under concurrency:
single-use `PairingGrant` redemption; at most one active
mutation-authorized `TEACHER_CONTROLLER`; at most one active
`CLASSROOM_DISPLAY`; and that participant replacement cannot leave two
active authorities accidentally. Exact SQL/constraint/locking technique is
deferred to physical schema design; "check first, insert later" without
database-backed concurrency protection is not sufficient.

## Identity and Session Persistence

PostgreSQL is the initial authoritative persistence location for
server-side revocable state requiring durability, conceptually including
`TeacherAccount`, `ExternalIdentityLink`, backend-managed browser session
state, `PairingGrant`, `SessionParticipant` authorization/revocation, and
`ClassroomSession` authoritative durable state.

Redis is not introduced solely because backend-managed browser sessions
exist. Redis may be evaluated later only if measured scale, session
throughput, latency, or horizontal runtime topology provides actual
evidence; an in-process-only session store is not sufficient for the
production architecture. SSE connection objects themselves are runtime
resources and are not persisted as database entities.

## Durable Save Semantics

ADR-0007 remains binding: Penatika may report `SAVED` only after the
required authoritative PostgreSQL transaction commits successfully.
In-memory state mutation, an emitted SSE event, request acceptance before
commit, and queued work not yet committed do not prove durable save
success.

## Flyway Migration Strategy

Flyway 13.x owns physical schema evolution through version-controlled SQL
migrations. Explicit PostgreSQL SQL is preferred; Penatika is intentionally
PostgreSQL-specific at this persistence boundary, and an abstract
database-neutral migration DSL is not introduced merely for hypothetical
future portability.

After implementation begins, `migrations/schema` plus Flyway history become
the executable physical schema source of truth; `DATA_MODEL.md` remains the
conceptual domain/data model. No runtime framework may silently become
schema authority — production strategies equivalent to Hibernate
`ddl-auto=create`/`update`, automatic ORM schema mutation, or ad-hoc manual
schema creation are not used.

Use deterministic versioned SQL migration naming, for example
`V001__description.sql`, `V002__description.sql`. Exact domain names are
deferred; no migration files are created by this ADR. Once applied to a
shared environment, a migration version must never be reused for different
content; do not silently rewrite already-applied migration semantics — use
a new corrective migration instead.

Penatika uses forward-oriented production migration; a down script is not
required for every migration. Rollback may use application rollback while
schema remains compatible, a corrective forward migration, or
backup/restore for severe incidents. Destructive changes require impact
analysis, data migration/backfill, deployment ordering, recovery strategy,
and consumer compatibility analysis. Expand/contract is used only where
deployment overlap actually requires it.

Repeatable migrations may later be used for deliberately replaceable
database objects such as views or functions, when their lifecycle justifies
it; they are not used for ordinary table evolution.

## Migration Safety

Flyway `clean` is prohibited for shared development environments, pilot,
staging, and production. It may be used only for explicitly disposable
local/test databases under safe configuration. An unknown non-empty database
is not automatically baselined; a real baseline operation requires an
explicit controlled reason.

Production migration execution is a controlled release/deployment action.
A design in which every backend replica independently races to perform
schema migrations at startup is not established here; exact deployment
orchestration remains OAD-010. For local development and automated
integration tests, application-integrated Flyway execution may later be
used where deterministic and safe.

## Runtime vs Migration Privileges

Conceptually separate a migration principal (DDL/schema-change authority
needed for migrations) from an application runtime principal (only
DML/object permissions needed by the application). The running backend must
not require database superuser privileges. Exact role names, credentials,
and secret delivery belong to implementation and OAD-010.

## Retention / Deletion

The persistence design must support
[DATA_RETENTION_POLICY.md](../../06_delivery/DATA_RETENTION_POLICY.md). It
must be possible to implement and test transient diagnostic cleanup where
enabled, saved-session expiry, retained-annotation expiry,
teacher-triggered deletion, account deletion, primary purge, backup-expiry
coordination, and export authorization and retrieval. A generic database
TTL is not used as the product policy — retention rules come from product
policy, and exact cleanup scheduling remains implementation/operations
work. OAD-011 is not activated merely because cleanup jobs eventually exist.

## Backup / Recovery Boundary

Selecting PostgreSQL does not resolve managed database provider, backup
technology, point-in-time recovery, RPO, RTO, encryption-at-rest
implementation, or restore orchestration. Those remain OAD-010/operations
concerns. Deletion must not be claimed fully complete while protected
backup remnants remain within the approved expiry window.

## Cache / Vector Boundary

No cache is selected by this ADR. Redis is not added automatically. If a
cache is later introduced, it must have an explicit authoritative source,
TTL, invalidation policy, and acceptable staleness. Authoritative mutable
state remains PostgreSQL unless a later Accepted ADR changes the relevant
boundary. No vector database is selected (see "Relational / JSONB
Boundary" above).

PostgreSQL-specific capabilities (JSONB, check constraints, partial
indexes, unique indexes, transaction/locking features, `timestamptz`,
database-native index types) may be used where they provide clear
correctness or operational value; useful PostgreSQL features are not
avoided solely for hypothetical portability, but database-specific behavior
remains isolated to persistence/migration adapters.

`timestamptz` is preferred for authoritative machine instants; genuinely
local calendar concepts are not forced into UTC instants where their domain
semantics differ.

Large binary content is not stored in PostgreSQL by default. Future
PDF/PPT import is post-MVP; if binary content later becomes required,
object storage is evaluated separately, with appropriate metadata/reference
stored in PostgreSQL. Object storage is not selected now.

## Consequences

### Positive

- Resolves OAD-003 while preserving ADR-0013's Hexagonal persistence
  boundary and module-first ownership.
- Gives critical authoritative paths (revision conflict detection,
  single-use pairing, one-active-controller/display enforcement, durable
  save truthfulness) direct SQL/constraint visibility instead of ORM
  indirection.
- Keeps domain/application code free of persistence framework coupling.
- Defers JDK distribution, build tool, deployment platform, and other
  OAD-006–OAD-011/OAD-010 decisions unaffected.
- Uses one database for the modular monolith without collapsing module
  ownership boundaries.

### Negative / Costs

- Explicit SQL requires more handwritten mapping code than an ORM,
  especially as the number of persisted aggregates grows.
- Module- and port-boundary discipline must be enforced so persistence
  adapters do not become a de facto shared repository layer.
- Concurrency-safe revision/pairing/participant enforcement must be
  deliberately designed into schema and queries rather than assumed from
  framework defaults.
- Flyway migration discipline (naming, no-reuse, safety rules) must be
  followed consistently once migrations exist.

## Alternatives Considered

1. **PostgreSQL 19 (pre-release).** Technically credible; not selected
   because it is not the current stable production major at decision time.
2. **MySQL.** A capable relational database; no Penatika requirement favors
   it over PostgreSQL.
3. **MongoDB.** Strong document-storage model; not preferred as the primary
   authoritative store because Penatika contains strong
   identity/ownership/session/revision/provenance relationships that favor
   a relational model.
4. **Redis as primary/session authority.** Not selected as the
   authoritative store; may later serve as a cache/specialized optimization
   if evidence exists.
5. **JPA/Hibernate.** Mature and productive; not the initial baseline
   because critical Penatika paths require explicit SQL/concurrency
   visibility (see "Data Access Strategy").
6. **Spring Data JDBC.** A credible simpler mapping approach; not the
   initial mandatory baseline, but possible future module-local use with
   evidence.
7. **jOOQ.** An excellent SQL-first/type-safe alternative; not initially
   selected because generation/tooling cost is not yet justified.
8. **R2DBC.** Not selected because the selected SSE architecture does not
   require reactive database access.
9. **Liquibase.** A strong migration alternative; Flyway is chosen for its
   straightforward SQL-first PostgreSQL migration model.
10. **ORM-generated schema.** Rejected as physical schema authority; Flyway
    migrations are the schema source of truth once created.

## Deferred Physical Decisions

This ADR does not create source, package, or migration directories, and
does not define DDL, table/column names, indexes, or exact constraints. It
does not select: cloud PostgreSQL vendor, managed-database provider, backup
technology, secret manager, build tool, or JDK distribution. Exact
migration file contents, database roles/credentials, connection-pool
configuration, and PostgreSQL Docker/local-development tooling remain
deferred to source scaffolding and OAD-010.

## Follow-Up Decisions

1. Define physical schema, migrations, and constraints from
   [DATA_MODEL.md](../DATA_MODEL.md) during source scaffolding.
2. Resolve OAD-006 through OAD-011 according to their existing `Needed
   Before` rules; OAD-011 remains conditional.
3. Resolve OAD-010 for deployment platform, managed PostgreSQL provider (if
   any), backup/PITR, and secret management.
4. Define exact database role separation (migration vs. runtime principal)
   during implementation.
5. Define architecture-fitness verification for the Hexagonal persistence
   boundary (per ADR-0013) once source scaffolding begins.

## Related Requirements / ADRs

- [ADR-0001 — Use a Modular Monolith for the Initial Backend](./ADR-0001-modular-monolith-backend.md)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0012 — Use Server-Sent Events for Realtime Push with Existing HTTP Commands](./ADR-0012-sse-realtime-push-with-existing-http-commands.md)
- [ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture](./ADR-0013-java21-module-first-hexagonal-backend.md)
- [Data Model](../DATA_MODEL.md)
- [Data Retention, History, Export, and Deletion Policy](../../06_delivery/DATA_RETENTION_POLICY.md)
- [Data Persistence Standard](../../standards/07_DATA_PERSISTENCE_STANDARD.md)
- [System Architecture](../SYSTEM_ARCHITECTURE.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-07` | Accepted | Resolve OAD-003 with PostgreSQL 18.x, Flyway 13.x, and SQL-first Hexagonal persistence adapters |
