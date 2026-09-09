# Source Scaffolding Working Plan

> **Document role:** This is a working engineering plan for physical source
> scaffolding. It is not product, architecture, wire-contract, or persistent
> data authority. It may select routine physical choices that Accepted ADRs
> explicitly deferred to Source Scaffolding. If it conflicts with canonical
> product, architecture, contract, or policy documents, the canonical source
> wins and this plan must be corrected.

## Metadata

| Field | Value |
|---|---|
| Phase | Source Scaffolding |
| Batch | SS-04 — Test + Contract Validation Harness |
| Date | `2026-09-09` |
| Branch | `feat/source-scaffolding` |
| Starting main SHA | `8b93c0415e913e194f93dcbabb264f4141fd65bd` |
| Status | SS-01 complete; SS-02 complete; SS-03 complete; SS-04 complete; SS-05 next; SS-06 pending |
| Implementation state | Backend/frontend shells plus executable architecture, dependency, and contract safeguards scaffolded (no product behavior) |
| SS-02 selected baseline | Java 21 LTS; Apache Maven 3.9.16; Maven Wrapper Plugin 3.3.4 (`only-script`, distribution SHA-256 pinned); Spring Boot 4.1.1 (GA) |
| SS-03 selected baseline | Node.js 24.21.0 LTS (Krypton); npm 11.19.0; React / React DOM 19.2.8; Vite 8.2.2; TypeScript 7.0.2; Vitest 5.0.0; React Testing Library 16.3.3; jest-dom 7.0.1; jsdom 30.0.1 |
| SS-04 selected baseline | ArchUnit 1.5.0; Redocly CLI 2.51.2; Ajv 8.17.1; openapi-typescript 7.13.0 accepted for deterministic role-scoped transport declarations |

## 1. Objective

Source Scaffolding establishes reproducible backend and frontend build
surfaces, executable architecture boundaries, contract-conformance tooling,
and configuration/persistence mechanisms without implementing product
workflows. SS-01 freezes the smallest coherent physical strategy required
before files are generated in later batches.

## 2. Authoritative Sources

- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md) and
  Accepted [ADRs](../02_architecture/adr/) own system boundaries and
  architecture decisions.
- [ADR-0001](../02_architecture/adr/ADR-0001-modular-monolith-backend.md) and
  [ADR-0013](../02_architecture/adr/ADR-0013-java21-module-first-hexagonal-backend.md)
  own the one-deployable modular-monolith and module-first Hexagonal backend.
- [ADR-0008](../02_architecture/adr/ADR-0008-browser-first-react-client-strategy.md)
  owns React, TypeScript, Vite, Teacher Web, and Classroom Display boundaries.
- [ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md),
  [ADR-0011](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md),
  and [ADR-0012](../02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md)
  own contract, identity/session, and HTTP/SSE boundaries.
- [ADR-0014](../02_architecture/adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)
  owns PostgreSQL, Flyway, and Spring JDBC/JdbcClient persistence strategy.
- [`contracts/`](../../contracts/README.md) owns cross-component wire truth.
- Repository [engineering standards](../standards/00_STANDARD_INDEX.md) own
  implementation, testing, security, and dependency rules.
- [Project Status](../PROJECT_STATUS.md) owns phase routing only.

## 3. Repository Evidence at SS-01

### Tracked Today

- Root tracked content is documentation/tooling only: `contracts/`, `docs/`,
  `scripts/`, repository guidance, READMEs, and `.gitignore`.
- `scripts/` contains documentation and the Python project-mode validator.
- The complete Contract Foundation is tracked under `contracts/`.
- No tracked backend or frontend root exists.
- No build manifest, wrapper, package manifest, lockfile, application source,
  migration, deployment file, or CI workflow exists.

### Local-Only Today

- No untracked or ignored backend/web directories were found at SS-01.
- Git does not track empty directories; a local empty directory would not
  establish a repository contract in any case.

### Proposed by This Plan

- `backend/` becomes the backend build and source root in SS-02.
- `web/` becomes the frontend workspace root in SS-03.
- Existing `contracts/`, `docs/`, and `scripts/` remain repository-level
  authorities/tooling and are not moved under either application root.

## 4. Decisions Frozen

