# Known Limitations — Penatika

> Active list of unsupported or unresolved behavior. Initialization does not imply an application build exists.

## Current Repository Limitations

- No application source code or executable prototype exists; client and backend architecture are selected but not scaffolded.
- The backend baseline is Java 21 LTS + Spring Boot 4.x with module-first Hexagonal Architecture; persistence technology is selected as PostgreSQL 18.x + Flyway 13.x + Spring JDBC/JdbcClient (ADR-0014), but physical schema, SQL migration files, indexes, query implementation, and runtime data access do not yet exist; identity architecture is OIDC with backend-managed browser sessions, but the JDK distribution, exact framework patch, build tool, concrete OIDC provider, physical session store, realtime transport, other providers, field-level contracts, backup/recovery, managed database deployment, and deployment platform are not selected.
- Contract strategy is selected, but no field-level OpenAPI or JSON Schema contract exists yet; AsyncAPI remains inactive pending OAD-005.
- Developer setup contains no application install/run commands.
- Deployment, runbook, and release checklist remain conditional.

## Product Limitations for the MVP Baseline

- Subject scope is Mathematics only.
- Grade scope is 4 SD through 9 SMP.
- Validation work initially prioritizes Grade 5 fractions and Grade 7 basic algebra or linear equations.
- Student devices are not supported as a core experience.
- PDF/PowerPoint import, attendance, school administration, advanced graphing, and 3D visualization are not included.
- The product does not act as an autonomous teacher.
- No proprietary smart board is required or selected.

## Unresolved Behavior

- Curriculum architecture is selected (ADR-0015: controlled versioned corpus with deterministic metadata-first retrieval), but no normalized curriculum corpus/import artifact, physical curriculum schema, ingestion tooling, or runtime retrieval implementation exists yet. Official Guidance substantial-content usage remains pending explicit licensing/usage review; no legal approval is claimed.
- Mathematics validator architecture is selected (ADR-0016: scoped deterministic `EXACT_RATIONAL`/`AFFINE_EXPRESSION`/`LINEAR_EQUATION` validators using exact rational arithmetic), but no validator source, parser implementation, Apache Commons Numbers dependency, Mathematics contract/schema, or test corpus implementation exists yet.
- AI generation gateway architecture is selected (ADR-0017: OpenRouter, server-owned `ROUTER`/`FAST`/`QUALITY` model profiles, pre-provider scope/resource/allowance pipeline, privacy-constrained provider routing), but no AI SDK, API key, dependency, approved provider-route evidence, contract, or implementation exists yet.
- Supported structured content blocks.
- Target device/browser/display matrix.
- Formal accessibility target.
- Identity architecture is selected, but the concrete OIDC provider, physical session store, deployment configuration, exact cookie/CSRF details, and field-level auth contracts are not implemented.
- Field-level contracts.
- Remaining persistence, identity implementation/provider, realtime, field-level contract, provider, build-tool, and deployment decisions.
- Numerical latency/reliability targets where evidence is required.

## Trust and Quality Limitations

- Deterministic validation cannot be assumed to cover all Mathematics content; MVP scope is limited to exact-rational fraction arithmetic and restricted one-variable affine/linear-equation algebra (ADR-0016).
- Unsupported or inconclusive content must remain explicitly labeled.
- AI quality, latency, privacy, and cost have not been evaluated against candidate providers.
- Normative curriculum authority and its ingestion/versioning/retrieval architecture are selected, but operational curriculum grounding cannot be claimed until the corpus, physical schema, retrieval implementation, and applicable guidance-usage review are complete and tested.
- Market and classroom efficacy are product hypotheses, not validated outcomes.

## Prior Product Context

Penatika succeeds the Pendago / AI Teaching Canvas concept. Pendago material has been reviewed and classified in [PENDAGO_MIGRATION_REVIEW.md](./PENDAGO_MIGRATION_REVIEW.md). Only explicitly mapped material may influence Penatika.

## Last Updated

`2026-09-07`
