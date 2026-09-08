# ADR-0019 — Use a Portable Single-Linux-VPS Deployment Baseline for MVP/Pilot, Initially on Tencent Cloud Lighthouse Jakarta

| Field | Value |
|---|---|
| ADR | `ADR-0019` |
| Status | Accepted |
| Date | `2026-09-08` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `PR-036`, `NFR §5 Availability`, `NFR §12 Maintainability and Portability`, [DATA_RETENTION_POLICY.md](../../06_delivery/DATA_RETENTION_POLICY.md) |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

OAD-010 selects the deployment platform, environments, secret-management approach, and regional requirements for Penatika's MVP/founder-led classroom pilot. All application/provider architecture decisions that deployment planning depends on are already resolved: client strategy ([ADR-0008](./ADR-0008-browser-first-react-client-strategy.md)), backend ([ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md)), persistence ([ADR-0014](./ADR-0014-postgresql-flyway-sql-first-persistence.md)), identity ([ADR-0011](./ADR-0011-oidc-backend-managed-browser-sessions.md)), realtime ([ADR-0012](./ADR-0012-sse-realtime-push-with-existing-http-commands.md)), generative AI ([ADR-0017](./ADR-0017-openrouter-bounded-generation-and-usage-controls.md)), speech ([ADR-0018](./ADR-0018-deepgram-push-to-talk-speech-recognition.md)), and curriculum ([ADR-0015](./ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)). None of these decisions selected where Penatika runs, how it reaches HTTPS traffic, how PostgreSQL is durably backed up off-host, how secrets reach the running process, or which environments exist — that is this decision.

Penatika is a founder-led MVP with no confirmed multi-tenant or high-availability requirement yet. The governing principle is: **lock the deployment model, not the vendor.** Penatika must have a concrete, buildable MVP/pilot deployment architecture, but normal application execution must remain portable between ordinary Linux VPS providers so that a provider or pricing change never forces an application rewrite.

This ADR does not scaffold source, create a Dockerfile, create a Compose file, create a Caddyfile, create Terraform/Ansible/cloud-init, create a GitHub Actions workflow, create database migrations, create contracts, create secrets, provision a real Tencent account/resource, or implement a backup script.

## Decision

Penatika selects a **portable single-Linux-VPS deployment baseline** for the founder-led MVP and classroom pilot. This decision has three distinct layers that must not be conflated:

**A. Architecture decision — LOCKED.** One ordinary Linux VPS runs Docker Engine + Docker Compose, fronted by a reverse proxy that terminates HTTPS, colocating a PostgreSQL container with the backend container for the pilot. Normal application execution depends only on portable standards (Linux, OCI containers, Docker Compose, PostgreSQL, HTTPS, standard DNS, filesystem/container volumes, ordinary outbound HTTPS) — never on a vendor-proprietary API or SDK.

**B. Initial provider selection — SELECTED BUT REPLACEABLE.** The initial PILOT VPS provider is **Tencent Cloud Lighthouse**, region **Jakarta, Indonesia**, running **Ubuntu Server 24.04 LTS (x86_64)**. This is an operational infrastructure choice, not a permanent application-platform dependency. Penatika does **not** depend on Tencent-proprietary APIs or SDKs for normal runtime behavior, and this ADR does not claim "Penatika can only run on Tencent Cloud."

**C. Operational/commercial plan selection — NOT ARCHITECTURE.** The exact Tencent Lighthouse commercial server plan, promotional price, renewal price, and billing term are operational/commercial information tracked outside this ADR. They are not locked as architecture and may change without triggering an architecture review, provided the deployment class described in this ADR remains unchanged.

### Scope

This decision applies to:

- the MVP and classroom-pilot (`PILOT`) deployment class and topology;
- the initial `PILOT` provider, region, and OS selection;
- environment model (`LOCAL` / `PILOT` / `PROD`);
- the reverse proxy, container runtime, database colocation, backup boundary, secrets boundary, registry, and CI/CD boundary conceptual choices for `PILOT`.