| Concern | Decision | Rationale | Authority / Evidence | Implemented In |
|---|---|---|---|---|
| Backend root | `backend/` | Keeps the one backend build isolated from repository-level contracts/docs and symmetrical with the separate web workspace | ADR-0001, ADR-0013, current root inventory | SS-02 |
| Backend build tool | Apache Maven stable `3.9.x` line; pin the current supported patch in the wrapper | One deployable Java application needs a low-complexity declarative build; Spring Boot supports Maven directly; Maven 4 remains preview at SS-01 | ADR-0013, dependency standard, official Maven/Spring support evidence checked `2026-09-09` | SS-02 |
| Wrapper | Commit Maven Wrapper scripts/properties using the non-binary `only-script` distribution and a pinned distribution SHA-256 | Cross-platform reproducibility without committing a wrapper JAR | Dependency standard; Maven Wrapper capability | SS-02 |
| Backend project shape | One Maven project producing one executable artifact; business modules are Java package boundaries, not Maven submodules | Matches one deployable, current scale, and minimum enforceable structure without multi-module build overhead | ADR-0001, ADR-0013 | SS-02 |
| Maven coordinates | `io.github.sipratama.penatika:penatika-backend` | Uses the evidenced repository owner/project identity without inventing a company or legal domain | Git remote `sipratama/penatika`; code-quality naming rules | SS-02 |
| Base Java package | `io.github.sipratama.penatika` | Stable reverse-namespace derived from actual repository identity; does not encode a machine path | Git remote; ADR-0013 deferred physical package decision | SS-02 |
| Initial backend modules | `identity`, `lesson`, `classroom` | These capabilities are required to establish the first contract-backed slice; Pairing and Classroom Scene stay inside `classroom` initially | System Architecture responsibilities; Contract Foundation surface | SS-02 |
| Later backend modules | Recognize `ai`, `mathematics`, and `curriculum`, but do not scaffold them yet | They are accepted architecture capabilities but not needed to prove the initial physical source boundary | System Architecture; scope control | Later implementation/scaffolding when first required |
| Local Hexagonal convention | Module-first packages: `domain`, `application`, optional `application.port.in`/`out`, `adapter.in`, and `adapter.out`; materialize only packages with real code | Enforces inward dependency while avoiding ports/adapters ceremony | ADR-0013 | SS-02 onward |
| Spring composition | `io.github.sipratama.penatika.PenatikaApplication` at the base package; framework composition/configuration under `io.github.sipratama.penatika.bootstrap` | Base entry point composes all modules while keeping cross-cutting wiring out of business packages | ADR-0013 Spring/framework boundary | SS-02 |
| Backend architecture fitness | JUnit architecture suite using ArchUnit; package/module rules assigned to SS-04 | Makes ADR-0013 dependency rules executable without requiring build submodules | ADR-0013; Architecture and Testing standards | SS-04 |
| Frontend root | `web/` | Matches browser application terminology already used in ADR-0008 and cleanly separates it from the backend | ADR-0008; current root inventory | SS-03 |
| Node runtime | Node.js `24.x` LTS policy; pin an exact supported patch in `web/.nvmrc` and require major `24` in workspace engines | Node 24 is the current LTS line at SS-01 and supports the accepted current frontend stack without adopting a Current/non-LTS release | ADR-0008; official Node release status checked `2026-09-09` | SS-03 |
| Package manager | `npm`, using the npm release bundled/compatible with the pinned Node runtime and an exact root `packageManager` declaration | Native workspaces satisfy two apps plus bounded shared packages without another bootstrap tool | ADR-0008; npm workspace support; dependency standard | SS-03 |
| Lockfile | One committed `web/package-lock.json`; no pnpm/yarn lockfiles | Reproducible workspace resolution with the selected manager | Dependency standard | SS-03 |
| Workspace strategy | Native npm workspaces only; no Nx, Turborepo, or other monorepo orchestrator | Two Vite applications do not yet justify another orchestration dependency | ADR-0008; dependency minimization | SS-03 |
| Teacher application | `web/apps/teacher` | Preparation and private Controller remain modes/routes in one Teacher Web build | ADR-0008 | SS-03 |
| Display application | `web/apps/display` | Preserves the separate classroom-public build boundary | ADR-0008 | SS-03 |
| Shared frontend packages | Create only when consumed; use private `@penatika/*` workspace names and explicit Display-safe ownership | Avoids speculative packages and prevents Teacher-private code from reaching Display dependencies | ADR-0008 shared-code/privacy boundary | SS-03 onward |
| Contract consumption | Selective transport-boundary generation plus conformance tests; no generated server stubs and no generated core domain types | Preserves contracts as authority without coupling core models to generator output | ADR-0010, ADR-0013, contracts README | SS-04 |
| Backend contract mapping | Handwritten HTTP/SSE adapter DTOs mapped explicitly to application/domain types and checked for OpenAPI/schema conformance | Avoids generated transport models becoming domain objects | ADR-0010, ADR-0013 | SS-04 and implementation slices |
| Frontend contract mapping | Role-scoped generated transport types/clients may live in dedicated private workspaces; exact generator selected after an SS-04 compatibility spike | Reduces drift while preventing Display from importing Teacher-only operations/types | ADR-0008, ADR-0010 | SS-04 |
| Backend test baseline | JUnit Jupiter through Spring Boot test support; context/build smoke in SS-02, behavior tests with implementation | Ecosystem-aligned baseline with no alternate runner | Testing strategy and standard | SS-02 onward |
| Frontend test baseline | Vitest + React Testing Library for unit/component tests; browser E2E assigned to Playwright when journeys exist | Vite-aligned fast tests plus user-facing DOM semantics; E2E remains targeted | ADR-0008; Testing strategy and standard | SS-03/SS-04 onward |
| Contract validation | Repository-level pinned validation commands for OpenAPI lint/bundle and Draft 2020-12 schema compilation; consumer conformance tests supplement them | Keeps machine contracts independently valid and source consumers accountable | ADR-0010; API and Testing standards | SS-04 |
| Persistence scaffolding | Add PostgreSQL/Flyway/JdbcClient mechanism and configuration boundaries only | ADR-0014 selects mechanism, not physical domain schema | ADR-0014 | SS-05 |
| Migration policy | Reserve Flyway location, but add no migration file until real schema is specified; never add an empty `V001__init.sql` | Migration files must own actual durable schema changes | ADR-0014; Data Persistence standard | First schema-owning implementation batch |
| Configuration namespace | Spring/application properties use `penatika.*`; server environment variables use `PENATIKA_*`; Vite-exposed public values use `VITE_PENATIKA_*` only | Clear ownership and Vite compatibility while keeping secrets server-side | Configuration and Security standards | SS-05 |
| Secrets | No secrets in Git, `.env`, images, frontend bundles, examples, or logs; use runtime environment/file-backed secret injection | Preserves existing security and deployment boundaries | Security standard; Configuration policy | SS-05 onward |
| Environment policy | Preserve `LOCAL` / `PILOT` / `PROD`; check in only safe base configuration and optional non-secret examples | Prevents developer-local or production credentials becoming source defaults | Configuration policy; ADR-0019 reference | SS-05 |

