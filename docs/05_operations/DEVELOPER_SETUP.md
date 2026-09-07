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

Contract strategy is selected: contract-first OpenAPI 3.1.x for synchronous HTTP and JSON Schema Draft 2020-12 for justified reusable wire schemas. Redocly CLI is the baseline OpenAPI lint/bundle tool. AsyncAPI 3.1.x remains conditional on OAD-005. No field-level contracts exist, and contract tooling is not installed or configured yet.

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
- database and migration tooling;
- identity/authentication architecture — resolved by ADR-0011; provider, session store, and implementation remain pending;
- realtime protocol;
- contract formats and tooling — resolved by ADR-0010; field-level contracts remain pending;
- initial provider strategy for AI, speech, curriculum, and Mathematics validation;
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
installation or application run commands exist. Database and migration
technology, physical identity/session implementation, concrete OIDC provider,
realtime transport, field-level contracts, other providers, deployment, build
tool, JDK distribution, exact framework patches, and exact contract-tool pins
remain open.

## 9. Change Log

| Date | Change | Author |
|---|---|---|
| `2026-09-07` | Revise backend baseline to Java 21 LTS and record module-first Hexagonal Architecture (ADR-0013) without adding setup commands | Claude |
| `2026-09-07` | Record selected OIDC and backend-managed session architecture without inventing provider or setup commands | Codex |
| `2026-09-06` | Record contract-first OpenAPI/JSON Schema strategy without installing tooling or defining fields | Codex |
| `2026-09-06` | Record Java 25 LTS / Spring Boot 4.x modular-monolith backend without adding setup commands | Codex |
| `2026-09-06` | Record resolved client architecture without adding application setup commands | Codex |
| `2026-09-06` | Initial pre-source developer setup baseline | Codex |
