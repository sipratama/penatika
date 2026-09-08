# Developer Setup — Penatika

> Active baseline for working with the initialized repository. Application setup commands will be added only after technology decisions and source scaffolding.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Active documentation baseline; application setup pending |
| Last Updated | `2026-09-07` |

## 1. Current Repository State

Penatika currently contains canonical product, feature, architecture, design, engineering, operations, and delivery documentation. Application source code, package manifests, database migrations, and deployment configuration do not yet exist.

Client architecture is selected: React, TypeScript, Vite, and browser-first Teacher Web + Classroom Display. No frontend source exists and no dependency installation command exists yet. Package manager and exact runtime version are not selected merely by this ADR.

Backend architecture is selected: Java 21 LTS, Spring Boot 4.x, one deployable modular monolith, and module-first Hexagonal Architecture (ADR-0013). No backend source exists. The exact Spring Boot patch, supported OpenJDK-compatible JDK distribution, and build tool will be selected during source scaffolding.

Persistence architecture is selected: PostgreSQL 18.x, Flyway 13.x, and Spring JDBC/JdbcClient-style SQL-first persistence adapters behind module-owned output ports (ADR-0014). No physical schema, migrations, datasource configuration, or local PostgreSQL setup exists yet.

Curriculum architecture is selected: a controlled, versioned, human-verified curriculum corpus with deterministic metadata-first retrieval (ADR-0015). No curriculum corpus data, import artifact, ingestion tooling, or local setup command exists yet.

Mathematics validator architecture is selected: scoped deterministic `EXACT_RATIONAL`, `AFFINE_EXPRESSION`, and `LINEAR_EQUATION` validators using exact rational arithmetic, with Apache Commons Numbers `BigFraction` as the initial primitive (ADR-0016). No validator source, parser implementation, Apache Commons Numbers dependency installation, Mathematics contract/schema, or test corpus implementation exists yet.

AI generation architecture is selected: OpenRouter as the controlled generative-model gateway behind a Penatika-owned `GenerativeModelPort`, with server-owned `ROUTER`/`FAST`/`QUALITY` model profiles and a pre-provider scope/resource/per-teacher-allowance pipeline (ADR-0017). No AI SDK, OpenRouter API key, dependency installation, generation/usage contract or schema, or implementation exists yet.

Contract strategy is selected: contract-first OpenAPI 3.1.x for synchronous HTTP and JSON Schema Draft 2020-12 for justified reusable wire schemas. Redocly CLI is the baseline OpenAPI lint/bundle tool. AsyncAPI 3.1.x remains conditional on OAD-005. No field-level contracts exist, and contract tooling is not installed or configured yet.

Deployment architecture is selected: a portable single-Linux-VPS MVP/pilot baseline — Ubuntu Server 24.04 LTS, Docker Engine + Docker Compose, Caddy as reverse proxy/HTTPS entry point, PostgreSQL colocated on the same host for `PILOT`, an off-host backup boundary, and a `LOCAL`/`PILOT`/`PROD` environment model — initially deployed on Tencent Cloud Lighthouse, Jakarta, as a replaceable provider ([ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md)). No VPS is provisioned, no Dockerfile exists, no Compose file exists, no Caddyfile exists, no GitHub Actions workflow exists, no GHCR pipeline exists, no deployment credential exists, and no backup implementation exists yet. This documentation change provisions no production/pilot infrastructure.

Identity architecture is selected: OpenID Connect Authorization Code flow with
PKCE `S256`, the Penatika Backend as confidential OIDC client/relying party,
and backend-managed browser sessions. Penatika has no local teacher password
store, and OAuth/OIDC tokens are not stored by browser application JavaScript.
The concrete OIDC provider, local provider/mock setup, physical session store,
and exact security configuration are not selected. No identity setup commands
exist or should be invented yet.

Do not invent setup commands before the remaining stack is selected.

## 2. Current Prerequisites

- Git.
- Python 3 for the repository validator.
- A Markdown-capable editor.
- Access to the repository.