The Maven and Node choices are routine, reversible scaffolding selections. They
do not change the accepted system architecture and therefore do not require a
new ADR.

## 5. Frozen Target Repository Layout

The labels below assign creation ownership. SS-01 creates none of these source
directories.

```text
/
├── backend/                                      CREATE IN SS-02
│   ├── pom.xml                                   CREATE IN SS-02
│   ├── mvnw                                      CREATE IN SS-02
│   ├── mvnw.cmd                                  CREATE IN SS-02
│   ├── .mvn/wrapper/maven-wrapper.properties     CREATE IN SS-02
│   └── src/
│       ├── main/
│       │   ├── java/io/github/sipratama/penatika/
│       │   │   ├── PenatikaApplication.java      CREATE IN SS-02
│       │   │   ├── bootstrap/                    CREATE WHEN COMPOSITION EXISTS
│       │   │   ├── identity/                     CREATE IN SS-02
│       │   │   ├── lesson/                       CREATE IN SS-02
│       │   │   └── classroom/                    CREATE IN SS-02
│       │   └── resources/
│       │       ├── application.yaml              CREATE IN SS-05
│       │       └── db/migration/                  LATER; FIRST REAL MIGRATION ONLY
│       └── test/
│           └── java/io/github/sipratama/penatika/
│               ├── PenatikaApplicationTests.java CREATE IN SS-02
│               └── architecture/                 CURRENT
├── web/                                          CURRENT
│   ├── package.json                              CURRENT
│   ├── package-lock.json                         CURRENT
│   ├── .nvmrc                                    CURRENT
│   ├── apps/
│   │   ├── teacher/                              CURRENT
│   │   └── display/                              CURRENT
│   └── packages/
│       ├── transport-teacher/                    CURRENT; GENERATED TRANSPORT ONLY
│       ├── transport-display/                    CURRENT; GENERATED TRANSPORT ONLY
│       ├── scene-renderer/                       LATER, WHEN FIRST CONSUMED
│       ├── design-tokens/                        LATER, WHEN FIRST CONSUMED
│       └── ui/                                   LATER, DISPLAY-SAFE EXPORTS ONLY
├── contracts/                                    EXISTING; CANONICAL WIRE AUTHORITY
├── docs/                                         EXISTING; CANONICAL DOCS/PLANS
└── scripts/                                      EXISTING; REPOSITORY VALIDATION TOOLING
    └── contract-validation/                      CURRENT; CREATE IN SS-04
```