This decision does not imply:

- that Tencent Cloud is the only Linux VPS provider Penatika can ever use;
- that a specific commercial SKU or promotional price is architecture;
- that `PROD` commercial deployment architecture is decided;
- that Kubernetes, managed PostgreSQL, multi-node, multi-region, or a queue is introduced;
- that OAD-011 is activated.

## Deployment Class: One Ordinary Linux VPS

The MVP/pilot deployment class is **one ordinary Linux VPS**, not a managed container platform, not a serverless platform, and not a cluster.

Conceptual deployment:

```text
Ubuntu 24.04 LTS (x86_64)
│
└── Docker Compose
    │
    ├── caddy               (reverse proxy / HTTPS)
    │
    ├── penatika-backend    (Java 21 / Spring Boot 4.x OCI image)
    │
    └── postgres            (PostgreSQL 18.x container)
        + persistent database volume
```

Normal Penatika runtime depends only on:

- Linux;
- OCI-compatible containers;
- Docker;
- Docker Compose;
- PostgreSQL;
- HTTPS;
- standard DNS;
- filesystem/container volumes;
- ordinary outbound HTTPS.

Penatika does not depend on Tencent-specific application SDKs, and Tencent APIs are not used inside Penatika domain/application code for normal runtime behavior.

Teacher Web and Classroom Display ([ADR-0008](./ADR-0008-browser-first-react-client-strategy.md)) remain separate build artifacts and may be served as static files through the same reverse-proxy/static-delivery layer. They are not merged into one frontend application.

Kubernetes, Docker Swarm, Nomad, a service mesh, ECS, App Runner, a serverless backend, and a multi-node cluster are explicitly not introduced by this decision.

## Initial Provider Selection: Tencent Cloud Lighthouse, Jakarta

The initial `PILOT` provider is **Tencent Cloud Lighthouse**, region **Jakarta, Indonesia**, because initial Penatika users are in Indonesia and this provider currently offers a suitable Jakarta-hosted Linux VPS with favorable MVP/pilot economics and simple founder-led operations. This is an initial provider selection within the locked deployment class described above, not a permanent application-platform dependency.

Provider re-evaluation is expected and does not, by itself, require re-litigating the architecture (see "Provider Portability Rule").

## Initial Server Sizing (Guideline, Not a Locked SKU)

This ADR does not lock a specific Tencent commercial SKU or promotional price. It defines only an initial sizing guideline.

Recommended minimum pilot starting point:

- ≥ 2 vCPU;
- ≥ 4 GB RAM;
- ≥ 50–60 GB SSD-class persistent storage.

A 2 vCPU / 8 GB RAM configuration may be selected operationally if economically attractive, for additional PostgreSQL/JVM headroom, but is not required by architecture. A 2 GB RAM instance must not be the recommended pilot baseline: the same host initially runs Linux, Docker, Java 21/Spring Boot, PostgreSQL, the reverse proxy, logs, and backup operations concurrently.

Capacity must be measured, not assumed. Vertical scaling (a larger VPS) is the first response to ordinary MVP/pilot resource pressure. Kubernetes or a microservice split is not introduced merely because resource usage grows (see "Scale / Migration Triggers").

## Host Runtime: Docker Engine + Docker Compose

**Docker Engine + Docker Compose** is the initial application runtime/orchestration model. The backend deployable artifact is an **immutable OCI-compatible container image**; it must not bake credentials, environment-specific configuration, teacher data, or provider secrets into the image. The same application image must remain usable on another compatible Linux VPS provider.

## Reverse Proxy and HTTPS: Caddy

**Caddy** is the initial reverse proxy / HTTPS entry point, selected for simple founder-led operations, automatic certificate management, portability across ordinary Linux deployments, and fitness for Penatika's initial topology.

Conceptual public topology:

