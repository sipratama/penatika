# Source Scaffolding Checkpoint — Complete

> **Document role:** This document is a historical handoff snapshot and
> merge-readiness record. It is **non-authoritative**. If it conflicts with a
> current canonical document, the canonical document wins.

## 1. Purpose

This checkpoint records completion of the Penatika Source Scaffolding phase on
`feat/source-scaffolding`: SS-01 through SS-06, the established physical
engineering baseline, the reviewed validation evidence, and the deliberate
handoff boundary before merge into `main`.

Product/application implementation has **not** started. This checkpoint does
not replace the authoritative product, architecture, contract, data, policy,
standards, or living status documents.

## 2. Snapshot Metadata

| Field | Value |
|---|---|
| Date | `2026-09-10` |
| Branch | `feat/source-scaffolding` |
| Pre-checkpoint Source Scaffolding HEAD | `9cda13938612af33d09b9c812ef4672b4ab6efb2` |
| `origin/main` baseline SHA | `8b93c0415e913e194f93dcbabb264f4141fd65bd` |
| Merge-base SHA | `8b93c0415e913e194f93dcbabb264f4141fd65bd` (`origin/main` is an ancestor of the pre-checkpoint HEAD) |
| OpenAPI | `3.1.2` |
| Penatika HTTP contract version | `0.8.0` |
| JSON Schema | Draft 2020-12 |
| Implementation state | Source Scaffolding complete; application implementation not started |

The checkpoint commit SHA will exist only after human review and commit; it is
intentionally not invented in this snapshot.

## 3. Completed Source Scaffolding Scope

- **SS-01** froze the physical repository, backend build, and frontend
  workspace strategy.
- **SS-02** established the Java/Maven/Spring Boot backend shell and initial
  physical capability roots.
- **SS-03** established the npm workspace and separate Teacher and Classroom
  Display application shells.
- **SS-04** established executable architecture and privacy-boundary checks,
  contract validation, and deterministic role-scoped transport generation.
- **SS-05** established typed configuration and the disabled-by-default
  PostgreSQL/Flyway/JdbcClient persistence mechanism baseline.
- **SS-06** completed the cross-cutting consistency and merge-readiness audit.

## 4. Frozen Physical Repository Shape

```text
/
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .mvn/
│   └── src/
├── web/
│   ├── apps/
│   │   ├── teacher/
│   │   └── display/
│   ├── packages/
│   │   ├── transport-teacher/
│   │   └── transport-display/
│   └── scripts/
├── contracts/
├── scripts/
│   └── contract-validation/
└── docs/
```

Build and generated directories such as `target/`, `node_modules/`, `dist/`,
and `.tmp/` are not part of the source structure.

## 5. Backend Baseline

| Concern | Established baseline |
|---|---|
| Java | 21 |
| Maven | 3.9.16 through the committed wrapper |
| Spring Boot | 4.1.1 |
| Project shape | One Maven build project and one executable backend artifact |
| Coordinates | `io.github.sipratama.penatika:penatika-backend` |
| Architecture | One deployable modular monolith with module-first Hexagonal boundaries |
| Initial capability roots | `identity`, `lesson`, `classroom` |

Package structures are materialized only when a real responsibility exists.
No production `ai`, `mathematics`, or `curriculum` package is scaffolded solely
as a placeholder, and no global technical-layer-first source structure has
been introduced.

## 6. Frontend Baseline

| Concern | Established baseline |
|---|---|
| Node.js | 24.21.0 |
| npm | 11.19.0 |
| React / React DOM | 19.2.8 |
| Vite | 8.2.2 |
| TypeScript | 7.0.2 |
| Vitest | 5.0.0 |
| React Testing Library | 16.3.3 |
| Teacher Web | `web/apps/teacher` (`@penatika/teacher`) |
| Classroom Display Web | `web/apps/display` (`@penatika/display`) |

