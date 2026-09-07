# Known Limitations — Penatika

> Active list of unsupported or unresolved behavior. Initialization does not imply an application build exists.

## Current Repository Limitations

- No application source code or executable prototype exists; client and backend architecture are selected but not scaffolded.
- The backend baseline is Java 25 LTS + Spring Boot 4.x, but the JDK distribution, exact framework patch, build tool, database, identity system, realtime transport, providers, field-level contracts, and deployment platform are not selected.
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

- Curriculum ingestion, normalization, integrity/versioning, local-context modeling, retrieval, and official-guidance usage/licensing.
- Supported structured content blocks.
- Target device/browser/display matrix.
- Formal accessibility target.
- Exact identity/pairing implementation where relevant.
- Field-level contracts.
- Remaining persistence, identity, realtime, field-level contract, provider, build-tool, and deployment decisions.
- Numerical latency/reliability targets where evidence is required.

## Trust and Quality Limitations

- Deterministic validation cannot be assumed to cover all Mathematics content.
- Unsupported or inconclusive content must remain explicitly labeled.
- AI quality, latency, privacy, and cost have not been evaluated against candidate providers.
- Normative curriculum authority is selected, but operational curriculum grounding cannot be claimed until ingestion, integrity/versioning, provenance, retrieval, and applicable guidance-usage implementation are complete and tested.
- Market and classroom efficacy are product hypotheses, not validated outcomes.

## Prior Product Context

Penatika succeeds the Pendago / AI Teaching Canvas concept. Pendago material has been reviewed and classified in [PENDAGO_MIGRATION_REVIEW.md](./PENDAGO_MIGRATION_REVIEW.md). Only explicitly mapped material may influence Penatika.

## Last Updated

`2026-09-06`