```text
Internet
   |
 HTTPS
   |
 Caddy
   |
   +--> Teacher Web static assets
   |
   +--> Classroom Display static assets
   |
   +--> /api/* -> Penatika Backend
   |
   +--> SSE endpoint -> Penatika Backend
```

The backend container is not exposed directly as the normal public application entry point. PostgreSQL is never publicly exposed. Exact domain names remain deferred. Real classroom pilot usage must not rely on plain HTTP: before real pilot activation, a valid domain or equivalent approved hostname must exist and public traffic must use HTTPS.

## Backend

[ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) is preserved unchanged: Java 21 LTS, Spring Boot 4.x, module-first Hexagonal Architecture, one deployable modular monolith. The backend deploys as an immutable OCI-compatible container image built from that codebase; no credentials, environment-specific configuration, teacher data, or provider secrets are baked into the image.

## Frontend

[ADR-0008](./ADR-0008-browser-first-react-client-strategy.md) is preserved unchanged: Teacher Web and Classroom Display are separate React + TypeScript + Vite build/deployment artifacts, delivered as ordinary static assets. Frontend deployment does not require a Next.js server runtime, a Node application server for static assets, a Tencent-specific frontend hosting SDK, or SSR infrastructure. Public browser bundles must never contain the OpenRouter credential, the Deepgram credential, the PostgreSQL credential, a confidential OIDC client secret, or a server-deployment credential.

## PostgreSQL

[ADR-0014](./ADR-0014-postgresql-flyway-sql-first-persistence.md) is preserved unchanged: PostgreSQL 18.x, Flyway, Spring JDBC/JdbcClient, SQL-first persistence. For `PILOT`, PostgreSQL runs on the same VPS as a dedicated container with persistent storage, binding only to the private Docker/internal host boundary — it must never publish its port publicly. Resource settings must be appropriate for the VPS capacity in use.

Single-VPS colocated PostgreSQL is an intentional MVP/pilot trade-off and is explicitly **not** declared sufficient for indefinite commercial scale (see "Scale / Migration Triggers"). No physical schema or migration is created by this ADR.

## Data Durability and Backup Boundary

A Docker volume is persistent storage; it is **not** a backup. Provider VPS disk is not a backup. A same-provider snapshot must not be the only backup strategy.

The canonical requirement: **primary application server failure must not destroy the only usable copy of durable pilot data.** This requires an automated PostgreSQL backup plus an off-host backup copy at a provider-replaceable destination — conceptually S3-compatible object storage or another approved external backup target. Backup architecture is not locked to Tencent COS; Tencent COS may be selected operationally later if useful, but it is not mandated.

## Backup Model (Baseline, Not Implemented Here)

Baseline future backup behavior:

- automated daily PostgreSQL backup;
- encrypted before or during off-host storage;
- stored outside the primary VPS;
- retention aligned with [DATA_RETENTION_POLICY.md](../../06_delivery/DATA_RETENTION_POLICY.md);
- backup failure must become observable;
- a documented restoration procedure;
- a restore exercised before meaningful reliance on pilot data.

A provider snapshot is, at most, an optional supplemental recovery mechanism — never the canonical independent backup. Exact backup tool, object-storage vendor, schedule, retention counts, RPO, and RTO remain implementation/evidence decisions; this ADR does not invent unsupported numerical RPO/RTO targets.

## Environment Model

Penatika distinguishes three environments: `LOCAL`, `PILOT`, `PROD`.

- **`LOCAL`** — developer workstation; development/test data only; no real classroom/teacher operational data by default.
- **`PILOT`** — remote founder-operated deployment; initial target is Tencent Cloud Lighthouse Jakarta; approved real classroom pilot use only after readiness gates.
- **`PROD`** — future commercial production environment; **not provisioned by this decision**; its deployment architecture must be reviewed before commercial activation.

This decision does not require multiple always-running paid VPS environments and does not require a permanent remote `DEV` server. A temporary remote development/test environment may be created operationally if needed. There is no cross-environment sharing of database, secrets, or sessions.

