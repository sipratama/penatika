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

- environment identity;
- public client origin(s);
- backend and realtime endpoints;
- regional/timezone behavior where required;
- feature maturity flags with explicit ownership.

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
- session-signing keys.

Secrets must use an approved secret manager or local ignored mechanism and must never be committed, logged, embedded in client bundles, or copied into documentation.

## 4. Environment Model

The environment topology is not selected. At minimum, implementation planning should distinguish local development, automated test, controlled evaluation, pilot/staging, and production when those environments become relevant.

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
- Environment topology and deployment platform.
- Secret manager.
- Exact configuration keys and ownership.
- Runtime feature-flag approach and governance.
- Provider-specific retention and regional settings.

## 7. Related Documents

- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)
- [Deployment](./DEPLOYMENT.md)

## 8. Change Log

| Date | Change | Author |
|---|---|---|
| `2026-09-07` | Record persistence (PostgreSQL/Flyway) configuration categories without inventing environment-variable names or pool values | Claude |
| `2026-09-07` | Record OIDC, backend-session, CSRF, pairing, and participant configuration categories without inventing keys or providers | Codex |
| `2026-09-06` | Align degraded-mode configuration wording with Q-03 / ADR-0007 | Codex |
| `2026-09-06` | Initial conceptual configuration baseline | Codex |