Inside a materialized backend business module, the representative convention
is:

```text
<module>/
├── domain/                       framework-light business concepts/rules
├── application/                  use-case orchestration
│   └── port/
│       ├── in/                   only when a meaningful input port exists
│       └── out/                  only for meaningful external boundaries
└── adapter/
    ├── in/                       HTTP/SSE/other inbound delivery adapters
    └── out/                      persistence/provider/technical adapters
```

Packages and interfaces are created only with a real responsibility. SS-02
must not populate every branch with placeholder types merely to make the tree
look complete.

## 6. Backend Scaffold Strategy

### Build and Artifact

- Use one Maven project rooted at `backend/`.
- Use the stable Maven `3.9.x` line, with the exact patch and distribution
  checksum pinned by the Maven Wrapper during SS-02.
- Use one executable artifact, `penatika-backend`.
- Use Java release `21`; remain JDK-vendor-neutral.
- Select a supported stable Spring Boot `4.x` patch during SS-02 through Spring
  Boot dependency management rather than individually versioning managed
  dependencies.
- Do not introduce Maven submodules until measured build or ownership pressure
  demonstrates value.

Maven is selected over Gradle because the current project needs one conventional
Java artifact, curated Spring dependency management, straightforward plugin
configuration, and low build-logic surface. Gradle remains a valid alternative,
but its additional DSL/build-model flexibility has no evidenced benefit for
this initial shape.

### Modules and Dependency Direction

- `identity` owns Teacher account/session and participant authority concepts.
- `lesson` owns lesson and immutable `LessonVersion` readiness concepts.
- `classroom` owns Classroom Session, Pairing, Controller command/revision,
  Classroom Scene projection, SSE, and Display synchronization concepts.
- Pairing is not a top-level module because its lifecycle and authority are
  bounded to Classroom Session behavior.
- `ai`, `mathematics`, and `curriculum` remain recognized architecture modules,
  but no empty source packages are created before their implementation scope.

Module internals are not public by default. Cross-module calls use supported
application/module boundaries, never another module's adapter, persistence
implementation, or internal domain package. No internal HTTP or broker is
introduced.

### Composition

`PenatikaApplication` lives at the base package so Spring can compose the
application. `bootstrap` may contain framework startup, dependency wiring,
cross-cutting security/CORS/property configuration, and technical composition.
It must not contain business workflows, module-owned invariants, transport
DTOs, or persistence queries.

## 7. Frontend Scaffold Strategy

- Use one `web/` npm workspace with two independently buildable Vite apps.
- Pin Node `24.x` LTS at an exact supported patch in `.nvmrc` during SS-03.
- Commit exactly one `package-lock.json` and an exact `packageManager` value.
- Use npm workspaces only; do not add Nx/Turborepo.
- `apps/teacher` contains Preparation and Controller modes/routes in one
  Teacher-private application.
- `apps/display` is a separate build and entry point with no Teacher command or
  private-state capability.

Shared packages are private workspace packages and exist only when at least one
real consumer requires them. Display dependencies are allow-listed. Display
must never depend on Teacher-private feature state, AI/private proposal state,
Teacher authorization logic, Controller behavior, or a generated transport
package that exposes Teacher-only operations.

## 8. Contract-to-Source Strategy

The existing OpenAPI and standalone JSON Schemas remain canonical. Source code
is a consumer and must not create parallel contract definitions.

- Do not generate code during SS-01.
- Do not generate Spring server stubs or use generated DTOs as domain models.
- Backend adapters use explicit transport types and mapping, with tests proving
  response/request and error conformance.
- SS-04 performs a bounded generator compatibility spike for frontend
  transport-only types/clients.