## Provider Portability Rule

Penatika's normal application architecture must remain portable between compatible Linux VPS providers. A provider change does **not** automatically require a new architecture ADR when all of the following remain true:

- single Linux VPS deployment class remains;
- an Ubuntu/Linux-compatible environment remains;
- OCI containers remain supported;
- Docker Compose remains supported;
- the PostgreSQL deployment model remains equivalent;
- security/privacy requirements remain satisfied;
- required regional/data-processing requirements remain satisfied.

Examples of replaceable provider choices may include Tencent Cloud Lighthouse, AWS Lightsail, DigitalOcean, Vultr, another credible Linux VPS provider, or an Indonesian/local provider satisfying the same requirements. This ADR does not rank or select those alternatives beyond Tencent as the initial provider.

## When a New Architecture Decision Is Required

A new architecture review/ADR is required if Penatika changes deployment class materially — for example: single VPS → managed PostgreSQL; single VPS → multiple application nodes; single VPS → container platform; single VPS → Kubernetes; single VPS → serverless; single-region → multi-region; colocated PostgreSQL → separate managed database architecture — or if commercial `PROD` requires materially stronger availability, or new regulatory/data-residency requirements arise. Changing the VPS *provider* while remaining inside the deployment class described in this ADR is not, by itself, one of these triggers.

## Provider Re-Evaluation Triggers

Tencent Cloud Lighthouse is explicitly replaceable. The provider may be re-evaluated if: promotional pricing ends; renewal pricing becomes unattractive; another provider offers materially better value; Jakarta availability changes; reliability is insufficient; support is inadequate; storage/network constraints become problematic; backup options become inadequate; security requirements increase; commercial production begins; managed services become economically justified; or compliance/privacy requirements change. Changing provider must not require rewriting Penatika application logic.

## Region / Data Location

The initial `PILOT` provider region is **Jakarta, Indonesia**, selected because initial Penatika users are in Indonesia and the currently selected provider offers a suitable Jakarta deployment. Penatika-managed durable pilot data should remain on the selected Jakarta-hosted primary environment and approved off-host backup locations. This ADR does not claim that all Penatika data always stays in Indonesia: existing external processors remain OpenRouter ([ADR-0017](./ADR-0017-openrouter-bounded-generation-and-usage-controls.md)) and Deepgram Australia ([ADR-0018](./ADR-0018-deepgram-push-to-talk-speech-recognition.md)), whose existing privacy/data-processing restrictions remain binding; the OIDC provider is still to be concretely selected. Moving Penatika-managed durable primary application data outside Indonesia requires an explicit regional/privacy review. Provider portability does not mean region/data-processing review can be skipped.

## Secrets

For this portable VPS baseline, Penatika does not require a cloud-vendor secret manager. Runtime secrets must exist only server-side, remain outside Git, remain outside container images, remain outside frontend artifacts, be readable only by the required deployment/runtime identity, and use restrictive filesystem permissions. Docker Compose file-mounted secrets / root-owned host secret files are preferred over embedding secrets into committed `.env` files.

Conceptually, server runtime secrets include: the OpenRouter credential, the Deepgram credential, a future OIDC confidential-client secret, the PostgreSQL application credential, a migration credential, a backup-destination credential, and a deployment-related pull credential where needed. This ADR does not define real values and does not create secret files. A managed secret service may replace host-managed secrets in a future deployment architecture.

## Server Access Security

Baseline `PILOT` VPS security requirements:

- **Public:** TCP 80 only for HTTPS redirect/certificate bootstrap where needed; TCP 443 for application traffic.
- **Administration:** SSH key authentication; no password-based SSH login for normal administration; root password login disabled; administrative SSH access restricted where operationally practical.
- **Database:** PostgreSQL port not public.
- **Application:** backend port not normally public.

