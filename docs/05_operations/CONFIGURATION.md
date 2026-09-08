# Configuration — Penatika

> Defines configuration ownership and categories without inventing environment-variable names or provider settings before technology selection.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Active conceptual baseline |
| Last Updated | `2026-09-07` |

## 1. Configuration Principles

- Configuration changes behavior within approved architecture; it must not silently redefine product requirements.
- Secrets remain separate from non-secret configuration.
- Client-delivered configuration is public and cannot contain authoritative secrets.
- Environment-specific values must not be hard-coded into domain logic.
- Safe defaults must not bypass authorization, validation, privacy, or assurance requirements.
- Configuration names and schemas become authoritative only after stack and deployment decisions.

## 2. Configuration Categories

### Runtime and Environment

- environment identity (`LOCAL` / `PILOT` / `PROD` per [ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md));
- public client origin(s);
- backend and realtime endpoints;
- external hostname/domain (deferred until selected before real pilot activation);
- application image version/tag (immutable release identity, not `latest`);
- regional/timezone behavior where required;
- feature maturity flags with explicit ownership.

### Deployment

- deployment environment (`LOCAL` / `PILOT` / `PROD`);
- initial `PILOT` provider/region marker (Tencent Cloud Lighthouse, Jakarta), recorded as replaceable operational information, not architecture;
- backup destination category;
- environment-specific resource limits.

Exact provider account identifiers, VPS hostnames/IPs, and commercial plan details are operational information, not committed configuration. See [ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md) for the deployment architecture baseline.

### Identity and Session

- OIDC issuer and discovery endpoint;
- OIDC client identifier and confidential-client credential where required;
- authorization redirect/callback URI;
- authenticated session-cookie security and transient authorization-transaction controls;
- browser-session idle and absolute lifetimes;
- trusted browser origins and credentialed CORS policy where applicable;
- CSRF protection configuration;
- pairing-grant lifetime fixed at the approved five-minute baseline unless a reviewed security change is accepted;
- pairing attempt, rate, and anti-enumeration limits;
- controller/display participant revocation and replacement policy.

The concrete OIDC provider, physical session store, secret manager, exact
cookie/header names, and environment-variable names remain open. Configuration
must not expose OAuth/OIDC tokens or confidential-client credentials to browser
applications.

### Persistence

- PostgreSQL endpoint/database identifier;
- runtime database credential;
- migration credential where separated from the runtime credential;
- connection-pool configuration;
- query and transaction timeout categories;
- Flyway configuration and migration location;
- schema compatibility checks.

Persistence technology is selected as PostgreSQL 18.x, Flyway 13.x, and
Spring JDBC/JdbcClient
([ADR-0014](../02_architecture/adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)).
Exact environment-variable names, connection-pool values, and secret-manager
integration remain open.

### Classroom Session

- command and payload bounds;
- reconnect and stale-state policy;
- session duration bounds;
- save and retention settings;
- degraded-mode capability settings follow Q-03 / ADR-0007.

### AI and Speech

- provider/model identifiers;
- credentials and endpoint settings;
- timeouts, retries, concurrency, quotas, and cost limits;
- prompt/policy version;
- provider data-retention controls;
- fallback strategy if selected.

### Mathematics Assurance

- enabled validators and versions;
- supported grade/topic scope;
- validation timeouts and resource limits;
- corpus or rule-set version.

### Curriculum

- authority level and controlled source identifier;
- normative Mathematics source BSKAP 046/H/KR/2025 and explicit supersession state;
- source version, phase/scope, and provenance settings;
- official-guidance activation and usage/licensing controls;
- local-context source and version controls;
- ingestion or retrieval endpoint;
- integrity and licensing metadata;
- active supported scope.

### Observability

- log level;
- trace and metric exporters;
- sampling;
- redaction policy;
- alerting integration;
- environment and deployment markers.

## 3. Secret Classification

Secret examples expected after provider selection:

- identity signing or client secrets;
- database credentials;
- AI and speech provider credentials;
- curriculum-source credentials if required;
- telemetry ingestion secrets;
- session-signing keys;
- backup-destination credential;
- deployment-related pull/deploy credential where needed.

Per [ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md), the portable VPS baseline does not require a cloud-vendor secret manager; runtime secrets are server-side only, outside Git, outside container images, and outside frontend artifacts, preferring Docker Compose file-mounted secrets / root-owned host secret files over committed `.env` files. No real secret values or environment-variable names are defined by this document.

Secrets must use an approved secret manager or local ignored mechanism and must never be committed, logged, embedded in client bundles, or copied into documentation.

## 4. Environment Model

Per [ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md), the environment topology is `LOCAL` / `PILOT` / `PROD`. `LOCAL` is the developer workstation with development/test data only; `PILOT` is the remote founder-operated deployment, initially on Tencent Cloud Lighthouse, Jakarta; `PROD` is a future commercial environment not provisioned by that decision. There is no cross-environment sharing of database, secrets, or sessions.

Production-like data must not be copied into lower environments without an approved protected process.

## 5. Validation and Startup Behavior

After implementation:

- configuration should be validated at startup;
- missing required secrets should fail clearly and safely;
- invalid bounds or incompatible versions should not silently fall back;
- public error output must not reveal secret values;
- effective configuration should be observable without exposing secrets.

## 6. Open Decisions

- Configuration library and schema format.
- Secret manager (a managed secret service may later replace host-managed secrets; not required for the ADR-0019 baseline).
- Exact configuration keys and ownership.
- Runtime feature-flag approach and governance.
- Provider-specific retention and regional settings.
- Exact backup tool, off-host destination, and schedule (ADR-0019 defers these to implementation/evidence).

## 7. Related Documents

- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)
- [Deployment](./DEPLOYMENT.md)

## 8. Change Log

| Date | Change | Author |
|---|---|---|
| `2026-09-08` | Record deployment/environment configuration categories (LOCAL/PILOT/PROD, deployment secrets, backup-destination credential) for the portable single-Linux-VPS baseline (ADR-0019) without real values | Claude |
| `2026-09-07` | Record persistence (PostgreSQL/Flyway) configuration categories without inventing environment-variable names or pool values | Claude |
| `2026-09-07` | Record OIDC, backend-session, CSRF, pairing, and participant configuration categories without inventing keys or providers | Codex |
| `2026-09-06` | Align degraded-mode configuration wording with Q-03 / ADR-0007 | Codex |
| `2026-09-06` | Initial conceptual configuration baseline | Codex |
