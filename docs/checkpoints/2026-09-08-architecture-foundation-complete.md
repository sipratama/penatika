# Architecture Foundation Checkpoint — Complete

> **Document role:** This is a historical handoff snapshot and merge-readiness record. It is **non-authoritative**. If this checkpoint conflicts with a current canonical document, the canonical document wins.

## 1. Purpose

This checkpoint records the state of the Penatika Architecture Foundation at the point where every unconditional Open Architecture Decision (OAD) is resolved and the `feat/architecture-foundation` branch is ready for deliberate merge into `main`. It exists to give a future contributor or AI agent a fast, accurate cross-device orientation snapshot without having to re-derive it from the full ADR history.

It supersedes the older [2026-09-07 checkpoint after OAD-004](./2026-09-07-architecture-foundation-after-oad004.md) **as a handoff snapshot only**. That older checkpoint remains historically truthful for its own creation date and is not modified; it is simply no longer the latest snapshot.

## 2. Snapshot Metadata

| Field | Value |
|---|---|
| Date | `2026-09-08` |
| Branch | `feat/architecture-foundation` |
| Pre-checkpoint architecture HEAD | `6d5bdd52627c388673dd618bf6693aa2120323f0` |
| origin/main baseline SHA | `7af7d3092796b597ca02d2f505651238f5964b1d` |
| Merge-base SHA | `7af7d3092796b597ca02d2f505651238f5964b1d` (origin/main is a direct ancestor of HEAD) |
| Implementation state | Pre-source / Pre-scaffolding |

This checkpoint was authored after a documentation-only audit remediation pass on top of the pre-checkpoint architecture HEAD above. The working tree at checkpoint-authoring time therefore differs from that HEAD by a small set of minor consistency fixes (listed in §8) plus this checkpoint file and the `PROJECT_STATUS.md` update. The final commit SHA that actually contains this checkpoint will be created only after human review of the working tree; it is intentionally not invented here.

## 3. Product Baseline Summary

Penatika is a teacher-first teaching copilot for Indonesian Mathematics teachers (Grade 4 SD–9 SMP) that separates a private teacher controller from a classroom-safe display, keeps backend-authoritative classroom session state, and treats AI output as an untrusted, schema/policy/assurance-gated proposal rather than an authority. The initial pilot is scoped to Grade 5 Fractions and Grade 7 Basic Algebra/Linear Equations under a teacher-first freemium SaaS model. Product Discovery, the Q-01–Q-07/OPD-001–OPD-007 product decision set, product governance, business model, data-retention policy, and the first-pilot plan are all resolved — see [Product Brief](../00_product/PRODUCT_BRIEF.md), [PRD](../00_product/PRD.md), and [PILOT_PLAN.md](../06_delivery/PILOT_PLAN.md).

## 4. Architecture Decision Summary

- **Frontend** — React + TypeScript + Vite, browser-first; Teacher Web (Preparation + private Controller) and Classroom Display Web are separate build/deployment artifacts; no Next.js/SSR baseline; no offline authoritative replay. ([ADR-0008](../02_architecture/adr/ADR-0008-browser-first-react-client-strategy.md))
- **Backend** — Java 21 LTS, Spring Boot 4.x, one deployable modular monolith, module-first Hexagonal Architecture; framework-light domain; ports only at meaningful boundaries; no global controller/service/repository/entity layering. ([ADR-0013](../02_architecture/adr/ADR-0013-java21-module-first-hexagonal-backend.md), supersedes historical [ADR-0009](../02_architecture/adr/ADR-0009-java-spring-boot-backend.md))
- **Persistence** — PostgreSQL 18.x, Flyway-controlled SQL migrations, Spring JDBC/JdbcClient SQL-first adapters; no JPA/Hibernate, Redis, cache, or vector database. ([ADR-0014](../02_architecture/adr/ADR-0014-postgresql-flyway-sql-first-persistence.md))
- **Identity** — OIDC Authorization Code + PKCE `S256`, backend-managed opaque browser sessions, OAuth/OIDC tokens server-side only; concrete OIDC provider still deferred. ([ADR-0011](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md))
- **Realtime** — synchronous HTTPS commands plus Server-Sent Events push, `Last-Event-ID` resync into backend-authoritative reconciliation; no WebSocket or queue requirement; AsyncAPI inactive. ([ADR-0012](../02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md))
- **Contracts** — contract-first OpenAPI 3.1.x + JSON Schema Draft 2020-12; strategy complete, field-level contracts intentionally not started. ([ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md))
- **Curriculum** — controlled, versioned, human-verified corpus with deterministic metadata-first retrieval; BSKAP 046/H/KR/2025 normative authority; AI is never curriculum authority; no live-web or vector retrieval baseline. ([ADR-0015](../02_architecture/adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md))
- **Mathematics** — scoped deterministic validators (`EXACT_RATIONAL`, `AFFINE_EXPRESSION`, `LINEAR_EQUATION`) using exact rational arithmetic; no floating-point truth, no general CAS, no LLM-as-validator. ([ADR-0016](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md))
- **Generative AI** — OpenRouter behind a Penatika-owned `GenerativeModelPort`; server-owned `ROUTER`/`FAST`/`QUALITY` profiles; pre-provider scope/resource/allowance pipeline; privacy-constrained routing (`zdr=true`, `data_collection=deny`); no autonomous agent/tool-loop, no uncontrolled fallback. ([ADR-0017](../02_architecture/adr/ADR-0017-openrouter-bounded-generation-and-usage-controls.md))
- **Speech** — Deepgram Nova-3 Indonesian, Australia regional endpoint, `mip_opt_out=true`; backend-mediated completed-utterance push-to-talk; deterministic `DIRECT_ACTION`-first routing; raw audio non-durable by default; no always-listening baseline. ([ADR-0018](../02_architecture/adr/ADR-0018-deepgram-push-to-talk-speech-recognition.md))
- **Deployment** — portable single ordinary Linux VPS (Ubuntu Server 24.04 LTS), Docker Engine + Docker Compose, Caddy for HTTPS, PostgreSQL colocated on the same host for `PILOT`, an off-host backup boundary, `LOCAL`/`PILOT`/`PROD` environments; initial replaceable provider is Tencent Cloud Lighthouse, Jakarta; no Kubernetes, no managed-cloud requirement; single VPS is one failure domain. ([ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md))