A firewall, automatic/security OS patch discipline, minimal installed services, least privilege, and timely security updates are required. An alternate SSH port is not invented as a security requirement, and direct SSH into application containers is not a normal operational workflow.

## Outbound Connectivity

The backend requires outbound HTTPS access to OpenRouter, Deepgram Australia, the future selected OIDC provider, the approved backup destination if backend/host backup tooling requires it, and approved package/registry endpoints during deployment. Providers must not be allowed to bypass Penatika's existing privacy and policy boundaries.

## Container Registry

The backend deployable artifact uses an OCI-compatible image registry. **GitHub Container Registry (GHCR)** is the acceptable and preferred initial operational registry for repository-adjacent simplicity, but the application architecture must not depend on GHCR-specific APIs — registry selection is replaceable operational infrastructure. This ADR does not create container images or registry configuration, and Tencent Container Registry is not required.

## CI/CD Boundary

**GitHub Actions** is the future baseline CI/CD system.

Conceptual pipeline:

```text
GitHub
  |
  v
quality gates
  |
  v
backend build/test
frontend build/test
  |
  v
backend OCI image
  |
  v
OCI registry
  |
  v
approved PILOT deployment
  |
  v
VPS:
docker compose pull
explicit migration step
docker compose up / controlled replacement
static frontend deployment
health/smoke verification
```

Workflow files are not implemented by this ADR. `PILOT` deployment must use a dedicated deployment identity; a personal/root SSH key is not the canonical CI deployment identity. A dedicated restricted deployment SSH key/user may be used for the founder-led VPS baseline. Deployment credentials must be stored in approved GitHub environment secrets or equivalent protected secret storage; provider API credentials are not placed in the repository.

## Deployment Approval

`PILOT` deployment is not equivalent to ordinary local development. Future GitHub Actions deployment to `PILOT` must have an explicit protected deployment boundary — conceptually a GitHub Environment plus restricted deployment credentials plus explicit approval/manual promotion where practical. Commercial `PROD` deployment is not defined by this decision.

## Database Migrations

[ADR-0014](./ADR-0014-postgresql-flyway-sql-first-persistence.md) is preserved: Flyway migrations are version-controlled deployment artifacts. Future deployment must execute database migration as an explicit step, using a migration identity distinct from the normal runtime application identity; the long-running backend should not have unrestricted schema DDL privileges by default. Migration failure must block deployment progression. Migration files and Flyway runtime configuration are not created by this ADR.

## Release / Rollback

Deployment must use immutable/versioned application artifacts; mutable `latest` is not the authoritative release identity. A Git commit SHA or an immutable version tag/image digest is the deployable identity. Enough previous application artifacts/releases must be kept to allow application rollback where safe. Database rollback must not assume arbitrary reverse migrations are safe; forward-fix is preferred for applied durable schema changes unless a tested rollback is explicitly designed. Release scripts are not implemented by this ADR.

## Observability

A large observability stack is not introduced during architecture foundation. The required lightweight portable baseline: structured backend logs; correlation/request identifiers; container health checks; an application health endpoint; disk-space, memory, and CPU visibility; PostgreSQL health visibility; backup success/failure visibility; log rotation; and externally observable service availability before meaningful pilot use.

Never logged: API credentials; session credentials; raw PTT audio; confidential OAuth/OIDC tokens; database passwords; full provider request/response payloads by default; unrestricted prompts/transcripts. Prometheus, Grafana, Datadog, New Relic, and ELK/OpenSearch are not selected as mandatory MVP infrastructure; they may be considered later based on evidence.

## Caching / Static Delivery

A CDN is not required for the first pilot. Caddy may serve static React/Vite build artifacts directly. A CDN/object-storage static hosting layer may be introduced later if traffic, performance, or commercial production requires it; no provider-specific CDN dependency is introduced now.

## Availability Trade-Off

