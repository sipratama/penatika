# Developer Setup — Penatika

> Active setup and validation baseline for the current Source Scaffolding phase.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Active Source Scaffolding setup |
| Last Updated | `2026-09-09` |

## 1. Current Repository State

The repository contains canonical documentation, contracts, and separate
backend and frontend build roots.

Backend shell:

- Java 21;
- Maven 3.9.16 through the committed wrapper;
- Spring Boot 4.1.1;
- ArchUnit 1.5.0 architecture fitness checks;
- one Maven project rooted at `backend/`;
- no product endpoints or business implementation.

Frontend workspace:

- Node.js 24.21.0 and npm 11.19.0;
- React 19, TypeScript 7, and Vite 8;
- separate Teacher and Classroom Display application shells;
- an executable TypeScript dependency-boundary guard;
- no product behavior, API client, routing, auth, state library, PWA, or UI framework.

Contract Foundation:

- OpenAPI 3.1.2;
- Penatika HTTP contract version 0.8.0;
- reusable JSON Schemas using Draft 2020-12.

Contract validation tooling:

- Redocly CLI 2.51.2 lint and dereferenced bundling;
- Ajv 8.17.1 Draft 2020-12 schema/fixture validation;
- deterministic role-scoped transport declarations generated with
  `openapi-typescript` 7.13.0.

The repository still has no physical database schema or migrations, product
application behavior, OIDC implementation, persistence implementation, or
deployment configuration.

## 2. Current Prerequisites

- Git;
- a Java 21-compatible JDK;
- Node.js 24.21.0;
- npm 11.19.0;
- Python 3;
- access to the repository.

The project does not mandate a specific JDK vendor. PostgreSQL, containers, and
cloud infrastructure are not required for SS-04 validation.

## 3. Backend Setup and Validation

The backend Maven project is rooted at `backend/`. From the repository root,
use the normal Unix/macOS invocation:

```bash
cd backend
./mvnw test
cd ..
```

The equivalent repository-root command is:

```bash
./backend/mvnw -f backend/pom.xml test
```

Both commands include the Spring context smoke test, production architecture
fitness rules, and negative test-fixture proof that the rules detect invalid
dependencies.

Plain `./backend/mvnw test` from the repository root is not a supported project
invocation. Running a wrapper script by path does not change Maven's working
directory; use `backend/` as the working directory or select
`backend/pom.xml` explicitly with `-f`.

To inspect the pinned toolchain from the repository root:

```bash
./backend/mvnw -f backend/pom.xml --version
```

## 4. Frontend Setup and Validation

Use Node.js 24.21.0 as recorded in `web/.nvmrc`. The repository does not mandate
a particular Node version manager.

```bash
cd web
node --version
npm --version
npm ci
npm test
npm run build
cd ..
```

Expected runtime versions are `v24.21.0` and npm `11.19.0`. `npm ci` installs
the exact committed lockfile resolution. The aggregate build and test commands
cover both application workspaces. From `web/`, individual commands are also
available:

```bash
npm run build:teacher
npm run test:teacher
npm run build:display
npm run test:display
npm run test:boundaries
```

The boundary command runs its synthetic violation self-tests and then checks
the real workspace. The current shells define build and test commands only;
product runtime flows and local application configuration have not been
implemented.

## 5. Contract Validation

The repository contract harness is independent of backend and frontend
compilation:

```bash
cd scripts/contract-validation
node --version
npm --version
npm ci
npm test
cd ../..
```

Expected runtime versions are Node `v24.21.0` and npm `11.19.0`. The aggregate
test lints and dereference-bundles the canonical OpenAPI document, compiles the
canonical Draft 2020-12 schemas by `$id`, validates representative positive and
negative fixtures, and verifies committed role-scoped transport declarations
are deterministic and current.

Intentional contract changes regenerate those accepted transport declarations
from the harness directory with:

```bash
npm run generate:transports
```

The harness `.tmp/` contents are disposable and non-authoritative.

## 6. Repository Validation

From the repository root, run:

```bash
python3 scripts/validate_template.py --project-mode
```

This validates local Markdown links and unresolved core metadata placeholders.
Run the independent contract harness above as well; neither structural command
by itself proves product and architecture quality.

## 7. Required Reading Before Implementation

Read selectively:

1. `AGENTS.md`;
2. `docs/PROJECT_STATUS.md` for current phase routing;
3. the relevant PRD section and feature specification;
4. `docs/02_architecture/SYSTEM_ARCHITECTURE.md` and relevant ADRs;
5. relevant contracts under `contracts/`;
6. relevant standards under `docs/standards/`;
7. related source and tests.

## 8. Current Boundaries and Later Setup

Do not invent setup for mechanisms that are not implemented. Later work must
add exact instructions when the corresponding source exists, including:

- local configuration and secret handling;
- database setup and migrations;
- identity/provider fakes or local emulators;
- product application run commands;
- integration, end-to-end, accessibility, and AI-evaluation commands;
- formatting and linting;
- deployment and operational setup.

Source Scaffolding must remain product-behavior-free until the planned batches
authorize implementation. Material architecture decisions remain governed by
`AGENTS.md` and Accepted ADRs.

## 9. Development Safety

- Never commit secrets or raw production/student data.
- Do not use raw classroom audio as a fixture.
- Prefer synthetic or sanitized lesson and evaluation data.
- Do not add a repository-root Maven aggregator; the backend project root is `backend/`.
- Do not weaken schemas, validation, authorization, or tests to make a prototype appear complete.

## 10. Open Work

SS-05 is next and owns the configuration and persistence mechanism baseline.
Physical product database schema/migrations, identity/session implementation,
product behavior, deployment files, CI/CD, and backup implementation remain
pending.

## 11. Change Log

| Date | Change | Author |
|---|---|---|
| `2026-09-09` | Establish SS-04 backend architecture, frontend boundary, contract validation, and deterministic transport-generation commands | Codex |
| `2026-09-09` | Synchronize setup with the Java/Maven backend shell, Node/npm Teacher and Display shells, Contract Foundation, and supported backend invocation semantics | Codex |
| `2026-09-08` | Record portable single-Linux-VPS MVP/pilot deployment baseline (ADR-0019) without adding Dockerfile, Compose, Caddy, or CI/CD commands | Claude |
| `2026-09-07` | Record OpenRouter bounded-generation AI baseline (ADR-0017) without adding AI SDK, API key, or dependency-install commands | Claude |
| `2026-09-07` | Record scoped deterministic Mathematics validator baseline (ADR-0016) without adding validator source, parser, or dependency-install commands | Claude |
| `2026-09-07` | Record controlled versioned curriculum corpus baseline (ADR-0015) without adding corpus/import/local setup commands | Claude |
| `2026-09-07` | Record PostgreSQL 18.x, Flyway 13.x, and Spring JDBC/JdbcClient persistence baseline (ADR-0014) without adding installation, Docker, or migration commands | Claude |
| `2026-09-07` | Revise backend baseline to Java 21 LTS and record module-first Hexagonal Architecture (ADR-0013) without adding setup commands | Claude |
| `2026-09-07` | Record selected OIDC and backend-managed session architecture without inventing provider or setup commands | Codex |
| `2026-09-06` | Record contract-first OpenAPI/JSON Schema strategy without installing tooling or defining fields | Codex |
| `2026-09-06` | Record Java 25 LTS / Spring Boot 4.x modular-monolith backend without adding setup commands | Codex |
| `2026-09-06` | Record resolved client architecture without adding application setup commands | Codex |
| `2026-09-06` | Initial pre-source developer setup baseline | Codex |
