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
- one Maven project rooted at `backend/`;
- no product endpoints or business implementation.

Frontend workspace:

- Node.js 24.21.0 and npm 11.19.0;
- React 19, TypeScript 7, and Vite 8;
- separate Teacher and Classroom Display application shells;
- no product behavior, API client, routing, auth, state library, PWA, or UI framework.

Contract Foundation:

- OpenAPI 3.1.2;
- Penatika HTTP contract version 0.8.0;
- reusable JSON Schemas using Draft 2020-12.

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
cloud infrastructure are not required for SS-03 validation.

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
npm run build
npm test
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
```

The current shells define build and test commands only; product runtime flows
and local application configuration have not been implemented.

## 5. Repository Validation

From the repository root, run:

```bash
python3 scripts/validate_template.py --project-mode
```

This validates local Markdown links and unresolved core metadata placeholders.
It does not yet run the SS-04 OpenAPI/JSON Schema validation harness or prove
product and architecture quality.

## 6. Required Reading Before Implementation

Read selectively:

1. `AGENTS.md`;
2. `docs/PROJECT_STATUS.md` for current phase routing;
3. the relevant PRD section and feature specification;
4. `docs/02_architecture/SYSTEM_ARCHITECTURE.md` and relevant ADRs;
5. relevant contracts under `contracts/`;
6. relevant standards under `docs/standards/`;
7. related source and tests.

## 7. Current Boundaries and Later Setup

Do not invent setup for mechanisms that are not implemented. Later work must
add exact instructions when the corresponding source exists, including:

- local configuration and secret handling;
- database setup and migrations;
- identity/provider fakes or local emulators;
- product application run commands;
- contract, architecture, integration, end-to-end, accessibility, and AI-evaluation commands;
- formatting and linting;
- deployment and operational setup.

Source Scaffolding must remain product-behavior-free until the planned batches
authorize implementation. Material architecture decisions remain governed by
`AGENTS.md` and Accepted ADRs.

## 8. Development Safety

- Never commit secrets or raw production/student data.
- Do not use raw classroom audio as a fixture.
- Prefer synthetic or sanitized lesson and evaluation data.
- Do not add a repository-root Maven aggregator; the backend project root is `backend/`.
- Do not weaken schemas, validation, authorization, or tests to make a prototype appear complete.

## 9. Open Work

SS-04 still needs to establish the architecture and contract validation
harness. Physical database schema/migrations, identity/session implementation,
persistence implementation, product behavior, local application configuration,
deployment files, CI/CD, and backup implementation remain pending.

## 10. Change Log

| Date | Change | Author |
|---|---|---|
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