Single VPS means one infrastructure failure domain. For MVP/pilot this is an intentional founder-led cost/complexity trade-off. This ADR does not claim zero downtime, high availability, multi-zone availability, or fault tolerance against complete VPS/provider failure. Penatika still requires graceful application degradation per [ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md), but graceful degradation cannot make an unavailable single server available. This limitation remains visible in [KNOWN_LIMITATIONS.md](../../06_delivery/KNOWN_LIMITATIONS.md).

## Scale / Migration Triggers

The architecture is not preemptively split. The first scaling strategy is: measure → optimize → vertically scale the VPS if appropriate. The deployment architecture is revisited only when evidence shows, for example: sustained CPU pressure; sustained RAM pressure; JVM/PostgreSQL contention; disk capacity pressure; unacceptable database I/O contention; restore/recovery requirements exceeding single-VPS capability; concurrent classroom usage materially exceeding the host; availability expectations incompatible with one failure domain; commercial `PROD` activation; or compliance/regulatory requirements changing. No arbitrary user-count threshold is defined without evidence.

## OAD-011 Remains Conditional

OAD-011 (background execution and queue needs) remains `CONDITIONAL` and is **not** activated by this decision. Deployment on Docker Compose does not imply a queue. Kafka, RabbitMQ, SQS, Redis Streams, a background worker fleet, and a distributed job scheduler are not added by this ADR. Background/queue architecture is introduced only if measured workflow or reliability evidence later requires it.

## Cost Control

The deployment architecture remains suitable for a founder-led MVP/pilot. The current provider choice is influenced by favorable MVP/pilot economics; promotional pricing is temporary operational information, not architecture; the exact monthly/yearly price is not locked. Multiple always-running servers are avoided without demonstrated need, and managed-enterprise infrastructure is avoided before product evidence justifies it. Resource usage must be measured, and the provider can be changed when economics change — but not by compromising backup, privacy, secret protection, or database durability merely to reduce the bill.

## Consequences

### Positive

- Resolves OAD-010 with a concrete, buildable MVP/pilot deployment architecture while keeping every application/runtime dependency (Linux, OCI containers, Docker Compose, PostgreSQL, HTTPS) portable across ordinary Linux VPS providers.
- Separates architecture (locked), initial provider (replaceable), and commercial plan (not architecture), so a pricing or provider change never forces a rewrite or a new ADR by itself.
- Keeps operational complexity proportionate to a founder-led MVP: no Kubernetes, no managed database, no queue, no CDN, no large observability stack.
- Establishes an explicit off-host backup boundary before any real pilot data exists, preventing the "single VPS is the only copy of the data" failure mode.
- Gives PILOT a concrete security baseline (SSH keys only, no public PostgreSQL/backend port, firewalled) without inventing a bespoke security model.

### Negative / Costs

- Single VPS is one failure domain; the pilot has no built-in high availability, and an outage on the primary host takes the whole application down until it is restored.
- Colocating PostgreSQL with the application on the same host means database and application resource pressure compete on one machine.
- Backup, restore, and observability baselines described here still require real implementation and evidence before they can be relied upon.
- Provider portability requires continued discipline (no Tencent-specific SDKs/APIs in application code); this constraint must be actively maintained during implementation, not just declared here.

### Required Follow-Up

- Provision the actual Tencent Cloud Lighthouse Jakarta VPS and Ubuntu 24.04 LTS baseline (operational work, not part of this ADR).
- Author the Dockerfile(s), `compose.yaml`, and Caddyfile during/after source scaffolding.
- Implement the automated PostgreSQL backup job and off-host destination, and exercise a restore before relying on pilot data.
- Implement the GitHub Actions pipeline, GHCR publishing, and the protected `PILOT` deployment environment.
- Select a valid domain/hostname and activate HTTPS before real classroom pilot use.
- Resolve the concrete OIDC provider and its deployment configuration.

## Alternatives Considered