## 5. Final OAD Register

| ID | Decision | Status |
|---|---|---|
| OAD-001 | Client application strategy and frontend framework(s) | COMPLETE |
| OAD-002 | Backend language and framework | COMPLETE |
| OAD-003 | Database and migration technology | COMPLETE |
| OAD-004 | Identity, authentication, and account model | COMPLETE |
| OAD-005 | Realtime transport and reconnect protocol | COMPLETE |
| OAD-006 | AI provider/model strategy and fallback | COMPLETE |
| OAD-007 | Speech recognition strategy | COMPLETE |
| OAD-008 | Mathematics validator approach per content type | COMPLETE |
| OAD-009 | Curriculum ingestion, normalization, integrity/versioning, local-context modeling, and retrieval | COMPLETE |
| OAD-010 | Deployment platform, environments, secret management, and regional requirements | COMPLETE |
| OAD-011 | Background execution and queue needs | CONDITIONAL |
| OAD-012 | Contract protocols and schema tooling | COMPLETE |

No unconditional OAD remains `NEXT` or `PENDING`. OAD-011 remains evidence-triggered and is **not** activated by this checkpoint.

## 6. Key Architecture Invariants

- Teacher request authorizes AI generation; teacher approval authorizes publication of AI-generated student-facing semantic content ([ADR-0006](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md)).
- The backend remains authoritative for classroom/session state; no browser client becomes offline authority ([ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md), [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md)).
- Cross-component wire interfaces are contract-first ([ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)).
- The backend is module-first Hexagonal: business-capability modules are the primary boundary, with ports/adapters used locally where warranted — not a global technical-layer architecture ([ADR-0013](../02_architecture/adr/ADR-0013-java21-module-first-hexagonal-backend.md)).
- Mathematics assurance uses exact/deterministic validation; AI cannot convert `INVALID`/`UNSUPPORTED` to `VALID` ([ADR-0016](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md)).
- Curriculum authority is controlled, versioned, and human-reviewed; AI/provider model knowledge never becomes curriculum authority ([ADR-0015](../02_architecture/adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)).
- AI and speech provider adapters are replaceable infrastructure, never product policy or authorization authority ([ADR-0017](../02_architecture/adr/ADR-0017-openrouter-bounded-generation-and-usage-controls.md), [ADR-0018](../02_architecture/adr/ADR-0018-deepgram-push-to-talk-speech-recognition.md)).
- The MVP/pilot deployment is a portable single-Linux-VPS baseline; the provider is replaceable but the deployment class is locked ([ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md)).
- A single VPS is one infrastructure failure domain and is explicitly **not** high availability for MVP/pilot ([ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md)).

## 7. Explicitly Deferred Implementation Work

- Field-level OpenAPI/JSON Schema contracts (strategy complete, field definitions not started).
- Physical database schema and Flyway migrations.
- Concrete OIDC provider selection and deployment configuration.
- Source scaffolding for backend and frontend applications.
- AI (OpenRouter), speech (Deepgram), and curriculum-corpus provider/adapter implementation, credentials, and evaluation evidence.
- Deployment implementation evidence: VPS provisioning, Dockerfile/Compose/Caddy, off-host backup implementation and restore exercise, CI/CD pipeline, and domain/HTTPS activation.
- Stage A/Stage B pilot execution and evidence per [PILOT_PLAN.md](../06_delivery/PILOT_PLAN.md).