- Any generated frontend output is role-scoped so Display cannot import
  Teacher-only operations. Generated output includes traceable generator and
  source-contract version metadata and is reproducible from pinned tooling.
- `classroom-display-projection.schema.json` remains the sole reusable Display
  projection owner. Runtime/schema validation and generated TypeScript types,
  if used, derive from it rather than restating it.
- Repository-level contract validation remains independent of backend and
  frontend compilation.

The SS-04 spike accepted `openapi-typescript` `7.13.0`. A deterministic tooling
script derives disposable Teacher and Display subsets from the dereferenced
canonical OpenAPI document using explicit operation allowlists, generates each
role twice with no byte difference, rejects opposite-role operation IDs, and
checks the Display projection members resolved from the standalone schema.
Generated declarations require no patching and remain transport-only under
`web/packages/transport-teacher/` and `web/packages/transport-display/`.

## 9. Test and Architecture Fitness

### SS-02 Backend Baseline

- JUnit Jupiter through Spring Boot test support.
- Maven compile/test/package smoke evidence.
- Minimal application-context smoke only; no fake product behavior.

### SS-03 Frontend Baseline

- Vitest for unit tests.
- React Testing Library for component behavior using accessible/user-facing
  queries.
- Independent build and smoke test for Teacher and Display apps.
- Exact resolved baseline: Node.js `24.21.0` LTS (`Krypton`) with bundled npm
  `11.19.0`; React and React DOM `19.2.8`; Vite `8.2.2`; TypeScript `7.0.2`;
  Vitest `5.0.0`; React Testing Library `16.3.3`; jest-dom `7.0.1`; jsdom
  `30.0.1`; React type packages `@types/react` `19.2.18` and
  `@types/react-dom` `19.2.7`; Vite React plugin `6.1.1`.
- The root workspace owns shared development tooling; each private application
  manifest owns only its React/React DOM runtime dependencies. No shared
  workspace package or monorepo orchestrator exists.
- The `2026-09-09` macOS recovery proved clean npm installation plus independent
  and aggregate frontend build/test gates. The backend Maven project root is
  `backend/`; both `cd backend && ./mvnw test` and the repository-root equivalent
  `./backend/mvnw -f backend/pom.xml test` pass. Plain `./backend/mvnw test` from
  the repository root is not a supported project invocation because Maven uses
  the caller's working directory unless `-f` selects `backend/pom.xml`.

### SS-04 Fitness and Contract Harness

- ArchUnit `1.5.0` rules enforce inward domain/application dependencies,
  private `identity`/`lesson`/`classroom` domain and adapter internals, and the
  absence of global technical-layer dumping-ground packages. Test-only invalid
  fixtures prove the rules are non-vacuous while production matches may remain
  empty during scaffolding.
- A Node guard using the TypeScript `7.0.2` compiler API rejects Teacher/Display
  cross-imports, sibling manifest dependencies, arbitrary shared paths, and
  unapproved `@penatika/*` packages. Synthetic self-tests prove both directions
  and private-package denial.
- Redocly CLI `2.51.2` runs recommended OpenAPI lint with only
  `no-empty-servers`, `info-license`, and `no-unused-components` disabled, then
  produces an ignored dereferenced bundle from the canonical OpenAPI document.
- Ajv `8.17.1` in Draft 2020-12 mode resolves the canonical primitive `$id`,
  compiles the Display projection, and proves representative valid and invalid
  closed-boundary fixtures.
- `openapi-typescript` `7.13.0` is accepted for deterministic role-scoped,
  runtime-free transport declarations. The frontend allowlist permits Teacher
  only `transport-teacher` and Display only `transport-display`.
- Playwright is reserved for selected cross-app browser journeys once a real
  journey exists; no empty E2E ceremony is created in SS-04.

## 10. Persistence Boundary

SS-05 may scaffold only the selected mechanism:

- PostgreSQL `18.x` compatibility;
- Flyway `13.x` dependency/configuration boundary;
- Spring JDBC/JdbcClient access boundary;
- module-owned output ports and persistence adapters when a real use case
  needs them;
- integration-test support against real PostgreSQL behavior where practical.

No domain migration is authorized by SS-01. Do not add placeholder migration
files, ORM schema generation, JPA/Hibernate, Redis, cache, or vector database.
The first Flyway migration must be derived from an approved physical data-model
task and must own actual tables/constraints/indexes.

## 11. Security and Configuration Boundary