1. **Tencent Cloud Lighthouse, Jakarta.** **Selected initial provider**, for a suitable Linux VPS model, Jakarta availability, initial Indonesia-pilot proximity, favorable current MVP/pilot economics, and simple founder-operated deployment. Provider is replaceable; promotional pricing is not architecture.
2. **Generic Linux VPS from another provider** (AWS Lightsail, DigitalOcean, Vultr, or a comparable/local provider). A valid future substitute under the same architecture class; not ranked or selected now.
3. **AWS ECS + RDS / equivalent managed platform.** Not selected now. A credible future option once operations/availability requirements justify the additional cost and complexity.
4. **Kubernetes.** Rejected for MVP/pilot due to unnecessary operational complexity relative to a founder-led single-application deployment.
5. **Single unmanaged bare-metal server.** Not preferred; a VPS offers simpler founder-led operations (imaging, resizing, provider support) without the physical-hardware burden.
6. **Fully serverless architecture.** Not selected because it would materially change the established Java, PostgreSQL, and stateful-session architecture already locked by ADR-0011/ADR-0013/ADR-0014.
7. **Managed PostgreSQL from day one.** Credible but deferred until cost, availability, or recovery needs justify separating the database from the application host.

## Deferred Implementation Decisions

This ADR does not select or define: the exact Tencent commercial SKU or price; real domain/hostname; Dockerfile, Compose, or Caddyfile contents; the exact backup tool, object-storage vendor, or backup schedule; exact RPO/RTO numbers; the exact GitHub Actions workflow files; the exact deployment SSH key/user provisioning steps; the exact database role/credential names; the exact log-rotation and health-check implementation; and the concrete OIDC provider deployment configuration.

## Follow-Up Decisions

1. Provision the initial Tencent Cloud Lighthouse Jakarta VPS and record actual sizing/OS evidence.
2. Author Dockerfile(s), `compose.yaml`, and Caddyfile once source scaffolding begins.
3. Select and implement the off-host backup destination and exercise a restore before pilot data reliance.
4. Implement the GitHub Actions CI/CD pipeline and the protected `PILOT` GitHub Environment.
5. Select a domain/hostname and activate HTTPS before real classroom pilot use.
6. Resolve the concrete OIDC provider and record its deployment configuration in [CONFIGURATION.md](../../05_operations/CONFIGURATION.md).
7. Re-evaluate the VPS provider if any trigger in "Provider Re-Evaluation Triggers" occurs.

## Related Requirements / ADRs

- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0008 — Use Browser-First React Clients with Separate Teacher and Classroom Display Boundaries](./ADR-0008-browser-first-react-client-strategy.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0012 — Use Server-Sent Events for Realtime Push with Existing HTTP Commands](./ADR-0012-sse-realtime-push-with-existing-http-commands.md)
- [ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture](./ADR-0013-java21-module-first-hexagonal-backend.md)
- [ADR-0014 — Use PostgreSQL with Flyway and SQL-First Hexagonal Persistence](./ADR-0014-postgresql-flyway-sql-first-persistence.md)
- [ADR-0017 — Use OpenRouter for Bounded Generative AI with Scope, Quota, and Privacy Routing Controls](./ADR-0017-openrouter-bounded-generation-and-usage-controls.md)
- [ADR-0018 — Use Deepgram Nova-3 for Backend-Mediated Push-to-Talk Speech Recognition](./ADR-0018-deepgram-push-to-talk-speech-recognition.md)
- [Data Retention, History, Export, and Deletion Policy](../../06_delivery/DATA_RETENTION_POLICY.md)
- [Non-Functional Requirements](../NON_FUNCTIONAL_REQUIREMENTS.md)
- [Threat Model](../../04_engineering/THREAT_MODEL.md)
- [Test Strategy](../../04_engineering/TEST_STRATEGY.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-08` | Accepted | Resolve OAD-010 with a portable single-Linux-VPS MVP/pilot deployment baseline (Docker Compose, Caddy, colocated PostgreSQL, off-host backup boundary), initially on Tencent Cloud Lighthouse Jakarta as a replaceable provider |