## 8. Minor Remediations Applied During This Audit

The following stale documentation wording — contradicting already-Accepted ADRs — was corrected during this audit. No product or architecture decision was changed.

- `docs/05_operations/DEPLOYMENT.md` — corrected a status claim that deployment architecture was "intentionally not selected"; it is now selected (ADR-0019). The document remains Conditional pending real deployment execution evidence.
- `docs/05_operations/RUNBOOK.md` — removed "deployment platform" from Open Decisions (resolved by ADR-0019); retained observability tooling and backup implementation as genuinely open.
- `docs/00_product/PRODUCT_BRIEF.md` — corrected a Technology Constraints bullet claiming language/framework/database/cloud/identity/realtime/AI/speech providers were undecided; all are resolved via Accepted ADRs, with implementation/evidence still pending.
- `docs/06_delivery/PILOT_PLAN.md` — corrected an Open Follow-Ups bullet describing AI/speech/identity/persistence/realtime/deployment as open "technology decisions"; reworded to implementation evidence, since the architecture decisions are resolved.

## 9. Validation Evidence

- `git diff --check` — clean, before and after remediation.
- `git fsck --no-progress` — no material repository corruption reported.
- Branch ancestry: `git merge-base --is-ancestor origin/main HEAD` — confirmed `origin/main` (`7af7d309...`) is a direct ancestor of the architecture branch HEAD (`6d5bdd52...`).
- Non-mutating conflict preview: `git merge-tree <merge-base> origin/main HEAD` — 0 `CONFLICT` markers across the full output; the branch fast-forwards cleanly from `origin/main`.
- README bilingual mirror review (`README.md` / `README.en.md`) — Deployment target, Java 21, contract strategy, and outstanding-work rows confirmed as semantic mirrors.
- Stale-decision search across all Markdown for `OAD-010`, `not decided`, `not selected`, Java 25, `Tencent-only`, `Kubernetes`, `Kafka`/`RabbitMQ`, and related terms — all current-canonical matches confirmed correct; remaining Java 25 mentions are historical (superseded ADR-0009, historical checkpoint, changelog entries) or explicit current supersession explanations in ADR-0013.
- Source/contract boundary review (`git ls-files`) — no Java/React project, no `pom.xml`/`build.gradle`/application `package.json`, no Dockerfile/Compose/Caddyfile/Terraform/CI workflow, no migrations, no field-level OpenAPI/JSON Schema contracts.
- Secret/artifact scan — no `BEGIN PRIVATE KEY`, provider API key patterns, `client_secret=`, bearer tokens, or committed `.env`/`.pem`/`.key` files found.
- **Template validator: PASS — executed through Podman `python:3.13-slim`** (`podman run --rm -v "$(pwd):/repo" -w /repo python:3.13-slim python scripts/validate_template.py --project-mode` → `Validation passed (project mode).`). Local `python3`/`python` resolve only to the Windows Store execution-alias stub on this machine; Docker CLI is unavailable; Podman was used per the established fallback procedure.
- `python -m py_compile scripts/validate_template.py` via the same container — no syntax error (exit 0).
- A second validator run after this checkpoint and the `PROJECT_STATUS.md` update is required and recorded in the final task report, to catch any broken Markdown link introduced by this checkpoint file.

## 10. Merge Readiness Statement

`feat/architecture-foundation` is ready to be **deliberately** merged into `main` **only after** this checkpoint change (and the accompanying `PROJECT_STATUS.md` update and minor remediations) is reviewed, committed, and pushed by a human or an explicitly authorized follow-up action. This checkpoint task itself does not commit, push, or merge.

## 11. Next Phase

After the deliberate merge to `main`, the next progress phase is **Field-Level Contract Foundation** — defining field-level OpenAPI operations and JSON Schema payloads for the structured lesson/scene, session/command/projection, AI proposal, and assurance/provenance boundaries already scoped by [SYSTEM_ARCHITECTURE.md §12](../02_architecture/SYSTEM_ARCHITECTURE.md). Source scaffolding does **not** begin at that point; it remains a later, separately gated phase.

## 12. Historical Checkpoint Note

[2026-09-07 — Architecture Foundation Checkpoint After OAD-004](./2026-09-07-architecture-foundation-after-oad004.md) is historical and superseded **only as a handoff snapshot**. It predates ADR-0013 (Java 21 LTS) and ADR-0019 (deployment), and any Java 25 or "deployment platform not selected" content in that file reflects its original creation state and is not rewritten. Canonical architecture documents (`SYSTEM_ARCHITECTURE.md`, the ADR register) are never described as "superseded" by a checkpoint — checkpoints are non-authoritative handoff snapshots, and canonical documents always win in a conflict.