Source Scaffolding may establish locations and safe defaults for Spring
Security composition, trusted-origin/CORS policy, session/security adapters,
and typed configuration properties. It does not implement OIDC login, durable
browser sessions, PairingGrant security, Controller authorization, Display
authorization, or first-slice handlers.

No temporary `permitAll`-everything baseline is allowed. If a runnable shell
has no implemented public endpoint, protected behavior remains denied rather
than bypassed for convenience.

Configuration rules:

- backend properties use the `penatika.*` namespace;
- server environment variables use `PENATIKA_*`;
- `VITE_PENATIKA_*` is reserved for explicitly public frontend build-time
  configuration and can never contain credentials or server secrets;
- environment semantics remain `LOCAL`, `PILOT`, and `PROD`;
- committed configuration contains only safe defaults or non-secret examples;
- required secrets fail clearly when the owning feature is activated and are
  never silently replaced with insecure defaults.

## 12. Source Scaffolding Sequence

| Batch | Objective | Allowed Outputs | Forbidden Scope | Validation | Completion Condition |
|---|---|---|---|---|---|
| SS-01 | Freeze plan and physical layout | This plan and phase routing | Build/source/package/migration/deployment files | Project validator, diff/scope audit | Physical decisions explicit; SS-02 routable |
| SS-02 | Backend Build + Module Skeleton | Maven wrapper/POM, Java 21 Spring Boot shell, initial module package boundaries, backend smoke test | Product handlers, persistence schema, real auth/session behavior | Wrapper build/test/package and architecture-aware review | Reproducible backend shell with no business implementation |
| SS-03 | Frontend Workspace + Teacher/Display Application Shells | npm workspace/lockfile, two React/TS/Vite app shells, unit/component smoke tests | Product workflows, third Controller app, shared private/public coupling | Clean install, independent build/test for both apps | Reproducible separate Teacher and Display builds |
| SS-04 | Test + Contract Validation Harness | ArchUnit rules, frontend dependency guard, pinned OpenAPI/JSON Schema validation, generator spike/transport outputs if accepted | Domain implementation or contract edits hidden as harness work | Architecture tests, lint/bundle/schema compile, reproducibility check | Executable boundaries and contract-consumer strategy proven |
| SS-05 | Configuration + Persistence Mechanism Baseline | Safe config namespaces, typed property boundaries, PostgreSQL/Flyway/JdbcClient mechanism, integration smoke support | Domain DDL/migration, OIDC/pairing/command implementation, deployment files | Startup/config failure tests and PostgreSQL mechanism smoke | Mechanisms ready without invented product data model |
| SS-06 | Source Scaffolding Consistency / Readiness Audit | Focused remediation and phase/status evidence | First vertical-slice implementation or unrelated refactor | Full backend/frontend/contract/scaffolding validation | Whole phase coherent and merge-ready |

Normal batch flow is change → validation → commit → push/review → next batch on
`feat/source-scaffolding`. A completed SS batch does not create a checkpoint.
Create one final Source Scaffolding handoff checkpoint only after SS-06 when
the entire phase is complete and ready for deliberate merge, unless the human
explicitly requests an earlier legitimate handoff.

## 13. SS-01 Non-Goals

SS-01 does not create or implement:

- Java source or a Spring Boot bootstrap;
- Maven/Gradle files or wrappers;
- dependencies;
- React/Vite applications, npm initialization, or lockfiles;
- database migrations or physical domain schema;
- HTTP handlers or generated contract DTOs/clients;
- Teacher authentication or browser-session behavior;
- Classroom Session start, Pairing, `NEXT`, SSE, or Display synchronization;
- AI, speech, Mathematics, curriculum, or digital ink implementation;
- Docker, Compose, Caddy, deployment, or CI/CD;
- a checkpoint or application feature slice.

SS-01 freezes the physical plan only.

## 14. Current Position

```text
Architecture Foundation                       COMPLETE / MERGED
Contract Foundation                           COMPLETE / MERGED
Source Scaffolding                            ACTIVE
SS-01 Plan + Repository Layout Freeze         COMPLETE
SS-02 Backend Build + Module Skeleton         COMPLETE
SS-03 Frontend Workspace + Teacher/Display    COMPLETE
SS-04 Test + Contract Validation Harness      COMPLETE
SS-05 Configuration + Persistence Baseline    NEXT
SS-06 Consistency / Readiness Audit            PENDING
Application Implementation                    PENDING
```
