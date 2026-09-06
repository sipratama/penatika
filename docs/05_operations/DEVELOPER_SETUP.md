# Developer Setup — Penatika

> Active baseline for working with the initialized repository. Application setup commands will be added only after technology decisions and source scaffolding.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Active documentation baseline; application setup pending |
| Last Updated | `2026-09-06` |

## 1. Current Repository State

Penatika currently contains canonical product, feature, architecture, design, engineering, operations, and delivery documentation. Application source code, package manifests, database migrations, and deployment configuration do not yet exist.

Do not invent setup commands before the relevant stack is selected.

## 2. Current Prerequisites

- Git.
- Python 3 for the repository validator.
- A Markdown-capable editor.
- Access to the repository.

No Node.js, JVM, .NET, Go, Rust, database, container, or cloud prerequisite is currently authoritative.

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

- client strategy and frontend framework;
- backend language/framework;
- database and migration tooling;
- authentication approach;
- realtime protocol;
- contract formats and tooling;
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

All application-specific setup remains open because no implementation stack has been selected.

## 9. Change Log

| Date | Change | Author |
|---|---|---|
| `2026-09-06` | Initial pre-source developer setup baseline | Codex |