Preparation and Controller remain modes/routes of Teacher Web rather than a
third application. Teacher and Classroom Display are distinct build
boundaries. The workspace uses native npm workspaces without Nx or Turborepo.
Source Scaffolding did not select a production router, state/query framework,
PWA strategy, or UI framework, and neither application shell contains product
behavior.

## 7. Contract / Transport Baseline

The canonical HTTP contract remains
[`contracts/openapi/openapi.yaml`](../../contracts/openapi/openapi.yaml), using
OpenAPI `3.1.2` and Penatika HTTP contract version `0.8.0`. Reusable Draft
2020-12 schemas remain owned by
[`wire-primitives.schema.json`](../../contracts/schemas/wire-primitives.schema.json)
and
[`classroom-display-projection.schema.json`](../../contracts/schemas/classroom-display-projection.schema.json).

The pinned tooling baseline is Redocly CLI `2.51.2`, Ajv `8.17.1`, and
openapi-typescript `7.13.0`. Generated transport declarations are isolated in
`@penatika/transport-teacher` and `@penatika/transport-display`.

- Canonical contracts remain authoritative.
- Generated declarations are transport-only, not domain or application types.
- Generation is deterministic and derived from canonical operation allowlists.
- Generated files are never hand-edited.
- Teacher and Display transport boundaries remain separated.

## 8. Executable Architecture Safeguards

Backend ArchUnit rules enforce inward domain dependencies, prevent application
code from depending outward improperly, isolate module internals, reject a
global technical-layer architecture, and keep JDBC, DataSource, Flyway, and
PostgreSQL technology out of business domain/application code. Deliberately
invalid test-only fixtures prove these rules are non-vacuous.

The frontend boundary guard enforces:

- Teacher cannot depend on the Display application.
- Display cannot depend on the Teacher application.
- Teacher may use `@penatika/transport-teacher` only.
- Display may use `@penatika/transport-display` only.
- Opposite-role transport dependencies are forbidden.
- Arbitrary shared-source escape paths and unapproved `@penatika/*` packages
  are forbidden.

## 9. Configuration Baseline

Typed application configuration uses the `penatika.*` namespace. Supported
environment values are `LOCAL`, `PILOT`, and `PROD`, with `LOCAL` as the
default. Server environment variables use the `PENATIKA_*` prefix, including:

- `PENATIKA_ENVIRONMENT`
- `PENATIKA_PERSISTENCE_ENABLED`
- `PENATIKA_DB_URL`
- `PENATIKA_DB_USERNAME`
- `PENATIKA_DB_PASSWORD`
- `PENATIKA_MIGRATIONS_ENABLED`

Persistence and migrations are disabled by default. Secrets remain external;
no operational credentials are committed or copied into this checkpoint.

## 10. Persistence Mechanism Baseline

| Concern | Established baseline |
|---|---|
| PostgreSQL | 18.x |
| Verified integration image | `postgres:18.6-bookworm` |
| PostgreSQL JDBC | 42.7.13 |
| Flyway | 13.5.0 |
| SQL access | Spring JDBC / JdbcClient |
| Testcontainers | 2.0.5 |
| Maven Failsafe | 3.5.6 |

A real PostgreSQL integration smoke proves DataSource composition, JdbcClient
`SELECT 1`, and the Flyway empty baseline. Versioned domain migrations and
product tables both remain at **zero**. No JPA/Hibernate, Redis, cache, vector
database, or product persistence port/adapter has been introduced. This
mechanism baseline does **not** mean a physical product schema exists.

## 11. Test / Validation Baseline

The established executable gates are:

```text
cd backend && ./mvnw test
cd backend && ./mvnw verify
cd web && npm ci && npm test && npm run build
cd scripts/contract-validation && npm ci && npm test
python3 scripts/validate_template.py --project-mode
```

`./mvnw test` is the database-free backend fast gate. `./mvnw verify` adds the
PostgreSQL integration smoke.

The following evidence is inherited from the reviewed SS-06 audit at the
pre-checkpoint HEAD; these expensive suites were not rerun merely to author
checkpoint prose:

| Gate | Reviewed SS-06 result |
|---|---|
| Backend fast tests | 16/16 PASS |
| PostgreSQL integration | 1/1 PASS |
| Teacher tests | 1/1 PASS |
| Display tests | 1/1 PASS |
| Frontend boundary self-tests | 5/5 PASS |
| Ajv schema tests | 9/9 PASS |
| Redocly lint/bundle | PASS |
| Generator current, deterministic, and role-isolated | PASS |
| Project-mode template validator | PASS |
| `git diff --check` | PASS |

No CI evidence is claimed; no CI pipeline exists yet.

## 12. Source Scaffolding vs Product Implementation Boundary

Source Scaffolding established builds, workspace/application shells, physical
module roots, architecture fitness, the contract harness, role-scoped
transport declarations, typed configuration, the persistence mechanism, and
the integration-test mechanism.

It did **not** implement:

- Teacher authentication/OIDC or backend browser sessions;
- Lesson or Classroom Session behavior/persistence;
- PairingGrant, Controller participant, or Display participant behavior;
- `DIRECT_ACTION: NEXT`, revision, or idempotency business logic;
- the Display snapshot, SSE, or Display synchronization endpoints;
- AI generation, speech, digital ink, Mathematics validation runtime, or
  curriculum ingestion/retrieval runtime;
- deployment or CI/CD.

## 13. Contract / Architecture Integrity

- Contract changes during SS-06: **NONE**.
- Architecture decision changes during SS-06: **NONE**.
- ADR changes during SS-06: **NONE**.

The Source Scaffolding phase consumes the completed Architecture and Contract
Foundations; it does not replace or supersede them. Canonical contracts and
ADRs remain authoritative over this historical checkpoint.

## 14. Explicitly Deferred Work

- first vertical slice application implementation;
- physical product database schema, real Flyway domain migrations, and
  module-owned persistence ports/adapters;
- OIDC/browser-session implementation and authorization/CSRF runtime;
- Pairing and Classroom command/revision/idempotency runtime;
- SSE/reconnect and Display synchronization runtime;
- AI/OpenRouter and Deepgram integrations;
- digital ink, Mathematics runtime, and curriculum ingestion/retrieval runtime;
- Playwright activation when a real cross-application journey exists;
- Docker/Compose/Caddy deployment, CI/CD, and pilot execution.

## 15. Historical Notes

The previous phase handoff is the immutable
[Contract Foundation Checkpoint — Complete](./2026-09-09-contract-foundation-complete.md).
It remains historically correct, as do both Architecture Foundation
checkpoints. This Source Scaffolding checkpoint is simply the latest
phase-handoff snapshot and does not supersede canonical architecture or
contracts.

Published SS-03 history containing wording equivalent to "not yet solved"
also remains untouched. Later recovery and status commits resolved that state;
published Git history is not rewritten for cosmetic reasons.

## 16. Merge Readiness

| Check | Result |
|---|---|
| `origin/main` SHA | `8b93c0415e913e194f93dcbabb264f4141fd65bd` |
| Feature pre-checkpoint SHA | `9cda13938612af33d09b9c812ef4672b4ab6efb2` |
| Merge-base | `8b93c0415e913e194f93dcbabb264f4141fd65bd` |
| `origin/main` ancestor check | PASS (exit 0) |
| Non-mutating `git merge-tree` preview | PASS; no conflict reported in committed history |

The branch is ready for deliberate merge only after this checkpoint is
reviewed, checkpoint/status validation passes, a human commits and pushes the
changes, and the remote checkpoint is verified. This task does not merge.

Because the checkpoint remains an uncommitted working-tree change during its
creation, the merge-tree preview covers the existing committed branch history
only. It cannot represent this checkpoint until a human commits it.

## 17. Next Phase After Merge

After `feat/source-scaffolding` is deliberately merged into `main`, application
implementation may begin. A likely next planning task is the first protected
vertical slice already defined by Contract Foundation, but this checkpoint
does not implement or redesign it. Exact branch and task sequencing is decided
after the Source Scaffolding merge is verified.