No Node.js or Java installation is currently required to validate this documentation-only repository. Java 21 will become the backend runtime prerequisite when source scaffolding begins, but the JDK distribution and installation procedure are not yet selected. No database, container, or cloud prerequisite is currently authoritative.

## 3. Validate the Initialized Repository

Run:

```bash
python3 scripts/validate_template.py --project-mode
```

This validates Markdown links and unresolved core metadata placeholders. It does not validate product quality or application behavior.

## 4. Required Reading Before Implementation

Read selectively:

1. `AGENTS.md`;
2. `docs/PROJECT_STATUS.md` when current phase / unresolved architecture decisions need to be understood;
3. relevant section of `docs/00_product/PRD.md`;
4. relevant feature specification in `docs/01_features/`;
5. `docs/02_architecture/SYSTEM_ARCHITECTURE.md` and relevant ADRs;
6. relevant contracts after they are created;
7. relevant standards in `docs/standards/`;
8. source and tests after they exist.

## 5. Before Source Scaffolding

Resolve and record at minimum:

- client strategy and frontend framework — resolved by ADR-0008;
- backend language/framework and module-first Hexagonal Architecture — resolved by ADR-0013 (supersedes ADR-0009);
- database and migration tooling — resolved by ADR-0014 (PostgreSQL 18.x, Flyway 13.x, Spring JDBC/JdbcClient); physical schema and migrations remain pending;
- curriculum ingestion/normalization/retrieval — resolved by ADR-0015 (controlled versioned corpus, deterministic metadata-first retrieval); the actual corpus/import artifact remains pending;
- Mathematics validator approach — resolved by ADR-0016 (scoped deterministic exact-rational and restricted affine/linear-equation validators); the validator source/parser/dependency pin remains pending;
- AI generation gateway strategy — resolved by ADR-0017 (OpenRouter, server-owned `ROUTER`/`FAST`/`QUALITY` model profiles, pre-provider scope/resource/allowance pipeline); the port/adapter implementation, approved provider-route evidence, and dependency pin remain pending;
- identity/authentication architecture — resolved by ADR-0011; provider, session store, and implementation remain pending;
- realtime protocol;
- contract formats and tooling — resolved by ADR-0010; field-level contracts remain pending;
- speech recognition provider strategy;
- deployment platform, environments, and secret-management approach — resolved by ADR-0019 (portable single-Linux-VPS baseline, initially Tencent Cloud Lighthouse Jakarta); VPS provisioning, Dockerfile/Compose/Caddy, CI/CD, and backup implementation remain pending;
- local secret/configuration approach.

Material decisions require ADRs where defined by `AGENTS.md`.

## 6. Future Setup Sections

When source exists, this document must add exact, executed instructions for:

- dependency installation;
- local configuration and secret handling;
- database setup and migrations;
- provider fakes or local emulators;
- running each client and backend;
- unit, integration, contract, end-to-end, accessibility, and AI-evaluation commands;
- formatting and linting;
- supported operating systems or known setup limitations.

## 7. Development Safety

- Never commit secrets or raw production/student data.
- Do not use raw classroom audio as a fixture.
- Prefer synthetic or sanitized lesson and evaluation data.
- Do not create application folders until architecture and stack decisions justify their structure.
- Do not weaken schema, validation, authorization, or tests to make a prototype appear complete.

## 8. Open Decisions

Frontend and backend source have not been scaffolded, and no dependency
installation or application run commands exist. Physical identity/session
implementation, concrete OIDC provider, physical database schema/migrations,
field-level contracts, other providers, build tool, JDK distribution, exact
framework patches, and exact contract-tool pins remain open. Deployment
architecture is selected (ADR-0019), but VPS provisioning, Dockerfile,
Compose file, Caddy configuration, CI/CD pipeline, deployment credentials,
and backup implementation remain open.

## 9. Change Log

| Date | Change | Author |
|---|---|---|
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
